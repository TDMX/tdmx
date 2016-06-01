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
package org.tdmx.server.ws.mrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.mrs.Relay;
import org.tdmx.core.api.v01.mrs.RelayResponse;
import org.tdmx.core.api.v01.mrs.ws.MRS;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.lib.message.domain.Chunk;
import org.tdmx.lib.message.service.ChunkService;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelName;
import org.tdmx.lib.zone.domain.DestinationSession;
import org.tdmx.lib.zone.domain.EndpointPermission;
import org.tdmx.lib.zone.domain.FlowQuota;
import org.tdmx.lib.zone.domain.MessageState;
import org.tdmx.lib.zone.domain.MessageStatus;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.AgentCredentialValidator;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.ChannelService.SubmitMessageOperationStatus;
import org.tdmx.lib.zone.service.ChannelService.SubmitMessageResultHolder;
import org.tdmx.lib.zone.service.DestinationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.server.ros.client.RelayClientService;
import org.tdmx.server.tos.client.TransferClientService;
import org.tdmx.server.tos.client.TransferStatus;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.ApiValidator;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.mrs.MRSServerSession.DestinationContextHolder;
import org.tdmx.server.ws.security.service.AuthorizedSessionLookupService;

/**
 * ChannelMessages are submitted one at a time from concurrent clients sharing the same session. The message is
 * automatically considered relayed once all chunks have been received from the message. Chunks must be relayed
 * sequentially, we only allow to repeat send the "last" chunk received.
 * 
 * @author Peter
 *
 */
