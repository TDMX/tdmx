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
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.lib.message.domain.Chunk;
import org.tdmx.lib.message.service.ChunkService;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.DestinationSession;
import org.tdmx.lib.zone.domain.EndpointPermission;
import org.tdmx.lib.zone.domain.MessageDescriptor;
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
			// TODO relay in Chunk
		} else {

			// TODO other relays
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
		Channel sessionChannel = session.getChannel();

		org.tdmx.core.api.v01.msg.Channel channel = null; // TODO map from "session"
		if (sessionChannel != null) {
			channel = d2a.mapChannel(sessionChannel);
		} else {
			// temporary channel concept SCS->MRS

		}
		if (!SignatureUtils.checkEndpointPermissionSignature(channel, auth, true)) {
			setError(ErrorCode.InvalidSignatureEndpointPermission, response);
			return;
		}
		// the signature of the Authorization needs checking.
		EndpointPermission otherPerm = a2d.mapEndpointPermission(auth);

		AgentCredentialDescriptor dac = credentialFactory.createAgentCredential(otherPerm.getSignature()
				.getCertificateChain());
		if (dac == null || dac.getCredentialType() != AgentCredentialType.DAC) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return;
		}
		if (!credentialValidator.isValid(dac)) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return;
		}

		// we need to find the local Domain to be able then to set/process the CA on it.
		/*
		 * String remoteDomain = dac.getDomainName(); String localDomain = null; if
		 * (auth.getChannel().getDestination().getDomain().equals(remoteDomain)) { // if the signer's domain matches the
		 * channel destination's domain then this is a reqRecvAuthorization at // the // origin, where we need to lookup
		 * the zone and domain of the origin, given the domain of the origin. localDomain =
		 * auth.getChannel().getOrigin().getDomain(); } else { // if the signer's domain matches the channel origin's
		 * domain then the CA is a reqSendAuthorization at the // destination, where we need to lookup the zone of the
		 * destination, but we only have the domain of the // destination. localDomain =
		 * auth.getChannel().getDestination().getDomain(); }
		 * 
		 * // TODO the ROOT CA of the signing party needs to be trusted in DNS! We don't want to allow someone just to
		 * fake // channel authorizations and inject them via the relay without also spoofing DNS too!
		 * 
		 * // TODO lookup the origin or destination domain in DNS and retrieve their zone root information AccountZone
		 * accountZone = null; if (accountZone == null) { // FIXME! temporary fallback on recursive lookup of domain to
		 * domain parent until matches with a zone. accountZone = lookupAccountZoneByDomain(localDomain); }
		 * 
		 * if (accountZone == null) { setError(ErrorCode.ZoneNotFound, response); return; }
		 */
		Zone zone = session.getZone();
		Long channelId = sessionChannel.getId(); // FIXME

		// using the ZoneDB specified in the AccountZone's partitionID, find the Zone and then Domain
		// and then call ChannelService#relayChannelAuthorization
		channelService.relayAuthorization(zone, channelId, otherPerm);

		response.setSuccess(true);
	}

	// handle the channel destinationsession relayed inbound.
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

		AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(ds.getUsersignature().getUserIdentity()
				.getUsercertificate(), ds.getUsersignature().getUserIdentity().getDomaincertificate(), ds
				.getUsersignature().getUserIdentity().getRootcertificate());
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
		MessageDescriptor md = a2d.mapMessage(msg);

		AgentCredentialDescriptor srcUc = credentialFactory.createAgentCredential(header.getUsersignature()
				.getUserIdentity().getUsercertificate(), header.getUsersignature().getUserIdentity()
				.getDomaincertificate(), header.getUsersignature().getUserIdentity().getRootcertificate());
		if (srcUc == null || AgentCredentialType.UC != srcUc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return;
		}
		AgentCredentialDescriptor dstUc = credentialFactory.createAgentCredential(header.getTo().getUsercertificate(),
				header.getTo().getDomaincertificate(), header.getTo().getRootcertificate());
		if (dstUc == null || AgentCredentialType.UC != dstUc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return;
		}

		Zone zone = session.getZone();
		Channel channel = session.getChannel();

		// check the flow is not already throttled
		SubmitMessageResultHolder result = channelService.relayMessage(zone, channel, md);
		if (result.status != null) {
			setError(mapSubmitOperationStatus(result.status), response);
			return;
		}

		// persist Chunk via ChunkService
		chunkService.createOrUpdate(c);

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

}
