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
package org.tdmx.server.ws.mds;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Error;
import org.tdmx.core.api.v01.mds.Download;
import org.tdmx.core.api.v01.mds.DownloadResponse;
import org.tdmx.core.api.v01.mds.GetAddress;
import org.tdmx.core.api.v01.mds.GetAddressResponse;
import org.tdmx.core.api.v01.mds.GetFlowTarget;
import org.tdmx.core.api.v01.mds.GetFlowTargetResponse;
import org.tdmx.core.api.v01.mds.ListChannelAuthorization;
import org.tdmx.core.api.v01.mds.ListChannelAuthorizationResponse;
import org.tdmx.core.api.v01.mds.ListFlow;
import org.tdmx.core.api.v01.mds.ListFlowResponse;
import org.tdmx.core.api.v01.mds.Receive;
import org.tdmx.core.api.v01.mds.ReceiveResponse;
import org.tdmx.core.api.v01.mds.SetFlowTargetSession;
import org.tdmx.core.api.v01.mds.SetFlowTargetSessionResponse;
import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Flowtarget;
import org.tdmx.core.api.v01.msg.Flowtargetsession;
import org.tdmx.core.api.v01.msg.UserIdentity;
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
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelFlowOrigin;
import org.tdmx.lib.zone.domain.ChannelFlowSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelFlowTargetDescriptor;
import org.tdmx.lib.zone.domain.ChannelSearchCriteria;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.AgentCredentialValidator;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.FlowTargetService;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.ApiValidator;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedAgentLookupService;