public class MRSImpl implements MRS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MRSImpl.class);

	private AuthorizedSessionLookupService<MRSServerSession> authorizedSessionService;

	private DomainService domainService;
	private AddressService addressService;
	private ServiceService serviceService;
	private ChannelService channelService;
	private DestinationService destinationService;

	private AgentCredentialFactory credentialFactory;
	private AgentCredentialService credentialService;
	private AgentCredentialValidator credentialValidator;

	private ChunkService chunkService;

	private final DomainToApiMapper d2a = new DomainToApiMapper();
	private final ApiToDomainMapper a2d = new ApiToDomainMapper();
	private final ApiValidator validator = new ApiValidator();

	private TransferClientService transferService;
	private RelayClientService relayClientService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public RelayResponse relay(Relay parameters) {

		RelayResponse response = new RelayResponse();
		if (parameters.getPermission() != null) {
			processChannelAuthorization(parameters.getPermission(), response);
		} else if (parameters.getDestinationsession() != null) {
			processChannelDestinationSession(parameters.getDestinationsession(), response);
		} else if (parameters.getMsg() != null) {
			processMessage(parameters.getMsg(), response);
		} else if (parameters.getChunk() != null) {
			// TODO #70: relay in Chunk
		} else if (parameters.getDr() != null) {
			// TODO #95: relay in DR
		} else if (parameters.getRelayStatus() != null) {
			// TODO #93: relay in FC-open, notify ROS of FC change
		} else {
			// none of the above - equals missing data.
			ErrorCode.setError(ErrorCode.MissingRelayPayload, response);
		}
		return response;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// handle the channel authorization relayed inbound.
	private void processChannelAuthorization(Permission auth, RelayResponse response) {
		MRSServerSession session = authorizedSessionService.getAuthorizedSession();

		// check that the Channel and EndpointPermission represented by the Auth is complete
		if (validator.checkEndpointPermission(auth, response) == null) {
			return;
		}

		org.tdmx.core.api.v01.msg.Channel channel = null;

		Channel sessionChannel = session.getChannel();
		if (sessionChannel != null) {
			channel = d2a.mapChannel(sessionChannel);
		}
		TemporaryChannel tempChannel = session.getTemporaryChannel();
		if (tempChannel != null) {
			channel = d2a.mapChannel(tempChannel);
		}

		if (!SignatureUtils.checkEndpointPermissionSignature(channel, auth)) {
			ErrorCode.setError(ErrorCode.InvalidSignatureEndpointPermission, response);
			return;
		}
		// the signature of the Authorization needs checking.
		EndpointPermission otherPerm = a2d.mapEndpointPermission(auth);

		AgentCredentialDescriptor dac = credentialFactory
				.createAgentCredential(otherPerm.getSignature().getCertificateChain());
		if (dac == null || dac.getCredentialType() != AgentCredentialType.DAC) {
			ErrorCode.setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return;
		}
		if (!credentialValidator.isValid(dac)) {
			ErrorCode.setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return;
		}

		Zone zone = session.getZone();
		Channel usedChannel = null;
		if (sessionChannel != null) {
			// apply the new permission
			usedChannel = channelService.relayAuthorization(zone, sessionChannel.getId(), otherPerm);
		} else if (tempChannel != null) {
			// create a new Channel and swap the tempChannel for newChannel
			Channel newChannel = channelService.relayInitialAuthorization(zone, tempChannel.getId(), otherPerm);
			session.setTemporaryChannel(null);
			session.setChannel(newChannel);
			usedChannel = newChannel;
		}
		// receiver provide the flowquota's relaystate back to the caller in the relay response.
		if (usedChannel.isRecv()) {
			response.setRelayStatus(d2a.mapFlowControlStatus(usedChannel.getQuota().getRelayStatus()));
		}
		response.setSuccess(true);
	}

	// handle the channel destination session relayed inbound.
	private void processChannelDestinationSession(Destinationsession ds, RelayResponse response) {
		MRSServerSession session = authorizedSessionService.getAuthorizedSession();

		// check that the Channel and EndpointPermission represented by the Auth is complete
		if (validator.checkDestinationsession(ds, response) == null) {
			return;
		}

		Channel sessionChannel = session.getChannel();
		org.tdmx.core.api.v01.msg.Channel channel = d2a.mapChannel(sessionChannel);

		if (!SignatureUtils.checkDestinationSessionSignature(channel.getDestination().getServicename(), ds)) {
			ErrorCode.setError(ErrorCode.InvalidSignatureDestinationSession, response);
			return;
		}

		AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(
				ds.getUsersignature().getUserIdentity().getUsercertificate(),
				ds.getUsersignature().getUserIdentity().getDomaincertificate(),
				ds.getUsersignature().getUserIdentity().getRootcertificate());
		if (uc == null || uc.getCredentialType() != AgentCredentialType.UC) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, response);
			return;
		}
		if (!credentialValidator.isValid(uc)) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, response);
			return;
		}
		DestinationSession cds = a2d.mapDestinationSession(ds);

		Zone zone = session.getZone();

		// we don't tell the destination side of the receivers flow control status
		// only the other way around.
		channelService.relayChannelDestinationSession(zone, sessionChannel.getId(), cds);

		response.setSuccess(true);
	}

	// handle the message inbound with 1st chunk
	private void processMessage(Msg msg, RelayResponse response) {
		MRSServerSession session = authorizedSessionService.getAuthorizedSession();

		// check that the Message provided is complete. For messages relayed within the same segment
		// the chunk's are not actually transferred. TODO check definition "shortcut"==same segment / rename
		if (validator.checkMessage(msg, response, !session.isShortcutSession()) == null) {
			return;
		}
		Header header = msg.getHeader();
		Payload payload = msg.getPayload();
		if (!SignatureUtils.checkMsgId(header, payload, header.getUsersignature().getSignaturevalue().getTimestamp())) {
			ErrorCode.setError(ErrorCode.InvalidMsgId, response);
			return;
		}
		if (!SignatureUtils.checkMessageSignature(header, payload)) {
			ErrorCode.setError(ErrorCode.InvalidSignatureMessage, response);
			return;
		}
		ChannelMessage m = a2d.mapMessage(msg);

		AgentCredentialDescriptor srcUc = credentialFactory.createAgentCredential(
				header.getUsersignature().getUserIdentity().getUsercertificate(),
				header.getUsersignature().getUserIdentity().getDomaincertificate(),
				header.getUsersignature().getUserIdentity().getRootcertificate());
		if (srcUc == null || AgentCredentialType.UC != srcUc.getCredentialType()) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, response);
			return;
		}
		// check srcUser's domain matches the origin's domain of the channel
		if (!msg.getHeader().getChannel().getOrigin().getDomain().equals(srcUc.getDomainName())) {
			ErrorCode.setError(ErrorCode.ChannelOriginUserDomainMismatch, response);
			return;
		}
		if (!msg.getHeader().getChannel().getOrigin().getLocalname().equals(srcUc.getAddressName())) {
			ErrorCode.setError(ErrorCode.ChannelOriginUserDomainMismatch, response);
			return;
		}

		AgentCredentialDescriptor dstUc = credentialFactory.createAgentCredential(header.getTo().getUsercertificate(),
				header.getTo().getDomaincertificate(), header.getTo().getRootcertificate());
		if (dstUc == null || AgentCredentialType.UC != dstUc.getCredentialType()) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, response);
			return;
		}
		// check destUser's domain matches the destination's domain of the channel
		if (!msg.getHeader().getChannel().getDestination().getDomain().equals(dstUc.getDomainName())) {
			ErrorCode.setError(ErrorCode.ChannelDestinationUserDomainMismatch, response);
			return;
		}
		if (!msg.getHeader().getChannel().getDestination().getLocalname().equals(dstUc.getAddressName())) {
			ErrorCode.setError(ErrorCode.ChannelDestinationUserDomainMismatch, response);
			return;
		}

		Zone zone = session.getZone();
		Channel channel = session.getChannel();

		m.setChannel(channel);
		m.initMessageState(zone, MessageStatus.READY, srcUc.getSerialNumber(), dstUc.getSerialNumber());

		// check if we have space and are allowed to receive the message
		SubmitMessageResultHolder result = channelService.checkChannelQuota(zone, channel, m.getPayloadLength(),
				m.getPayloadLength());
		if (result.status != null) {
			// provide the flowquota's relaystate back to the caller in the relay response.
			response.setRelayStatus(d2a.mapFlowControlStatus(result.flowQuota.getRelayStatus()));
			ErrorCode.setError(mapSubmitOperationStatus(result.status), response);
			return;
		}

		// the chunks are not transferred for shortcut relaying.
		if (!session.isShortcutSession()) {

			Chunk c = a2d.mapChunk(msg.getChunk());

			// validate chunk MAC - error chunk MAC
			if (validator.checkChunkMac(msg.getChunk(), m.getScheme(), response) == null) {
				return;
			}

			MessageRelayContext mrc = new MessageRelayContext(m);
			session.setMessageContext(m.getMsgId(), mrc);

			if (handleChunkReceipt(zone, session, mrc, c, response) == null) {
				return;
			}
		} else {

			// persist the message itself. MessageStatus is READY, ProcessingState is "none".
			FlowQuota afterSendQuota = channelService.relayInMessage(zone, m);
			response.setRelayStatus(d2a.mapFlowControlStatus(afterSendQuota.getRelayStatus()));

			// attempt to "fast" transfer the MSG to the MDS responsible for the channel's destination.
			transferReceiver(session, m.getState());
		}

		response.setSuccess(true);
	}

	private Chunk handleChunkReceipt(Zone zone, MRSServerSession session, MessageRelayContext mrc, Chunk c,
			RelayResponse response) {
		if (mrc.setChunkReceivedInOrder(c.getPos(), c.getMac())) {
			// persist Chunk via ChunkService
			chunkService.createOrUpdate(c);

			if (mrc.isComplete()) {
				log.debug("Received all chunks for message " + mrc.getMsgId());
				// received all chunks - finish the message
				session.removeMessageContext(mrc.getMsgId());
				if (!mrc.isCorrect()) {
					// TODO #107: chunk cleanup - remove all previously received chunks
					// since channel message is not yet persisted and the quota is untouched,
					// we don't need to undo much, just one chunk
					ErrorCode.setError(ErrorCode.InvalidMessageMacOfMac, response);
					return null;
				} else {
					// persist the message itself only after all chunks are received, on receipt of last chunk
					FlowQuota afterSendQuota = channelService.relayInMessage(zone, mrc.getMsg());
					response.setRelayStatus(d2a.mapFlowControlStatus(afterSendQuota.getRelayStatus()));
					// fast transfer to MDS
					transferReceiver(session, mrc.getMsg().getState());
				}
			} else {
				log.debug("Expecting further chunks for message " + mrc.getMsgId());
			}
		} else {
			// Error chunkproblem sequence problem, relaying side forced to restart
			session.removeMessageContext(mrc.getMsgId());
			// TODO #107: chunk cleanup - remove all previously received chunks
			ErrorCode.setError(ErrorCode.InvalidChunkOrder, response);
			return null;
		}
		return c;
	}

	private void transferReceiver(MRSServerSession session, MessageState state) {
		final ChannelName cn = state.getChannelName();
		DestinationContextHolder ddh = session.getDestinationContext(cn.getDestinationName());

		// TODO LATER: improve IO usage by not retrying on each received message when we don't have a receiver session.

		// transfer message to MDS for same domain receiver to take.
		// we don't retry since the receiver finds the messages from the DB if we don't fast transfer with this TOS
		// mechanism.
		final TransferStatus ts = transferService.transferMDS(ddh.getTosTcpAddress(), ddh.getSessionId(),
				session.getAccountZone(), ddh.getDestination(), state.getId());
		if (!ts.isSuccess()) {
			// we clear the not working TOS address of the MDS and it's session
			if (ts.getErrorCode() != org.tdmx.server.tos.client.TransferStatus.ErrorCode.PCS_SESSION_NOT_FOUND) {
				// PCS session not found is normal if there is no active receiver.
				log.warn("MRS->MDS message transfer failure  " + ts);
			}
			ddh.setSessionId(null);
			ddh.setTosTcpAddress(null);
		} else {
			// possibly changed
			ddh.setTosTcpAddress(ts.getTosTcpAddress());
			ddh.setSessionId(ts.getSessionId());
		}
	}

	private ErrorCode mapSubmitOperationStatus(SubmitMessageOperationStatus status) {
		switch (status) {
		case FLOW_CONTROL_CLOSED:
			return ErrorCode.ReceiveFlowControlClosed;
		case CHANNEL_CLOSED:
			return ErrorCode.ReceiveChannelClosed;
		case MESSAGE_TOO_LARGE:
			return ErrorCode.SubmitMessageTooLarge;
		case NOT_ENOUGH_QUOTA_AVAILABLE:
			return ErrorCode.SubmitQuotaNotSufficient;
		default:
			return null;
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AuthorizedSessionLookupService<MRSServerSession> getAuthorizedSessionService() {
		return authorizedSessionService;
	}

	public void setAuthorizedSessionService(AuthorizedSessionLookupService<MRSServerSession> authorizedSessionService) {
		this.authorizedSessionService = authorizedSessionService;
	}

	public DomainService getDomainService() {
		return domainService;
	}

	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}

	public AddressService getAddressService() {
		return addressService;
	}

	public void setAddressService(AddressService addressService) {
		this.addressService = addressService;
	}

	public ServiceService getServiceService() {
		return serviceService;
	}

	public void setServiceService(ServiceService serviceService) {
		this.serviceService = serviceService;
	}

	public ChannelService getChannelService() {
		return channelService;
	}

	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

	public DestinationService getDestinationService() {
		return destinationService;
	}

	public void setDestinationService(DestinationService destinationService) {
		this.destinationService = destinationService;
	}

	public AgentCredentialFactory getCredentialFactory() {
		return credentialFactory;
	}

	public void setCredentialFactory(AgentCredentialFactory credentialFactory) {
		this.credentialFactory = credentialFactory;
	}

	public AgentCredentialService getCredentialService() {
		return credentialService;
	}

	public void setCredentialService(AgentCredentialService credentialService) {
		this.credentialService = credentialService;
	}

	public AgentCredentialValidator getCredentialValidator() {
		return credentialValidator;
	}

	public void setCredentialValidator(AgentCredentialValidator credentialValidator) {
		this.credentialValidator = credentialValidator;
	}

	public ChunkService getChunkService() {
		return chunkService;
	}

	public void setChunkService(ChunkService chunkService) {
		this.chunkService = chunkService;
	}

	public TransferClientService getTransferService() {
		return transferService;
	}

	public void setTransferService(TransferClientService transferService) {
		this.transferService = transferService;
	}

	public RelayClientService getRelayClientService() {
		return relayClientService;
	}

	public void setRelayClientService(RelayClientService relayClientService) {
		this.relayClientService = relayClientService;
	}

}
