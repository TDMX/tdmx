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
import org.tdmx.core.api.v01.sp.zas.CreateAddress;
import org.tdmx.core.api.v01.sp.zas.CreateAddressResponse;
import org.tdmx.core.api.v01.sp.zas.CreateAdministrator;
import org.tdmx.core.api.v01.sp.zas.CreateAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.CreateDomain;
import org.tdmx.core.api.v01.sp.zas.CreateDomainResponse;
import org.tdmx.core.api.v01.sp.zas.CreateIpZone;
import org.tdmx.core.api.v01.sp.zas.CreateIpZoneResponse;
import org.tdmx.core.api.v01.sp.zas.CreateService;
import org.tdmx.core.api.v01.sp.zas.CreateServiceResponse;
import org.tdmx.core.api.v01.sp.zas.CreateUser;
import org.tdmx.core.api.v01.sp.zas.CreateUserResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteAddress;
import org.tdmx.core.api.v01.sp.zas.DeleteAddressResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteAdministrator;
import org.tdmx.core.api.v01.sp.zas.DeleteAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteChannelAuthorization;
import org.tdmx.core.api.v01.sp.zas.DeleteChannelAuthorizationResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteDomain;
import org.tdmx.core.api.v01.sp.zas.DeleteDomainResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteIpZone;
import org.tdmx.core.api.v01.sp.zas.DeleteIpZoneResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteService;
import org.tdmx.core.api.v01.sp.zas.DeleteServiceResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteUser;
import org.tdmx.core.api.v01.sp.zas.DeleteUserResponse;
import org.tdmx.core.api.v01.sp.zas.GetChannelAuthorization;
import org.tdmx.core.api.v01.sp.zas.GetChannelAuthorizationResponse;
import org.tdmx.core.api.v01.sp.zas.GetFlowState;
import org.tdmx.core.api.v01.sp.zas.GetFlowStateResponse;
import org.tdmx.core.api.v01.sp.zas.GetFlowTargetState;
import org.tdmx.core.api.v01.sp.zas.GetFlowTargetStateResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyAdministrator;
import org.tdmx.core.api.v01.sp.zas.ModifyAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyFlowTargetState;
import org.tdmx.core.api.v01.sp.zas.ModifyFlowTargetStateResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyIpZone;
import org.tdmx.core.api.v01.sp.zas.ModifyIpZoneResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyService;
import org.tdmx.core.api.v01.sp.zas.ModifyServiceResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyUser;
import org.tdmx.core.api.v01.sp.zas.ModifyUserResponse;
import org.tdmx.core.api.v01.sp.zas.SearchAddress;
import org.tdmx.core.api.v01.sp.zas.SearchAddressResponse;
import org.tdmx.core.api.v01.sp.zas.SearchAdministrator;
import org.tdmx.core.api.v01.sp.zas.SearchAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.SearchChannelAuthorization;
import org.tdmx.core.api.v01.sp.zas.SearchChannelAuthorizationResponse;
import org.tdmx.core.api.v01.sp.zas.SearchDomain;
import org.tdmx.core.api.v01.sp.zas.SearchDomainResponse;
import org.tdmx.core.api.v01.sp.zas.SearchFlowState;
import org.tdmx.core.api.v01.sp.zas.SearchFlowStateResponse;
import org.tdmx.core.api.v01.sp.zas.SearchFlowTargetState;
import org.tdmx.core.api.v01.sp.zas.SearchFlowTargetStateResponse;
import org.tdmx.core.api.v01.sp.zas.SearchIpZone;
import org.tdmx.core.api.v01.sp.zas.SearchIpZoneResponse;
import org.tdmx.core.api.v01.sp.zas.SearchService;
import org.tdmx.core.api.v01.sp.zas.SearchServiceResponse;
import org.tdmx.core.api.v01.sp.zas.SearchUser;
import org.tdmx.core.api.v01.sp.zas.SearchUserResponse;
import org.tdmx.core.api.v01.sp.zas.SetChannelAuthorization;
import org.tdmx.core.api.v01.sp.zas.SetChannelAuthorizationResponse;
import org.tdmx.core.api.v01.sp.zas.common.Acknowledge;
import org.tdmx.core.api.v01.sp.zas.common.Error;
import org.tdmx.core.api.v01.sp.zas.common.Page;
import org.tdmx.core.api.v01.sp.zas.msg.Address;
import org.tdmx.core.api.v01.sp.zas.msg.Administrator;
import org.tdmx.core.api.v01.sp.zas.msg.Administratorstate;
import org.tdmx.core.api.v01.sp.zas.msg.CredentialStatus;
import org.tdmx.core.api.v01.sp.zas.msg.IpAddressList;
import org.tdmx.core.api.v01.sp.zas.msg.Service;
import org.tdmx.core.api.v01.sp.zas.msg.Servicestate;
import org.tdmx.core.api.v01.sp.zas.msg.User;
import org.tdmx.core.api.v01.sp.zas.msg.Userstate;
import org.tdmx.core.api.v01.sp.zas.report.Incident;
import org.tdmx.core.api.v01.sp.zas.report.IncidentResponse;
import org.tdmx.core.api.v01.sp.zas.report.Report;
import org.tdmx.core.api.v01.sp.zas.report.ReportResponse;
import org.tdmx.core.api.v01.sp.zas.ws.ZAS;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.zone.domain.AddressID;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainID;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.domain.ServiceID;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.ServiceService;
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

	private AgentCredentialFactory credentialFactory;
	private AgentCredentialService credentialService;

	public enum ErrorCode {
		// authorization errors
		MissingCredentials(403, "Missing Credentials."),
		NonZoneAdministratorAccess(403, "Non ZoneAdministrator access."),
		NonDomainAdministratorAccess(403, "Non DomainAdministrator access."),
		OutOfZoneAccess(403, "ZAC only authorized on own subdomains."),
		OutOfDomainAccess(403, "DAC only authorized on own domain."),
		// business logic errors
		DomainNotSpecified(500, "Domain not supplied."),
		NotNormalizedDomain(500, "Domain not normalized to uppercase."),
		DomainExists(500, "Domain exists."),
		DomainNotFound(500, "Domain not found."),
		AddressExists(500, "Address exists."),
		AddressNotFound(500, "Address not found."),
		ServiceExists(500, "Service exists."),
		ServiceNotFound(500, "Service not found."),

		InvalidDomainAdministratorCredentials(500, "Invalid DAC credentials."),
		DomainAdministratorCredentialsExist(500, "DACs exists."),
		DomainAdministratorCredentialNotFound(500, "DAC not found."),
		InvalidUserCredentials(500, "Invalid User credentials."),
		UserCredentialsExist(500, "UCs exists."),
		UserCredentialNotFound(500, "UC not found."),
		CredentialsExist(500, "Credentials exists."),
		AddressesExist(500, "Addresses exists."),
		ServicesExist(500, "Addresses exists."), ;

		private final int errorCode;
		private final String errorDescription;

		public int getErrorCode() {
			return errorCode;
		}

		public String getErrorDescription() {
			return errorDescription;
		}

		private ErrorCode(int ec, String description) {
			this.errorCode = ec;
			this.errorDescription = description;
		}
	}

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
		String zoneApex = checkZACDomainAuthorization(parameters.getDomain(), response);
		if (zoneApex == null) {
			return response;
		}
		DomainID id = new DomainID(parameters.getDomain(), zoneApex);
		// check if the domain exists already
		Domain domain = getDomainService().findById(id);
		if (domain != null) {
			setError(ErrorCode.DomainExists, response);
			return response;
		}

		// create the domain
		domain = new Domain(id);

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

		String zoneApex = checkZACDomainAuthorization(parameters.getDomain(), response);
		if (zoneApex == null) {
			return response;
		}
		DomainID id = new DomainID(parameters.getDomain(), zoneApex);
		// check if the domain exists already
		Domain domain = getDomainService().findById(id);
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check the domain can be deleted if it has no credentials
		AgentCredentialSearchCriteria dcSc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 1));
		dcSc.setDomainName(parameters.getDomain());
		dcSc.setType(AgentCredentialType.DAC);
		List<AgentCredential> credentials = getCredentialService().search(zoneApex, dcSc);
		if (credentials.size() > 0) {
			setError(ErrorCode.DomainAdministratorCredentialsExist, response);
			return response;
		}

		// and no addresses
		AddressSearchCriteria sac = new AddressSearchCriteria(new PageSpecifier(0, 1));
		sac.setDomainName(id.getDomainName());
		List<org.tdmx.lib.zone.domain.Address> addresses = getAddressService().search(zoneApex, sac);
		if (addresses.size() > 0) {
			setError(ErrorCode.AddressesExist, response);
			return response;
		}

		// and no services
		ServiceSearchCriteria ssc = new ServiceSearchCriteria(new PageSpecifier(0, 1));
		ssc.setDomainName(id.getDomainName());
		List<org.tdmx.lib.zone.domain.Service> services = getServiceService().search(zoneApex, ssc);
		if (services.size() > 0) {
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
		String zoneApex = checkZACAuthorization(response);
		if (zoneApex == null) {
			return response;
		}
		DomainSearchCriteria criteria = new DomainSearchCriteria(mapPage(parameters.getPage()));
		// make sure client stipulates a domain which is within the zone.
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			if (!StringUtils.isSuffix(parameters.getFilter().getDomain(), zoneApex)) {
				setError(ErrorCode.OutOfZoneAccess, response);
				return response;
			}
			criteria.setDomainName(parameters.getFilter().getDomain());
		}
		List<Domain> domains = domainService.search(zoneApex, criteria);
		for (Domain d : domains) {
			response.getDomains().add(d.getId().getDomainName());
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

		String zoneApex = authorizedUser.getTdmxZoneInfo().getZoneRoot();
		if (zoneApex == null) {
			return response;
		}

		if (parameters.getFilter().getUser() != null) {
			// if a user credential is provided then it't not so much a search as a lookup
			AgentCredential uc = credentialFactory.createUC(parameters.getFilter().getUser().getUsercertificate(),
					parameters.getFilter().getUser().getDomaincertificate(), parameters.getFilter().getUser()
							.getRootcertificate());
			if (uc == null) {
				setError(ErrorCode.InvalidUserCredentials, response);
				return response;
			}
			// we check that the provided domain is the DAC's domain.
			if (checkDomainAuthorization(authorizedUser, uc.getDomainName(), response) == null) {
				return response;
			}
			AgentCredential c = credentialService.findById(uc.getId());
			if (c != null) {
				response.getUserstates().add(mapUserstate(c));
			}
		} else {
			AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(mapPage(parameters.getPage()));
			if (authorizedUser.isTdmxDomainAdminCertificate()) {
				if (!StringUtils.hasText(parameters.getFilter().getDomain())) {
					// we fix the search to search only the DAC's domain.
					parameters.getFilter().setDomain(authorizedUser.getCommonName());
				}
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
			List<AgentCredential> credentials = credentialService.search(zoneApex, sc);
			for (AgentCredential c : credentials) {
				response.getUserstates().add(mapUserstate(c));
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

		String zoneApex = checkZACAuthorization(response);
		if (zoneApex == null) {
			return response;
		}

		if (parameters.getFilter().getAdministrator() != null) {
			// if a DAC credential is provided then it't not so much a search as a lookup
			AgentCredential uc = credentialFactory.createDAC(parameters.getFilter().getAdministrator()
					.getDomaincertificate(), parameters.getFilter().getAdministrator().getRootcertificate());
			if (uc == null) {
				setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
				return response;
			}
			AgentCredential c = credentialService.findById(uc.getId());
			if (c != null) {
				response.getAdministratorstates().add(mapAdministratorstate(c));
			}
		} else {
			AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(mapPage(parameters.getPage()));
			if (StringUtils.hasText(parameters.getFilter().getDomain())) {
				// we check that the provided domain is the ZAC's root domain.
				if (!StringUtils.isSuffix(parameters.getFilter().getDomain(), zoneApex)) {
					setError(ErrorCode.OutOfZoneAccess, response);
					return response;
				}
				sc.setDomainName(parameters.getFilter().getDomain());
			}
			if (parameters.getFilter().getStatus() != null) {
				sc.setStatus(AgentCredentialStatus.valueOf(parameters.getFilter().getStatus().value()));
			}
			sc.setType(AgentCredentialType.DAC);
			List<AgentCredential> credentials = credentialService.search(zoneApex, sc);
			for (AgentCredential c : credentials) {
				response.getAdministratorstates().add(mapAdministratorstate(c));
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

		String zoneApex = authorizedUser.getTdmxZoneInfo().getZoneRoot();
		if (zoneApex == null) {
			return response;
		}
		// try to constuct the UC given the data provided
		AgentCredential uc = credentialFactory.createUC(parameters.getUser().getUsercertificate(), parameters.getUser()
				.getDomaincertificate(), parameters.getUser().getRootcertificate());
		if (uc == null) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}

		// check that the UC credential exists
		AgentCredential existingCred = credentialService.findById(uc.getId());
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

		String zoneApex = checkDomainAuthorization(parameters.getAddress().getDomain(), response);
		if (zoneApex == null) {
			return response;
		}
		DomainID domainId = new DomainID(parameters.getAddress().getDomain(), zoneApex);
		// check if the domain exists already
		Domain domain = getDomainService().findById(domainId);
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		AddressID id = new AddressID(parameters.getAddress().getLocalname(), parameters.getAddress().getDomain(),
				zoneApex);
		// check if the domain exists already
		org.tdmx.lib.zone.domain.Address a = getAddressService().findById(id);
		if (a != null) {
			setError(ErrorCode.AddressExists, response);
			return response;
		}

		// create the address
		a = new org.tdmx.lib.zone.domain.Address(id);

		getAddressService().createOrUpdate(a);
		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "deleteChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteChannelAuthorization")
	public DeleteChannelAuthorizationResponse deleteChannelAuthorization(
			@WebParam(partName = "parameters", name = "deleteChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteChannelAuthorization parameters) {
		// TODO Auto-generated method stub
		return null;
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
	@WebResult(name = "getChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/getChannelAuthorization")
	public GetChannelAuthorizationResponse getChannelAuthorization(
			@WebParam(partName = "parameters", name = "getChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") GetChannelAuthorization parameters) {
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

		String zoneApex = authorizedUser.getTdmxZoneInfo().getZoneRoot();
		if (zoneApex == null) {
			return response;
		}
		// try to constuct the UC given the data provided
		AgentCredential uc = credentialFactory.createUC(parameters.getUser().getUsercertificate(), parameters.getUser()
				.getDomaincertificate(), parameters.getUser().getRootcertificate());
		if (uc == null) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}

		// check that the UC credential exists
		AgentCredential existingCred = credentialService.findById(uc.getId());
		if (existingCred == null) {
			setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}

		// delete the UC
		credentialService.delete(uc);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "modifyFlowTargetStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyFlowTargetState")
	public ModifyFlowTargetStateResponse modifyFlowTargetState(
			@WebParam(partName = "parameters", name = "modifyFlowTargetState", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") ModifyFlowTargetState parameters) {
		// TODO Auto-generated method stub
		return null;
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

		String zoneApex = checkDomainAuthorization(authorizedUser, parameters.getService().getDomain(), response);
		if (zoneApex == null) {
			return response;
		}

		// lookup existing service exists
		ServiceID serviceId = new ServiceID(parameters.getService().getServicename(), parameters.getService()
				.getDomain(), zoneApex);
		org.tdmx.lib.zone.domain.Service existingService = getServiceService().findById(serviceId);
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
	@WebResult(name = "getFlowStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/getFlowState")
	public GetFlowStateResponse getFlowState(
			@WebParam(partName = "parameters", name = "getFlowState", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") GetFlowState parameters) {
		// TODO Auto-generated method stub
		return null;
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

		String zoneApex = authorizedUser.getTdmxZoneInfo().getZoneRoot();
		if (zoneApex == null) {
			return response;
		}

		AddressSearchCriteria sc = new AddressSearchCriteria(mapPage(parameters.getPage()));
		if (authorizedUser.isTdmxDomainAdminCertificate()) {
			if (!StringUtils.hasText(parameters.getFilter().getDomain())) {
				// we fix the search to search only the DAC's domain.
				parameters.getFilter().setDomain(authorizedUser.getCommonName());
			}
		}
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			// we check that the provided domain is the DAC's domain.
			if (checkDomainAuthorization(authorizedUser, parameters.getFilter().getDomain(), response) == null) {
				return response;
			}
			sc.setDomainName(parameters.getFilter().getDomain());
		}
		sc.setLocalName(parameters.getFilter().getLocalname());
		List<org.tdmx.lib.zone.domain.Address> addresses = addressService.search(zoneApex, sc);
		for (org.tdmx.lib.zone.domain.Address a : addresses) {
			response.getAddresses().add(mapAddress(a));
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
		String zoneApex = checkZACAuthorization(response);
		if (zoneApex == null) {
			return response;
		}
		// try to constuct new DAC given the data provided
		AgentCredential dac = credentialFactory.createDAC(parameters.getAdministrator().getDomaincertificate(),
				parameters.getAdministrator().getRootcertificate());
		if (dac == null) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}
		if (parameters.getStatus() != null) {
			dac.setCredentialStatus(AgentCredentialStatus.valueOf(parameters.getStatus().value()));
		} else {
			dac.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		}

		// check that the Domain Exists
		DomainID id = new DomainID(dac.getDomainName(), dac.getId().getZoneApex());
		// check if the domain exists already
		Domain domain = getDomainService().findById(id);
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check that the DAC credential doesn't already exist
		AgentCredential existingCred = credentialService.findById(dac.getId());
		if (existingCred != null) {
			setError(ErrorCode.DomainAdministratorCredentialsExist, response);
			return response;
		}

		// create the DAC
		credentialService.createOrUpdate(dac);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "searchFlowTargetStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchFlowTargetState")
	public SearchFlowTargetStateResponse searchFlowTargetState(
			@WebParam(partName = "parameters", name = "searchFlowTargetState", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchFlowTargetState parameters) {
		// TODO Auto-generated method stub
		return null;
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

		String zoneApex = checkDomainAuthorization(authorizedUser, parameters.getService().getDomain(), response);
		if (zoneApex == null) {
			return response;
		}

		// lookup existing service exists
		ServiceID serviceId = new ServiceID(parameters.getService().getServicename(), parameters.getService()
				.getDomain(), zoneApex);
		org.tdmx.lib.zone.domain.Service existingService = getServiceService().findById(serviceId);
		if (existingService == null) {
			setError(ErrorCode.ServiceNotFound, response);
			return response;
		}

		// delete the existing service
		serviceService.delete(existingService);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "getFlowTargetStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/getFlowTargetState")
	public GetFlowTargetStateResponse getFlowTargetState(
			@WebParam(partName = "parameters", name = "getFlowTargetState", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") GetFlowTargetState parameters) {
		// TODO Auto-generated method stub
		return null;
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

		String zoneApex = authorizedUser.getTdmxZoneInfo().getZoneRoot();
		if (zoneApex == null) {
			return response;
		}

		ServiceSearchCriteria sc = new ServiceSearchCriteria(mapPage(parameters.getPage()));
		if (authorizedUser.isTdmxDomainAdminCertificate()) {
			if (!StringUtils.hasText(parameters.getFilter().getDomain())) {
				// we fix the search to search only the DAC's domain.
				parameters.getFilter().setDomain(authorizedUser.getCommonName());
			}
		}
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			// we check that the provided domain is the DAC's domain.
			if (checkDomainAuthorization(authorizedUser, parameters.getFilter().getDomain(), response) == null) {
				return response;
			}
			sc.setDomainName(parameters.getFilter().getDomain());
		}
		sc.setServiceName(parameters.getFilter().getServicename());
		List<org.tdmx.lib.zone.domain.Service> services = serviceService.search(zoneApex, sc);
		for (org.tdmx.lib.zone.domain.Service s : services) {
			response.getServicestates().add(mapService(s));
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

		String zoneApex = checkDomainAuthorization(parameters.getAddress().getDomain(), response);
		if (zoneApex == null) {
			return response;
		}

		// check if there are any UCs
		AgentCredentialSearchCriteria acSc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 1));
		acSc.setAddressName(parameters.getAddress().getLocalname());
		acSc.setDomainName(parameters.getAddress().getDomain());
		acSc.setType(AgentCredentialType.UC);
		List<AgentCredential> ucs = getCredentialService().search(zoneApex, acSc);
		if (ucs.size() > 0) {
			setError(ErrorCode.UserCredentialsExist, response);
			return response;
		}

		AddressID id = new AddressID(parameters.getAddress().getLocalname(), parameters.getAddress().getDomain(),
				zoneApex);
		// dont allow creation if we find the address exists already
		org.tdmx.lib.zone.domain.Address a = getAddressService().findById(id);
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
		PKIXCertificate authorizedUser = checkZACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		String zoneApex = authorizedUser.getTdmxZoneInfo().getZoneRoot();
		if (zoneApex == null) {
			return response;
		}
		// try to constuct the DAC given the data provided
		AgentCredential dac = credentialFactory.createDAC(parameters.getAdministrator().getDomaincertificate(),
				parameters.getAdministrator().getRootcertificate());
		if (dac == null) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}

		// check that the DAC credential exists
		AgentCredential existingCred = credentialService.findById(dac.getId());
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
	@WebResult(name = "searchFlowStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchFlowState")
	public SearchFlowStateResponse searchFlowState(
			@WebParam(partName = "parameters", name = "searchFlowState", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchFlowState parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "setChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/setChannelAuthorization")
	public SetChannelAuthorizationResponse setChannelAuthorization(
			@WebParam(partName = "parameters", name = "setChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SetChannelAuthorization parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "createUserResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createUser")
	public CreateUserResponse createUser(
			@WebParam(partName = "parameters", name = "createUser", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateUser parameters) {
		//

		CreateUserResponse response = new CreateUserResponse();
		PKIXCertificate authorizedUser = checkZACorDACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		String zoneApex = authorizedUser.getTdmxZoneInfo().getZoneRoot();
		if (zoneApex == null) {
			return response;
		}
		// try to constuct new UC given the data provided
		AgentCredential uc = credentialFactory.createUC(parameters.getUser().getUsercertificate(), parameters.getUser()
				.getDomaincertificate(), parameters.getUser().getRootcertificate());
		if (uc == null) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}
		if (parameters.getStatus() != null) {
			uc.setCredentialStatus(AgentCredentialStatus.valueOf(parameters.getStatus().value()));
		} else {
			uc.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		}

		// check that the Address Exists
		AddressID id = new AddressID(uc.getAddressName(), uc.getDomainName(), uc.getId().getZoneApex());
		// check if the address exists already
		org.tdmx.lib.zone.domain.Address address = getAddressService().findById(id);
		if (address == null) {
			setError(ErrorCode.AddressNotFound, response);
			return response;
		}

		// check that the UC credential doesn't already exist
		AgentCredential existingCred = credentialService.findById(uc.getId());
		if (existingCred != null) {
			setError(ErrorCode.UserCredentialsExist, response);
			return response;
		}

		// create the UC
		credentialService.createOrUpdate(uc);

		response.setSuccess(true);
		return response;
	}

	@Override
	@WebResult(name = "searchChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchChannelAuthorization")
	public SearchChannelAuthorizationResponse searchChannelAuthorization(
			@WebParam(partName = "parameters", name = "searchChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchChannelAuthorization parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "createServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createService")
	public CreateServiceResponse createService(
			@WebParam(partName = "parameters", name = "createService", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateService parameters) {
		CreateServiceResponse response = new CreateServiceResponse();

		String zoneApex = checkDomainAuthorization(parameters.getService().getDomain(), response);
		if (zoneApex == null) {
			return response;
		}

		DomainID domainId = new DomainID(parameters.getService().getDomain(), zoneApex);
		// check if the domain exists already
		Domain domain = getDomainService().findById(domainId);
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		ServiceID serviceId = new ServiceID(parameters.getService().getServicename(), parameters.getService()
				.getDomain(), zoneApex);
		// check if the service exists already
		org.tdmx.lib.zone.domain.Service service = getServiceService().findById(serviceId);
		if (service != null) {
			setError(ErrorCode.ServiceExists, response);
			return response;
		}

		// create the service
		org.tdmx.lib.zone.domain.Service s = new org.tdmx.lib.zone.domain.Service(serviceId);
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
		PKIXCertificate authorizedUser = checkZACAuthorized(response);
		if (authorizedUser == null) {
			return response;
		}

		String zoneApex = authorizedUser.getTdmxZoneInfo().getZoneRoot();
		if (zoneApex == null) {
			return response;
		}
		// try to constuct the DAC given the data provided
		AgentCredential dac = credentialFactory.createDAC(parameters.getAdministrator().getDomaincertificate(),
				parameters.getAdministrator().getRootcertificate());
		if (dac == null) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}

		// check that the DAC credential exists
		AgentCredential existingCred = credentialService.findById(dac.getId());
		if (existingCred == null) {
			setError(ErrorCode.DomainAdministratorCredentialNotFound, response);
			return response;
		}

		// delete the existing DAC
		credentialService.delete(existingCred);

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

	// private String checkDomainAuthorization(String domain, Acknowledge ack) {
	// PKIXCertificate user = getAgentService().getAuthenticatedAgent();
	//
	// ErrorCode error = null;
	// if (!StringUtils.hasText(domain)) {
	// // if no domain is passed in then the authenticated agent needs to be a ZAC
	// if (user.isTdmxZoneAdminCertificate()) {
	// return user.getTdmxZoneInfo().getZoneRoot();
	// }
	// errorDescription = "Agent is not a ZAC.";
	// } else {
	// if (!domain.toUpperCase().equals(domain)) {
	// errorDescription = "Domain not normalized to uppercase.";
	// } else if (user.isTdmxZoneAdminCertificate()) {
	// String zoneApex = user.getTdmxZoneInfo().getZoneRoot();
	// if (domain.endsWith(zoneApex)) {
	// return zoneApex;
	// }
	// errorDescription = "ZAC only authorized on own subdomains.";
	// } else if (user.isTdmxDomainAdminCertificate()) {
	// // a DAC needs to match the domain exactly, doesn't administer subdomains
	// String dacDomainName = user.getCommonName();
	// if (dacDomainName.equals(domain)) {
	// return user.getTdmxZoneInfo().getZoneRoot();
	// }
	// errorDescription = "DAC only authorized on own domain.";
	// } else {
	// errorDescription = "Agent is not a ZAC or DAC.";
	// }
	// }
	// setError(401, errorDescription, ack);
	// return null;
	// }

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

	private PKIXCertificate checkZACAuthorized(Acknowledge ack) {
		PKIXCertificate user = getAgentService().getAuthenticatedAgent();
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

	/**
	 * Checks the AuthenticatedAgent is a ZAC.
	 * 
	 * @param ack
	 * @return null if not authorized, setting ack.Error, else the zoneApex.
	 */
	private String checkZACAuthorization(Acknowledge ack) {
		PKIXCertificate user = checkZACAuthorized(ack);
		if (user == null) {
			return null;
		}
		return user.getTdmxZoneInfo().getZoneRoot();
	}

	private String checkDomainAuthorization(PKIXCertificate authorizedAgent, String domain, Acknowledge ack) {
		String zoneApex = authorizedAgent.getTdmxZoneInfo().getZoneRoot();

		if (!StringUtils.isSuffix(domain, zoneApex)) {
			setError(ErrorCode.OutOfZoneAccess, ack);
			return null;
		}

		// domain must match domain administrator of authorized user
		if (authorizedAgent.isTdmxDomainAdminCertificate() && !authorizedAgent.getCommonName().equals(domain)) {
			setError(ErrorCode.OutOfDomainAccess, ack);
			return null;
		}
		return zoneApex;

	}

	/**
	 * Checks the AuthenticatedAgent is a ZAC authorized to perform administration on the domain, and return the agent's
	 * zoneApex. If the agent is not authorized then the acknowledge's Error info will be set and null returned.
	 * 
	 * @param ack
	 * @return null if not authorized, setting ack.Error, else the zoneApex.
	 */
	private String checkZACDomainAuthorization(String domain, Acknowledge ack) {
		if (!checkDomain(domain, ack)) {
			return null;
		}

		PKIXCertificate user = checkZACAuthorized(ack);
		if (user == null) {
			return null;
		}
		return checkDomainAuthorization(user, domain, ack);
	}

	/**
	 * Checks the AuthenticatedAgent is a ZAC or DAC, and return the agent's zoneApex. If the agent is not authorized
	 * then the acknowledge's Error info will be set and null returned.
	 * 
	 * @param ack
	 * @return null if not authorized, setting ack.Error, else the zoneApex.
	 */
	private String checkDomainAuthorization(String domain, Acknowledge ack) {
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

	private PageSpecifier mapPage(Page p) {
		return new PageSpecifier(p.getNumber(), p.getSize());
	}

	private Userstate mapUserstate(AgentCredential cred) {
		User u = new User();
		u.setUsercertificate(cred.getPublicKey().getX509Encoded());
		u.setDomaincertificate(cred.getIssuerPublicKey().getX509Encoded());
		u.setRootcertificate(cred.getZoneRootPublicKey().getX509Encoded());

		Userstate us = new Userstate();
		us.setStatus(CredentialStatus.fromValue(cred.getCredentialStatus().name()));
		us.setUser(u);
		us.setWhitelist(new IpAddressList()); // TODO ipwhitelist
		return us;
	}

	private Administratorstate mapAdministratorstate(AgentCredential cred) {
		Administrator u = new Administrator();
		u.setDomaincertificate(cred.getIssuerPublicKey().getX509Encoded());
		u.setRootcertificate(cred.getZoneRootPublicKey().getX509Encoded());

		Administratorstate us = new Administratorstate();
		us.setStatus(CredentialStatus.fromValue(cred.getCredentialStatus().name()));
		us.setAdministrator(u);
		us.setWhitelist(new IpAddressList()); // TODO ipwhitelist
		return us;
	}

	private Address mapAddress(org.tdmx.lib.zone.domain.Address address) {
		Address a = new Address();
		a.setDomain(address.getId().getDomainName());
		a.setLocalname(address.getId().getLocalName());
		return a;
	}

	private Servicestate mapService(org.tdmx.lib.zone.domain.Service service) {
		Service s = new Service();
		s.setDomain(service.getId().getDomainName());
		s.setServicename(service.getId().getServiceName());

		Servicestate ss = new Servicestate();
		ss.setService(s);
		ss.setConcurrencyLimit(service.getConcurrencyLimit());
		return ss;
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
}