public class MDSImpl implements MDS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MDSImpl.class);

	private AuthenticatedAgentLookupService agentService;

	private DomainService domainService;
	private AddressService addressService;
	private ServiceService serviceService;
	private ChannelService channelService;
	private FlowTargetService flowTargetService;

	private AgentCredentialFactory credentialFactory;
	private AgentCredentialService credentialService;
	private AgentCredentialValidator credentialValidator;

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
		ep.setServiceprovider("TODO"); // TODO from the zone

		response.setDestination(ep);
		response.setSuccess(true);
		return response;
	}

	@Override
	public ForgetResponse forget(Forget parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetFlowTargetResponse getFlowTarget(GetFlowTarget parameters) {
		GetFlowTargetResponse response = new GetFlowTargetResponse();
		PKIXCertificate authorizedUser = checkUserAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		if (!StringUtils.hasText(parameters.getServicename())) {
			setError(ErrorCode.MissingService, response);
			return response;
		}

		// check that the UC credential exists
		AgentCredential existingCred = credentialService.findByFingerprint(authorizedUser.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}

		// lookup existing service exists
		org.tdmx.lib.zone.domain.Service existingService = getServiceService().findByName(existingCred.getDomain(),
				parameters.getServicename());
		if (existingService == null) {
			setError(ErrorCode.ServiceNotFound, response);
			return response;
		}

		// TODO find by fingerprint and serviceName
		FlowTarget existingFlowTarget = flowTargetService.findByTargetService(existingCred, existingService);
		if (existingFlowTarget != null) {
			Flowtarget ft = d2a.mapFlowTarget(existingFlowTarget);
			response.setFlowtarget(ft);
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public ListFlowResponse listFlow(ListFlow parameters) {
		ListFlowResponse response = new ListFlowResponse();
		PKIXCertificate authorizedUser = checkUserAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}
		if (!StringUtils.hasText(parameters.getServicename())) {
			setError(ErrorCode.MissingService, response);
			return response;
		}
		Zone zone = getAgentService().getZone();

		ChannelFlowSearchCriteria sc = new ChannelFlowSearchCriteria(a2d.mapPage(parameters.getPage()));
		sc.setDomainName(authorizedUser.getTdmxDomainName());
		sc.setTargetFingerprint(authorizedUser.getFingerprint());
		sc.getDestination().setDomainName(authorizedUser.getTdmxDomainName());
		sc.getDestination().setServiceName(parameters.getServicename());
		if (parameters.getOrigin() != null) {
			sc.getOrigin().setDomainName(parameters.getOrigin().getDomain());
			sc.getOrigin().setLocalName(parameters.getOrigin().getLocalname());
			sc.getOrigin().setServiceProvider(parameters.getOrigin().getServiceprovider());
			if (parameters.getOrigin().getUserIdentity() != null) {
				AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(parameters.getOrigin()
						.getUserIdentity().getUsercertificate(), parameters.getOrigin().getUserIdentity()
						.getDomaincertificate(), parameters.getOrigin().getUserIdentity().getRootcertificate());
				if (uc == null || AgentCredentialType.UC != uc.getCredentialType()) {
					setError(ErrorCode.InvalidUserCredentials, response);
					return response;
				}
				sc.setSourceFingerprint(uc.getFingerprint());
			}
		}
		List<ChannelFlowOrigin> channelFlows = channelService.search(zone, sc);
		for (ChannelFlowOrigin flow : channelFlows) {
			response.getFlows().add(d2a.mapFlow(flow));
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public PrepareResponse prepare(Prepare parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetFlowTargetSessionResponse setFlowTargetSession(SetFlowTargetSession parameters) {
		SetFlowTargetSessionResponse response = new SetFlowTargetSessionResponse();
		PKIXCertificate authorizedUser = checkUserAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		if (!StringUtils.hasText(parameters.getServicename())) {
			setError(ErrorCode.MissingService, response);
			return response;
		}

		// check that the UC credential exists
		AgentCredential existingCred = credentialService.findByFingerprint(authorizedUser.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}

		// lookup existing service exists
		org.tdmx.lib.zone.domain.Service existingService = getServiceService().findByName(existingCred.getDomain(),
				parameters.getServicename());
		if (existingService == null) {
			setError(ErrorCode.ServiceNotFound, response);
			return response;
		}

		// validate all FlowTargetSession fields are specified.
		Flowtargetsession fts = validator.checkFlowtargetsession(parameters.getFlowtargetsession(), response);
		if (fts == null) {
			return response;
		}

		UserIdentity id = d2a.mapUserIdentity(existingCred.getCertificateChain());
		// check that the FTS signature is ok for the targetagent.
		if (!SignatureUtils.checkFlowTargetSessionSignature(parameters.getServicename(), id,
				parameters.getFlowtargetsession())) {
			setError(ErrorCode.InvalidSignatureFlowTargetSession, response);
			return response;
		}

		FlowTarget ft = a2d.mapFlowTarget(existingCred, existingService, parameters.getFlowtargetsession());
		flowTargetService.setSession(ft);

		Zone zone = getAgentService().getZone();

		boolean more = true;
		// fetch ALL Channels which have this FlowTarget as Destination.
		for (int pageNo = 0; more; pageNo++) {
			ChannelSearchCriteria sc = new ChannelSearchCriteria(new PageSpecifier(pageNo, getBatchSize()));
			sc.setDomain(existingCred.getDomain());
			sc.getDestination().setLocalName(existingCred.getAddress().getLocalName());
			sc.getDestination().setDomainName(existingCred.getDomain().getDomainName());
			sc.getDestination().setServiceName(existingService.getServiceName());
			// sc.getDestination().setServiceProvider(authorizedUser.getTdmxZoneInfo().getMrsUrl()); TODO

			List<Channel> channels = channelService.search(zone, sc);
			for (Channel channel : channels) {
				ChannelFlowTargetDescriptor cftd = ft.getDescriptor(zone, channel.getOrigin());
				channelService.setChannelFlowTarget(zone, channel.getId(), cftd);
			}
			if (channels.isEmpty()) {
				more = false;
			}
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public DownloadResponse download(Download parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReceiveResponse receive(Receive parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RollbackResponse rollback(Rollback parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListChannelAuthorizationResponse listChannelAuthorization(ListChannelAuthorization parameters) {
		ListChannelAuthorizationResponse response = new ListChannelAuthorizationResponse();
		PKIXCertificate authorizedUser = checkUserAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		if (!StringUtils.hasText(parameters.getServicename())) {
			setError(ErrorCode.MissingService, response);
			return response;
		}

		Zone zone = getAgentService().getZone();

		ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(
				a2d.mapPage(parameters.getPage()));
		sc.setDomainName(authorizedUser.getTdmxDomainName());
		sc.getDestination().setLocalName(authorizedUser.getTdmxUserName());
		sc.getDestination().setDomainName(authorizedUser.getTdmxDomainName());
		// FIXME SP: sc.getDestination().setServiceProvider(authorizedUser.getTdmxZoneInfo().getMrsUrl()); // TODO SP
		sc.getDestination().setServiceName(parameters.getServicename());
		if (parameters.getOrigin() != null) {
			sc.getOrigin().setDomainName(parameters.getOrigin().getDomain());
			sc.getOrigin().setLocalName(parameters.getOrigin().getLocalname());
			// FIXME SP: sc.getOrigin().setServiceProvider(parameters.getOrigin().getServiceprovider());
		}
		List<ChannelAuthorization> channelAuths = channelService.search(zone, sc);
		for (ChannelAuthorization channelAuth : channelAuths) {
			response.getChannelauthorizations().add(d2a.mapChannelAuthorization(channelAuth));
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public CommitResponse commit(Commit parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecoverResponse recover(Recover parameters) {
		// TODO Auto-generated method stub
		return null;
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

	public FlowTargetService getFlowTargetService() {
		return flowTargetService;
	}

	public void setFlowTargetService(FlowTargetService flowTargetService) {
		this.flowTargetService = flowTargetService;
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

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

}
