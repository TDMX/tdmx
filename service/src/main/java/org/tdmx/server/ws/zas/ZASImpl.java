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
package org.tdmx.server.ws.zas;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Error;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.report.Incident;
import org.tdmx.core.api.v01.report.IncidentResponse;
import org.tdmx.core.api.v01.report.Report;
import org.tdmx.core.api.v01.report.ReportResponse;
import org.tdmx.core.api.v01.zas.CreateAddress;
import org.tdmx.core.api.v01.zas.CreateAddressResponse;
import org.tdmx.core.api.v01.zas.CreateAdministrator;
import org.tdmx.core.api.v01.zas.CreateAdministratorResponse;
import org.tdmx.core.api.v01.zas.CreateDomain;
import org.tdmx.core.api.v01.zas.CreateDomainResponse;
import org.tdmx.core.api.v01.zas.CreateIpZone;
import org.tdmx.core.api.v01.zas.CreateIpZoneResponse;
import org.tdmx.core.api.v01.zas.CreateService;
import org.tdmx.core.api.v01.zas.CreateServiceResponse;
import org.tdmx.core.api.v01.zas.CreateUser;
import org.tdmx.core.api.v01.zas.CreateUserResponse;
import org.tdmx.core.api.v01.zas.DeleteAddress;
import org.tdmx.core.api.v01.zas.DeleteAddressResponse;
import org.tdmx.core.api.v01.zas.DeleteAdministrator;
import org.tdmx.core.api.v01.zas.DeleteAdministratorResponse;
import org.tdmx.core.api.v01.zas.DeleteChannelAuthorization;
import org.tdmx.core.api.v01.zas.DeleteChannelAuthorizationResponse;
import org.tdmx.core.api.v01.zas.DeleteDomain;
import org.tdmx.core.api.v01.zas.DeleteDomainResponse;
import org.tdmx.core.api.v01.zas.DeleteIpZone;
import org.tdmx.core.api.v01.zas.DeleteIpZoneResponse;
import org.tdmx.core.api.v01.zas.DeleteService;
import org.tdmx.core.api.v01.zas.DeleteServiceResponse;
import org.tdmx.core.api.v01.zas.DeleteUser;
import org.tdmx.core.api.v01.zas.DeleteUserResponse;
import org.tdmx.core.api.v01.zas.ModifyAdministrator;
import org.tdmx.core.api.v01.zas.ModifyAdministratorResponse;
import org.tdmx.core.api.v01.zas.ModifyFlowTargetResponse;
import org.tdmx.core.api.v01.zas.ModifyIpZone;
import org.tdmx.core.api.v01.zas.ModifyIpZoneResponse;
import org.tdmx.core.api.v01.zas.ModifyService;
import org.tdmx.core.api.v01.zas.ModifyServiceResponse;
import org.tdmx.core.api.v01.zas.ModifyUser;
import org.tdmx.core.api.v01.zas.ModifyUserResponse;
import org.tdmx.core.api.v01.zas.SearchAddress;
import org.tdmx.core.api.v01.zas.SearchAddressResponse;
import org.tdmx.core.api.v01.zas.SearchAdministrator;
import org.tdmx.core.api.v01.zas.SearchAdministratorResponse;
import org.tdmx.core.api.v01.zas.SearchChannelAuthorization;
import org.tdmx.core.api.v01.zas.SearchChannelAuthorizationResponse;
import org.tdmx.core.api.v01.zas.SearchDomain;
import org.tdmx.core.api.v01.zas.SearchDomainResponse;
import org.tdmx.core.api.v01.zas.SearchFlowTargetResponse;
import org.tdmx.core.api.v01.zas.SearchIpZone;
import org.tdmx.core.api.v01.zas.SearchIpZoneResponse;
import org.tdmx.core.api.v01.zas.SearchService;
import org.tdmx.core.api.v01.zas.SearchServiceResponse;
import org.tdmx.core.api.v01.zas.SearchUser;
import org.tdmx.core.api.v01.zas.SearchUserResponse;
import org.tdmx.core.api.v01.zas.SetChannelAuthorization;
import org.tdmx.core.api.v01.zas.SetChannelAuthorizationResponse;
import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ProcessingStatus;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.FlowTargetSearchCriteria;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.AgentCredentialValidator;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.ChannelService.SetAuthorizationOperationStatus;
import org.tdmx.lib.zone.service.ChannelService.SetAuthorizationResultHolder;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.FlowTargetService;
import org.tdmx.lib.zone.service.FlowTargetService.ModifyOperationStatus;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.ApiValidator;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedAgentLookupService;

