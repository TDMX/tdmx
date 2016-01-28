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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.common.domain.ProcessingStatus;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.EndpointPermission;

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
	 * Provides the MRS web service client and the remove MRS sessionId.
	 */
	private RelayConnectionProvider relayConnectionProvider;

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
			break;
		case Fetch:
			break;
		case MetaData:
			relayMetaData(job.getChannelContext(), job);
			break;
		default:
			break;
		}
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void relayMetaData(RelayChannelContext ctx, RelayJobContext job) {
		// fetch the channel from the DB with uptodate info
		Channel c = relayDataService.getChannel(ctx.getAccountZone(), ctx.getZone(), ctx.getDomain(),
				job.getObjectId());
		if (c == null) {
			log.warn("Unable to find channel for meta data relay. " + job);
			return;
		}

		// relay the CA
		if (c.getAuthorization().getProcessingState().getStatus() == ProcessingStatus.PENDING) {
			if (ctx.getDirection() == RelayDirection.Both) {
				// TODO #93: LATER shortcut relay for same domain
			} else {
				EndpointPermission perm = ctx.getDirection() == RelayDirection.Fowards
						? c.getAuthorization().getSendAuthorization() : c.getAuthorization().getRecvAuthorization();
				if (perm != null) {
					MRSSessionHolder sh = getSessionHolder(ctx);
					if (sh.isValid()) {
						// TODO #93: use the MRS to relay the CA to the other side
					} else {
						// TODO #93: update CA processing state to error of the MRS session holder error
					}
				} else {
					// TODO #93: set the PS to error - missing perm
				}
			}
		}

		// relay the CDS receiver to sender
		if (c.getProcessingState().getStatus() == ProcessingStatus.PENDING) {
			if (ctx.getDirection() == RelayDirection.Both) {
				// TODO #93: LATER shortcut relay for same domain
			} else if (ctx.getDirection() == RelayDirection.Backwards) {
				if (c.getSession() != null) {
					MRSSessionHolder sh = getSessionHolder(ctx);
					if (sh.isValid()) {
						// TODO #93: use the MRS to relay the CDS to the sender side
					} else {
						// TODO update CDS processing state to error of the MRS session holder error
					}

				} else {
					// TODO #93: error no CDS
				}
			} else {
				// it is an error to want to relay the CDS from origin to destination
				// TODO #93: error
			}
		}

		// relay the FC-open receiver to sender
		if (c.getQuota().getProcessingState().getStatus() == ProcessingStatus.PENDING) {
			if (ctx.getDirection() == RelayDirection.Both) {
				// TODO #93: error should never be set to pending
			} else if (ctx.getDirection() == RelayDirection.Backwards) {
				MRSSessionHolder sh = getSessionHolder(ctx);
				if (sh.isValid()) {
					// TODO #93: use the MRS to relay the FC-open to the sender side
				} else {
					// TODO update FC processing state to error of the MRS session holder error
				}
			} else {
				// it is an error to want to relay the CDS from origin to destination
				// TODO #93: error
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

}
