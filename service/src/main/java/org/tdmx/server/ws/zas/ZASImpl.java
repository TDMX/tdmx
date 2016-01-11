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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Error;
import org.tdmx.core.api.v01.msg.Address;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Service;
import org.tdmx.core.api.v01.msg.UserIdentity;
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
import org.tdmx.core.api.v01.zas.ModifyIpZone;
import org.tdmx.core.api.v01.zas.ModifyIpZoneResponse;
import org.tdmx.core.api.v01.zas.ModifyUser;
import org.tdmx.core.api.v01.zas.ModifyUserResponse;
import org.tdmx.core.api.v01.zas.SearchAddress;
import org.tdmx.core.api.v01.zas.SearchAddressResponse;
import org.tdmx.core.api.v01.zas.SearchAdministrator;
import org.tdmx.core.api.v01.zas.SearchAdministratorResponse;
import org.tdmx.core.api.v01.zas.SearchChannel;
import org.tdmx.core.api.v01.zas.SearchChannelResponse;
import org.tdmx.core.api.v01.zas.SearchDestination;
import org.tdmx.core.api.v01.zas.SearchDestinationResponse;
import org.tdmx.core.api.v01.zas.SearchDomain;
import org.tdmx.core.api.v01.zas.SearchDomainResponse;
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
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.Destination;
import org.tdmx.lib.zone.domain.DestinationSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.AgentCredentialValidator;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.ChannelService.SetAuthorizationOperationStatus;
import org.tdmx.lib.zone.service.ChannelService.SetAuthorizationResultHolder;
import org.tdmx.lib.zone.service.DestinationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.server.ros.client.RelayClientService;
import org.tdmx.server.ros.client.RelayStatus;
import org.tdmx.server.session.SessionCertificateInvalidationService;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.ApiValidator;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedClientLookupService;
import org.tdmx.server.ws.security.service.AuthorizedSessionLookupService;