public class ZASImpl implements ZAS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ZASImpl.class);

	private AuthenticatedAgentLookupService agentService;
	private DomainService domainService;
	private AddressService addressService;
	private ServiceService serviceService;
	private ChannelService channelService;
	private FlowTargetService flowTargetService;

	private AgentCredentialFactory credentialFactory;
	private AgentCredentialService credentialService;
	private AgentCredentialValidator credentialValidator;

	// TODO ChannelAuth effect on ZAS
	// flowtarget independent of channel auths. Destination side just defines the state unilateraly.
	// design jobs / process flow around channel auths
	// trigger transfer of flowtargetsession from dest to origin SP based on channel authorization
	// flowtargetsession arrives at origin SP where it applies it creates new flows if needed and assigns the flowtarget
	// originator uc can then send message
	// arrival of message creates flows on destination side as needed
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
	@WebResult(name = "createDomainResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createDomain")
	public CreateDomainResponse createDomain(
			@WebParam(partName = "parameters", name = "createDomain", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateDomain parameters) {
		CreateDomainResponse response = new CreateDomainResponse();

		PKIXCertificate authorizedUser = checkZACAuthorized(getAgentService().getAuthenticatedAgent(), response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}

		String domainName = checkZACDomainAuthorization(parameters.getDomain(), zone, response);
		if (domainName == null) {
			return response;
		}
		// check if the domain exists already
		Domain existingDomain = getDomainService().findByName(zone, domainName);
		if (existingDomain != null) {
			setError(ErrorCode.DomainExists, response);
			return response;
		}

		// create the domain
		Domain domain = new Domain(zone, domainName);

		getDomainService().createOrUpdate(domain);
		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "deleteDomainResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteDomain")
	public DeleteDomainResponse deleteDomain(
			@WebParam(partName = "parameters", name = "deleteDomain", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteDomain parameters) {
		DeleteDomainResponse response = new DeleteDomainResponse();

		PKIXCertificate authorizedUser = checkZACAuthorized(getAgentService().getAuthenticatedAgent(), response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}

		String domainName = checkZACDomainAuthorization(parameters.getDomain(), zone, response);
		if (domainName == null) {
			return response;
		}
		// check if the domain exists already
		Domain domain = getDomainService().findByName(zone, domainName);
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check the domain can be deleted if it has no credentials
		AgentCredentialSearchCriteria dcSc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 1));
		dcSc.setDomainName(parameters.getDomain());
		dcSc.setType(AgentCredentialType.DAC);
		List<AgentCredential> credentials = getCredentialService().search(zone, dcSc);
		if (!credentials.isEmpty()) {
			setError(ErrorCode.DomainAdministratorCredentialsExist, response);
			return response;
		}

		// and no addresses
		AddressSearchCriteria sac = new AddressSearchCriteria(new PageSpecifier(0, 1));
		sac.setDomainName(domain.getDomainName());
		List<org.tdmx.lib.zone.domain.Address> addresses = getAddressService().search(zone, sac);
		if (!addresses.isEmpty()) {
			setError(ErrorCode.AddressesExist, response);
			return response;
		}

		// and no services
		ServiceSearchCriteria ssc = new ServiceSearchCriteria(new PageSpecifier(0, 1));
		ssc.setDomainName(domain.getDomainName());
		List<org.tdmx.lib.zone.domain.Service> services = getServiceService().search(zone, ssc);
		if (!services.isEmpty()) {
			setError(ErrorCode.ServicesExist, response);
			return response;
		}

		getDomainService().delete(domain);
		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "searchDomainResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchDomain")
	public SearchDomainResponse searchDomain(
			@WebParam(partName = "parameters", name = "searchDomain", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchDomain parameters) {

		SearchDomainResponse response = new SearchDomainResponse();
		PKIXCertificate authorizedUser = checkZACAuthorized(getAgentService().getAuthenticatedAgent(), response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}
		DomainSearchCriteria criteria = new DomainSearchCriteria(a2d.mapPage(parameters.getPage()));
		// make sure client stipulates a domain which is within the zone.
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			if (!StringUtils.isSuffix(parameters.getFilter().getDomain(), zone.getZoneApex())) {
				setError(ErrorCode.OutOfZoneAccess, response);
				return response;
			}
			criteria.setDomainName(parameters.getFilter().getDomain());
		}
		List<Domain> domains = domainService.search(zone, criteria);
		for (Domain d : domains) {
			response.getDomains().add(d.getDomainName());
		}
		response.setPage(parameters.getPage());
		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "searchUserResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchUser")
	public SearchUserResponse searchUser(
			@WebParam(partName = "parameters", name = "searchUser", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchUser parameters) {

		SearchUserResponse response = new SearchUserResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}

		if (parameters.getFilter().getUserIdentity() != null) {
			// if a user credential is provided then it't not so much a search as a lookup

			AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(parameters.getFilter()
					.getUserIdentity().getUsercertificate(), parameters.getFilter().getUserIdentity()
					.getDomaincertificate(), parameters.getFilter().getUserIdentity().getRootcertificate());
			if (uc == null || AgentCredentialType.UC != uc.getCredentialType()) {
				setError(ErrorCode.InvalidUserCredentials, response);
				return response;
			}
			// we check that the provided domain is the DAC's domain.
			if (checkDomainAuthorization(authorizedUser, uc.getDomainName(), response) == null) {
				return response;
			}
			AgentCredential c = credentialService.findByFingerprint(uc.getFingerprint());
			if (c != null) {
				response.getUsers().add(d2a.mapUser(c));
			}
		} else {
			AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(a2d.mapPage(parameters.getPage()));
			if (authorizedUser.isTdmxDomainAdminCertificate()
					&& !StringUtils.hasText(parameters.getFilter().getDomain())) {
				// we fix the search to search only the DAC's domain.
				parameters.getFilter().setDomain(authorizedUser.getCommonName());
			}
			if (StringUtils.hasText(parameters.getFilter().getDomain())) {
				// we check that the provided domain is the DAC's domain.
				if (checkDomainAuthorization(authorizedUser, parameters.getFilter().getDomain(), response) == null) {
					return response;
				}
				sc.setDomainName(parameters.getFilter().getDomain());
			}
			sc.setAddressName(parameters.getFilter().getLocalname());
			if (parameters.getFilter().getStatus() != null) {
				sc.setStatus(AgentCredentialStatus.valueOf(parameters.getFilter().getStatus().value()));
			}
			sc.setType(AgentCredentialType.UC);
			List<AgentCredential> credentials = credentialService.search(zone, sc);
			for (AgentCredential c : credentials) {
				response.getUsers().add(d2a.mapUser(c));
			}
		}
		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	@WebResult(name = "searchAdministratorResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchAdministrator")
	public SearchAdministratorResponse searchAdministrator(
			@WebParam(partName = "parameters", name = "searchAdministrator", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchAdministrator parameters) {
		SearchAdministratorResponse response = new SearchAdministratorResponse();

		PKIXCertificate authorizedUser = checkZACAuthorized(getAgentService().getAuthenticatedAgent(), response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}

		if (parameters.getFilter().getAdministratorIdentity() != null) {
			AdministratorIdentity dacIdentity = validator.checkAdministratorIdentity(parameters.getFilter()
					.getAdministratorIdentity(), response);
			if (dacIdentity == null) {
				return response;
			}

			// if a DAC credential is provided then it't not so much a search as a lookup
			AgentCredentialDescriptor dac = credentialFactory.createAgentCredential(dacIdentity.getDomaincertificate(),
					dacIdentity.getRootcertificate());
			if (dac == null || AgentCredentialType.DAC != dac.getCredentialType()) {
				setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
				return response;
			}
			AgentCredential c = credentialService.findByFingerprint(dac.getFingerprint());
			if (c != null) {
				response.getAdministrators().add(d2a.mapAdministrator(c));
			}
		} else {
			AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(a2d.mapPage(parameters.getPage()));
			if (StringUtils.hasText(parameters.getFilter().getDomain())) {
				// we check that the provided domain is the ZAC's root domain.
				if (!StringUtils.isSuffix(parameters.getFilter().getDomain(), zone.getZoneApex())) {
					setError(ErrorCode.OutOfZoneAccess, response);
					return response;
				}
				sc.setDomainName(parameters.getFilter().getDomain());
			}
			if (parameters.getFilter().getStatus() != null) {
				sc.setStatus(AgentCredentialStatus.valueOf(parameters.getFilter().getStatus().value()));
			}
			sc.setType(AgentCredentialType.DAC);
			List<AgentCredential> credentials = credentialService.search(zone, sc);
			for (AgentCredential c : credentials) {
				response.getAdministrators().add(d2a.mapAdministrator(c));
			}
		}
		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	@WebResult(name = "modifyUserResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyUser")
	public ModifyUserResponse modifyUser(
			@WebParam(partName = "parameters", name = "modifyUser", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") ModifyUser parameters) {
		ModifyUserResponse response = new ModifyUserResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		// try to constuct the UC given the data provided
		AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(parameters.getUserIdentity()
				.getUsercertificate(), parameters.getUserIdentity().getDomaincertificate(), parameters
				.getUserIdentity().getRootcertificate());
		if (uc == null || AgentCredentialType.UC != uc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}

		Zone zone = checkDomainAuthorization(authorizedUser, uc.getDomainName(), response);
		if (zone == null) {
			return response;
		}

		// check that the UC credential exists
		AgentCredential existingCred = credentialService.findByFingerprint(uc.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}
		if (parameters.getStatus() != null) {
			existingCred.setCredentialStatus(AgentCredentialStatus.valueOf(parameters.getStatus().value()));
		}

		// update the existing UC
		credentialService.createOrUpdate(existingCred);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "searchIpZoneResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchIpZone")
	public SearchIpZoneResponse searchIpZone(
			@WebParam(partName = "parameters", name = "searchIpZone", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "createAddressResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createAddress")
	public CreateAddressResponse createAddress(
			@WebParam(partName = "parameters", name = "createAddress", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateAddress parameters) {
		CreateAddressResponse response = new CreateAddressResponse();

		Zone zone = checkDomainAuthorization(parameters.getAddress().getDomain(), response);
		if (zone == null) {
			return response;
		}
		// check if the domain exists already
		Domain domain = getDomainService().findByName(zone, parameters.getAddress().getDomain());
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check if the domain exists already
		org.tdmx.lib.zone.domain.Address a = getAddressService().findByName(domain,
				parameters.getAddress().getLocalname());
		if (a != null) {
			setError(ErrorCode.AddressExists, response);
			return response;
		}

		// create the address
		a = new org.tdmx.lib.zone.domain.Address(domain, parameters.getAddress().getLocalname());

		getAddressService().createOrUpdate(a);
		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "deleteChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteChannelAuthorization")
	public DeleteChannelAuthorizationResponse deleteChannelAuthorization(
			@WebParam(partName = "parameters", name = "deleteChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteChannelAuthorization parameters) {
		DeleteChannelAuthorizationResponse response = new DeleteChannelAuthorizationResponse();

		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}
		String domainName = parameters.getDomain();

		// check DAC is authorized on the channelauths domain
		Zone zone = checkDomainAuthorization(authorizedUser, domainName, response);
		if (zone == null) {
			return response;
		}

		// validate all channel and provided permission fields are specified.
		Channel channel = validator.checkChannel(parameters.getChannel(), response);
		if (channel == null) {
			return response;
		}

		Domain domain = getDomainService().findByName(zone, domainName);
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		ChannelAuthorization ca = channelService.findByChannel(zone, domain, a2d.mapChannelOrigin(channel.getOrigin()),
				a2d.mapChannelDestination(channel.getDestination()));
		if (ca == null) {
			setError(ErrorCode.ChannelAuthorizationNotFound, response);
			return response;
		}
		channelService.delete(ca.getChannel());

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "incidentResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:report", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/incident")
	public IncidentResponse incident(
			@WebParam(partName = "parameters", name = "incident", targetNamespace = "urn:tdmx:api:v1.0:sp:report") Incident parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "createIpZoneResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createIpZone")
	public CreateIpZoneResponse createIpZone(
			@WebParam(partName = "parameters", name = "createIpZone", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "deleteIpZoneResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteIpZone")
	public DeleteIpZoneResponse deleteIpZone(
			@WebParam(partName = "parameters", name = "deleteIpZone", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "reportResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:report", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/report")
	public ReportResponse report(
			@WebParam(partName = "parameters", name = "report", targetNamespace = "urn:tdmx:api:v1.0:sp:report") Report parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "deleteUserResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteUser")
	public DeleteUserResponse deleteUser(
			@WebParam(partName = "parameters", name = "deleteUser", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteUser parameters) {
		DeleteUserResponse response = new DeleteUserResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		// try to constuct the UC given the data provided
		AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(parameters.getUserIdentity()
				.getUsercertificate(), parameters.getUserIdentity().getDomaincertificate(), parameters
				.getUserIdentity().getRootcertificate());
		if (uc == null || AgentCredentialType.UC != uc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}

		// check user belongs to DAC's domain
		Zone zone = checkDomainAuthorization(authorizedUser, uc.getDomainName(), response);
		if (zone == null) {
			return response;
		}

		// check that the UC credential exists
		AgentCredential existingCred = credentialService.findByFingerprint(uc.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}

		// delete any FlowTargets
		boolean moreFT = true;
		while (moreFT) {
			FlowTargetSearchCriteria casc = new FlowTargetSearchCriteria(new PageSpecifier(0, getBatchSize()));
			casc.getTarget().setAgent(existingCred);

			List<FlowTarget> flowTargets = flowTargetService.search(zone, casc);
			for (FlowTarget ft : flowTargets) {
				flowTargetService.delete(ft);
			}
			if (flowTargets.isEmpty()) {
				moreFT = false;
			}
		}

		// delete the UC
		credentialService.delete(existingCred);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "modifyIpZoneResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyIpZone")
	public ModifyIpZoneResponse modifyIpZone(
			@WebParam(partName = "parameters", name = "modifyIpZone", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") ModifyIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "modifyServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyService")
	public ModifyServiceResponse modifyService(
			@WebParam(partName = "parameters", name = "modifyService", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") ModifyService parameters) {
		ModifyServiceResponse response = new ModifyServiceResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = checkDomainAuthorization(authorizedUser, parameters.getService().getDomain(), response);
		if (zone == null) {
			return response;
		}

		Domain domain = getDomainService().findByName(zone, parameters.getService().getDomain());
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}
		// lookup existing service exists
		org.tdmx.lib.zone.domain.Service existingService = getServiceService().findByName(domain,
				parameters.getService().getServicename());
		if (existingService == null) {
			setError(ErrorCode.ServiceNotFound, response);
			return response;
		}
		existingService.setConcurrencyLimit(parameters.getConcurrencyLimit());

		// update the existing service
		serviceService.createOrUpdate(existingService);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "searchAddressResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchAddress")
	public SearchAddressResponse searchAddress(
			@WebParam(partName = "parameters", name = "searchAddress", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchAddress parameters) {
		SearchAddressResponse response = new SearchAddressResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}

		if (!StringUtils.hasText(parameters.getFilter().getDomain()) && authorizedUser.isTdmxDomainAdminCertificate()) {
			// we fix the search to search only the DAC's domain.
			parameters.getFilter().setDomain(authorizedUser.getCommonName());
		}
		AddressSearchCriteria sc = new AddressSearchCriteria(a2d.mapPage(parameters.getPage()));
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			// we check that the provided domain is the DAC's domain.
			if (checkDomainAuthorization(authorizedUser, parameters.getFilter().getDomain(), response) == null) {
				return response;
			}
			sc.setDomainName(parameters.getFilter().getDomain());
		}
		sc.setLocalName(parameters.getFilter().getLocalname());
		List<org.tdmx.lib.zone.domain.Address> addresses = addressService.search(zone, sc);
		for (org.tdmx.lib.zone.domain.Address a : addresses) {
			response.getAddresses().add(d2a.mapAddress(a));
		}
		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	@WebResult(name = "createAdministratorResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createAdministrator")
	public CreateAdministratorResponse createAdministrator(
			@WebParam(partName = "parameters", name = "createAdministrator", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateAdministrator parameters) {
		CreateAdministratorResponse response = new CreateAdministratorResponse();

		PKIXCertificate authorizedUser = checkZACAuthorized(getAgentService().getAuthenticatedAgent(), response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}
		// try to constuct new DAC given the data provided
		AdministratorIdentity dacIdentity = validator.checkAdministratorIdentity(parameters.getAdministratorIdentity(),
				response);
		if (dacIdentity == null) {
			return response;
		}

		AgentCredentialDescriptor dac = credentialFactory.createAgentCredential(dacIdentity.getDomaincertificate(),
				dacIdentity.getRootcertificate());
		if (dac == null) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}
		if (!credentialValidator.isValid(dac)) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}

		// check if the domain exists already
		Domain domain = getDomainService().findByName(zone, dac.getDomainName());
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check that the DAC credential doesn't already exist
		AgentCredential existingCred = credentialService.findByFingerprint(dac.getFingerprint());
		if (existingCred != null) {
			setError(ErrorCode.DomainAdministratorCredentialsExist, response);
			return response;
		}

		AgentCredential newCred = new AgentCredential(zone, domain, dac);
		if (parameters.getStatus() != null) {
			newCred.setCredentialStatus(AgentCredentialStatus.valueOf(parameters.getStatus().value()));
		}

		// create the DAC
		credentialService.createOrUpdate(newCred);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "deleteServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteService")
	public DeleteServiceResponse deleteService(
			@WebParam(partName = "parameters", name = "deleteService", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteService parameters) {
		DeleteServiceResponse response = new DeleteServiceResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = checkDomainAuthorization(authorizedUser, parameters.getService().getDomain(), response);
		if (zone == null) {
			return response;
		}

		Domain domain = getDomainService().findByName(zone, parameters.getService().getDomain());
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}
		// lookup existing service exists
		org.tdmx.lib.zone.domain.Service existingService = getServiceService().findByName(domain,
				parameters.getService().getServicename());
		if (existingService == null) {
			setError(ErrorCode.ServiceNotFound, response);
			return response;
		}

		// delete any FlowTargets
		boolean moreFT = true;
		while (moreFT) {
			FlowTargetSearchCriteria casc = new FlowTargetSearchCriteria(new PageSpecifier(0, getBatchSize()));
			casc.setService(existingService);

			List<FlowTarget> flowTargets = flowTargetService.search(zone, casc);
			for (FlowTarget ft : flowTargets) {
				flowTargetService.delete(ft);
			}
			if (flowTargets.isEmpty()) {
				moreFT = false;
			}
		}

		// delete the existing service
		serviceService.delete(existingService);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "searchServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchService")
	public SearchServiceResponse searchService(
			@WebParam(partName = "parameters", name = "searchService", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchService parameters) {
		SearchServiceResponse response = new SearchServiceResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}

		if (!StringUtils.hasText(parameters.getFilter().getDomain()) && authorizedUser.isTdmxDomainAdminCertificate()) {
			// we fix the search to search only the DAC's domain.
			parameters.getFilter().setDomain(authorizedUser.getCommonName());
		}
		ServiceSearchCriteria sc = new ServiceSearchCriteria(a2d.mapPage(parameters.getPage()));
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			// we check that the provided domain is the DAC's domain.
			if (checkDomainAuthorization(authorizedUser, parameters.getFilter().getDomain(), response) == null) {
				return response;
			}
			sc.setDomainName(parameters.getFilter().getDomain());
		}
		sc.setServiceName(parameters.getFilter().getServicename());
		List<org.tdmx.lib.zone.domain.Service> services = serviceService.search(zone, sc);
		for (org.tdmx.lib.zone.domain.Service s : services) {
			response.getServicestates().add(d2a.mapService(s));
		}
		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	@WebResult(name = "deleteAddressResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteAddress")
	public DeleteAddressResponse deleteAddress(
			@WebParam(partName = "parameters", name = "deleteAddress", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteAddress parameters) {
		DeleteAddressResponse response = new DeleteAddressResponse();

		Zone zone = checkDomainAuthorization(parameters.getAddress().getDomain(), response);
		if (zone == null) {
			return response;
		}

		// check if there are any UCs
		AgentCredentialSearchCriteria acSc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 1));
		acSc.setAddressName(parameters.getAddress().getLocalname());
		acSc.setDomainName(parameters.getAddress().getDomain());
		acSc.setType(AgentCredentialType.UC);
		List<AgentCredential> ucs = getCredentialService().search(zone, acSc);
		if (!ucs.isEmpty()) {
			setError(ErrorCode.UserCredentialsExist, response);
			return response;
		}

		Domain domain = getDomainService().findByName(zone, parameters.getAddress().getDomain());
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}
		// dont allow creation if we find the address exists already
		org.tdmx.lib.zone.domain.Address a = getAddressService().findByName(domain,
				parameters.getAddress().getLocalname());
		if (a == null) {
			setError(ErrorCode.AddressNotFound, response);
			return response;
		}

		// delete the address
		getAddressService().delete(a);
		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "modifyAdministratorResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyAdministrator")
	public ModifyAdministratorResponse modifyAdministrator(
			@WebParam(partName = "parameters", name = "modifyAdministrator", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") ModifyAdministrator parameters) {
		ModifyAdministratorResponse response = new ModifyAdministratorResponse();

		PKIXCertificate authorizedUser = checkZACAuthorized(getAgentService().getAuthenticatedAgent(), response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}
		// try to constuct the DAC given the data provided
		AdministratorIdentity dacIdentity = validator.checkAdministratorIdentity(parameters.getAdministratorIdentity(),
				response);
		if (dacIdentity == null) {
			return response;
		}

		AgentCredentialDescriptor dac = credentialFactory.createAgentCredential(dacIdentity.getDomaincertificate(),
				dacIdentity.getRootcertificate());
		if (dac == null || AgentCredentialType.DAC != dac.getCredentialType()) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}

		// check that the DAC credential exists
		AgentCredential existingCred = credentialService.findByFingerprint(dac.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.DomainAdministratorCredentialNotFound, response);
			return response;
		}
		if (parameters.getStatus() != null) {
			existingCred.setCredentialStatus(AgentCredentialStatus.valueOf(parameters.getStatus().value()));
		}
		// update the DAC
		credentialService.createOrUpdate(existingCred);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "setChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/setChannelAuthorization")
	public SetChannelAuthorizationResponse setChannelAuthorization(
			@WebParam(partName = "parameters", name = "setChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SetChannelAuthorization parameters) {
		SetChannelAuthorizationResponse response = new SetChannelAuthorizationResponse();

		PKIXCertificate authorizedUser = checkDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}
		String domainName = parameters.getDomain();

		// check DAC is authorized on the channelauths domain
		Zone zone = checkDomainAuthorization(authorizedUser, domainName, response);
		if (zone == null) {
			return response;
		}

		// validate all channel and provided permission fields are specified.
		Currentchannelauthorization ca = validator.checkChannelauthorization(
				parameters.getCurrentchannelauthorization(), response);
		if (ca == null) {
			return response;
		}
		// check the signature of the current ca is ok
		if (!SignatureUtils.checkChannelAuthorizationSignature(ca)) {
			setError(ErrorCode.InvalidSignatureChannelAuthorization, response);
			return response;
		}

		// check that the channel origin or channel destination matches the ca's domain
		if (!(domainName.equals(ca.getChannel().getOrigin().getDomain()) || domainName.equals(ca.getChannel()
				.getDestination().getDomain()))) {
			setError(ErrorCode.ChannelAuthorizationDomainMismatch, response);
			return response;
		}
		// note if the domain matches both send and recv then we validate both

		if (domainName.equals(ca.getChannel().getOrigin().getDomain())) {
			// if the origin domain matches the domain - check that we have a send permission and that the signature is
			// ok

			if (ca.getOrigin() == null) {
				setError(ErrorCode.MissingEndpointPermission, response);
				return response;
			}
			if (!SignatureUtils.checkEndpointPermissionSignature(ca.getChannel(), ca.getOrigin(), true)) {
				setError(ErrorCode.InvalidSignatureEndpointPermission, response);
				return response;
			}
		}

		if (domainName.equals(ca.getChannel().getDestination().getDomain())) {
			// if the destination domain matches the ca's domain - check that we have a recv permission and that the
			// signature is ok

			if (ca.getDestination() == null) {
				setError(ErrorCode.MissingEndpointPermission, response);
				return response;
			}
			if (!SignatureUtils.checkEndpointPermissionSignature(ca.getChannel(), ca.getDestination(), true)) {
				setError(ErrorCode.InvalidSignatureEndpointPermission, response);
				return response;
			}
		}

		Domain domain = getDomainService().findByName(zone, domainName);
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// we construct a detached channel authorization from the provided request data

		SetAuthorizationResultHolder operationStatus = channelService.setAuthorization(zone, domain,
				a2d.mapChannelOrigin(ca.getChannel().getOrigin()),
				a2d.mapChannelDestination(ca.getChannel().getDestination()), a2d.mapChannelAuthorization(ca));
		if (operationStatus.status != null) {
			setError(mapSetAuthorizationOperationStatus(operationStatus.status), response);
			return response;
		}
		if (operationStatus.channelAuthorization != null) {
			if (operationStatus.channelAuthorization.getProcessingState().getStatus() == ProcessingStatus.PENDING) {
				// TODO initiate transfer of send/recv auth to other party ( processing state )
				// by caller, depending on whether processingstate is PENDING.

			}
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "createUserResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createUser")
	public CreateUserResponse createUser(
			@WebParam(partName = "parameters", name = "createUser", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateUser parameters) {
		CreateUserResponse response = new CreateUserResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		// try to constuct new UC given the data provided
		AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(parameters.getUserIdentity()
				.getUsercertificate(), parameters.getUserIdentity().getDomaincertificate(), parameters
				.getUserIdentity().getRootcertificate());
		if (uc == null) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}

		// check the domain name of UC matches the DAC's domain or we are ZAC
		Zone zone = checkDomainAuthorization(authorizedUser, uc.getDomainName(), response);
		if (zone == null) {
			return response;
		}

		// check if the domain exists already
		Domain domain = getDomainService().findByName(zone, uc.getDomainName());
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check if the address exists already
		org.tdmx.lib.zone.domain.Address address = getAddressService().findByName(domain, uc.getAddressName());
		if (address == null) {
			setError(ErrorCode.AddressNotFound, response);
			return response;
		}

		// check that the UC credential doesn't already exist
		AgentCredential existingCred = credentialService.findByFingerprint(uc.getFingerprint());
		if (existingCred != null) {
			setError(ErrorCode.UserCredentialsExist, response);
			return response;
		}

		AgentCredential newCred = new AgentCredential(zone, domain, address, uc);
		if (parameters.getStatus() != null) {
			newCred.setCredentialStatus(AgentCredentialStatus.valueOf(parameters.getStatus().value()));
		}

		// create the UC
		credentialService.createOrUpdate(newCred);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "searchChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchChannelAuthorization")
	public SearchChannelAuthorizationResponse searchChannelAuthorization(
			@WebParam(partName = "parameters", name = "searchChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchChannelAuthorization parameters) {
		SearchChannelAuthorizationResponse response = new SearchChannelAuthorizationResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}

		ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(
				a2d.mapPage(parameters.getPage()));
		if (!StringUtils.hasText(parameters.getFilter().getDomain()) && authorizedUser.isTdmxDomainAdminCertificate()) {
			// we fix the search to search only the DAC's domain.
			parameters.getFilter().setDomain(authorizedUser.getCommonName());
		}
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			// we check that the provided domain is the DAC's domain.
			if (checkDomainAuthorization(authorizedUser, parameters.getFilter().getDomain(), response) == null) {
				return response;
			}
			sc.setDomainName(parameters.getFilter().getDomain());
		}
		if (parameters.getFilter().getOrigin() != null) {
			sc.getOrigin().setDomainName(parameters.getFilter().getOrigin().getDomain());
			sc.getOrigin().setLocalName(parameters.getFilter().getOrigin().getLocalname());
			sc.getOrigin().setServiceProvider(parameters.getFilter().getOrigin().getServiceprovider());
		}
		if (parameters.getFilter().getDestination() != null) {
			sc.getDestination().setDomainName(parameters.getFilter().getDestination().getDomain());
			sc.getDestination().setLocalName(parameters.getFilter().getDestination().getLocalname());
			sc.getDestination().setServiceProvider(parameters.getFilter().getDestination().getServiceprovider());
			sc.getDestination().setServiceName(parameters.getFilter().getDestination().getServicename());
		}
		sc.setUnconfirmed(parameters.getFilter().isUnconfirmedFlag()); // TODO test
		List<org.tdmx.lib.zone.domain.ChannelAuthorization> channelAuthorizations = channelService.search(zone, sc);
		for (org.tdmx.lib.zone.domain.ChannelAuthorization ca : channelAuthorizations) {
			response.getChannelauthorizations().add(d2a.mapChannelAuthorization(ca));
		}
		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	@WebResult(name = "createServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createService")
	public CreateServiceResponse createService(
			@WebParam(partName = "parameters", name = "createService", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateService parameters) {
		CreateServiceResponse response = new CreateServiceResponse();

		Zone zone = checkDomainAuthorization(parameters.getService().getDomain(), response);
		if (zone == null) {
			return response;
		}

		// check if the domain exists already
		Domain domain = getDomainService().findByName(zone, parameters.getService().getDomain());
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check if the service exists already
		org.tdmx.lib.zone.domain.Service service = getServiceService().findByName(domain,
				parameters.getService().getServicename());
		if (service != null) {
			setError(ErrorCode.ServiceExists, response);
			return response;
		}

		// create the service
		org.tdmx.lib.zone.domain.Service s = new org.tdmx.lib.zone.domain.Service(domain, parameters.getService()
				.getServicename());
		s.setConcurrencyLimit(parameters.getConcurrencyLimit());

		getServiceService().createOrUpdate(s);
		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "deleteAdministratorResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteAdministrator")
	public DeleteAdministratorResponse deleteAdministrator(
			@WebParam(partName = "parameters", name = "deleteAdministrator", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteAdministrator parameters) {
		DeleteAdministratorResponse response = new DeleteAdministratorResponse();
		PKIXCertificate authorizedUser = checkZACAuthorized(getAgentService().getAuthenticatedAgent(), response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}
		// try to constuct the DAC given the data provided
		AdministratorIdentity dacIdentity = validator.checkAdministratorIdentity(parameters.getAdministratorIdentity(),
				response);
		if (dacIdentity == null) {
			return response;
		}
		AgentCredentialDescriptor dac = credentialFactory.createAgentCredential(dacIdentity.getDomaincertificate(),
				dacIdentity.getRootcertificate());
		if (dac == null || AgentCredentialType.DAC != dac.getCredentialType()) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}

		// check that the DAC credential exists
		AgentCredential existingCred = credentialService.findByFingerprint(dac.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.DomainAdministratorCredentialNotFound, response);
			return response;
		}

		// delete the existing DAC
		credentialService.delete(existingCred);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "searchFlowResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchFlow")
	public org.tdmx.core.api.v01.zas.SearchFlowResponse searchFlow(
			@WebParam(partName = "parameters", name = "searchFlow", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") org.tdmx.core.api.v01.zas.SearchFlow parameters) {
		// TODO
		return null;
	}

	@Override
	@WebResult(name = "searchFlowTargetResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchFlowTarget")
	public org.tdmx.core.api.v01.zas.SearchFlowTargetResponse searchFlowTarget(
			@WebParam(partName = "parameters", name = "searchFlowTarget", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") org.tdmx.core.api.v01.zas.SearchFlowTarget parameters) {

		SearchFlowTargetResponse response = new SearchFlowTargetResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		Zone zone = getAgentService().getZone();
		if (zone == null) {
			return response;
		}

		FlowTargetSearchCriteria sc = new FlowTargetSearchCriteria(a2d.mapPage(parameters.getPage()));
		if (authorizedUser.isTdmxDomainAdminCertificate() && !StringUtils.hasText(parameters.getFilter().getDomain())) {
			// we fix the search to search only the DAC's domain.
			parameters.getFilter().setDomain(authorizedUser.getCommonName());
		}
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			// we check that the provided domain is the DAC's domain.
			if (checkDomainAuthorization(authorizedUser, parameters.getFilter().getDomain(), response) == null) {
				return response;
			}
			sc.getTarget().setDomainName(parameters.getFilter().getDomain());
		}
		if (parameters.getFilter().getUserIdentity() != null) {
			// if a user credential is provided then it't not so much a search as a lookup

			AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(parameters.getFilter()
					.getUserIdentity().getUsercertificate(), parameters.getFilter().getUserIdentity()
					.getDomaincertificate(), parameters.getFilter().getUserIdentity().getRootcertificate());
			if (uc == null || AgentCredentialType.UC != uc.getCredentialType()) {
				setError(ErrorCode.InvalidUserCredentials, response);
				return response;
			}
			// we check that the provided domain is the DAC's domain.
			if (checkDomainAuthorization(authorizedUser, uc.getDomainName(), response) == null) {
				return response;
			}
			AgentCredential specificUser = credentialService.findByFingerprint(uc.getFingerprint());
			if (specificUser != null) {
				sc.getTarget().setAgent(specificUser);
			}
		}
		sc.getTarget().setAddressName(parameters.getFilter().getLocalname());
		if (parameters.getFilter().getStatus() != null) {
			sc.getTarget().setStatus(AgentCredentialStatus.valueOf(parameters.getFilter().getStatus().value()));
		}
		sc.setServiceName(parameters.getFilter().getServicename());

		List<FlowTarget> flowtargets = flowTargetService.search(zone, sc);
		for (FlowTarget ft : flowtargets) {
			response.getFlowtargets().add(d2a.mapFlowTarget(ft));
		}

		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	@WebResult(name = "modifyFlowTargetResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyFlowTarget")
	public org.tdmx.core.api.v01.zas.ModifyFlowTargetResponse modifyFlowTarget(
			@WebParam(partName = "parameters", name = "modifyFlowTarget", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") org.tdmx.core.api.v01.zas.ModifyFlowTarget parameters) {
		ModifyFlowTargetResponse response = new ModifyFlowTargetResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(parameters.getFlowdestination()
				.getTarget().getUsercertificate(), parameters.getFlowdestination().getTarget().getDomaincertificate(),
				parameters.getFlowdestination().getTarget().getRootcertificate());
		if (uc == null || AgentCredentialType.UC != uc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}

		Zone zone = checkDomainAuthorization(authorizedUser, uc.getDomainName(), response);
		if (zone == null) {
			return response;
		}

		// check that the UC credential exists
		AgentCredential existingCred = credentialService.findByFingerprint(uc.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}

		// lookup existing service exists
		org.tdmx.lib.zone.domain.Service existingService = getServiceService().findByName(existingCred.getDomain(),
				parameters.getFlowdestination().getServicename());
		if (existingService == null) {
			setError(ErrorCode.ServiceNotFound, response);
			return response;
		}

		ModifyOperationStatus status = flowTargetService.modifyConcurrency(existingCred, existingService,
				parameters.getConcurrencyLimit());
		if (ModifyOperationStatus.SUCCESS != status) {
			setError(mapModifyConcurrencyOperationStatus(status), response);
			return response;
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

	/**
	 * Checks the AuthenticatedAgent is authorized to perform administration on the domain, and return the agent's
	 * zoneApex. If the agent is not authorized then the acknowledge's Error info will be set and null returned.
	 * 
	 * @param ack
	 * @return null if not authorized, setting ack.Error, else the zoneApex.
	 */

	private boolean checkDomain(String domain, Acknowledge ack) {
		if (!StringUtils.hasText(domain)) {
			setError(ErrorCode.DomainNotSpecified, ack);
			return false;
		} else if (!StringUtils.isLowerCase(domain)) {
			setError(ErrorCode.NotNormalizedDomain, ack);
			return false;
		}
		return true;
	}

	private PKIXCertificate checkZACAuthorized(PKIXCertificate user, Acknowledge ack) {
		if (user == null) {
			setError(ErrorCode.MissingCredentials, ack);
			return null;
		}
		if (!user.isTdmxZoneAdminCertificate()) {
			setError(ErrorCode.NonZoneAdministratorAccess, ack);
			return null;
		}
		return user;
	}

	private PKIXCertificate checkZACorDACAuthorized(Acknowledge ack) {
		PKIXCertificate user = getAgentService().getAuthenticatedAgent();
		if (user == null) {
			setError(ErrorCode.MissingCredentials, ack);
			return null;
		}
		if (!user.isTdmxZoneAdminCertificate() && !user.isTdmxDomainAdminCertificate()) {
			setError(ErrorCode.NonDomainAdministratorAccess, ack);
			return null;
		}
		return user;
	}

	private PKIXCertificate checkDACAuthorized(Acknowledge ack) {
		PKIXCertificate user = getAgentService().getAuthenticatedAgent();
		if (user == null) {
			setError(ErrorCode.MissingCredentials, ack);
			return null;
		}
		if (!user.isTdmxDomainAdminCertificate()) {
			setError(ErrorCode.NonDomainAdministratorAccess, ack);
			return null;
		}
		return user;
	}

	private Zone checkDomainAuthorization(PKIXCertificate authorizedAgent, String domain, Acknowledge ack) {
		Zone zone = getAgentService().getZone();

		if (!StringUtils.isSuffix(domain, zone.getZoneApex())) {
			setError(ErrorCode.OutOfZoneAccess, ack);
			return null;
		}

		// domain must match domain administrator of authorized user
		if (authorizedAgent.isTdmxDomainAdminCertificate() && !authorizedAgent.getCommonName().equals(domain)) {
			setError(ErrorCode.OutOfDomainAccess, ack);
			return null;
		}
		return zone;

	}

	/**
	 * Checks the domain is within the authorized zone.
	 * 
	 * @param ack
	 * @return null if not authorized, setting ack.Error, else the domain.
	 */
	private String checkZACDomainAuthorization(String domain, Zone zone, Acknowledge ack) {
		if (!checkDomain(domain, ack)) {
			return null;
		}
		if (!StringUtils.isSuffix(domain, zone.getZoneApex())) {
			setError(ErrorCode.OutOfZoneAccess, ack);
			return null;
		}
		return domain;
	}

	/**
	 * Checks the AuthenticatedAgent is a ZAC or DAC, and return the agent's zoneApex. If the agent is not authorized
	 * then the acknowledge's Error info will be set and null returned.
	 * 
	 * @param ack
	 * @return null if not authorized, setting ack.Error, else the zoneApex.
	 */
	private Zone checkDomainAuthorization(String domain, Acknowledge ack) {
		if (!checkDomain(domain, ack)) {
			return null;
		}

		PKIXCertificate user = checkZACorDACAuthorized(ack);
		if (user == null) {
			return null;
		}
		return checkDomainAuthorization(user, domain, ack);
	}

	private void setError(ErrorCode ec, Acknowledge ack) {
		Error error = new Error();
		error.setCode(ec.getErrorCode());
		error.setDescription(ec.getErrorDescription());
		ack.setError(error);
		ack.setSuccess(false);
	}

	private ErrorCode mapModifyConcurrencyOperationStatus(ModifyOperationStatus status) {
		switch (status) {
		case FLOWTARGET_NOT_FOUND:
			return ErrorCode.FlowTargetNotFound;
		default:
			return null;
		}
	}

	private ErrorCode mapSetAuthorizationOperationStatus(SetAuthorizationOperationStatus status) {
		switch (status) {
		case RECEIVER_AUTHORIZATION_CONFIRMATION_MISMATCH:
			return ErrorCode.ReceiverChannelAuthorizationMismatch;
		case RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING:
			return ErrorCode.ReceiverChannelAuthorizationMissing;
		case SENDER_AUTHORIZATION_CONFIRMATION_MISMATCH:
			return ErrorCode.SenderChannelAuthorizationMismatch;
		case SENDER_AUTHORIZATION_CONFIRMATION_MISSING:
			return ErrorCode.SenderChannelAuthorizationMissing;
		case RECEIVER_AUTHORIZATION_CONFIRMATION_PROVIDED:
			return ErrorCode.ReceiverChannelAuthorizationProvided;
		case SENDER_AUTHORIZATION_CONFIRMATION_PROVIDED:
			return ErrorCode.SenderChannelAuthorizationProvided;
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

	public AgentCredentialFactory getCredentialFactory() {
		return credentialFactory;
	}

	public void setCredentialFactory(AgentCredentialFactory credentialFactory) {
		this.credentialFactory = credentialFactory;
	}

	public AgentCredentialValidator getCredentialValidator() {
		return credentialValidator;
	}

	public void setCredentialValidator(AgentCredentialValidator credentialValidator) {
		this.credentialValidator = credentialValidator;
	}

	public AgentCredentialService getCredentialService() {
		return credentialService;
	}

	public void setCredentialService(AgentCredentialService credentialService) {
		this.credentialService = credentialService;
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

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

}
