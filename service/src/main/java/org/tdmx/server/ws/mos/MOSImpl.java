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
package org.tdmx.server.ws.mos;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Error;
import org.tdmx.core.api.v01.mos.GetAddress;
import org.tdmx.core.api.v01.mos.GetAddressResponse;
import org.tdmx.core.api.v01.mos.GetChannel;
import org.tdmx.core.api.v01.mos.GetChannelResponse;
import org.tdmx.core.api.v01.mos.GetMessageDeliveryStatus;
import org.tdmx.core.api.v01.mos.GetMessageDeliveryStatusResponse;
import org.tdmx.core.api.v01.mos.ListChannel;
import org.tdmx.core.api.v01.mos.ListChannelResponse;
import org.tdmx.core.api.v01.mos.Receipt;
import org.tdmx.core.api.v01.mos.ReceiptResponse;
import org.tdmx.core.api.v01.mos.Submit;
import org.tdmx.core.api.v01.mos.SubmitResponse;
import org.tdmx.core.api.v01.mos.Upload;
import org.tdmx.core.api.v01.mos.UploadResponse;
import org.tdmx.core.api.v01.mos.ws.MOS;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.tx.Commit;
import org.tdmx.core.api.v01.tx.CommitResponse;
import org.tdmx.core.api.v01.tx.Forget;
import org.tdmx.core.api.v01.tx.ForgetResponse;
import org.tdmx.core.api.v01.tx.Prepare;
import org.tdmx.core.api.v01.tx.PrepareResponse;
import org.tdmx.core.api.v01.tx.Recover;
import org.tdmx.core.api.v01.tx.RecoverResponse;
import org.tdmx.core.api.v01.tx.Rollback;
import org.tdmx.core.api.v01.tx.RollbackResponse;
import org.tdmx.core.api.v01.tx.Transaction;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.message.domain.Chunk;
import org.tdmx.lib.message.service.ChunkService;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelFlowMessage;
import org.tdmx.lib.zone.domain.FlowControlStatus;
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
import org.tdmx.server.ws.security.service.AuthenticatedAgentLookupService;

