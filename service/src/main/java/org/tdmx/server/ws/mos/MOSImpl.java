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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Error;
import org.tdmx.core.api.v01.mos.GetAddress;
import org.tdmx.core.api.v01.mos.GetAddressResponse;
import org.tdmx.core.api.v01.mos.GetMessageDeliveryStatus;
import org.tdmx.core.api.v01.mos.GetMessageDeliveryStatusResponse;
import org.tdmx.core.api.v01.mos.ListChannelAuthorization;
import org.tdmx.core.api.v01.mos.ListChannelAuthorizationResponse;
import org.tdmx.core.api.v01.mos.ListFlow;
import org.tdmx.core.api.v01.mos.ListFlowResponse;
import org.tdmx.core.api.v01.mos.Submit;
import org.tdmx.core.api.v01.mos.SubmitResponse;
import org.tdmx.core.api.v01.mos.Upload;
import org.tdmx.core.api.v01.mos.UploadResponse;
import org.tdmx.core.api.v01.mos.ws.MOS;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
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
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.AgentCredentialValidator;
import org.tdmx.lib.zone.service.ChannelAuthorizationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.FlowTargetService;
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
	private ChannelAuthorizationService channelAuthorizationService;
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
		ep.setServiceprovider("TODO"); // TODO from the zone
		response.setOrigin(ep);

		response.setSuccess(true);
		return response;
	}

	@Override
	public SubmitResponse submit(Submit parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetMessageDeliveryStatusResponse getMessageDeliveryStatus(GetMessageDeliveryStatus parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListFlowResponse listFlow(ListFlow parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UploadResponse upload(Upload parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListChannelAuthorizationResponse listChannelAuthorization(ListChannelAuthorization parameters) {
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

	public ChannelAuthorizationService getChannelAuthorizationService() {
		return channelAuthorizationService;
	}

	public void setChannelAuthorizationService(ChannelAuthorizationService channelAuthorizationService) {
		this.channelAuthorizationService = channelAuthorizationService;
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
