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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Error;
import org.tdmx.core.api.v01.mrs.Relay;
import org.tdmx.core.api.v01.mrs.RelayResponse;
import org.tdmx.core.api.v01.mrs.ws.MRS;
import org.tdmx.core.api.v01.msg.Authorization;
import org.tdmx.core.api.v01.msg.Channelflowtarget;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.EndpointPermission;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.AgentCredentialValidator;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.FlowTargetService;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.ApiValidator;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;

public class MRSImpl implements MRS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// TODO #69 public trusted client authentication of SP. cn==SP mrs url, since we dont want to do PKIX validation for
	// each request, but only for new connections, we can cache authorized client certificates, so using HTTS with
	// keep-alive would not incur a significant performance penality w.r.t SSL level certificate trust checking
	// or we have a different endpoint for relay sessions which authorize the client's certificates directly, cache them
	// for the mrs endpoint and checks the PKIX validation to know/trusted roots.

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MRSImpl.class);

	private AccountZoneService accountZoneService;
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;

	private ZoneService zoneService;

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
	@WebResult(name = "relayResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mrs", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mrs-definition/relay")
	public RelayResponse relay(
			@WebParam(partName = "parameters", name = "relay", targetNamespace = "urn:tdmx:api:v1.0:sp:mrs") Relay parameters) {
		RelayResponse response = new RelayResponse();
		if (parameters.getAuthorization() != null) {
			processChannelAuthorization(parameters.getAuthorization(), response);
		} else if (parameters.getChannelflowtarget() != null) {
			processChannelFlowTarget(parameters.getChannelflowtarget(), response);
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
	private void processChannelAuthorization(Authorization auth, RelayResponse response) {
		// check that the Channel and EndpointPermission represented by the Auth is complete
		if (validator.checkAuthorization(auth, response) == null) {
			return;
		}
		if (!SignatureUtils.checkEndpointPermissionSignature(auth.getChannel(), auth, true)) {
			setError(ErrorCode.InvalidSignatureEndpointPermission, response);
			return;
		}
		ChannelOrigin origin = a2d.mapChannelOrigin(auth.getChannel().getOrigin());
		ChannelDestination dest = a2d.mapChannelDestination(auth.getChannel().getDestination());
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
		String remoteDomain = dac.getDomainName();
		String localDomain = null;
		if (auth.getChannel().getDestination().getDomain().equals(remoteDomain)) {
			// if the signer's domain matches the channel destination's domain then this is a reqRecvAuthorization at
			// the
			// origin, where we need to lookup the zone and domain of the origin, given the domain of the origin.
			localDomain = auth.getChannel().getOrigin().getDomain();
		} else {
			// if the signer's domain matches the channel origin's domain then the CA is a reqSendAuthorization at the
			// destination, where we need to lookup the zone of the destination, but we only have the domain of the
			// destination.
			localDomain = auth.getChannel().getDestination().getDomain();
		}

		// TODO the ROOT CA of the signing party needs to be trusted in DNS! We don't want to allow someone just to fake
		// channel authorizations and inject them via the relay without also spoofing DNS too!

		// TODO lookup the origin or destination domain in DNS and retrieve their zone root information
		AccountZone accountZone = null;
		if (accountZone == null) {
			// FIXME! temporary fallback on recursive lookup of domain to domain parent until matches with a zone.
			accountZone = lookupAccountZoneByDomain(localDomain);
		}

		if (accountZone == null) {
			setError(ErrorCode.ZoneNotFound, response);
			return;
		}

		// using the ZoneDB specified in the AccountZone's partitionID, find the Zone and then Domain
		// and then call ChannelService#relayChannelAuthorization
		Zone zone = null;
		try {
			getZonePartitionIdProvider().setPartitionId(accountZone.getZonePartitionId());

			zone = getZoneService().findByZoneApex(accountZone.getZoneApex());
			if (zone == null) {
				setError(ErrorCode.ZoneNotFound, response);
				return;
			}
			Domain domain = domainService.findByName(zone, localDomain);
			if (domain == null) {
				setError(ErrorCode.DomainNotFound, response);
				return;
			}
			channelService.relayAuthorization(zone, domain, origin, dest, otherPerm);
		} finally {
			getZonePartitionIdProvider().clearPartitionId();
		}

		response.setSuccess(true);
	}

	// handle the channel flowtarget relayed inbound.
	private void processChannelFlowTarget(Channelflowtarget cft, RelayResponse response) {
		// check that the Channel and EndpointPermission represented by the Auth is complete
		if (validator.checkChannelflowtarget(cft, response) == null) {
			return;
		}
		if (!SignatureUtils.checkFlowTargetSessionSignature(cft.getServicename(), cft.getTarget(),
				cft.getFlowtargetsession())) {
			setError(ErrorCode.InvalidSignatureFlowTargetSession, response);
			return;
		}
		// TODO tbc..

		// TODO define the channelService api to accept inbound CFT
	}

	private void setError(ErrorCode ec, Acknowledge ack) {
		Error error = new Error();
		error.setCode(ec.getErrorCode());
		error.setDescription(ec.getErrorDescription());
		ack.setError(error);
		ack.setSuccess(false);
	}

	private AccountZone lookupAccountZoneByDomain(String searchDomain) {
		AccountZone accountZone = null;
		while (accountZone == null && searchDomain.indexOf(".") != -1) {
			accountZone = accountZoneService.findByZoneApex(searchDomain);
			if (accountZone == null) {
				searchDomain = searchDomain.substring(searchDomain.indexOf(".") + 1); // strip off domain prefix to try
																						// to
				// converge on the zone root.
			}
		}
		return accountZone;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AccountZoneService getAccountZoneService() {
		return accountZoneService;
	}

	public void setAccountZoneService(AccountZoneService accountZoneService) {
		this.accountZoneService = accountZoneService;
	}

	public ThreadLocalPartitionIdProvider getZonePartitionIdProvider() {
		return zonePartitionIdProvider;
	}

	public void setZonePartitionIdProvider(ThreadLocalPartitionIdProvider zonePartitionIdProvider) {
		this.zonePartitionIdProvider = zonePartitionIdProvider;
	}

	public ZoneService getZoneService() {
		return zoneService;
	}

	public void setZoneService(ZoneService zoneService) {
		this.zoneService = zoneService;
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
