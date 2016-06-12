/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.server.ros;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.mrs.Relay;
import org.tdmx.core.api.v01.mrs.RelayResponse;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.chunk.domain.Chunk;
import org.tdmx.lib.chunk.service.ChunkService;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.EndpointPermission;
import org.tdmx.lib.zone.domain.FlowControlStatus;
import org.tdmx.lib.zone.domain.MessageStatus;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;

/**
 * Handles the execution of individual relay jobs.
 * 
 * @author Peter
 *
 */
public class RelayJobExecutionServiceImpl implements RelayJobExecutionService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayJobExecutionServiceImpl.class);

	/**
	 * Provides all data from the DB for the ROS.
	 */
	private RelayDataService relayDataService;

	/**
	 * The service providing chunk data.
	 */
	private ChunkService chunkService;

	/**
	 * Provides the MRS web service client and the remove MRS sessionId.
	 */
	private RelayConnectionProvider relayConnectionProvider;

	private final DomainToApiMapper d2a = new DomainToApiMapper();
	private final ApiToDomainMapper a2d = new ApiToDomainMapper();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void executeJob(RelayJobContext job) {
		switch (job.getType()) {
		case Data:
			// relay data
			relayMessageData(job.getChannelContext(), job);
			break;
		case Fetch:
			// fetch data
			fetchMessageData(job.getChannelContext(), job);
			break;
		case MetaData:
			relayMetaData(job.getChannelContext(), job);
			break;
		default:
			break;
		}
	}

	@Override
	public void setSegmentScsUrl(String segmentScsUrl) {
		// the connection provider gives a shortcut MRS when relaying "within" our own segment.
		relayConnectionProvider.setSegmentScsUrl(segmentScsUrl);
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void fetchMessageData(RelayChannelContext ctx, RelayJobContext job) {
		// fetch the message data from the DB
		List<Long> stateIds = relayDataService.getRelayMessages(ctx.getAccountZone(), ctx.getZone(), ctx.getDomain(),
				ctx.getChannel(), ctx.getMaxConcurrentMessages());

		job.setObjectIds(stateIds);
	}

	private void relayMessageData(RelayChannelContext ctx, RelayJobContext job) {
		// fetch the msg from the db if it wasn't fetched already
		ChannelMessage msg = relayDataService.getMessage(ctx.getAccountZone(), ctx.getZone(), ctx.getDomain(),
				ctx.getChannel(), job.getObjectId());
		if (msg == null) {
			log.warn("Unable to find message for " + job);
			return;
		}
		if (MessageStatus.SUBMITTED != msg.getState().getStatus()) {
			log.warn("Message state not submitted for " + job);
			return;
		}
		if (ProcessingStatus.PENDING != msg.getState().getProcessingState().getStatus()) {
			log.warn("Message processing state not pending for " + job);
			return;
		}

		// relay the MSG
		MRSSessionHolder sh = getSessionHolder(ctx);
		if (sh.isValid()) {
			// use the MRS to relay the Msg to the other side
			if (log.isDebugEnabled()) {
				log.debug("Relay MSG " + msg.getMsgId() + " on " + ctx.getChannelKey());
			}

			Relay relayMsg = new Relay();
			relayMsg.setSessionId(sh.getMrsSessionId());
			relayMsg.setMsg(d2a.mapChannelMessage(msg));

			ProcessingState error = null;
			try {
				RelayResponse rr = sh.getMrs().relay(relayMsg);
				if (!rr.isSuccess()) {
					error = ProcessingState.error(rr.getError().getCode(), rr.getError().getDescription());
				}
				if (rr.isSuccess() && !sh.isSameSegment()) {
					error = relayChunks(sh, msg);
				}

				if (error != null) {
					// we have an error relaying the msg or the chunks
					relayDataService.updateMessageProcessingState(ctx.getAccountZone(), ctx.getZone(), ctx.getDomain(),
							ctx.getChannel(), msg.getState().getId(), error);
				} else {
					// successful relay - rr can indicate flowcontrol "closed" by remote after transfer - so stop
					// sending.
					FlowControlStatus relayStatus = a2d.mapFlowControlStatus(rr.getRelayStatus());
					job.setFlowStatus(relayStatus);

					// decrease the channel quota's buffer and track the relay status.
					// we don't communicate to any MOS that flow control can be opened, this is just detected by each
					// mos by trying to send.
					relayDataService.updatePostRelayChannelMessage(ctx.getAccountZone(), ctx.getZone(), ctx.getDomain(),
							msg, relayStatus);

					if (!sh.isSameSegment()) {
						// TODO #107: delete chunks if !sameSegment
						log.info("Delete chunks for message " + msg.getMsgId());
					}
				}
			} catch (WebServiceException wse) {
				// runtime error handling
				if (log.isDebugEnabled()) {
					log.debug("MRS call to relay MSG failed.", wse);
				}
				String errorInfo = StringUtils.getExceptionSummary(wse);
				log.info("MRS relay MSG call to remote failed " + errorInfo);
				error = ProcessingState.error(ErrorCode.RelayChannelMessageFault.getErrorCode(),
						ErrorCode.RelayChannelMessageFault.getErrorDescription(errorInfo));

				relayDataService.updateMessageProcessingState(ctx.getAccountZone(), ctx.getZone(), ctx.getDomain(),
						ctx.getChannel(), job.getObjectId(), error);

			}

		} else {
			// update MSG processing state to error of the MRS session holder error
			ProcessingState error = ProcessingState.error(sh.getErrorCode(), sh.getErrorMessage());
			relayDataService.updateMessageProcessingState(ctx.getAccountZone(), ctx.getZone(), ctx.getDomain(),
					ctx.getChannel(), job.getObjectId(), error);
		}
	}

	private ProcessingState relayChunks(MRSSessionHolder sh, ChannelMessage msg) {
		for (int pos = 0; pos < msg.getNumberOfChunks(); pos++) {
			// relay chunks sequentially with max 1 retry each
			if (log.isDebugEnabled()) {
				log.debug("Relay Chunk " + msg.getMsgId() + " pos " + pos);
			}

			Chunk chunk = chunkService.fetchChunk(msg, pos);
			if (chunk == null) {
				ProcessingState error = ProcessingState.error(ErrorCode.ChunkDataLost.getErrorCode(),
						ErrorCode.ChunkDataLost.getErrorDescription(msg.getMsgId(), pos));
				return error;
			}

			Relay relayMsg = new Relay();
			relayMsg.setSessionId(sh.getMrsSessionId());

			relayMsg.setChunk(d2a.mapChunk(chunk));

			ProcessingState error = null;
			int retries = 0;
			do {
				error = null;
				try {
					RelayResponse rr = sh.getMrs().relay(relayMsg);
					if (!rr.isSuccess()) {
						error = ProcessingState.error(rr.getError().getCode(), rr.getError().getDescription());

					}
				} catch (WebServiceException wse) {
					// runtime error handling
					if (log.isDebugEnabled()) {
						log.debug("MRS call to relay Chunk failed.", wse);
					}
					String errorInfo = StringUtils.getExceptionSummary(wse);
					log.info("MRS relay MSG call to remote failed " + errorInfo);
					error = ProcessingState.error(ErrorCode.RelayMessageChunkFault.getErrorCode(),
							ErrorCode.RelayMessageChunkFault.getErrorDescription(errorInfo));
				}

				retries++;
			} while (error != null && retries < 2);

			// error after 1 retry - leave
			if (error != null) {
				return error;
			}
		}
		return null;
	}

	private void relayMetaData(RelayChannelContext ctx, RelayJobContext job) {
		// fetch the channel from the DB with up-to-date info
		Channel c = relayDataService.getChannel(ctx.getAccountZone(), ctx.getZone(), ctx.getDomain(),
				job.getObjectId());
		if (c == null) {
			log.warn("Unable to find channel for meta data relay. " + job);
			return;
		}

		// relay the CA
		if (c.getAuthorization().getProcessingState().getStatus() == ProcessingStatus.PENDING) {
			EndpointPermission perm = ctx.getDirection() == RelayDirection.Fowards
					? c.getAuthorization().getSendAuthorization() : c.getAuthorization().getRecvAuthorization();
			if (perm != null) {
				MRSSessionHolder sh = getSessionHolder(ctx);
				if (sh.isValid()) {
					// use the MRS to relay the CA to the other side
					if (log.isDebugEnabled()) {
						log.debug("Relay CA " + ctx.getChannelKey());
					}

					Relay relayCA = new Relay();
					relayCA.setSessionId(sh.getMrsSessionId());
					relayCA.setPermission(d2a.mapPermission(perm));
					try {
						RelayResponse rr = sh.getMrs().relay(relayCA);
						if (rr.isSuccess()) {
							relayDataService.updateChannelAuthorizationProcessingState(ctx.getAccountZone(),
									ctx.getZone(), ctx.getDomain(), job.getObjectId(), ProcessingState.none());

						} else {
							ProcessingState error = ProcessingState.error(rr.getError().getCode(),
									rr.getError().getDescription());
							relayDataService.updateChannelAuthorizationProcessingState(ctx.getAccountZone(),
									ctx.getZone(), ctx.getDomain(), job.getObjectId(), error);
						}
					} catch (WebServiceException wse) {
						// runtime error handling
						if (log.isDebugEnabled()) {
							log.debug("MRS call failed", wse);
						}
						String errorInfo = StringUtils.getExceptionSummary(wse);
						log.info("MRS relay CA call to remote failed " + errorInfo);
						ProcessingState error = ProcessingState.error(
								ErrorCode.RelayChannelAuthorizationFault.getErrorCode(),
								ErrorCode.RelayChannelAuthorizationFault.getErrorDescription(errorInfo));

						relayDataService.updateChannelAuthorizationProcessingState(ctx.getAccountZone(), ctx.getZone(),
								ctx.getDomain(), job.getObjectId(), error);

					}
				} else {
					// update CA processing state to error of the MRS session holder error
					ProcessingState error = ProcessingState.error(sh.getErrorCode(), sh.getErrorMessage());
					relayDataService.updateChannelAuthorizationProcessingState(ctx.getAccountZone(), ctx.getZone(),
							ctx.getDomain(), job.getObjectId(), error);
				}
			} else {
				// set the PS to error - missing perm
				ProcessingState error = ProcessingState.error(ErrorCode.MissingEndpointPermission.getErrorCode(),
						ErrorCode.MissingEndpointPermission.getErrorDescription());
				relayDataService.updateChannelAuthorizationProcessingState(ctx.getAccountZone(), ctx.getZone(),
						ctx.getDomain(), job.getObjectId(), error);
			}
		}

		// relay the CDS receiver to sender
		if (c.getProcessingState().getStatus() == ProcessingStatus.PENDING) {
			if (ctx.getDirection() == RelayDirection.Backwards) {
				if (c.getSession() != null) {
					MRSSessionHolder sh = getSessionHolder(ctx);
					if (sh.isValid()) {
						// use the MRS to relay the CDS to the sender side
						if (log.isDebugEnabled()) {
							log.debug("Relay CDS " + ctx.getChannelKey());
						}
						Relay relayCDS = new Relay();
						relayCDS.setSessionId(sh.getMrsSessionId());

						relayCDS.setDestinationsession(d2a.mapDestinationSession(c.getSession()));

						try {
							RelayResponse rr = sh.getMrs().relay(relayCDS);
							if (rr.isSuccess()) {
								relayDataService.updateChannelDestinationSessionProcessingState(ctx.getAccountZone(),
										ctx.getZone(), ctx.getDomain(), job.getObjectId(), ProcessingState.none());

							} else {
								ProcessingState error = ProcessingState.error(rr.getError().getCode(),
										rr.getError().getDescription());
								relayDataService.updateChannelDestinationSessionProcessingState(ctx.getAccountZone(),
										ctx.getZone(), ctx.getDomain(), job.getObjectId(), error);
							}
						} catch (WebServiceException wse) {
							// runtime error handling
							if (log.isDebugEnabled()) {
								log.debug("MRS call failed", wse);
							}
							String errorInfo = StringUtils.getExceptionSummary(wse);
							log.info("MRS relay CDS call to remote failed " + errorInfo);
							ProcessingState error = ProcessingState.error(
									ErrorCode.RelayDestinationSessionFault.getErrorCode(),
									ErrorCode.RelayDestinationSessionFault.getErrorDescription(errorInfo));

							relayDataService.updateChannelDestinationSessionProcessingState(ctx.getAccountZone(),
									ctx.getZone(), ctx.getDomain(), job.getObjectId(), error);

						}
					} else {
						// update CDS processing state to error of the MRS session holder error
						ProcessingState error = ProcessingState.error(sh.getErrorCode(), sh.getErrorMessage());
						relayDataService.updateChannelDestinationSessionProcessingState(ctx.getAccountZone(),
								ctx.getZone(), ctx.getDomain(), job.getObjectId(), error);
					}

				} else {
					// error no CDS
					ProcessingState error = ProcessingState.error(ErrorCode.MissingDestinationSession.getErrorCode(),
							ErrorCode.MissingDestinationSession.getErrorDescription());
					relayDataService.updateChannelDestinationSessionProcessingState(ctx.getAccountZone(), ctx.getZone(),
							ctx.getDomain(), job.getObjectId(), error);
				}
			} else {
				// it is an error to want to relay the CDS from origin to destination
				ProcessingState error = ProcessingState.error(ErrorCode.RelayChannelDestinationForwards.getErrorCode(),
						ErrorCode.RelayChannelDestinationForwards.getErrorDescription());
				relayDataService.updateChannelDestinationSessionProcessingState(ctx.getAccountZone(), ctx.getZone(),
						ctx.getDomain(), job.getObjectId(), error);
			}
		}

		// relay the FC-open receiver to sender
		if (c.getQuota().getProcessingState().getStatus() == ProcessingStatus.PENDING) {
			if (ctx.getDirection() == RelayDirection.Backwards) {
				MRSSessionHolder sh = getSessionHolder(ctx);
				if (sh.isValid()) {
					// use the MRS to relay the FC-open to the sender side
					if (log.isDebugEnabled()) {
						log.debug("Relay FC " + c.getQuota().getRelayStatus() + " " + ctx.getChannelKey());
					}

					Relay relayCDS = new Relay();
					relayCDS.setSessionId(sh.getMrsSessionId());

					relayCDS.setRelayStatus(d2a.mapFlowControlStatus(c.getQuota().getRelayStatus()));

					try {
						RelayResponse rr = sh.getMrs().relay(relayCDS);
						if (rr.isSuccess()) {
							relayDataService.updateChannelFlowControlProcessingState(ctx.getAccountZone(),
									ctx.getZone(), ctx.getDomain(), job.getObjectId(), ProcessingState.none());

						} else {
							ProcessingState error = ProcessingState.error(rr.getError().getCode(),
									rr.getError().getDescription());
							relayDataService.updateChannelFlowControlProcessingState(ctx.getAccountZone(),
									ctx.getZone(), ctx.getDomain(), job.getObjectId(), error);
						}
					} catch (WebServiceException wse) {
						// runtime error handling
						if (log.isDebugEnabled()) {
							log.debug("MRS relay FC call failed", wse);
						}
						String errorInfo = StringUtils.getExceptionSummary(wse);
						log.info("MRS relay FC call to remote failed " + errorInfo);
						ProcessingState error = ProcessingState.error(
								ErrorCode.RelayFlowControlOpenFault.getErrorCode(),
								ErrorCode.RelayFlowControlOpenFault.getErrorDescription(errorInfo));

						relayDataService.updateChannelFlowControlProcessingState(ctx.getAccountZone(), ctx.getZone(),
								ctx.getDomain(), job.getObjectId(), error);

					}
				} else {
					// update FC processing state to error of the MRS session holder error
					ProcessingState error = ProcessingState.error(sh.getErrorCode(), sh.getErrorMessage());
					relayDataService.updateChannelFlowControlProcessingState(ctx.getAccountZone(), ctx.getZone(),
							ctx.getDomain(), c.getQuota().getId(), error);
				}
			} else {
				// it is an error to want to relay the FC-open from origin to destination
				ProcessingState error = ProcessingState.error(ErrorCode.RelayFlowControlForwards.getErrorCode(),
						ErrorCode.RelayFlowControlForwards.getErrorDescription());
				relayDataService.updateChannelFlowControlProcessingState(ctx.getAccountZone(), ctx.getZone(),
						ctx.getDomain(), c.getQuota().getId(), error);
			}

		}
	}

	private MRSSessionHolder getSessionHolder(RelayChannelContext ctx) {
		MRSSessionHolder sh = ctx.getMrsSession();
		if (sh == null || !sh.isValid()) {
			log.debug("Setting up new MRS session to " + ctx.getChannelKey());

			sh = relayConnectionProvider.getMRS(ctx.getChannel(), ctx.getDirection());
			ctx.setMrsSession(sh);
		}
		return sh;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public RelayDataService getRelayDataService() {
		return relayDataService;
	}

	public void setRelayDataService(RelayDataService relayDataService) {
		this.relayDataService = relayDataService;
	}

	public RelayConnectionProvider getRelayConnectionProvider() {
		return relayConnectionProvider;
	}

	public void setRelayConnectionProvider(RelayConnectionProvider relayConnectionProvider) {
		this.relayConnectionProvider = relayConnectionProvider;
	}

	public ChunkService getChunkService() {
		return chunkService;
	}

	public void setChunkService(ChunkService chunkService) {
		this.chunkService = chunkService;
	}

}
