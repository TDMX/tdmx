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
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Error;
import org.tdmx.core.api.v01.mrs.Relay;
import org.tdmx.core.api.v01.mrs.RelayResponse;
import org.tdmx.core.api.v01.mrs.ws.MRS;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Flowcontrolstatus;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.message.domain.Chunk;
import org.tdmx.lib.message.service.ChunkService;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.DestinationSession;
import org.tdmx.lib.zone.domain.EndpointPermission;
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
import org.tdmx.server.tos.client.TransferClientService;
import org.tdmx.server.tos.client.TransferStatus;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.ApiValidator;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthorizedSessionLookupService;

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

	private int batchSize = 100;

	private TransferClientService transferService;

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
			// TODO #93: relay in FC-open
		} else {
			// none of the above - equals missing data.
			setError(ErrorCode.MissingRelayPayload, response);
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
			setError(ErrorCode.InvalidSignatureEndpointPermission, response);
			return;
		}
		// the signature of the Authorization needs checking.
		EndpointPermission otherPerm = a2d.mapEndpointPermission(auth);

		AgentCredentialDescriptor dac = credentialFactory
				.createAgentCredential(otherPerm.getSignature().getCertificateChain());
		if (dac == null || dac.getCredentialType() != AgentCredentialType.DAC) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return;
		}
		if (!credentialValidator.isValid(dac)) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return;
		}

		Zone zone = session.getZone();
		if (sessionChannel != null) {
			// TODO #93: return channel with flowquota
			channelService.relayAuthorization(zone, sessionChannel.getId(), otherPerm);
		} else if (tempChannel != null) {
			// create a new Channel and swap the tempChannel for newChannel
			Channel newChannel = channelService.relayInitialAuthorization(zone, tempChannel.getId(), otherPerm);
			session.setTemporaryChannel(null);
			session.setChannel(newChannel);
		}
		// TODO #93: receiver provide the flowquota's relaystate back to the caller in the relay response.

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
			setError(ErrorCode.InvalidSignatureDestinationSession, response);
			return;
		}

		AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(
				ds.getUsersignature().getUserIdentity().getUsercertificate(),
				ds.getUsersignature().getUserIdentity().getDomaincertificate(),
				ds.getUsersignature().getUserIdentity().getRootcertificate());
		if (uc == null || uc.getCredentialType() != AgentCredentialType.UC) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return;
		}
		if (!credentialValidator.isValid(uc)) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return;
		}
		DestinationSession cds = a2d.mapDestinationSession(ds);

		Zone zone = session.getZone();

		// we don't tell the destination side of the receivers flow control status
		// only the other way around.
		channelService.relayChannelDestinationSession(zone, sessionChannel.getId(), cds);

		response.setSuccess(true);
	}

	// handle the message inbound
	private void processMessage(Msg msg, RelayResponse response) {
		MRSServerSession session = authorizedSessionService.getAuthorizedSession();

		// check that the Message provided is complete
		if (validator.checkMessage(msg, response) == null) {
			return;
		}
		Header header = msg.getHeader();
		Payload payload = msg.getPayload();
		if (!SignatureUtils.checkMsgId(header, header.getUsersignature().getSignaturevalue().getTimestamp())) {
			setError(ErrorCode.InvalidMsgId, response);
			return;
		}
		if (!SignatureUtils.checkPayloadSignature(payload, header)) {
			setError(ErrorCode.InvalidSignatureMessagePayload, response);
			return;
		}
		if (!SignatureUtils.checkHeaderSignature(header)) {
			setError(ErrorCode.InvalidSignatureMessageHeader, response);
			return;
		}

		Chunk c = a2d.mapChunk(msg.getChunk());
		ChannelMessage m = a2d.mapMessage(msg);

		AgentCredentialDescriptor srcUc = credentialFactory.createAgentCredential(
				header.getUsersignature().getUserIdentity().getUsercertificate(),
				header.getUsersignature().getUserIdentity().getDomaincertificate(),
				header.getUsersignature().getUserIdentity().getRootcertificate());
		if (srcUc == null || AgentCredentialType.UC != srcUc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return;
		}
		// check srcUser's domain matches the origin's domain of the channel
		if (!msg.getHeader().getChannel().getOrigin().getDomain().equals(srcUc.getDomainName())) {
			setError(ErrorCode.ChannelOriginUserDomainMismatch, response);
			return;
		}
		if (!msg.getHeader().getChannel().getOrigin().getLocalname().equals(srcUc.getAddressName())) {
			setError(ErrorCode.ChannelOriginUserDomainMismatch, response);
			return;
		}

		AgentCredentialDescriptor dstUc = credentialFactory.createAgentCredential(header.getTo().getUsercertificate(),
				header.getTo().getDomaincertificate(), header.getTo().getRootcertificate());
		if (dstUc == null || AgentCredentialType.UC != dstUc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return;
		}
		// check destUser's domain matches the destination's domain of the channel
		if (!msg.getHeader().getChannel().getDestination().getDomain().equals(dstUc.getDomainName())) {
			setError(ErrorCode.ChannelDestinationUserDomainMismatch, response);
			return;
		}
		if (!msg.getHeader().getChannel().getDestination().getLocalname().equals(dstUc.getAddressName())) {
			setError(ErrorCode.ChannelDestinationUserDomainMismatch, response);
			return;
		}

		Zone zone = session.getZone();
		Channel channel = session.getChannel();

		m.setChannel(channel);

		SubmitMessageResultHolder result = channelService.preRelayMessage(zone, m);
		if (result.status != null) {
			// provide the flowquota's relaystate back to the caller in the relay response.
			response.setRelayStatus(Flowcontrolstatus.CLOSED);
			setError(mapSubmitOperationStatus(result.status), response);
			return;
		}

		// persist Chunk via ChunkService
		chunkService.createOrUpdate(c);

		// persist the message itself.
		m.setProcessingState(ProcessingState.pending());
		channelService.create(m);

		// attempt to "fast" transfer the MSG to the MDS responsible for the channel's destination.
		// TODO #96: keep track of when last tried to lookup any sessionId
		TransferStatus ts = transferService.transferMDS(null, null, session.getAccountZone(), zone, session.getDomain(),
				channel, m);
		// TODO #96: keep the tosAddress, sessionId in the session for later use.

		response.setSuccess(true);
	}

	private void setError(ErrorCode ec, Acknowledge ack) {
		Error error = new Error();
		error.setCode(ec.getErrorCode());
		error.setDescription(ec.getErrorDescription());
		ack.setError(error);
		ack.setSuccess(false);
	}

	private ErrorCode mapSubmitOperationStatus(SubmitMessageOperationStatus status) {
		switch (status) {
		case FLOW_CONTROL_CLOSED:
			return ErrorCode.ReceiveFlowControlClosed;
		case CHANNEL_CLOSED:
			return ErrorCode.ReceiveChannelClosed;
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

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public TransferClientService getTransferService() {
		return transferService;
	}

	public void setTransferService(TransferClientService transferService) {
		this.transferService = transferService;
	}

}
