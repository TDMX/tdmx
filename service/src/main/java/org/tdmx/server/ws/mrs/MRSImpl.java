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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.chunk.domain.Chunk;
import org.tdmx.lib.chunk.service.ChunkService;
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

import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;

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

	private ScheduledExecutorService msgTimeoutScheduler = Executors.newScheduledThreadPool(1,
			new RenamingThreadFactoryProxy("RelayMsgTimeoutScheduler", Executors.defaultThreadFactory()));

	private int messageIdleTimeoutSec = 300;

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
			processChunk(parameters.getContinuation(), parameters.getChunk(), response);
		} else if (parameters.getRelayStatus() != null) {
			// TODO #113: relay in FC-open, notify ROS of FC change
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
		// the chunk's are not actually transferred.
		if (validator.checkMessage(msg, response, !session.isSameSegmentShortcutSession()) == null) {
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
		MessageRelayContext mrc = new MessageRelayContext(m);
		session.setMessageContext(m.getMsgId(), mrc);

		if (session.isSameSegmentShortcutSession()) {
			// for messages not effectively relayed but are handled by the same segment, the chunks are shared between
			// the two
			// messages ( origin and destination ) and don't need transfer.
			finishMessage(session, mrc, response);

		} else {
			// propper relaying embeds the chunk in the msg transferred and then sends chunks individually, in order
			// afterwards, repeating at most the last chunk
			String fakedContinuationId = mrc.getContinuationId(0);
			processChunk(fakedContinuationId, msg.getChunk(), response);
		}

		response.setSuccess(true);
	}

	// handle a subsequent message chunk
	private void processChunk(String continuationId, org.tdmx.core.api.v01.msg.Chunk relayedChunk,
			RelayResponse response) {
		MRSServerSession session = authorizedSessionService.getAuthorizedSession();

		if (!StringUtils.hasText(continuationId)) {
			ErrorCode.setError(ErrorCode.MissingChunkContinuationId, response);
			return;
		}
		// safeguard to prohibit relay of chunk data on the same segment.
		if (session.isSameSegmentShortcutSession()) {
			ErrorCode.setError(ErrorCode.ChunkDataNotRelayed, response);
			return;
		}

		// check that the Message provided is complete. For messages relayed within the same segment
		// the chunk's are not actually transferred.
		if (validator.checkChunk(relayedChunk, response) == null) {
			return;
		}

		MessageRelayContext mrc = session.getMessageContext(relayedChunk.getMsgId());
		if (mrc == null) {
			ErrorCode.setError(ErrorCode.MessageNotFound, response);
			return;
		}
		if (!continuationId.equals(mrc.getContinuationId(relayedChunk.getPos()))) {
			ErrorCode.setError(ErrorCode.InvalidChunkContinuationId, response);
			return;
		}
		if (relayedChunk.getPos() < mrc.getLastChunkReceived()
				|| relayedChunk.getPos() > mrc.getLastChunkReceived() + 1) {
			ErrorCode.setError(ErrorCode.InvalidChunkOrder, response);
			return;
		}

		// validate chunk MAC - error chunk MAC
		if (validator.checkChunkMac(relayedChunk, mrc.getMsg().getScheme(), response) == null) {
			return;
		}

		Chunk c = a2d.mapChunk(mrc.getMsg(), relayedChunk);

		// persist Chunk via ChunkService
		chunkService.storeChunk(mrc.getMsg(), c);

		// calculate the next continuationId
		String nextContinuationId = mrc.getContinuationId(c.getPos() + 1);
		if (nextContinuationId == null) {
			log.debug("Received all chunks for message " + mrc.getMsgId());
			// received all chunks - finish the message
			finishMessage(session, mrc, response);

		} else {
			log.debug("Expecting further chunks for message " + mrc.getMsgId());
			rescheduleMessageTimeout(session, mrc);

		}
		// record the last successful uploaded so we allow only it again or the next.
		mrc.setLastChunkReceived(c.getPos());
		response.setContinuation(nextContinuationId);
		response.setSuccess(true);
	}

	private void abortMessage(MRSServerSession session, MessageRelayContext mrc) {
		cancelMessageTimeout(mrc);
		session.removeMessageContext(mrc.getMsgId());
		// chunk cleanup - remove all previously received chunks, since the msg doesn't exist yet and must be resent.
		deleteChunks(mrc.getMsg());
	}

	private void deleteChunks(ChannelMessage msg) {
		if (msg == null) {
			log.warn("No message to delete chunks for.");
			// TODO #112: incident service provider
			return;
		}
		if (!chunkService.deleteChunks(msg)) {
			log.warn("Unable to delete chunks for " + msg);
			// TODO #112: incident event relevant for the originating SP.
		}
	}

	private void finishMessage(MRSServerSession session, MessageRelayContext mrc, RelayResponse response) {
		// persist the message itself only after all chunks are received, on receipt of last chunk
		FlowQuota afterSendQuota = channelService.relayInMessage(session.getZone(), mrc.getMsg());
		response.setRelayStatus(d2a.mapFlowControlStatus(afterSendQuota.getRelayStatus()));
		// fast transfer to MDS
		transferReceiver(session, mrc.getMsg().getState());

		cancelMessageTimeout(mrc);
		session.removeMessageContext(mrc.getMsgId());
	}

	private void cancelMessageTimeout(MessageRelayContext mrc) {
		ScheduledFuture<?> f = mrc.getTimeoutFuture();
		if (f != null) {
			f.cancel(false);
			mrc.setTimeoutFuture(null);
		}
	}

	private void rescheduleMessageTimeout(MRSServerSession session, MessageRelayContext mrc) {
		// transaction will timeout after the tx timeout seconds after starting the tx.
		cancelMessageTimeout(mrc);
		ScheduledFuture<?> timeoutFuture = msgTimeoutScheduler.schedule(new Runnable() {
			@Override
			public void run() {
				// we simply forget the transaction if it times-out.
				log.info("Timeout of " + mrc.getMsgId());
				abortMessage(session, mrc);
			}
		}, getMessageIdleTimeoutSec(), TimeUnit.SECONDS);
		// we need to record the future object so we can cancel later ( when tx completes )
		mrc.setTimeoutFuture(timeoutFuture);
	}

	private void transferReceiver(MRSServerSession session, MessageState state) {
		final ChannelName cn = state.getChannelName();
		DestinationContextHolder ddh = session.getDestinationContext(cn.getDestinationName());
		if (ddh == null) {
			ddh = new DestinationContextHolder(cn.getDestinationName(), cn.getDestination());
			session.setDestinationContext(cn.getDestinationName(), ddh);
		}

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

	public int getMessageIdleTimeoutSec() {
		return messageIdleTimeoutSec;
	}

	public void setMessageIdleTimeoutSec(int messageIdleTimeoutSec) {
		this.messageIdleTimeoutSec = messageIdleTimeoutSec;
	}

}