public class MOSImpl implements MOS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MOSImpl.class);

	private AuthenticatedAgentLookupService agentService;

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
	public CommitResponse commit(Commit parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RollbackResponse rollback(Rollback parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ForgetResponse forget(Forget parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PrepareResponse prepare(Prepare parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecoverResponse recover(Recover parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetAddressResponse getAddress(GetAddress parameters) {
		GetAddressResponse response = new GetAddressResponse();
		PKIXCertificate authorizedUser = checkUserAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		// check that the UC credential exists
		AgentCredential existingCred = credentialService.findByFingerprint(authorizedUser.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}

		ChannelEndpoint ep = new ChannelEndpoint();
		ep.setDomain(existingCred.getDomain().getDomainName());
		ep.setLocalname(existingCred.getAddress().getLocalName());
		response.setOrigin(ep);

		response.setSuccess(true);
		return response;
	}

	@Override
	public SubmitResponse submit(Submit parameters) {
		SubmitResponse response = new SubmitResponse();
		PKIXCertificate authorizedUser = checkUserAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		// validate Msg fields present ( payload, header and chunk )
		if (validator.checkMessage(parameters.getMsg(), response) == null) {
			return response;
		}
		// TODO validate Tx fields present if provided
		Transaction tx = parameters.getTransaction();

		// TODO check chunk's mac

		// check chunk's mac is 1st CRC in payload CRC manifest

		Msg msg = parameters.getMsg();
		Header header = msg.getHeader();
		Payload payload = msg.getPayload();
		if (!SignatureUtils.checkMsgId(header)) {
			setError(ErrorCode.InvalidMsgId, response);
			return response;
		}
		if (!SignatureUtils.checkPayloadSignature(payload, header)) {
			setError(ErrorCode.InvalidSignatureMessagePayload, response);
			return response;
		}
		if (!SignatureUtils.checkHeaderSignature(header)) {
			setError(ErrorCode.InvalidSignatureMessageHeader, response);
			return response;
		}

		Chunk c = a2d.mapChunk(msg.getChunk());

		MessageDescriptor md = a2d.mapMessage(msg);

		AgentCredentialDescriptor srcUc = credentialFactory.createAgentCredential(header.getUsersignature()
				.getUserIdentity().getUsercertificate(), header.getUsersignature().getUserIdentity()
				.getDomaincertificate(), header.getUsersignature().getUserIdentity().getRootcertificate());
		if (srcUc == null || AgentCredentialType.UC != srcUc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}
		// check origin cert is same as msg channel origin cert.
		if (!srcUc.getFingerprint().equals(authorizedUser.getFingerprint())) {
			setError(ErrorCode.InvalidMessageSource, response);
			return response;
		}
		AgentCredentialDescriptor dstUc = credentialFactory.createAgentCredential(header.getTo().getUsercertificate(),
				header.getTo().getDomaincertificate(), header.getTo().getRootcertificate());
		if (dstUc == null || AgentCredentialType.UC != dstUc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}

		// create originating ChannelFlowMessage using the flowchannel's src and trg fingerprints to locate the
		// ChannelFlowOrigin to attach to.
		Zone zone = getAgentService().getZone();

		ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
		sc.setDomainName(authorizedUser.getTdmxDomainName());
		// TODO header channel origin matches the srcUser
		sc.getOrigin().setDomainName(header.getChannel().getOrigin().getDomain());
		sc.getOrigin().setLocalName(header.getChannel().getOrigin().getLocalname());
		// TODO header channel dest matches the to User
		sc.getDestination().setDomainName(header.getChannel().getDestination().getDomain());
		sc.getDestination().setLocalName(header.getChannel().getDestination().getLocalname());
		sc.getDestination().setServiceName(header.getChannel().getDestination().getServicename());

		List<Channel> flows = channelService.search(zone, sc);
		if (flows.isEmpty()) {
			setError(ErrorCode.ChannelNotFound, response);
			return response;
		}
		Channel flow = flows.get(0);
		// check the flow is not already throttled
		if (FlowControlStatus.OPEN != flow.getQuota().getSenderStatus()) {
			setError(ErrorCode.SubmitFlowControlClosed, response);
			return response;
		}

		SubmitMessageResultHolder result = channelService.submitMessage(zone, flow, md);
		if (result.status != null) {
			setError(mapSubmitOperationStatus(result.status), response);
			return response;
		}

		// persist Chunk
		chunkService.createOrUpdate(c);

		// give the caller the continuationId for the next chunk
		String continuationId = result.message.getContinuationId(c.getPos() + 1);
		if (continuationId == null) {
			// last chunk - what to do? TODO
		}
		response.setContinuation(continuationId);
		response.setSuccess(true);
		return response;
	}

	@Override
	public GetMessageDeliveryStatusResponse getMessageDeliveryStatus(GetMessageDeliveryStatus parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UploadResponse upload(Upload parameters) {
		UploadResponse response = new UploadResponse();
		PKIXCertificate authorizedUser = checkUserAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		String continuationId = parameters.getContinuation();
		if (!StringUtils.hasText(continuationId)) {
			setError(ErrorCode.MissingChunkContinuationId, response);
			return response;
		}

		// validate Chunk fields present
		if (validator.checkChunk(parameters.getChunk(), response) == null) {
			return response;
		}

		// TODO check chunk's mac

		Chunk c = a2d.mapChunk(parameters.getChunk());

		// create originating ChannelFlowMessage using the flowchannel's src and trg fingerprints to locate the
		// ChannelFlowOrigin to attach to.
		Zone zone = getAgentService().getZone();

		ChannelFlowMessage msg = channelService.findByMessageId(zone, c.getMsgId());
		if (msg == null) {
			setError(ErrorCode.MessageNotFound, response);
			return response;
		}
		// calculate the continuationId for the chunk and check that it matches the continuationId
		if (!continuationId.equals(msg.getContinuationId(c.getPos()))) {
			setError(ErrorCode.InvalidChunkContinuationId, response);
			return response;
		}

		chunkService.createOrUpdate(c);

		// calculate the next continuationId
		String nextContinuationId = msg.getContinuationId(c.getPos() + 1);
		if (nextContinuationId == null) {
			// this was the last chunk - what to do ? TODO
		}
		response.setContinuation(nextContinuationId);

		response.setSuccess(true);
		return response;
	}

	@Override
	public ReceiptResponse receipt(Receipt parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetChannelResponse getChannel(GetChannel parameters) {
		GetChannelResponse response = new GetChannelResponse();

		PKIXCertificate authorizedUser = checkUserAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();

		// TODO check we have all input parameters

		ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
		sc.setDomainName(authorizedUser.getTdmxDomainName());

		sc.getOrigin().setLocalName(authorizedUser.getTdmxUserName());
		sc.getOrigin().setDomainName(authorizedUser.getTdmxDomainName());
		sc.getDestination().setDomainName(parameters.getDestination().getDomain());
		sc.getDestination().setLocalName(parameters.getDestination().getLocalname());
		sc.getDestination().setServiceName(parameters.getDestination().getServicename());

		List<Channel> channels = channelService.search(zone, sc);
		for (Channel c : channels) {
			// only 1
			response.setChannelinfo(d2a.mapChannelInfo(c));
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public ListChannelResponse listChannel(ListChannel parameters) {
		ListChannelResponse response = new ListChannelResponse();
		PKIXCertificate authorizedUser = checkUserAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();

		ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(
				a2d.mapPage(parameters.getPage()));
		sc.setDomainName(authorizedUser.getTdmxDomainName());

		sc.getOrigin().setLocalName(authorizedUser.getTdmxUserName());
		sc.getOrigin().setDomainName(authorizedUser.getTdmxDomainName());
		if (parameters.getDestination() != null) {
			sc.getDestination().setDomainName(parameters.getDestination().getDomain());
			sc.getDestination().setLocalName(parameters.getDestination().getLocalname());
			sc.getDestination().setServiceName(parameters.getDestination().getServicename());
		}
		List<Channel> channels = channelService.search(zone, sc);
		for (Channel c : channels) {
			response.getChannelinfos().add(d2a.mapChannelInfo(c));
		}

		response.setSuccess(true);
		return response;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private PKIXCertificate checkUserAuthorized(Acknowledge ack) {
		PKIXCertificate user = getAgentService().getAuthenticatedAgent();
		if (user == null) {
			setError(ErrorCode.MissingCredentials, ack);
			return null;
		}
		if (!user.isTdmxUserCertificate()) {
			setError(ErrorCode.NonUserAccess, ack);
			return null;
		}
		return user;
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
			return ErrorCode.SubmitFlowControlClosed;
		default:
			return null;
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------
	public AuthenticatedAgentLookupService getAgentService() {
		return agentService;
	}

	public void setAgentService(AuthenticatedAgentLookupService agentService) {
		this.agentService = agentService;
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