public class ZASImpl implements ZAS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ZASImpl.class);

	private AuthorizedSessionLookupService<ZASServerSession> authorizedSessionService;
	private AuthenticatedClientLookupService authenticatedClientService;
	private SessionCertificateInvalidationService sessionInvalidationService;
	private RelayClientService relayClientService;

	private DomainService domainService;
	private AddressService addressService;
	private ServiceService serviceService;
	private ChannelService channelService;
	private DestinationService destinationService;

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
	public CreateDomainResponse createDomain(CreateDomain parameters) {
		final CreateDomainResponse response = new CreateDomainResponse();

		final ZASServerSession session = getZACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		String domainName = checkZoneBoundDomainAuthorization(parameters.getDomain(), zone, response);
		if (domainName == null) {
			return response;
		}

		// check if the domain exists already
		final Domain existingDomain = getDomainService().findByName(zone, domainName);
		if (existingDomain != null) {
			setError(ErrorCode.DomainExists, response);
			return response;
		}

		// create the domain
		final Domain domain = new Domain(zone, domainName);

		getDomainService().createOrUpdate(domain);
		response.setSuccess(true);
		return response;
	}

	@Override
	public DeleteDomainResponse deleteDomain(DeleteDomain parameters) {
		final DeleteDomainResponse response = new DeleteDomainResponse();

		final ZASServerSession session = getZACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();

		final String domainName = checkZoneBoundDomainAuthorization(parameters.getDomain(), zone, response);
		if (domainName == null) {
			return response;
		}
		// check if the domain exists already
		final Domain domain = getDomainService().findByName(zone, domainName);
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check the domain can be deleted if it has no credentials
		final AgentCredentialSearchCriteria dcSc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 1));
		dcSc.setDomainName(parameters.getDomain());
		dcSc.setType(AgentCredentialType.DAC);
		final List<AgentCredential> credentials = getCredentialService().search(zone, dcSc);
		if (!credentials.isEmpty()) {
			setError(ErrorCode.DomainAdministratorCredentialsExist, response);
			return response;
		}

		// and no addresses
		final AddressSearchCriteria sac = new AddressSearchCriteria(new PageSpecifier(0, 1));
		sac.setDomainName(domain.getDomainName());
		final List<org.tdmx.lib.zone.domain.Address> addresses = getAddressService().search(zone, sac);
		if (!addresses.isEmpty()) {
			setError(ErrorCode.AddressesExist, response);
			return response;
		}

		// and no services
		final ServiceSearchCriteria ssc = new ServiceSearchCriteria(new PageSpecifier(0, 1));
		ssc.setDomain(domain);
		final List<org.tdmx.lib.zone.domain.Service> services = getServiceService().search(zone, ssc);
		if (!services.isEmpty()) {
			setError(ErrorCode.ServicesExist, response);
			return response;
		}

		getDomainService().delete(domain);
		response.setSuccess(true);
		return response;
	}

	@Override
	public SearchDomainResponse searchDomain(SearchDomain parameters) {
		final SearchDomainResponse response = new SearchDomainResponse();

		final ZASServerSession session = getZACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();

		final DomainSearchCriteria criteria = new DomainSearchCriteria(a2d.mapPage(parameters.getPage()));
		// make sure client stipulates a domain which is within the zone.
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			if (!StringUtils.isSuffix(parameters.getFilter().getDomain(), zone.getZoneApex())) {
				setError(ErrorCode.OutOfZoneAccess, response);
				return response;
			}
			criteria.setDomainName(parameters.getFilter().getDomain());
		}
		final List<Domain> domains = domainService.search(zone, criteria);
		for (Domain d : domains) {
			response.getDomains().add(d.getDomainName());
		}
		response.setPage(parameters.getPage());
		response.setSuccess(true);
		return response;
	}

	@Override
	public SearchUserResponse searchUser(SearchUser parameters) {
		final SearchUserResponse response = new SearchUserResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		if (parameters.getFilter().getUserIdentity() != null) {
			// if a user credential is provided then it't not so much a search as a lookup
			final UserIdentity userIdentity = validator.checkUserIdentity(parameters.getFilter().getUserIdentity(),
					response);
			if (userIdentity == null) {
				return response;
			}

			final AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(
					userIdentity.getUsercertificate(), userIdentity.getDomaincertificate(),
					userIdentity.getRootcertificate());
			if (uc == null || AgentCredentialType.UC != uc.getCredentialType()) {
				setError(ErrorCode.InvalidUserCredentials, response);
				return response;
			}
			// we check that the provided domain is the DAC's domain.
			if (checkDomainBoundDomainAuthorization(uc.getDomainName(), authorizedDomainName, zone, response) == null) {
				return response;
			}
			final AgentCredential c = credentialService.findByFingerprint(uc.getFingerprint());
			if (c != null) {
				response.getUsers().add(d2a.mapUser(c));
			}
		} else {
			final AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(
					a2d.mapPage(parameters.getPage()));
			if (authorizedDomainName != null && !StringUtils.hasText(parameters.getFilter().getDomain())) {
				// we fix the search to search only the DAC's domain.
				parameters.getFilter().setDomain(authorizedDomainName);
			}
			if (StringUtils.hasText(parameters.getFilter().getDomain())) {
				// we check that the provided domain is the DAC's domain.
				if (checkDomainBoundDomainAuthorization(parameters.getFilter().getDomain(), authorizedDomainName, zone,
						response) == null) {
					return response;
				}
				sc.setDomainName(parameters.getFilter().getDomain());
			}
			sc.setAddressName(parameters.getFilter().getLocalname());
			if (parameters.getFilter().getStatus() != null) {
				sc.setStatus(AgentCredentialStatus.valueOf(parameters.getFilter().getStatus().value()));
			}
			sc.setType(AgentCredentialType.UC);
			final List<AgentCredential> credentials = credentialService.search(zone, sc);
			for (AgentCredential c : credentials) {
				response.getUsers().add(d2a.mapUser(c));
			}
		}
		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	public SearchAdministratorResponse searchAdministrator(SearchAdministrator parameters) {
		SearchAdministratorResponse response = new SearchAdministratorResponse();

		final ZASServerSession session = getZACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();

		if (parameters.getFilter().getAdministratorIdentity() != null) {
			final AdministratorIdentity dacIdentity = validator
					.checkAdministratorIdentity(parameters.getFilter().getAdministratorIdentity(), response);
			if (dacIdentity == null) {
				return response;
			}

			// if a DAC credential is provided then it't not so much a search as a lookup
			final AgentCredentialDescriptor dac = credentialFactory
					.createAgentCredential(dacIdentity.getDomaincertificate(), dacIdentity.getRootcertificate());
			if (dac == null || AgentCredentialType.DAC != dac.getCredentialType()) {
				setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
				return response;
			}
			if (checkZoneBoundDomainAuthorization(dac.getDomainName(), zone, response) == null) {
				return response;
			}
			final AgentCredential c = credentialService.findByFingerprint(dac.getFingerprint());
			if (c != null) {
				response.getAdministrators().add(d2a.mapAdministrator(c));
			}
		} else {
			final AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(
					a2d.mapPage(parameters.getPage()));
			if (StringUtils.hasText(parameters.getFilter().getDomain())) {
				// we check that the provided domain is the ZAC's root domain.
				if (checkZoneBoundDomainAuthorization(parameters.getFilter().getDomain(), zone, response) == null) {
					return response;
				}
				sc.setDomainName(parameters.getFilter().getDomain());
			}
			if (parameters.getFilter().getStatus() != null) {
				sc.setStatus(AgentCredentialStatus.valueOf(parameters.getFilter().getStatus().value()));
			}
			sc.setType(AgentCredentialType.DAC);
			final List<AgentCredential> credentials = credentialService.search(zone, sc);
			for (AgentCredential c : credentials) {
				response.getAdministrators().add(d2a.mapAdministrator(c));
			}
		}
		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	public ModifyUserResponse modifyUser(ModifyUser parameters) {
		final ModifyUserResponse response = new ModifyUserResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final UserIdentity userIdentity = validator.checkUserIdentity(parameters.getUserIdentity(), response);
		if (userIdentity == null) {
			return response;
		}

		// try to constuct the UC given the data provided
		final AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(userIdentity.getUsercertificate(),
				userIdentity.getDomaincertificate(), userIdentity.getRootcertificate());
		if (uc == null || AgentCredentialType.UC != uc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}
		// we check that the provided domain is the DAC's domain.
		if (checkDomainBoundDomainAuthorization(uc.getDomainName(), authorizedDomainName, zone, response) == null) {
			return response;
		}

		// check that the UC credential exists
		final AgentCredential existingCred = credentialService.findByFingerprint(uc.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}
		if (parameters.getStatus() != null) {
			existingCred.setCredentialStatus(AgentCredentialStatus.valueOf(parameters.getStatus().value()));
		}

		// update the existing UC
		credentialService.createOrUpdate(existingCred);

		if (AgentCredentialStatus.ACTIVE != existingCred.getCredentialStatus()) {
			// deactivate the suspended certificate from all sessions active in the PartitionControlService
			sessionInvalidationService.invalidateCertificate(existingCred.getPublicCertificate());
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public SearchIpZoneResponse searchIpZone(SearchIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CreateAddressResponse createAddress(CreateAddress parameters) {
		final CreateAddressResponse response = new CreateAddressResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final Address address = validator.checkAddress(parameters.getAddress(), response);
		if (address == null) {
			return response;
		}
		final String domainName = checkDomainBoundDomainAuthorization(parameters.getAddress().getDomain(),
				authorizedDomainName, zone, response);
		if (domainName == null) {
			return response;
		}
		// check if the domain exists already
		Domain domain = getDomainService().findByName(zone, domainName);
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check if the domain exists already
		final org.tdmx.lib.zone.domain.Address existingAddress = getAddressService().findByName(domain,
				parameters.getAddress().getLocalname());
		if (existingAddress != null) {
			setError(ErrorCode.AddressExists, response);
			return response;
		}

		// create the address
		final org.tdmx.lib.zone.domain.Address newAddress = new org.tdmx.lib.zone.domain.Address(domain,
				parameters.getAddress().getLocalname());

		getAddressService().createOrUpdate(newAddress);
		response.setSuccess(true);
		return response;
	}

	@Override
	public DeleteChannelAuthorizationResponse deleteChannelAuthorization(DeleteChannelAuthorization parameters) {
		final DeleteChannelAuthorizationResponse response = new DeleteChannelAuthorizationResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final String domainName = parameters.getDomain();
		if (checkDomainBoundDomainAuthorization(domainName, authorizedDomainName, zone, response) == null) {
			return response;
		}

		// validate all channel and provided permission fields are specified.
		final Channel channel = validator.checkChannel(parameters.getChannel(), response);
		if (channel == null) {
			return response;
		}

		final Domain existingDomain = getDomainService().findByName(zone, domainName);
		if (existingDomain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		final ChannelAuthorization existingCA = channelService.findByChannel(zone, existingDomain,
				a2d.mapChannelOrigin(channel.getOrigin()), a2d.mapChannelDestination(channel.getDestination()));
		if (existingCA == null) {
			setError(ErrorCode.ChannelAuthorizationNotFound, response);
			return response;
		}
		// deleting the Channel will cascade to automatically delete all ChannelFlowMessages
		channelService.delete(existingCA.getChannel());

		response.setSuccess(true);
		return response;
	}

	@Override
	public IncidentResponse incident(Incident parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CreateIpZoneResponse createIpZone(CreateIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeleteIpZoneResponse deleteIpZone(DeleteIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReportResponse report(Report parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeleteUserResponse deleteUser(DeleteUser parameters) {
		final DeleteUserResponse response = new DeleteUserResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		// try to construct the UC given the data provided
		final AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(
				parameters.getUserIdentity().getUsercertificate(), parameters.getUserIdentity().getDomaincertificate(),
				parameters.getUserIdentity().getRootcertificate());
		if (uc == null || AgentCredentialType.UC != uc.getCredentialType()) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}
		if (checkDomainBoundDomainAuthorization(uc.getDomainName(), authorizedDomainName, zone, response) == null) {
			return response;
		}

		// check that the UC credential exists
		final AgentCredential existingCred = credentialService.findByFingerprint(uc.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.UserCredentialNotFound, response);
			return response;
		}

		// delete the UC
		credentialService.delete(existingCred);

		// deactivate the certificate from all sessions active in the PartitionControlService
		sessionInvalidationService.invalidateCertificate(existingCred.getPublicCertificate());

		response.setSuccess(true);
		return response;
	}

	@Override
	public ModifyIpZoneResponse modifyIpZone(ModifyIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchAddressResponse searchAddress(SearchAddress parameters) {
		SearchAddressResponse response = new SearchAddressResponse();
		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final AddressSearchCriteria sc = new AddressSearchCriteria(a2d.mapPage(parameters.getPage()));
		if (!StringUtils.hasText(parameters.getFilter().getDomain()) && session.isDAC()) {
			// we fix the search to search only the DAC's domain.
			parameters.getFilter().setDomain(authorizedDomainName);
		}
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			// we check that the provided domain is the DAC's domain.
			if (checkDomainBoundDomainAuthorization(parameters.getFilter().getDomain(), authorizedDomainName, zone,
					response) == null) {
				return response;
			}
			sc.setDomainName(parameters.getFilter().getDomain());
		}
		sc.setLocalName(parameters.getFilter().getLocalname());

		final List<org.tdmx.lib.zone.domain.Address> addresses = addressService.search(zone, sc);
		for (org.tdmx.lib.zone.domain.Address a : addresses) {
			response.getAddresses().add(d2a.mapAddress(a));
		}
		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	public CreateAdministratorResponse createAdministrator(CreateAdministrator parameters) {
		final CreateAdministratorResponse response = new CreateAdministratorResponse();

		final ZASServerSession session = getZACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();

		// try to construct new DAC given the data provided
		final AdministratorIdentity dacIdentity = validator
				.checkAdministratorIdentity(parameters.getAdministratorIdentity(), response);
		if (dacIdentity == null) {
			return response;
		}

		final AgentCredentialDescriptor dac = credentialFactory
				.createAgentCredential(dacIdentity.getDomaincertificate(), dacIdentity.getRootcertificate());
		if (dac == null) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}
		if (checkZoneBoundDomainAuthorization(dac.getDomainName(), zone, response) == null) {
			return response;
		}
		if (!credentialValidator.isValid(dac)) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}

		// check if the domain exists already
		final Domain existingDomain = getDomainService().findByName(zone, dac.getDomainName());
		if (existingDomain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check that the DAC credential doesn't already exist
		final AgentCredential existingCred = credentialService.findByFingerprint(dac.getFingerprint());
		if (existingCred != null) {
			setError(ErrorCode.DomainAdministratorCredentialsExist, response);
			return response;
		}

		final AgentCredential newCred = new AgentCredential(zone, existingDomain, dac);
		if (parameters.getStatus() != null) {
			newCred.setCredentialStatus(AgentCredentialStatus.valueOf(parameters.getStatus().value()));
		}

		// TODO serialNumber > existing User creds serialNumber on the Address

		// create the DAC
		credentialService.createOrUpdate(newCred);

		response.setSuccess(true);
		return response;
	}

	@Override
	public DeleteServiceResponse deleteService(DeleteService parameters) {
		final DeleteServiceResponse response = new DeleteServiceResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final Service service = validator.checkService(parameters.getService(), response);
		if (service == null) {
			return response;
		}
		if (checkDomainBoundDomainAuthorization(service.getDomain(), authorizedDomainName, zone, response) == null) {
			return response;
		}

		final Domain existingDomain = getDomainService().findByName(zone, parameters.getService().getDomain());
		if (existingDomain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}
		// lookup existing service exists
		final org.tdmx.lib.zone.domain.Service existingService = getServiceService().findByName(existingDomain,
				parameters.getService().getServicename());
		if (existingService == null) {
			setError(ErrorCode.ServiceNotFound, response);
			return response;
		}

		// prohibit deleteService change if we have ChannelAuthorizations
		final ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
		sc.setDomain(existingDomain);
		sc.getDestination().setServiceName(existingService.getServiceName());
		final List<org.tdmx.lib.zone.domain.Channel> channels = channelService.search(zone, sc);
		if (!channels.isEmpty()) {
			setError(ErrorCode.ChannelAuthorizationExist, response);
			return response;
		}

		// delete any Destinations
		boolean moreDestinations = true;
		while (moreDestinations) {
			final DestinationSearchCriteria casc = new DestinationSearchCriteria(new PageSpecifier(0, getBatchSize()));
			casc.setService(existingService);

			final List<Destination> destinations = destinationService.search(zone, casc);
			for (Destination d : destinations) {
				destinationService.delete(d);
			}
			if (destinations.isEmpty()) {
				moreDestinations = false;
			}
		}

		// delete the existing service
		serviceService.delete(existingService);

		response.setSuccess(true);
		return response;
	}

	@Override
	public SearchServiceResponse searchService(SearchService parameters) {
		final SearchServiceResponse response = new SearchServiceResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final ServiceSearchCriteria sc = new ServiceSearchCriteria(a2d.mapPage(parameters.getPage()));
		if (!StringUtils.hasText(parameters.getFilter().getDomain()) && session.isDAC()) {
			// we fix the search to search only the DAC's domain.
			parameters.getFilter().setDomain(authorizedDomainName);
		}
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			// we check that the provided domain is the DAC's domain.
			if (checkDomainBoundDomainAuthorization(parameters.getFilter().getDomain(), authorizedDomainName, zone,
					response) == null) {
				return response;
			}
			sc.setDomainName(parameters.getFilter().getDomain());
		}
		sc.setServiceName(parameters.getFilter().getServicename());

		final List<org.tdmx.lib.zone.domain.Service> services = serviceService.search(zone, sc);
		for (org.tdmx.lib.zone.domain.Service s : services) {
			response.getServices().add(d2a.mapService(s));
		}
		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	public DeleteAddressResponse deleteAddress(DeleteAddress parameters) {
		final DeleteAddressResponse response = new DeleteAddressResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final Address address = validator.checkAddress(parameters.getAddress(), response);
		if (address == null) {
			return response;
		}
		final String domainName = checkDomainBoundDomainAuthorization(parameters.getAddress().getDomain(),
				authorizedDomainName, zone, response);
		if (domainName == null) {
			return response;
		}

		// check if there are any UCs
		final AgentCredentialSearchCriteria acSc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 1));
		acSc.setAddressName(address.getLocalname());
		acSc.setDomainName(domainName);
		acSc.setType(AgentCredentialType.UC);
		final List<AgentCredential> ucs = getCredentialService().search(zone, acSc);
		if (!ucs.isEmpty()) {
			setError(ErrorCode.UserCredentialsExist, response);
			return response;
		}

		final Domain domain = getDomainService().findByName(zone, parameters.getAddress().getDomain());
		if (domain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}
		// dont allow creation if we find the address exists already
		final org.tdmx.lib.zone.domain.Address existingAddress = getAddressService().findByName(domain,
				address.getLocalname());
		if (existingAddress == null) {
			setError(ErrorCode.AddressNotFound, response);
			return response;
		}

		// delete any Destinations for any service
		boolean moreDestinations = true;
		while (moreDestinations) {
			final DestinationSearchCriteria casc = new DestinationSearchCriteria(new PageSpecifier(0, getBatchSize()));
			casc.setAddress(existingAddress);

			final List<Destination> destinations = destinationService.search(zone, casc);
			for (Destination d : destinations) {
				destinationService.delete(d);
			}
			if (destinations.isEmpty()) {
				moreDestinations = false;
			}
		}

		// delete the address
		getAddressService().delete(existingAddress);
		response.setSuccess(true);
		return response;
	}

	@Override
	public ModifyAdministratorResponse modifyAdministrator(ModifyAdministrator parameters) {
		final ModifyAdministratorResponse response = new ModifyAdministratorResponse();

		final ZASServerSession session = getZACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();

		// try to constuct the DAC given the data provided
		final AdministratorIdentity dacIdentity = validator
				.checkAdministratorIdentity(parameters.getAdministratorIdentity(), response);
		if (dacIdentity == null) {
			return response;
		}

		final AgentCredentialDescriptor dac = credentialFactory
				.createAgentCredential(dacIdentity.getDomaincertificate(), dacIdentity.getRootcertificate());
		if (dac == null || AgentCredentialType.DAC != dac.getCredentialType()) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}
		if (checkZoneBoundDomainAuthorization(dac.getDomainName(), zone, response) == null) {
			return response;
		}

		// check that the DAC credential exists
		final AgentCredential existingCred = credentialService.findByFingerprint(dac.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.DomainAdministratorCredentialNotFound, response);
			return response;
		}
		if (parameters.getStatus() != null) {
			existingCred.setCredentialStatus(AgentCredentialStatus.valueOf(parameters.getStatus().value()));
		}
		// update the DAC
		credentialService.createOrUpdate(existingCred);

		if (AgentCredentialStatus.ACTIVE != existingCred.getCredentialStatus()) {
			// deactivate the suspended certificate from all sessions active in the PartitionControlService
			sessionInvalidationService.invalidateCertificate(existingCred.getPublicCertificate());
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public SetChannelAuthorizationResponse setChannelAuthorization(SetChannelAuthorization parameters) {
		SetChannelAuthorizationResponse response = new SetChannelAuthorizationResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final String domainName = parameters.getDomain();
		if (!StringUtils.hasText(domainName)) {
			// must have a domain
			setError(ErrorCode.MissingDomain, response);
			return response;
		}
		if (checkDomainBoundDomainAuthorization(domainName, authorizedDomainName, zone, response) == null) {
			return response;
		}

		// validate all channel and provided permission fields are specified.
		final Currentchannelauthorization ca = validator
				.checkChannelauthorization(parameters.getCurrentchannelauthorization(), response);
		if (ca == null) {
			return response;
		}
		// check the signature of the current ca is ok
		if (!SignatureUtils.checkChannelAuthorizationSignature(ca)) {
			setError(ErrorCode.InvalidSignatureChannelAuthorization, response);
			return response;
		}
		final AgentCredentialDescriptor signingDAC = credentialFactory.createAgentCredential(
				ca.getAdministratorsignature().getAdministratorIdentity().getDomaincertificate(),
				ca.getAdministratorsignature().getAdministratorIdentity().getRootcertificate());
		if (signingDAC == null) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}
		if (!domainName.equals(signingDAC.getDomainName())) {
			setError(ErrorCode.ChannelAuthorizationSignerDomainMismatch, response);
			return response;
		}

		// check that the channel origin or channel destination matches the ca's domain
		if (!(domainName.equals(ca.getChannel().getOrigin().getDomain())
				|| domainName.equals(ca.getChannel().getDestination().getDomain()))) {
			setError(ErrorCode.ChannelAuthorizationDomainMismatch, response);
			return response;
		}
		// note if the domain matches both send and recv then we validate both

		if (domainName.equals(ca.getChannel().getOrigin().getDomain())) {
			// if the origin domain matches the domain - check that we have a send permission and that the signature is
			// ok

			if (ca.getOriginPermission() == null) {
				setError(ErrorCode.MissingEndpointPermission, response);
				return response;
			}
			if (!SignatureUtils.checkEndpointPermissionSignature(ca.getChannel(), ca.getOriginPermission())) {
				setError(ErrorCode.InvalidSignatureEndpointPermission, response);
				return response;
			}
			final AgentCredentialDescriptor permissionDAC = credentialFactory.createAgentCredential(
					ca.getOriginPermission().getAdministratorsignature().getAdministratorIdentity()
							.getDomaincertificate(),
					ca.getOriginPermission().getAdministratorsignature().getAdministratorIdentity()
							.getRootcertificate());
			if (permissionDAC == null) {
				setError(ErrorCode.InvalidOriginPermissionAdministratorCredentials, response);
				return response;
			}
			// check that the signer of the permission's domain matches the origin's domain
			if (!domainName.equals(permissionDAC.getDomainName())) {
				setError(ErrorCode.OriginPermissionSignerDomainMismatch, response);
				return response;
			}
		}

		if (domainName.equals(ca.getChannel().getDestination().getDomain())) {
			// if the destination domain matches the ca's domain - check that we have a recv permission and that the
			// signature is ok

			if (ca.getDestinationPermission() == null) {
				setError(ErrorCode.MissingEndpointPermission, response);
				return response;
			}
			if (!SignatureUtils.checkEndpointPermissionSignature(ca.getChannel(), ca.getDestinationPermission())) {
				setError(ErrorCode.InvalidSignatureEndpointPermission, response);
				return response;
			}
			final AgentCredentialDescriptor permissionDAC = credentialFactory.createAgentCredential(
					ca.getDestinationPermission().getAdministratorsignature().getAdministratorIdentity()
							.getDomaincertificate(),
					ca.getDestinationPermission().getAdministratorsignature().getAdministratorIdentity()
							.getRootcertificate());
			if (permissionDAC == null) {
				setError(ErrorCode.InvalidDestinationPermissionAdministratorCredentials, response);
				return response;
			}
			// check that the signer of the permission's domain matches the origin's domain
			if (!domainName.equals(permissionDAC.getDomainName())) {
				setError(ErrorCode.DestinationPermissionSignerDomainMismatch, response);
				return response;
			}
		}

		final Domain existingDomain = getDomainService().findByName(zone, domainName);
		if (existingDomain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// we construct a detached channel authorization from the provided request data

		final SetAuthorizationResultHolder operationStatus = channelService.setAuthorization(zone, existingDomain,
				a2d.mapChannelOrigin(ca.getChannel().getOrigin()),
				a2d.mapChannelDestination(ca.getChannel().getDestination()), a2d.mapChannelAuthorization(ca));
		if (operationStatus.status != null) {
			setError(mapSetAuthorizationOperationStatus(operationStatus.status), response);
			return response;
		}
		if (operationStatus.channelAuthorization != null) {
			// the channelAuthorization also has the channel object fetched, everything detached
			if (operationStatus.channelAuthorization.getProcessingState().getStatus() == ProcessingStatus.PENDING) {
				// initiate transfer of send/recv auth to other party ( processing state )
				// by caller, depending on whether processingstate is PENDING.
				// we don't cache the rosTcpAddress and get it each time from the PCS since the ZAS session is bound to
				// the domain and not the channel
				RelayStatus rs = relayClientService.relayChannelAuthorization(null, zone, existingDomain,
						operationStatus.channelAuthorization.getChannel(), operationStatus.channelAuthorization);
				if (!rs.isSuccess()) {
					// TODO reset the relay processing status of the CA to error
				}
			}
		}

		response.setSuccess(true);
		return response;
	}

	@Override
	public CreateUserResponse createUser(CreateUser parameters) {
		final CreateUserResponse response = new CreateUserResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		// try to construct new UC given the data provided
		final AgentCredentialDescriptor uc = credentialFactory.createAgentCredential(
				parameters.getUserIdentity().getUsercertificate(), parameters.getUserIdentity().getDomaincertificate(),
				parameters.getUserIdentity().getRootcertificate());
		if (uc == null) {
			setError(ErrorCode.InvalidUserCredentials, response);
			return response;
		}
		if (checkDomainBoundDomainAuthorization(uc.getDomainName(), authorizedDomainName, zone, response) == null) {
			return response;
		}

		// check if the domain exists already
		final Domain existingDomain = getDomainService().findByName(zone, uc.getDomainName());
		if (existingDomain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check if the address exists already
		final org.tdmx.lib.zone.domain.Address address = getAddressService().findByName(existingDomain,
				uc.getAddressName());
		if (address == null) {
			setError(ErrorCode.AddressNotFound, response);
			return response;
		}

		// check that the UC credential doesn't already exist
		final AgentCredential existingCred = credentialService.findByFingerprint(uc.getFingerprint());
		if (existingCred != null) {
			setError(ErrorCode.UserCredentialsExist, response);
			return response;
		}

		final AgentCredential newCred = new AgentCredential(zone, existingDomain, address, uc);
		if (parameters.getStatus() != null) {
			newCred.setCredentialStatus(AgentCredentialStatus.valueOf(parameters.getStatus().value()));
		}

		// TODO serialNumber > existing User creds serialNumber on the Address

		// create the UC
		credentialService.createOrUpdate(newCred);

		response.setSuccess(true);
		return response;
	}

	@Override
	public CreateServiceResponse createService(CreateService parameters) {
		final CreateServiceResponse response = new CreateServiceResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final Service service = validator.checkService(parameters.getService(), response);
		if (service == null) {
			return response;
		}
		if (checkDomainBoundDomainAuthorization(service.getDomain(), authorizedDomainName, zone, response) == null) {
			return response;
		}

		// check if the domain exists already
		final Domain existingDomain = getDomainService().findByName(zone, service.getDomain());
		if (existingDomain == null) {
			setError(ErrorCode.DomainNotFound, response);
			return response;
		}

		// check if the service exists already
		final org.tdmx.lib.zone.domain.Service existingService = getServiceService().findByName(existingDomain,
				service.getServicename());
		if (existingService != null) {
			setError(ErrorCode.ServiceExists, response);
			return response;
		}

		// create the service
		final org.tdmx.lib.zone.domain.Service newService = new org.tdmx.lib.zone.domain.Service(existingDomain,
				service.getServicename());

		getServiceService().createOrUpdate(newService);
		response.setSuccess(true);
		return response;
	}

	@Override
	public DeleteAdministratorResponse deleteAdministrator(DeleteAdministrator parameters) {
		final DeleteAdministratorResponse response = new DeleteAdministratorResponse();

		final ZASServerSession session = getZACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();

		// try to constuct the DAC given the data provided
		final AdministratorIdentity dacIdentity = validator
				.checkAdministratorIdentity(parameters.getAdministratorIdentity(), response);
		if (dacIdentity == null) {
			return response;
		}
		final AgentCredentialDescriptor dac = credentialFactory
				.createAgentCredential(dacIdentity.getDomaincertificate(), dacIdentity.getRootcertificate());
		if (dac == null || AgentCredentialType.DAC != dac.getCredentialType()) {
			setError(ErrorCode.InvalidDomainAdministratorCredentials, response);
			return response;
		}
		if (checkZoneBoundDomainAuthorization(dac.getDomainName(), zone, response) == null) {
			return response;
		}

		// check that the DAC credential exists
		final AgentCredential existingCred = credentialService.findByFingerprint(dac.getFingerprint());
		if (existingCred == null) {
			setError(ErrorCode.DomainAdministratorCredentialNotFound, response);
			return response;
		}

		// delete the existing DAC
		credentialService.delete(existingCred);

		// deactivate the certificate from all sessions active in the PartitionControlService
		sessionInvalidationService.invalidateCertificate(existingCred.getPublicCertificate());

		response.setSuccess(true);
		return response;
	}

	@Override
	public SearchChannelResponse searchChannel(SearchChannel parameters) {
		final SearchChannelResponse response = new SearchChannelResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(
				a2d.mapPage(parameters.getPage()));

		if (!StringUtils.hasText(parameters.getFilter().getDomain()) && session.isDAC()) {
			parameters.getFilter().setDomain(authorizedDomainName);
		}
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			if (checkDomainBoundDomainAuthorization(parameters.getFilter().getDomain(), authorizedDomainName, zone,
					response) == null) {
				return response;
			}
		}
		sc.setDomainName(parameters.getFilter().getDomain());
		if (parameters.getFilter().getOrigin() != null) {
			sc.getOrigin().setDomainName(parameters.getFilter().getOrigin().getDomain());
			sc.getOrigin().setLocalName(parameters.getFilter().getOrigin().getLocalname());
		}
		if (parameters.getFilter().getDestination() != null) {
			sc.getDestination().setDomainName(parameters.getFilter().getDestination().getDomain());
			sc.getDestination().setLocalName(parameters.getFilter().getDestination().getLocalname());
			sc.getDestination().setServiceName(parameters.getFilter().getDestination().getServicename());
		}
		if (parameters.getFilter().isUnconfirmedFlag() != null) {
			sc.setUnconfirmed(parameters.getFilter().isUnconfirmedFlag());
		}

		final List<org.tdmx.lib.zone.domain.Channel> channels = channelService.search(zone, sc);
		for (org.tdmx.lib.zone.domain.Channel c : channels) {
			response.getChannelinfos().add(d2a.mapChannelInfo(c));
		}

		response.setSuccess(true);
		response.setPage(parameters.getPage());
		return response;
	}

	@Override
	public SearchDestinationResponse searchDestination(SearchDestination parameters) {
		final SearchDestinationResponse response = new SearchDestinationResponse();

		final ZASServerSession session = getZACorDACSession(response);
		if (session == null) {
			return response;
		}

		final Zone zone = session.getZone();
		final String authorizedDomainName = session.getAuthorizedDomainName();

		final DestinationSearchCriteria sc = new DestinationSearchCriteria(a2d.mapPage(parameters.getPage()));
		if (!StringUtils.hasText(parameters.getFilter().getDomain()) && session.isDAC()) {
			// we fix the search to search only the DAC's domain.
			parameters.getFilter().setDomain(authorizedDomainName);
		}
		if (StringUtils.hasText(parameters.getFilter().getDomain())) {
			// we check that the provided domain is the DAC's domain.
			if (checkDomainBoundDomainAuthorization(parameters.getFilter().getDomain(), authorizedDomainName, zone,
					response) == null) {
				return response;
			}
			sc.getDestination().setDomainName(parameters.getFilter().getDomain());
		}
		sc.getDestination().setLocalName(parameters.getFilter().getLocalname());
		sc.getDestination().setServiceName(parameters.getFilter().getServicename());

		final List<org.tdmx.lib.zone.domain.Destination> destinations = destinationService.search(zone, sc);
		for (org.tdmx.lib.zone.domain.Destination d : destinations) {
			ChannelDestination cd = new ChannelDestination();
			cd.setDomainName(d.getTarget().getDomain().getDomainName());
			cd.setLocalName(d.getTarget().getLocalName());
			cd.setServiceName(d.getService().getServiceName());

			// TODO check eager fetch dest.address.domain & dest.service
			response.getDestinationinfos().add(d2a.mapDestinationInfo(cd, d.getDestinationSession()));
		}

		response.setSuccess(true);
		response.setPage(parameters.getPage());
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

	private String checkZoneBoundDomainAuthorization(String inputDomainName, Zone authorizedZone, Acknowledge ack) {
		if (!checkDomain(inputDomainName, ack)) {
			return null;
		}
		if (!StringUtils.isSuffix(inputDomainName, authorizedZone.getZoneApex())) {
			setError(ErrorCode.OutOfZoneAccess, ack);
			return null;
		}
		return inputDomainName;
	}

	private String checkDomainBoundDomainAuthorization(String inputDomainName, String authorizedDomainName,
			Zone authorizedZone, Acknowledge ack) {
		String domainName = checkZoneBoundDomainAuthorization(inputDomainName, authorizedZone, ack);
		if (domainName == null) {
			return null;
		}

		// authorizedDomainName is null if ALL domains (within the Zone) are authorized
		if (StringUtils.hasText(authorizedDomainName) && !domainName.equals(authorizedDomainName)) {
			setError(ErrorCode.OutOfDomainAccess, ack);
			return null;
		}
		return domainName;
	}

	private ZASServerSession getZACSession(Acknowledge ack) {
		ZASServerSession session = authorizedSessionService.getAuthorizedSession();
		if (!session.isZAC()) {
			setError(ErrorCode.NonAdministratorAccess, ack);
			return null;
		}
		return session;
	}

	private ZASServerSession getZACorDACSession(Acknowledge ack) {
		return authorizedSessionService.getAuthorizedSession();
	}

	private ZASServerSession getDACSession(Acknowledge ack) {
		ZASServerSession session = authorizedSessionService.getAuthorizedSession();
		if (!session.isDAC()) {
			setError(ErrorCode.NonAdministratorAccess, ack);
			return null;
		}
		return session;
	}

	private void setError(ErrorCode ec, Acknowledge ack) {
		Error error = new Error();
		error.setCode(ec.getErrorCode());
		error.setDescription(ec.getErrorDescription());
		ack.setError(error);
		ack.setSuccess(false);
	}

	private ErrorCode mapSetAuthorizationOperationStatus(SetAuthorizationOperationStatus status) {
		switch (status) {
		case RECEIVER_AUTHORIZATION_CONFIRMATION_MISMATCH:
			return ErrorCode.ReceiverChannelAuthorizationMismatch;
		case RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING:
			return ErrorCode.ReceiverChannelAuthorizationMissing;
		case RECEIVER_AUTHORIZATION_CONFIRMATION_PROVIDED:
			return ErrorCode.ReceiverChannelAuthorizationProvided;
		case RECEIVER_SERVICE_NOT_FOUND:
			return ErrorCode.ServiceNotFound;
		case SENDER_AUTHORIZATION_CONFIRMATION_MISMATCH:
			return ErrorCode.SenderChannelAuthorizationMismatch;
		case SENDER_AUTHORIZATION_CONFIRMATION_MISSING:
			return ErrorCode.SenderChannelAuthorizationMissing;
		case SENDER_AUTHORIZATION_CONFIRMATION_PROVIDED:
			return ErrorCode.SenderChannelAuthorizationProvided;
		default:
			return null;
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public RelayClientService getRelayClientService() {
		return relayClientService;
	}

	public void setRelayClientService(RelayClientService relayClientService) {
		this.relayClientService = relayClientService;
	}

	public SessionCertificateInvalidationService getSessionInvalidationService() {
		return sessionInvalidationService;
	}

	public void setSessionInvalidationService(SessionCertificateInvalidationService sessionInvalidationService) {
		this.sessionInvalidationService = sessionInvalidationService;
	}

	public AuthorizedSessionLookupService<ZASServerSession> getAuthorizedSessionService() {
		return authorizedSessionService;
	}

	public void setAuthorizedSessionService(AuthorizedSessionLookupService<ZASServerSession> authorizedSessionService) {
		this.authorizedSessionService = authorizedSessionService;
	}

	public AuthenticatedClientLookupService getAuthenticatedClientService() {
		return authenticatedClientService;
	}

	public void setAuthenticatedClientService(AuthenticatedClientLookupService authenticatedClientService) {
		this.authenticatedClientService = authenticatedClientService;
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

	public DestinationService getDestinationService() {
		return destinationService;
	}

	public void setDestinationService(DestinationService destinationService) {
		this.destinationService = destinationService;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

}
