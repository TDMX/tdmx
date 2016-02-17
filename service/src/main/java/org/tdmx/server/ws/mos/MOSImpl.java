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
import org.tdmx.core.api.v01.mos.Acknowledge;
import org.tdmx.core.api.v01.mos.AcknowledgeResponse;
import org.tdmx.core.api.v01.mos.GetAddress;
import org.tdmx.core.api.v01.mos.GetAddressResponse;
import org.tdmx.core.api.v01.mos.GetChannel;
import org.tdmx.core.api.v01.mos.GetChannelResponse;
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
import org.tdmx.core.api.v01.tx.TransactionSpecification;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.message.domain.Chunk;
import org.tdmx.lib.message.service.ChunkService;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelName;
import org.tdmx.lib.zone.domain.Domain;
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
import org.tdmx.server.ros.client.RelayStatus;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.ApiValidator;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.mos.MOSServerSession.ChannelContextHolder;
import org.tdmx.server.ws.mos.MOSServerSession.MessageContextHolder;
import org.tdmx.server.ws.security.service.AuthenticatedClientLookupService;
import org.tdmx.server.ws.security.service.AuthorizedSessionLookupService;

public class MOSImpl implements MOS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MOSImpl.class);

	private AuthorizedSessionLookupService<MOSServerSession> authorizedSessionService;
	private AuthenticatedClientLookupService authenticatedClientService;

	private RelayClientService relayClientService;

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

	// TODO LATER: idea - keep an atomic integer of "msg"count being sent and make this a part of the factor for MOS
	// load

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public PrepareResponse prepare(Prepare parameters) {
		// TODO Auto-generated method stub
		return null;
	}

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
	public RecoverResponse recover(Recover parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetAddressResponse getAddress(GetAddress parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();
		Address oa = session.getOriginatingAddress();
		Domain domain = session.getDomain();

		GetAddressResponse response = new GetAddressResponse();

		ChannelEndpoint ep = new ChannelEndpoint();
		ep.setDomain(domain.getDomainName());
		ep.setLocalname(oa.getLocalName());
		response.setOrigin(ep);

		response.setSuccess(true);
		return response;
	}

	@Override
	public SubmitResponse submit(Submit parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();
		PKIXCertificate authorizedUser = authenticatedClientService.getAuthenticatedClient();

		SubmitResponse response = new SubmitResponse();

		// validate Msg fields present ( payload, header and chunk )
		if (validator.checkMessage(parameters.getMsg(), response) == null) {
			return response;
		}
		// TODO #100: validate Tx fields present if provided
		TransactionSpecification tx = parameters.getTransaction();

		// TODO #70: check chunk's mac

		Msg msg = parameters.getMsg();
		Header header = msg.getHeader();
		Payload payload = msg.getPayload();
		if (!SignatureUtils.checkMsgId(header, header.getUsersignature().getSignaturevalue().getTimestamp())) {
			ErrorCode.setError(ErrorCode.InvalidMsgId, response);
			return response;
		}
		if (!SignatureUtils.checkPayloadSignature(payload, header)) {
			ErrorCode.setError(ErrorCode.InvalidSignatureMessagePayload, response);
			return response;
		}
		if (!SignatureUtils.checkHeaderSignature(header)) {
			ErrorCode.setError(ErrorCode.InvalidSignatureMessageHeader, response);
			return response;
		}

		Chunk c = a2d.mapChunk(msg.getChunk());

		ChannelMessage m = a2d.mapMessage(msg);

		AgentCredentialDescriptor srcUc = credentialFactory.createAgentCredential(
				header.getUsersignature().getUserIdentity().getUsercertificate(),
				header.getUsersignature().getUserIdentity().getDomaincertificate(),
				header.getUsersignature().getUserIdentity().getRootcertificate());
		if (srcUc == null || AgentCredentialType.UC != srcUc.getCredentialType()) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}
		// check origin cert is same as msg channel origin cert.
		if (!srcUc.getFingerprint().equals(authorizedUser.getFingerprint())) {
			ErrorCode.setError(ErrorCode.InvalidMessageSource, response);
			return response;
		}
		m.setOriginSerialNr(1); // FIXME

		AgentCredentialDescriptor dstUc = credentialFactory.createAgentCredential(header.getTo().getUsercertificate(),
				header.getTo().getDomaincertificate(), header.getTo().getRootcertificate());
		if (dstUc == null || AgentCredentialType.UC != dstUc.getCredentialType()) {
			ErrorCode.setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}
		m.setDestinationSerialNr(1); // FIXME

		// create originating ChannelMessage
		Zone zone = session.getZone();

		// cache the last used Channel in the session to avoid this search if always sending to the same dest.
		ChannelName cn = a2d.mapChannelName(header.getChannel());
		if (!srcUc.getDomainName().equals(cn.getOrigin().getDomainName())
				|| !srcUc.getAddressName().equals(cn.getOrigin().getLocalName())) {
			// check sender cert matches origin of channel sending on.
			ErrorCode.setError(ErrorCode.InvalidChannelOrigin, response);
			return response;
		}
		if (!dstUc.getDomainName().equals(cn.getDestination().getDomainName())
				|| !dstUc.getAddressName().equals(cn.getDestination().getLocalName())) {
			// header channel dest matches the "to" User
			ErrorCode.setError(ErrorCode.InvalidChannelDestination, response);
			return response;
		}

		final String channelKey = cn.getChannelKey(cn.getOrigin().getDomainName());
		ChannelContextHolder cch = session.getChannel(channelKey);
		if (cch == null) {
			ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
			sc.setDomainName(authorizedUser.getTdmxDomainName());
			sc.getOrigin().setDomainName(header.getChannel().getOrigin().getDomain());
			sc.getOrigin().setLocalName(header.getChannel().getOrigin().getLocalname());
			sc.getDestination().setDomainName(header.getChannel().getDestination().getDomain());
			sc.getDestination().setLocalName(header.getChannel().getDestination().getLocalname());
			sc.getDestination().setServiceName(header.getChannel().getDestination().getServicename());

			List<Channel> channels = channelService.search(zone, sc);
			if (channels.isEmpty()) {
				ErrorCode.setError(ErrorCode.ChannelNotFound, response);
				return response;
			}
			Channel channel = channels.get(0);
			cch = session.addChannel(channelKey, channel);
		}

		m.setChannel(cch.getChannel());
		SubmitMessageResultHolder result = channelService.preSubmitMessage(zone, m);
		if (result.status != null) {
			ErrorCode.setError(mapSubmitOperationStatus(result.status), response);
			return response;
		}

		// persist Chunk
		chunkService.createOrUpdate(c);

		// add the sent message to the session.
		MessageContextHolder mch = session.addMessage(m);

		// give the caller the continuationId for the next chunk
		String continuationId = mch.getContinuationId(c.getPos() + 1);
		if (continuationId == null) {
			// last chunk - what to do? TODO #70: last chunk y/n? transaction y/n?
			try {
				m.setProcessingState(ProcessingState.pending());
				channelService.create(m);
			} finally {
				session.removeMessage(m);
			}

			// give the message to the ROS to relay.
			relayWithRetry(session, cch, m);
		}
		response.setContinuation(continuationId);
		response.setSuccess(true);
		return response;
	}

	@Override
	public UploadResponse upload(Upload parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		UploadResponse response = new UploadResponse();

		String continuationId = parameters.getContinuation();
		if (!StringUtils.hasText(continuationId)) {
			ErrorCode.setError(ErrorCode.MissingChunkContinuationId, response);
			return response;
		}

		// validate Chunk fields present
		if (validator.checkChunk(parameters.getChunk(), response) == null) {
			return response;
		}

		// TODO #70: check chunk's mac

		Chunk c = a2d.mapChunk(parameters.getChunk());

		// create originating ChannelMessage using the flowchannel's src and trg fingerprints to locate the
		// ChannelFlowOrigin to attach to.
		Zone zone = session.getZone();

		MessageContextHolder mch = session.getMessage(parameters.getChunk().getMsgId());
		if (mch == null) {
			ErrorCode.setError(ErrorCode.MessageNotFound, response);
			return response;
		}
		// calculate the continuationId for the chunk and check that it matches the continuationId
		if (!continuationId.equals(mch.getContinuationId(c.getPos()))) {
			ErrorCode.setError(ErrorCode.InvalidChunkContinuationId, response);
			return response;
		}

		chunkService.createOrUpdate(c);

		// calculate the next continuationId
		String nextContinuationId = mch.getContinuationId(c.getPos() + 1);
		if (nextContinuationId == null) {
			// this was the last chunk - what to do ? TODO #70: transaction y/n?
			// when to delete the msg from the

		}
		response.setContinuation(nextContinuationId);

		response.setSuccess(true);
		return response;
	}

	@Override
	public GetChannelResponse getChannel(GetChannel parameters) {
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		GetChannelResponse response = new GetChannelResponse();

		if (validator.checkChannelDestination(parameters.getDestination(), response) == null) {
			return response;
		}

		Zone zone = session.getZone();
		Address address = session.getOriginatingAddress();
		Domain domain = session.getDomain();

		ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
		sc.setDomain(domain);

		sc.getOrigin().setLocalName(address.getLocalName());
		sc.getOrigin().setDomainName(domain.getDomainName());
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
		MOSServerSession session = authorizedSessionService.getAuthorizedSession();

		ListChannelResponse response = new ListChannelResponse();

		Zone zone = session.getZone();
		Domain domain = session.getDomain();
		Address address = session.getOriginatingAddress();

		ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(
				a2d.mapPage(parameters.getPage()));
		sc.setDomain(domain);

		sc.getOrigin().setLocalName(address.getLocalName());
		sc.getOrigin().setDomainName(domain.getDomainName());
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

	@Override
	public ReceiptResponse receipt(Receipt parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcknowledgeResponse acknowledge(Acknowledge parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private ErrorCode mapSubmitOperationStatus(SubmitMessageOperationStatus status) {
		switch (status) {
		case FLOW_CONTROL_CLOSED:
			return ErrorCode.SubmitFlowControlClosed;
		case CHANNEL_CLOSED:
			return ErrorCode.SubmitChannelClosed;
		default:
			return null;
		}
	}

	private void relayWithRetry(MOSServerSession session, ChannelContextHolder cch, ChannelMessage msg) {
		final RelayStatus rs = relayClientService.relayChannelMessage(cch.getChannelKey(), session.getAccountZone(),
				session.getZone(), cch.getChannel().getDomain(), cch.getChannel(), msg);
		if (!rs.isSuccess()) {
			if (rs.getErrorCode().isRetryable()) {
				RelayStatus retry = relayClientService.relayChannelMessage(null /* get new ROS session */,
						session.getAccountZone(), session.getZone(), cch.getChannel().getDomain(), cch.getChannel(),
						msg);
				if (!retry.isSuccess()) {
					ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_RETRY,
							rs.getErrorCode().getErrorMessage());
					// TODO #93: channelService.updateStatusMessage(result.message.getId(), error);
				} else {
					// cache the potentially changed ROS address
					cch.setRosTcpAddress(retry.getRosTcpAddress());
				}
			} else {
				ProcessingState error = ProcessingState.error(ProcessingState.FAILURE_RELAY_RETRY,
						rs.getErrorCode().getErrorMessage());
				// TODO #93: channelService.updateStatusMessage(result.message.getId(), error);

			}

		} else {
			// cache the working ROS address
			cch.setRosTcpAddress(rs.getRosTcpAddress());
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AuthorizedSessionLookupService<MOSServerSession> getAuthorizedSessionService() {
		return authorizedSessionService;
	}

	public void setAuthorizedSessionService(AuthorizedSessionLookupService<MOSServerSession> authorizedSessionService) {
		this.authorizedSessionService = authorizedSessionService;
	}

	public AuthenticatedClientLookupService getAuthenticatedClientService() {
		return authenticatedClientService;
	}

	public void setAuthenticatedClientService(AuthenticatedClientLookupService authenticatedClientService) {
		this.authenticatedClientService = authenticatedClientService;
	}

	public RelayClientService getRelayClientService() {
		return relayClientService;
	}

	public void setRelayClientService(RelayClientService relayClientService) {
		this.relayClientService = relayClientService;
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
