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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.msg.Address;
import org.tdmx.core.api.v01.msg.AddressFilter;
import org.tdmx.core.api.v01.msg.AdministratorFilter;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelAuthorizationFilter;
import org.tdmx.core.api.v01.msg.ChannelDestination;
import org.tdmx.core.api.v01.msg.ChannelDestinationFilter;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Channelauthorization;
import org.tdmx.core.api.v01.msg.CredentialStatus;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.DomainFilter;
import org.tdmx.core.api.v01.msg.FlowControlLimit;
import org.tdmx.core.api.v01.msg.Grant;
import org.tdmx.core.api.v01.msg.Limit;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.api.v01.msg.Service;
import org.tdmx.core.api.v01.msg.ServiceFilter;
import org.tdmx.core.api.v01.msg.UserFilter;
import org.tdmx.core.api.v01.msg.UserIdentity;
import org.tdmx.core.api.v01.zas.CreateAddress;
import org.tdmx.core.api.v01.zas.CreateAddressResponse;
import org.tdmx.core.api.v01.zas.CreateAdministrator;
import org.tdmx.core.api.v01.zas.CreateAdministratorResponse;
import org.tdmx.core.api.v01.zas.CreateDomain;
import org.tdmx.core.api.v01.zas.CreateDomainResponse;
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
import org.tdmx.core.api.v01.zas.DeleteService;
import org.tdmx.core.api.v01.zas.DeleteServiceResponse;
import org.tdmx.core.api.v01.zas.DeleteUser;
import org.tdmx.core.api.v01.zas.DeleteUserResponse;
import org.tdmx.core.api.v01.zas.ModifyAdministrator;
import org.tdmx.core.api.v01.zas.ModifyAdministratorResponse;
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
import org.tdmx.core.api.v01.zas.SearchService;
import org.tdmx.core.api.v01.zas.SearchServiceResponse;
import org.tdmx.core.api.v01.zas.SearchUser;
import org.tdmx.core.api.v01.zas.SearchUserResponse;
import org.tdmx.core.api.v01.zas.SetChannelAuthorization;
import org.tdmx.core.api.v01.zas.SetChannelAuthorizationResponse;
import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestCredentialGenerator;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DestinationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.MockZonePartitionIdInstaller;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedClientService;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory;
import org.tdmx.server.ws.session.WebServiceSessionManager;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ZASImplUnitTest {

	private static final Logger log = LoggerFactory.getLogger(ZASImplUnitTest.class);

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private AgentCredentialService agentCredentialService;
	@Autowired
	private AgentCredentialFactory agentCredentialFactory;
	@Autowired
	@Named("ws.ZAS.SessionFactory")
	private WebServiceSessionFactory<ZASServerSession> serverSessionFactory;
	@Autowired
	private AuthenticatedClientService authenticatedClientService;
	@Autowired
	@Named("ws.ZAS.ServerSessionManager")
	private WebServiceSessionManager serverSessionManager;

	@Autowired
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	@Autowired
	private ZoneService zoneService;
	@Autowired
	private DomainService domainService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private ServiceService serviceService;
	@Autowired
	private ChannelService channelService;
	@Autowired
	private DestinationService destinationService;

	@Autowired
	@Named("ws.ZAS")
	private ZAS zas;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;

	private Zone zone;
	private org.tdmx.lib.zone.domain.Domain domain;
	private org.tdmx.lib.zone.domain.Service service;
	private org.tdmx.lib.zone.domain.Address address;
	private AccountZone accountZone;
	private PKIXCredential zac;
	private PKIXCredential dac;
	private PKIXCredential uc;

	private PKIXCredential uc2;
	private PKIXCredential dac2;

	private final String ZAC_SESSION_ID = "ZAC-1";
	private final String DAC_SESSION_ID = "DAC-1";

	@Before
	public void doSetup() throws Exception {

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockZonePartitionIdInstaller.ZP1_S1);
		input.setNumZACs(1);
		input.setNumDomains(1);
		input.setNumDACsPerDomain(1);
		input.setNumAddressesPerDomain(1);
		input.setNumUsersPerAddress(1);

		data = dataGenerator.setUp(input);

		accountZone = data.getAccountZone();
		zone = data.getZone();
		zac = data.getZacs().get(0).getCredential();
		domain = data.getDomains().get(0).getDomain();
		service = data.getDomains().get(0).getServices().get(0).getService();
		dac = data.getDomains().get(0).getDacs().get(0).getCredential();
		address = data.getDomains().get(0).getAddresses().get(0).getAddress();
		uc = data.getDomains().get(0).getAddresses().get(0).getUcs().get(0).getCredential();

		uc2 = TestCredentialGenerator.createUC(dac, address.getLocalName(), 2, 2);

		String subdomain = domain.getDomainName().substring(0, domain.getDomainName().indexOf(zone.getZoneApex()) - 1);
		dac2 = TestCredentialGenerator.createDAC(zac, subdomain, 2, 2);

		Map<SeedAttribute, Long> zacSeedAttributeMap = new HashMap<>();
		zacSeedAttributeMap.put(SeedAttribute.AccountZoneId, accountZone.getId());
		zacSeedAttributeMap.put(SeedAttribute.ZoneId, zone.getId());
		serverSessionManager.createSession(ZAC_SESSION_ID, zac.getPublicCert(), zacSeedAttributeMap);

		Map<SeedAttribute, Long> seedAttributeMap = new HashMap<>();
		seedAttributeMap.put(SeedAttribute.AccountZoneId, accountZone.getId());
		seedAttributeMap.put(SeedAttribute.ZoneId, zone.getId());
		seedAttributeMap.put(SeedAttribute.DomainId, domain.getId());
		serverSessionManager.createSession(DAC_SESSION_ID, dac.getPublicCert(), seedAttributeMap);
	}

	@After
	public void doTeardown() {
		authenticatedClientService.clearAuthenticatedClient();

		dataGenerator.tearDown(input, data);
	}

	@Test
	public void testAutowired() {
		assertNotNull(serverSessionFactory);
		assertNotNull(serverSessionManager);
		assertNotNull(authenticatedClientService);

		assertNotNull(zoneService);
		assertNotNull(agentCredentialService);
		assertNotNull(agentCredentialFactory);
		assertNotNull(domainService);
		assertNotNull(addressService);

		assertEquals(WebServiceApiName.ZAS, serverSessionManager.getApiName());

		// the service under test...
		assertNotNull(zas);
	}

	@Test
	public void testSearchDomain_ZAC_all() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchDomain req = new SearchDomain();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		DomainFilter uf = new DomainFilter();
		req.setFilter(uf);

		SearchDomainResponse response = zas.searchDomain(req);
		assertSuccess(response);
		assertEquals(1, response.getDomains().size());
	}

	@Test
	public void testSearchDomain_ZAC_invalidDomain() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchDomain req = new SearchDomain();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		DomainFilter uf = new DomainFilter();
		uf.setDomain("unknown.sub.domain.com");
		req.setFilter(uf);

		SearchDomainResponse response = zas.searchDomain(req);
		assertError(ErrorCode.OutOfZoneAccess, response);
	}

	@Test
	public void testSearchDomain_DAC_notAuthorized() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchDomain req = new SearchDomain();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		DomainFilter uf = new DomainFilter();
		req.setFilter(uf);

		SearchDomainResponse response = zas.searchDomain(req);
		assertError(ErrorCode.NonAdministratorAccess, response);
	}

	@Test
	public void testSearchUser_ZAC_all() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchUser req = new SearchUser();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertSuccess(response);
		assertEquals(1, response.getUsers().size());
	}

	@Test
	public void testSearchAddress_ZAC_all() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchAddress req = new SearchAddress();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AddressFilter uf = new AddressFilter();
		req.setFilter(uf);

		SearchAddressResponse response = zas.searchAddress(req);
		assertSuccess(response);
		assertEquals(1, response.getAddresses().size());
	}

	@Test
	public void testSearchUser_ZAC_statusOnly() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchUser req = new SearchUser();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		uf.setStatus(CredentialStatus.ACTIVE);
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertSuccess(response);
		assertEquals(1, response.getUsers().size());
	}

	@Test
	public void testSearchUser_ZAC_getUser() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchUser req = new SearchUser();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(uc.getIssuerPublicCert().getX509Encoded());
		u.setRootcertificate(uc.getZoneRootPublicCert().getX509Encoded());
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertSuccess(response);
		assertEquals(1, response.getUsers().size());
	}

	@Test
	public void testSearchUser_ZAC_invalidZone() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchUser req = new SearchUser();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		uf.setDomain("unknownzone.com");
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertError(ErrorCode.OutOfZoneAccess, response);
	}

	@Test
	public void testSearchAddress_ZAC_invalidZone() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchAddress req = new SearchAddress();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AddressFilter uf = new AddressFilter();
		uf.setDomain("unknownzone.com");
		req.setFilter(uf);

		SearchAddressResponse response = zas.searchAddress(req);
		assertError(ErrorCode.OutOfZoneAccess, response);
	}

	@Test
	public void testSearchUser_DAC_all() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchUser req = new SearchUser();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertSuccess(response);
		assertEquals(1, response.getUsers().size());
	}

	@Test
	public void testSearchAddress_DAC_all() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchAddress req = new SearchAddress();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AddressFilter uf = new AddressFilter();
		req.setFilter(uf);

		SearchAddressResponse response = zas.searchAddress(req);
		assertSuccess(response);
		assertEquals(1, response.getAddresses().size());
	}

	@Test
	public void testSearchUser_DAC_addressName() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchUser req = new SearchUser();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		uf.setLocalname(uc.getPublicCert().getCommonName());
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertSuccess(response);
		assertEquals(1, response.getUsers().size());
	}

	@Test
	public void testSearchAddress_DAC_addressName() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchAddress req = new SearchAddress();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AddressFilter uf = new AddressFilter();
		uf.setLocalname(uc.getPublicCert().getCommonName());
		req.setFilter(uf);

		SearchAddressResponse response = zas.searchAddress(req);
		assertSuccess(response);
		assertEquals(1, response.getAddresses().size());
	}

	@Test
	public void testSearchService_ZAC_all() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchService req = new SearchService();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ServiceFilter uf = new ServiceFilter();
		req.setFilter(uf);

		SearchServiceResponse response = zas.searchService(req);
		assertSuccess(response);
		assertEquals(1, response.getServices().size());
	}

	@Test
	public void testSearchService_ZAC_invalidZone() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchService req = new SearchService();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ServiceFilter uf = new ServiceFilter();
		uf.setDomain("unknownzone.com");
		req.setFilter(uf);

		SearchServiceResponse response = zas.searchService(req);
		assertError(ErrorCode.OutOfZoneAccess, response);
	}

	@Test
	public void testSearchService_DAC_all() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchService req = new SearchService();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ServiceFilter uf = new ServiceFilter();
		req.setFilter(uf);

		SearchServiceResponse response = zas.searchService(req);
		assertSuccess(response);
		assertEquals(1, response.getServices().size());
	}

	@Test
	public void testSearchService_DAC_serviceName() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchService req = new SearchService();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ServiceFilter uf = new ServiceFilter();
		uf.setServicename(service.getServiceName());
		req.setFilter(uf);

		SearchServiceResponse response = zas.searchService(req);
		assertSuccess(response);
		assertEquals(1, response.getServices().size());
	}

	@Test
	public void testSearchUser_DAC_suspended() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchUser req = new SearchUser();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		uf.setStatus(CredentialStatus.SUSPENDED);
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertSuccess(response);
		assertEquals(0, response.getUsers().size());
	}

	@Test
	public void testSearchUser_DAC_invalidDomain() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchUser req = new SearchUser();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		uf.setDomain("unknownsubdomain." + zone.getZoneApex());
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertError(ErrorCode.OutOfDomainAccess, response);
	}

	@Test
	public void testSearchAddress_DAC_invalidDomain() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchAddress req = new SearchAddress();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AddressFilter uf = new AddressFilter();
		uf.setDomain("unknownsubdomain." + zone.getZoneApex());
		req.setFilter(uf);

		SearchAddressResponse response = zas.searchAddress(req);
		assertError(ErrorCode.OutOfDomainAccess, response);
	}

	@Test
	public void testSearchService_DAC_invalidDomain() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchService req = new SearchService();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ServiceFilter uf = new ServiceFilter();
		uf.setDomain("unknownsubdomain." + zone.getZoneApex());
		req.setFilter(uf);

		SearchServiceResponse response = zas.searchService(req);
		assertError(ErrorCode.OutOfDomainAccess, response);
	}

	@Test
	public void testSearchUser_DAC_getUser() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchUser req = new SearchUser();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(uc.getIssuerPublicCert().getX509Encoded());
		u.setRootcertificate(uc.getZoneRootPublicCert().getX509Encoded());
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertSuccess(response);
		assertEquals(1, response.getUsers().size());
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_DAC() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		CreateDomain req = new CreateDomain();
		req.setSessionId(DAC_SESSION_ID);

		req.setDomain(dac.getPublicCert().getCommonName()); // DAC's cn is the domain
		CreateDomainResponse response = zas.createDomain(req);
		assertError(ErrorCode.NonAdministratorAccess, response);
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_ZAC_nonsubdomain() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateDomain req = new CreateDomain();
		req.setSessionId(ZAC_SESSION_ID);

		req.setDomain("not.a.subdomain.com"); // ZAC can only create subdomains of their root
		CreateDomainResponse response = zas.createDomain(req);
		assertError(ErrorCode.OutOfZoneAccess, response);
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_wrongDomain() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateDomain req = new CreateDomain();
		req.setSessionId(ZAC_SESSION_ID);

		req.setDomain("UPPERCASE." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		CreateDomainResponse response = zas.createDomain(req);
		assertError(ErrorCode.NotNormalizedDomain, response);
	}

	@Test
	public void testCreateDomain_Success() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateDomain req = new CreateDomain();
		req.setSessionId(ZAC_SESSION_ID);

		req.setDomain("lowercasesubdomain." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		CreateDomainResponse response = zas.createDomain(req);
		assertSuccess(response);
	}

	@Test
	public void testSearchAdministrator_ZAC_all() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchAdministrator req = new SearchAdministrator();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AdministratorFilter uf = new AdministratorFilter();
		req.setFilter(uf);

		SearchAdministratorResponse response = zas.searchAdministrator(req);
		assertSuccess(response);
		assertEquals(1, response.getAdministrators().size());
	}

	@Test
	public void testSearchAdministrator_ZAC_domain() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchAdministrator req = new SearchAdministrator();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AdministratorFilter uf = new AdministratorFilter();
		uf.setDomain(dac.getPublicCert().getCommonName());
		req.setFilter(uf);

		SearchAdministratorResponse response = zas.searchAdministrator(req);
		assertSuccess(response);
		assertEquals(1, response.getAdministrators().size());
	}

	@Test
	public void testSearchAdministrator_ZAC_suspended() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchAdministrator req = new SearchAdministrator();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AdministratorFilter uf = new AdministratorFilter();
		uf.setStatus(CredentialStatus.SUSPENDED);
		req.setFilter(uf);

		SearchAdministratorResponse response = zas.searchAdministrator(req);
		assertSuccess(response);
		assertEquals(0, response.getAdministrators().size());
	}

	@Test
	public void testSearchAdministrator_ZAC_nonsubdomainFails() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchAdministrator req = new SearchAdministrator();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AdministratorFilter uf = new AdministratorFilter();
		uf.setDomain("unknown.domain.com");
		req.setFilter(uf);

		SearchAdministratorResponse response = zas.searchAdministrator(req);
		assertError(ErrorCode.OutOfZoneAccess, response);
	}

	@Test
	public void testSearchAdministrator_DAC_notallowed() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchAdministrator req = new SearchAdministrator();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AdministratorFilter uf = new AdministratorFilter();
		req.setFilter(uf);

		SearchAdministratorResponse response = zas.searchAdministrator(req);
		assertError(ErrorCode.NonAdministratorAccess, response);
	}

	@Test
	@Ignore
	public void testSearchIpZone() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateAddress_ZAC() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateAddress req = new CreateAddress();
		req.setSessionId(ZAC_SESSION_ID);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain(dac.getPublicCert().getCommonName());
		ucAddress.setLocalname("anewaddressname");

		req.setAddress(ucAddress);

		CreateAddressResponse response = zas.createAddress(req);
		assertSuccess(response);
	}

	@Test
	public void testCreateAddress_DAC() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		CreateAddress req = new CreateAddress();
		req.setSessionId(DAC_SESSION_ID);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain(dac.getPublicCert().getCommonName());
		ucAddress.setLocalname("anewaddressname");

		req.setAddress(ucAddress);

		CreateAddressResponse response = zas.createAddress(req);
		assertSuccess(response);
	}

	@Test
	public void testCreateAddress_ZAC_MissingDomain() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateAddress req = new CreateAddress();
		req.setSessionId(ZAC_SESSION_ID);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain("unknownsubdomain." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		ucAddress.setLocalname(uc.getPublicCert().getCommonName());

		req.setAddress(ucAddress);

		CreateAddressResponse response = zas.createAddress(req);
		assertError(ErrorCode.DomainNotFound, response);
	}

	@Test
	public void testCreateService_DAC() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		CreateService req = new CreateService();
		req.setSessionId(DAC_SESSION_ID);

		// create the service
		Service s = new Service();
		s.setDomain(dac.getPublicCert().getCommonName());
		s.setServicename("anewservicename");

		req.setService(s);

		CreateServiceResponse response = zas.createService(req);
		assertSuccess(response);
	}

	@Test
	public void testCreateService_ZAC_MissingDomain() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateService req = new CreateService();
		req.setSessionId(ZAC_SESSION_ID);

		// create the service
		Service service = new Service();
		service.setDomain("unknownsubdomain." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		service.setServicename("anyoldservicename");

		req.setService(service);

		CreateServiceResponse response = zas.createService(req);
		assertError(ErrorCode.DomainNotFound, response);
	}

	@Test
	@Ignore
	public void testIncident() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testCreateIpZone() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testDeleteIpZone() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDeleteDomain_ZAC_DACsExist() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		DeleteDomain req = new DeleteDomain();
		req.setSessionId(ZAC_SESSION_ID);

		req.setDomain(domain.getDomainName());
		DeleteDomainResponse response = zas.deleteDomain(req);
		assertError(ErrorCode.DomainAdministratorCredentialsExist, response);
	}

	@Test
	public void testDeleteDomain_DAC_notAuthorized() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		DeleteDomain req = new DeleteDomain();
		req.setSessionId(DAC_SESSION_ID);

		req.setDomain(domain.getDomainName());
		DeleteDomainResponse response = zas.deleteDomain(req);
		assertError(ErrorCode.NonAdministratorAccess, response);
	}

	@Test
	public void testDeleteDomain_ZAC_AddressesExist() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {
			removeAgentCredentials(domain, AgentCredentialType.DAC);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		DeleteDomain req = new DeleteDomain();
		req.setSessionId(ZAC_SESSION_ID);

		req.setDomain(domain.getDomainName());
		DeleteDomainResponse response = zas.deleteDomain(req);
		assertError(ErrorCode.AddressesExist, response);
	}

	@Test
	public void testDeleteDomain_ZAC_ServicesExist() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		// delete any Destination on the domain
		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {
			removeDestinations(domain);
			removeAgentCredentials(domain);
			removeAddresses(domain);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		DeleteDomain req = new DeleteDomain();
		req.setSessionId(ZAC_SESSION_ID);

		req.setDomain(domain.getDomainName());
		DeleteDomainResponse response = zas.deleteDomain(req);
		assertError(ErrorCode.ServicesExist, response);
	}

	@Test
	public void testDeleteDomain_ZAC_ok() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {
			removeDestinations(domain);
			removeChannels(domain);
			removeAgentCredentials(domain);
			removeAddresses(domain);
			removeServices(domain);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		DeleteDomain req = new DeleteDomain();
		req.setSessionId(ZAC_SESSION_ID);

		req.setDomain(domain.getDomainName());
		DeleteDomainResponse response = zas.deleteDomain(req);
		assertSuccess(response);
	}

	@Test
	@Ignore
	public void testReport() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDeleteUser_ZAC() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		DeleteUser req = new DeleteUser();
		req.setSessionId(ZAC_SESSION_ID);

		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setUserIdentity(u);
		DeleteUserResponse response = zas.deleteUser(req);
		assertSuccess(response);
	}

	@Test
	public void testModiyUser_ZAC_suspended() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		ModifyUser req = new ModifyUser();
		req.setSessionId(ZAC_SESSION_ID);

		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setUserIdentity(u);
		req.setStatus(CredentialStatus.SUSPENDED);
		ModifyUserResponse response = zas.modifyUser(req);
		assertSuccess(response);
		// TODO check susp.
	}

	// TODO delete user which has is a target Channel.ChannelFlowTarget (+Flows)
	@Test
	public void testDeleteUser_DAC() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		DeleteUser req = new DeleteUser();
		req.setSessionId(DAC_SESSION_ID);

		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setUserIdentity(u);
		DeleteUserResponse response = zas.deleteUser(req);
		assertSuccess(response);
	}

	@Test
	public void testModiyUser_DAC_suspended() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		ModifyUser req = new ModifyUser();
		req.setSessionId(DAC_SESSION_ID);

		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setUserIdentity(u);
		req.setStatus(CredentialStatus.SUSPENDED);
		ModifyUserResponse response = zas.modifyUser(req);
		assertSuccess(response);
	}

	@Test
	@Ignore
	public void testModifyIpZone() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateAdministrator_Success() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateAdministrator req = new CreateAdministrator();
		req.setSessionId(ZAC_SESSION_ID);

		req.setStatus(CredentialStatus.ACTIVE);
		AdministratorIdentity a = new AdministratorIdentity();
		a.setDomaincertificate(dac2.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac2.getIssuerPublicCert().getX509Encoded());

		req.setAdministratorIdentity(a);
		CreateAdministratorResponse response = zas.createAdministrator(req);
		assertSuccess(response);
	}

	@Test
	public void testCreateAdministrator_Success_DefaultStatus() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateAdministrator req = new CreateAdministrator();
		req.setSessionId(ZAC_SESSION_ID);

		AdministratorIdentity a = new AdministratorIdentity();
		a.setDomaincertificate(dac2.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac2.getIssuerPublicCert().getX509Encoded());

		req.setAdministratorIdentity(a);
		CreateAdministratorResponse response = zas.createAdministrator(req);
		assertSuccess(response);
	}

	@Test
	public void testCreateAdministrator_DACExists() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateAdministrator req = new CreateAdministrator();
		req.setSessionId(ZAC_SESSION_ID);

		req.setStatus(CredentialStatus.ACTIVE);
		AdministratorIdentity a = new AdministratorIdentity();
		a.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setAdministratorIdentity(a);
		CreateAdministratorResponse response = zas.createAdministrator(req);
		assertError(ErrorCode.DomainAdministratorCredentialsExist, response);
	}

	@Test
	public void testCreateAdministrator_DomainNotExists() throws Exception {
		// create new credential for non-existent domain
		zonePartitionIdProvider.setPartitionId(MockZonePartitionIdInstaller.ZP1_S1);
		PKIXCredential dac3 = TestCredentialGenerator.createDAC(zac, "gugus", 2, 2);

		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateAdministrator req = new CreateAdministrator();
		req.setSessionId(ZAC_SESSION_ID);

		req.setStatus(CredentialStatus.ACTIVE);
		AdministratorIdentity a = new AdministratorIdentity();
		a.setDomaincertificate(dac3.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac3.getIssuerPublicCert().getX509Encoded());

		req.setAdministratorIdentity(a);
		CreateAdministratorResponse response = zas.createAdministrator(req);
		assertError(ErrorCode.DomainNotFound, response);
	}

	@Test
	public void testSearchDestination_DAC() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchDestination req = new SearchDestination();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ChannelDestinationFilter ftf = new ChannelDestinationFilter();
		req.setFilter(ftf);

		SearchDestinationResponse response = zas.searchDestination(req);
		assertSuccess(response);
		assertEquals(1, response.getDestinationinfos().size());
		// TODO alternatives
	}

	@Test
	public void testDeleteService_ZAC_ChannelsExist() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		DeleteService req = new DeleteService();
		req.setSessionId(ZAC_SESSION_ID);

		Service s = new Service();
		s.setDomain(domain.getDomainName());
		s.setServicename(service.getServiceName());

		req.setService(s);

		DeleteServiceResponse response = zas.deleteService(req);
		assertError(ErrorCode.ChannelAuthorizationExist, response);
	}

	@Test
	public void testDeleteService_ZAC() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {
			removeChannels(domain);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		DeleteService req = new DeleteService();
		req.setSessionId(ZAC_SESSION_ID);

		Service s = new Service();
		s.setDomain(domain.getDomainName());
		s.setServicename(service.getServiceName());

		req.setService(s);

		DeleteServiceResponse response = zas.deleteService(req);
		assertSuccess(response);
	}

	@Test
	public void testDeleteService_DAC_ChannelsExist() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		DeleteService req = new DeleteService();
		req.setSessionId(DAC_SESSION_ID);

		Service s = new Service();
		s.setDomain(domain.getDomainName());
		s.setServicename(service.getServiceName());

		req.setService(s);

		DeleteServiceResponse response = zas.deleteService(req);
		assertError(ErrorCode.ChannelAuthorizationExist, response);
	}

	@Test
	public void testDeleteService_DAC() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {
			removeChannels(domain);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		DeleteService req = new DeleteService();
		req.setSessionId(DAC_SESSION_ID);

		Service s = new Service();
		s.setDomain(domain.getDomainName());
		s.setServicename(service.getServiceName());

		req.setService(s);

		DeleteServiceResponse response = zas.deleteService(req);
		assertSuccess(response);
	}

	@Test
	public void testDeleteAddress_ZAC_ok() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateAddress req = new CreateAddress();
		req.setSessionId(ZAC_SESSION_ID);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain(dac.getPublicCert().getCommonName());
		ucAddress.setLocalname("anewaddressname");

		req.setAddress(ucAddress);

		CreateAddressResponse response = zas.createAddress(req);
		assertSuccess(response);

		DeleteAddress da = new DeleteAddress();
		da.setSessionId(ZAC_SESSION_ID);
		da.setAddress(ucAddress);

		DeleteAddressResponse daRes = zas.deleteAddress(da);
		assertSuccess(daRes);
	}

	@Test
	public void testDeleteAddress_ZAC_UCsExist() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		DeleteAddress req = new DeleteAddress();
		req.setSessionId(ZAC_SESSION_ID);

		Address a = new Address();
		a.setDomain(dac.getPublicCert().getCommonName());
		a.setLocalname(uc.getPublicCert().getCommonName());

		req.setAddress(a);

		DeleteAddressResponse response = zas.deleteAddress(req);
		assertError(ErrorCode.UserCredentialsExist, response);
	}

	@Test
	public void testSetChannelAuthorization_SendRecvSameDomain() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SetChannelAuthorization req = new SetChannelAuthorization();
		req.setSessionId(DAC_SESSION_ID);

		req.setDomain(domain.getDomainName());
		Currentchannelauthorization auth = new Currentchannelauthorization();

		Channel channel = new Channel();
		ChannelDestination dest = new ChannelDestination();
		dest.setDomain(domain.getDomainName());
		dest.setLocalname(uc.getPublicCert().getCommonName());
		dest.setServicename(service.getServiceName());
		channel.setDestination(dest);

		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain(domain.getDomainName());
		origin.setLocalname(uc.getPublicCert().getCommonName());
		channel.setOrigin(origin);
		auth.setChannel(channel);

		Date oneMonth = CalendarUtils.getDateWithOffset(new Date(), Calendar.MONTH, 1);
		Permission recvPermission = new Permission();
		recvPermission.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		recvPermission.setPermission(Grant.ALLOW);
		recvPermission.setValidUntil(CalendarUtils.cast(oneMonth));
		auth.setDestination(recvPermission);
		SignatureUtils.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_256_RSA, new Date(), channel,
				recvPermission);

		Permission sendPermission = new Permission();
		sendPermission.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		sendPermission.setPermission(Grant.ALLOW);
		sendPermission.setValidUntil(CalendarUtils.cast(oneMonth));
		auth.setOrigin(sendPermission);
		SignatureUtils.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_256_RSA, new Date(), channel,
				sendPermission);

		FlowControlLimit fcl = new FlowControlLimit();
		Limit undeliveredBuffer = new Limit();
		undeliveredBuffer.setHighBytes(ZoneFacade.ONE_GB);
		undeliveredBuffer.setLowBytes(ZoneFacade.ONE_MB);
		fcl.setUndeliveredBuffer(undeliveredBuffer);

		Limit unsentBuffer = new Limit();
		unsentBuffer.setHighBytes(ZoneFacade.ONE_GB);
		unsentBuffer.setLowBytes(ZoneFacade.ONE_MB);
		fcl.setUnsentBuffer(unsentBuffer);
		auth.setLimit(fcl);

		SignatureUtils.createChannelAuthorizationSignature(dac, SignatureAlgorithm.SHA_256_RSA, new Date(), auth);
		req.setCurrentchannelauthorization(auth);

		SetChannelAuthorizationResponse response = zas.setChannelAuthorization(req);
		assertSuccess(response);

		// tamper with the CA signature
		req.getCurrentchannelauthorization().getAdministratorsignature().getSignaturevalue().setSignature("gugus");
		response = zas.setChannelAuthorization(req);
		assertError(ErrorCode.InvalidSignatureChannelAuthorization, response);
	}

	@Test
	public void testSetChannelAuthorization_SendRecvSameDomain_NoService() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SetChannelAuthorization req = new SetChannelAuthorization();
		req.setSessionId(DAC_SESSION_ID);

		req.setDomain(domain.getDomainName());
		Currentchannelauthorization auth = new Currentchannelauthorization();

		Channel channel = new Channel();
		ChannelDestination dest = new ChannelDestination();
		dest.setDomain(domain.getDomainName());
		dest.setLocalname(uc.getPublicCert().getCommonName());
		dest.setServicename("gugus");
		channel.setDestination(dest);

		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain(domain.getDomainName());
		origin.setLocalname(uc.getPublicCert().getCommonName());
		channel.setOrigin(origin);
		auth.setChannel(channel);

		Date oneMonth = CalendarUtils.getDateWithOffset(new Date(), Calendar.MONTH, 1);
		Permission recvPermission = new Permission();
		recvPermission.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		recvPermission.setPermission(Grant.ALLOW);
		recvPermission.setValidUntil(CalendarUtils.cast(oneMonth));
		auth.setDestination(recvPermission);
		SignatureUtils.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_256_RSA, new Date(), channel,
				recvPermission);

		Permission sendPermission = new Permission();
		sendPermission.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		sendPermission.setPermission(Grant.ALLOW);
		sendPermission.setValidUntil(CalendarUtils.cast(oneMonth));
		auth.setOrigin(sendPermission);
		SignatureUtils.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_256_RSA, new Date(), channel,
				sendPermission);

		FlowControlLimit fcl = new FlowControlLimit();
		Limit undeliveredBuffer = new Limit();
		undeliveredBuffer.setHighBytes(ZoneFacade.ONE_GB);
		undeliveredBuffer.setLowBytes(ZoneFacade.ONE_MB);
		fcl.setUndeliveredBuffer(undeliveredBuffer);

		Limit unsentBuffer = new Limit();
		unsentBuffer.setHighBytes(ZoneFacade.ONE_GB);
		unsentBuffer.setLowBytes(ZoneFacade.ONE_MB);
		fcl.setUnsentBuffer(unsentBuffer);
		auth.setLimit(fcl);

		SignatureUtils.createChannelAuthorizationSignature(dac, SignatureAlgorithm.SHA_256_RSA, new Date(), auth);
		req.setCurrentchannelauthorization(auth);

		SetChannelAuthorizationResponse response = zas.setChannelAuthorization(req);
		assertError(ErrorCode.ServiceNotFound, response);
	}

	@Test
	public void testCreateUser_ZAC_UCExists() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateUser req = new CreateUser();
		req.setSessionId(ZAC_SESSION_ID);

		req.setStatus(CredentialStatus.ACTIVE);
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setUserIdentity(u);
		CreateUserResponse response = zas.createUser(req);
		assertError(ErrorCode.UserCredentialsExist, response);
	}

	@Test
	public void testCreateUser_ZAC_ok() {
		// create new ZAC credential
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateUser req = new CreateUser();
		req.setSessionId(ZAC_SESSION_ID);

		req.setStatus(CredentialStatus.ACTIVE);
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc2.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setUserIdentity(u);
		CreateUserResponse response = zas.createUser(req);
		assertSuccess(response);

		// TODO check originating flows created by using ZAS#searchFlow
	}

	@Test
	public void testCreateUser_DAC_ok() {
		// create new ZAC credential
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateUser req = new CreateUser();
		req.setSessionId(ZAC_SESSION_ID);

		req.setStatus(CredentialStatus.ACTIVE);
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc2.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setUserIdentity(u);
		CreateUserResponse response = zas.createUser(req);
		assertSuccess(response);

		// TODO check originating flows created by using ZAS#searchFlow
	}

	@Test
	public void testCreateUser_DAC_NOK_AddressNotFound() throws Exception {
		// create new credentials on unexisting address
		PKIXCredential uc3 = TestCredentialGenerator.createUC(dac, "gugus", 2, 2);

		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		CreateUser req = new CreateUser();
		req.setSessionId(ZAC_SESSION_ID);

		req.setStatus(CredentialStatus.ACTIVE);
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc3.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setUserIdentity(u);
		CreateUserResponse response = zas.createUser(req);
		assertError(ErrorCode.AddressNotFound, response);
	}

	@Test
	public void testSearchChannel_ZAC() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		SearchChannel req = new SearchChannel();
		req.setSessionId(ZAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ChannelAuthorizationFilter uf = new ChannelAuthorizationFilter();
		req.setFilter(uf);

		SearchChannelResponse response = zas.searchChannel(req);
		assertSuccess(response);
		assertEquals(1, response.getChannelinfos().size());
		// TODO alternatives
	}

	@Test
	public void testDeleteChannelAuthorization_DAC() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		SearchChannel req = new SearchChannel();
		req.setSessionId(DAC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ChannelAuthorizationFilter uf = new ChannelAuthorizationFilter();
		req.setFilter(uf);

		SearchChannelResponse response = zas.searchChannel(req);
		assertSuccess(response);
		assertEquals(1, response.getChannelinfos().size());
		// TODO alternatives

		Channelauthorization caToDel = response.getChannelinfos().get(0).getChannelauthorization();

		DeleteChannelAuthorization delReq = new DeleteChannelAuthorization();
		delReq.setSessionId(DAC_SESSION_ID);

		delReq.setDomain(dac.getPublicCert().getCommonName());
		delReq.setChannel(caToDel.getCurrent().getChannel());

		DeleteChannelAuthorizationResponse delRes = zas.deleteChannelAuthorization(delReq);
		assertSuccess(delRes);

		// check it's gone
		response = zas.searchChannel(req);
		assertSuccess(response);
		assertEquals(0, response.getChannelinfos().size());
	}

	@Test
	public void testModifyAdministrator_ZAC_suspend() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		ModifyAdministrator req = new ModifyAdministrator();
		req.setSessionId(ZAC_SESSION_ID);

		AdministratorIdentity u = new AdministratorIdentity();
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setAdministratorIdentity(u);
		req.setStatus(CredentialStatus.SUSPENDED);
		ModifyAdministratorResponse response = zas.modifyAdministrator(req);
		assertSuccess(response);
		// TODO check susp.
	}

	@Test
	public void testDeleteAdministrator_ZAC() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		DeleteAdministrator req = new DeleteAdministrator();
		req.setSessionId(ZAC_SESSION_ID);

		AdministratorIdentity u = new AdministratorIdentity();
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setAdministratorIdentity(u);
		DeleteAdministratorResponse response = zas.deleteAdministrator(req);
		assertSuccess(response);
	}

	@Test
	public void testDeleteAdministrator_DAC_notAuthorized() {
		authenticatedClientService.setAuthenticatedClient(dac.getPublicCert());

		DeleteAdministrator req = new DeleteAdministrator();
		req.setSessionId(DAC_SESSION_ID);

		AdministratorIdentity u = new AdministratorIdentity();
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		req.setAdministratorIdentity(u);
		DeleteAdministratorResponse response = zas.deleteAdministrator(req);
		assertError(ErrorCode.NonAdministratorAccess, response);
	}

	private void assertSuccess(Acknowledge ack) {
		assertNotNull(ack);
		String errorDesc = ack.getError() != null ? ack.getError().getDescription() : "ok";
		assertTrue("Error " + errorDesc, ack.isSuccess());
		assertNull(ack.getError());
	}

	private void assertError(ErrorCode expected, Acknowledge ack) {
		assertNotNull(ack);
		String errorDesc = ack.getError() != null ? ack.getError().getDescription() : "ok";
		assertFalse(errorDesc, ack.isSuccess());
		assertNotNull(ack.getError());
		assertEquals(expected.getErrorCode(), ack.getError().getCode());
		assertEquals(expected.getErrorDescription(), ack.getError().getDescription());
	}

	private void removeDestinations(Domain domain) {
		// delete any Destination on the domain
		org.tdmx.lib.zone.domain.DestinationSearchCriteria ftSc = new org.tdmx.lib.zone.domain.DestinationSearchCriteria(
				new PageSpecifier(0, 1000));
		ftSc.getDestination().setDomainName(domain.getDomainName());
		List<org.tdmx.lib.zone.domain.Destination> ftlist = destinationService.search(zone, ftSc);
		for (org.tdmx.lib.zone.domain.Destination ft : ftlist) {
			destinationService.delete(ft);
		}
	}

	private void removeChannels(Domain domain) {
		// delete any ChannelAuthorizations on the domain
		org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria caSc = new org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria(
				new PageSpecifier(0, 1000));
		caSc.setDomainName(domain.getDomainName());
		List<org.tdmx.lib.zone.domain.Channel> channelList = channelService.search(zone, caSc);
		for (org.tdmx.lib.zone.domain.Channel c : channelList) {
			channelService.delete(c);
		}
	}

	private void removeAgentCredentials(Domain domain) {
		// delete any UC+DAC on the domain
		org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria dacSc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dacSc.setDomainName(domain.getDomainName());
		List<AgentCredential> list = agentCredentialService.search(zone, dacSc);
		for (AgentCredential ac : list) {
			agentCredentialService.delete(ac);
		}
	}

	private void removeAgentCredentials(Domain domain, AgentCredentialType type) {
		// delete any UC+DAC on the domain
		org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria dacSc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dacSc.setDomainName(domain.getDomainName());
		dacSc.setType(type);
		List<AgentCredential> list = agentCredentialService.search(zone, dacSc);
		for (AgentCredential ac : list) {
			agentCredentialService.delete(ac);
		}
	}

	private void removeAddresses(Domain domain) {
		// delete any Address on the domain
		org.tdmx.lib.zone.domain.AddressSearchCriteria adSc = new org.tdmx.lib.zone.domain.AddressSearchCriteria(
				new PageSpecifier(0, 1000));
		adSc.setDomainName(domain.getDomainName());
		List<org.tdmx.lib.zone.domain.Address> addresses = addressService.search(zone, adSc);
		for (org.tdmx.lib.zone.domain.Address ad : addresses) {
			addressService.delete(ad);
		}
	}

	private void removeServices(Domain domain) {
		// delete any services on the domain
		org.tdmx.lib.zone.domain.ServiceSearchCriteria sSc = new org.tdmx.lib.zone.domain.ServiceSearchCriteria(
				new PageSpecifier(0, 1000));
		sSc.setDomainName(domain.getDomainName());
		List<org.tdmx.lib.zone.domain.Service> services = serviceService.search(zone, sSc);
		for (org.tdmx.lib.zone.domain.Service s : services) {
			serviceService.delete(s);
		}
	}
}
