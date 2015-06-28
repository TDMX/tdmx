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
import java.util.List;

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
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Channelauthorization;
import org.tdmx.core.api.v01.msg.CredentialStatus;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Destination;
import org.tdmx.core.api.v01.msg.DomainFilter;
import org.tdmx.core.api.v01.msg.EndpointPermission;
import org.tdmx.core.api.v01.msg.FlowControlLimit;
import org.tdmx.core.api.v01.msg.FlowDestination;
import org.tdmx.core.api.v01.msg.FlowFilter;
import org.tdmx.core.api.v01.msg.FlowTargetFilter;
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
import org.tdmx.core.api.v01.zas.ModifyFlowTarget;
import org.tdmx.core.api.v01.zas.ModifyFlowTargetResponse;
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
import org.tdmx.core.api.v01.zas.SearchFlow;
import org.tdmx.core.api.v01.zas.SearchFlowResponse;
import org.tdmx.core.api.v01.zas.SearchFlowTarget;
import org.tdmx.core.api.v01.zas.SearchFlowTargetResponse;
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
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.FlowTargetService;
import org.tdmx.lib.zone.service.MockZonePartitionIdInstaller;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AgentCredentialAuthorizationService.AuthorizationResult;
import org.tdmx.server.ws.security.service.AuthenticatedAgentService;

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
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;

	@Autowired
	private ZoneService zoneService;
	@Autowired
	private AuthenticatedAgentService authenticatedAgentService;
	@Autowired
	private DomainService domainService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private ServiceService serviceService;
	@Autowired
	private ChannelService channelService;
	@Autowired
	private FlowTargetService flowTargetService;

	@Autowired
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

	// private String localName;
	// private String serviceName;
	// private String domainName;

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

		uc2 = TestCredentialGenerator.createUC(dac, address.getLocalName(), 2);

		String subdomain = domain.getDomainName().substring(0, domain.getDomainName().indexOf(zone.getZoneApex()) - 1);
		dac2 = TestCredentialGenerator.createDAC(zac, subdomain, 2);
		// ZAS should use the "authenticated agent" to set the partitionID
	}

	@After
	public void doTeardown() {
		authenticatedAgentService.clearAuthenticatedAgent();

		dataGenerator.tearDown(input, data);
	}

	@Test
	public void testAutowired() {
		assertNotNull(zoneService);
		assertNotNull(agentCredentialService);
		assertNotNull(agentCredentialFactory);
		assertNotNull(authenticatedAgentService);
		assertNotNull(domainService);
		assertNotNull(addressService);

		// the service under test...
		assertNotNull(zas);
	}

	@Test
	public void testSearchDomain_ZAC_all() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchDomain req = new SearchDomain();

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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchDomain req = new SearchDomain();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchDomain req = new SearchDomain();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		DomainFilter uf = new DomainFilter();
		req.setFilter(uf);

		SearchDomainResponse response = zas.searchDomain(req);
		assertError(ErrorCode.NonZoneAdministratorAccess, response);
	}

	@Test
	public void testSearchUser_ZAC_all() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchAddress req = new SearchAddress();

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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchAddress req = new SearchAddress();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchAddress req = new SearchAddress();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchAddress req = new SearchAddress();

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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchService req = new SearchService();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ServiceFilter uf = new ServiceFilter();
		req.setFilter(uf);

		SearchServiceResponse response = zas.searchService(req);
		assertSuccess(response);
		assertEquals(1, response.getServicestates().size());
	}

	@Test
	public void testSearchService_ZAC_invalidZone() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchService req = new SearchService();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchService req = new SearchService();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ServiceFilter uf = new ServiceFilter();
		req.setFilter(uf);

		SearchServiceResponse response = zas.searchService(req);
		assertSuccess(response);
		assertEquals(1, response.getServicestates().size());
	}

	@Test
	public void testSearchService_DAC_serviceName() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchService req = new SearchService();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ServiceFilter uf = new ServiceFilter();
		uf.setServicename(service.getServiceName());
		req.setFilter(uf);

		SearchServiceResponse response = zas.searchService(req);
		assertSuccess(response);
		assertEquals(1, response.getServicestates().size());
	}

	@Test
	public void testSearchUser_DAC_suspended() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchAddress req = new SearchAddress();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchService req = new SearchService();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain(dac.getPublicCert().getCommonName()); // DAC's cn is the domain
		CreateDomainResponse response = zas.createDomain(req);
		assertError(ErrorCode.NonZoneAdministratorAccess, response);
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_ZAC_nonsubdomain() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain("not.a.subdomain.com"); // ZAC can only create subdomains of their root
		CreateDomainResponse response = zas.createDomain(req);
		assertError(ErrorCode.OutOfZoneAccess, response);
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_wrongDomain() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain("UPPERCASE." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		CreateDomainResponse response = zas.createDomain(req);
		assertError(ErrorCode.NotNormalizedDomain, response);
	}

	@Test
	public void testCreateDomain_Success() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain("lowercasesubdomain." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		CreateDomainResponse response = zas.createDomain(req);
		assertSuccess(response);
	}

	@Test
	public void testSearchAdministrator_ZAC_all() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchAdministrator req = new SearchAdministrator();

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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchAdministrator req = new SearchAdministrator();

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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchAdministrator req = new SearchAdministrator();

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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchAdministrator req = new SearchAdministrator();

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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchAdministrator req = new SearchAdministrator();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		AdministratorFilter uf = new AdministratorFilter();
		req.setFilter(uf);

		SearchAdministratorResponse response = zas.searchAdministrator(req);
		assertError(ErrorCode.NonZoneAdministratorAccess, response);
	}

	@Test
	@Ignore
	public void testSearchIpZone() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateAddress_ZAC() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain(dac.getPublicCert().getCommonName());
		ucAddress.setLocalname("anewaddressname");

		CreateAddress ca = new CreateAddress();
		ca.setAddress(ucAddress);

		CreateAddressResponse response = zas.createAddress(ca);
		assertSuccess(response);
	}

	@Test
	public void testCreateAddress_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain(dac.getPublicCert().getCommonName());
		ucAddress.setLocalname("anewaddressname");

		CreateAddress ca = new CreateAddress();
		ca.setAddress(ucAddress);

		CreateAddressResponse response = zas.createAddress(ca);
		assertSuccess(response);
	}

	@Test
	public void testCreateAddress_ZAC_MissingDomain() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain("unknownsubdomain." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		ucAddress.setLocalname(uc.getPublicCert().getCommonName());

		CreateAddress ca = new CreateAddress();
		ca.setAddress(ucAddress);

		CreateAddressResponse response = zas.createAddress(ca);
		assertError(ErrorCode.DomainNotFound, response);
	}

	@Test
	public void testCreateService_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// create the service
		Service s = new Service();
		s.setDomain(dac.getPublicCert().getCommonName());
		s.setServicename("anewservicename");

		CreateService ca = new CreateService();
		ca.setService(s);
		ca.setConcurrencyLimit(1);

		CreateServiceResponse response = zas.createService(ca);
		assertSuccess(response);
	}

	@Test
	public void testCreateService_ZAC_MissingDomain() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// create the service
		Service service = new Service();
		service.setDomain("unknownsubdomain." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		service.setServicename("anyoldservicename");

		CreateService ca = new CreateService();
		ca.setService(service);
		ca.setConcurrencyLimit(10);

		CreateServiceResponse response = zas.createService(ca);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteDomain ca = new DeleteDomain();

		ca.setDomain(domain.getDomainName());
		DeleteDomainResponse response = zas.deleteDomain(ca);
		assertError(ErrorCode.DomainAdministratorCredentialsExist, response);
	}

	@Test
	public void testDeleteDomain_DAC_notAuthorized() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteDomain ca = new DeleteDomain();

		ca.setDomain(domain.getDomainName());
		DeleteDomainResponse response = zas.deleteDomain(ca);
		assertError(ErrorCode.NonZoneAdministratorAccess, response);
	}

	@Test
	public void testDeleteDomain_ZAC_AddressesExist() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// delete any DACs on the domain
		org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria dacSc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dacSc.setDomainName(domain.getDomainName());
		dacSc.setType(AgentCredentialType.DAC);
		List<AgentCredential> list = agentCredentialService.search(zone, dacSc);
		for (AgentCredential ac : list) {
			agentCredentialService.delete(ac);
		}

		DeleteDomain ca = new DeleteDomain();

		ca.setDomain(domain.getDomainName());
		DeleteDomainResponse response = zas.deleteDomain(ca);
		assertError(ErrorCode.AddressesExist, response);
	}

	@Test
	public void testDeleteDomain_ZAC_ServicesExist() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// delete any FlowTarget on the domain
		org.tdmx.lib.zone.domain.FlowTargetSearchCriteria ftSc = new org.tdmx.lib.zone.domain.FlowTargetSearchCriteria(
				new PageSpecifier(0, 1000));
		ftSc.getTarget().setDomainName(domain.getDomainName());
		List<FlowTarget> ftlist = flowTargetService.search(zone, ftSc);
		for (FlowTarget ft : ftlist) {
			flowTargetService.delete(ft);
		}

		// delete any Agent on the domain
		org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria dacSc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dacSc.setDomainName(domain.getDomainName());
		List<AgentCredential> list = agentCredentialService.search(zone, dacSc);
		for (AgentCredential ac : list) {
			agentCredentialService.delete(ac);
		}

		// delete any address on the domain
		org.tdmx.lib.zone.domain.AddressSearchCriteria adSc = new org.tdmx.lib.zone.domain.AddressSearchCriteria(
				new PageSpecifier(0, 1000));
		adSc.setDomainName(domain.getDomainName());
		List<org.tdmx.lib.zone.domain.Address> addresses = addressService.search(zone, adSc);
		for (org.tdmx.lib.zone.domain.Address ad : addresses) {
			addressService.delete(ad);
		}

		DeleteDomain ca = new DeleteDomain();

		ca.setDomain(domain.getDomainName());
		DeleteDomainResponse response = zas.deleteDomain(ca);
		assertError(ErrorCode.ServicesExist, response);
	}

	@Test
	public void testDeleteDomain_ZAC_ok() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		removeFlowTargets(domain);
		removeChannelAuthorizations(domain);
		removeAgentCredentials(domain);
		removeAddresses(domain);
		removeServices(domain);

		DeleteDomain ca = new DeleteDomain();

		ca.setDomain(domain.getDomainName());
		DeleteDomainResponse response = zas.deleteDomain(ca);
		assertSuccess(response);
	}

	@Test
	@Ignore
	public void testReport() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDeleteUser_ZAC() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteUser ca = new DeleteUser();
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUserIdentity(u);
		DeleteUserResponse response = zas.deleteUser(ca);
		assertSuccess(response);
	}

	@Test
	public void testModiyUser_ZAC_suspended() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ModifyUser ca = new ModifyUser();
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUserIdentity(u);
		ca.setStatus(CredentialStatus.SUSPENDED);
		ModifyUserResponse response = zas.modifyUser(ca);
		assertSuccess(response);
		// TODO check susp.
	}

	@Test
	public void testModiyService_ZAC() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ModifyService ca = new ModifyService();
		Service u = new Service();
		u.setDomain(domain.getDomainName());
		u.setServicename(service.getServiceName());

		ca.setService(u);
		ca.setConcurrencyLimit(100);
		ModifyServiceResponse response = zas.modifyService(ca);
		assertSuccess(response);
		// TODO check susp.
	}

	@Test
	public void testModiyService_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ModifyService ca = new ModifyService();
		Service u = new Service();
		u.setDomain(domain.getDomainName());
		u.setServicename(service.getServiceName());

		ca.setService(u);
		ca.setConcurrencyLimit(99);
		ModifyServiceResponse response = zas.modifyService(ca);
		assertSuccess(response);
		// TODO check limit.
	}

	// TODO delete user which has is a target Channel.ChannelFlowTarget (+Flows)
	@Test
	public void testDeleteUser_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteUser ca = new DeleteUser();
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUserIdentity(u);
		DeleteUserResponse response = zas.deleteUser(ca);
		assertSuccess(response);
	}

	@Test
	public void testModiyUser_DAC_suspended() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ModifyUser ca = new ModifyUser();
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUserIdentity(u);
		ca.setStatus(CredentialStatus.SUSPENDED);
		ModifyUserResponse response = zas.modifyUser(ca);
		assertSuccess(response);
	}

	@Test
	public void testModifyFlowTarget_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		FlowDestination fd = new FlowDestination();
		fd.setServicename(service.getServiceName());
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());
		fd.setTarget(u);

		ModifyFlowTarget ft = new ModifyFlowTarget();
		ft.setFlowdestination(fd);
		ft.setConcurrencyLimit(100);

		ModifyFlowTargetResponse response = zas.modifyFlowTarget(ft);
		assertSuccess(response);

		// TODO test changed

		// TODO testModifyFlowTarget_DAC
	}

	@Test
	public void testModifyFlowTarget_ZAC() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		FlowDestination fd = new FlowDestination();
		fd.setServicename(service.getServiceName());
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());
		fd.setTarget(u);

		ModifyFlowTarget ft = new ModifyFlowTarget();
		ft.setFlowdestination(fd);
		ft.setConcurrencyLimit(999);

		ModifyFlowTargetResponse response = zas.modifyFlowTarget(ft);
		assertSuccess(response);

		// TODO test changed
	}

	@Test
	@Ignore
	public void testModifyIpZone() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateAdministrator_Success() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateAdministrator ca = new CreateAdministrator();
		ca.setStatus(CredentialStatus.ACTIVE);
		AdministratorIdentity a = new AdministratorIdentity();
		a.setDomaincertificate(dac2.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac2.getIssuerPublicCert().getX509Encoded());

		ca.setAdministratorIdentity(a);
		CreateAdministratorResponse response = zas.createAdministrator(ca);
		assertSuccess(response);
	}

	@Test
	public void testCreateAdministrator_Success_DefaultStatus() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateAdministrator ca = new CreateAdministrator();
		AdministratorIdentity a = new AdministratorIdentity();
		a.setDomaincertificate(dac2.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac2.getIssuerPublicCert().getX509Encoded());

		ca.setAdministratorIdentity(a);
		CreateAdministratorResponse response = zas.createAdministrator(ca);
		assertSuccess(response);
	}

	@Test
	public void testCreateAdministrator_DACExists() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateAdministrator ca = new CreateAdministrator();
		ca.setStatus(CredentialStatus.ACTIVE);
		AdministratorIdentity a = new AdministratorIdentity();
		a.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministratorIdentity(a);
		CreateAdministratorResponse response = zas.createAdministrator(ca);
		assertError(ErrorCode.DomainAdministratorCredentialsExist, response);
	}

	@Test
	public void testCreateAdministrator_DomainNotExists() throws Exception {
		// create new credential for non-existent domain
		zonePartitionIdProvider.setPartitionId(MockZonePartitionIdInstaller.ZP1_S1);
		PKIXCredential dac3 = TestCredentialGenerator.createDAC(zac, "gugus", 2);

		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateAdministrator ca = new CreateAdministrator();
		ca.setStatus(CredentialStatus.ACTIVE);
		AdministratorIdentity a = new AdministratorIdentity();
		a.setDomaincertificate(dac3.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac3.getIssuerPublicCert().getX509Encoded());

		ca.setAdministratorIdentity(a);
		CreateAdministratorResponse response = zas.createAdministrator(ca);
		assertError(ErrorCode.DomainNotFound, response);
	}

	@Test
	public void testSearchFlowTarget_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchFlowTarget req = new SearchFlowTarget();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		FlowTargetFilter ftf = new FlowTargetFilter();
		req.setFilter(ftf);

		SearchFlowTargetResponse response = zas.searchFlowTarget(req);
		assertSuccess(response);
		assertEquals(1, response.getFlowtargets().size());
		// TODO alternatives
	}

	@Test
	public void testDeleteService_ZAC_ChannelsExist() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteService ca = new DeleteService();
		Service s = new Service();
		s.setDomain(domain.getDomainName());
		s.setServicename(service.getServiceName());

		ca.setService(s);

		DeleteServiceResponse response = zas.deleteService(ca);
		assertError(ErrorCode.ChannelAuthorizationExist, response);
	}

	@Test
	public void testDeleteService_ZAC() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		removeChannels(domain, service);

		DeleteService ca = new DeleteService();
		Service s = new Service();
		s.setDomain(domain.getDomainName());
		s.setServicename(service.getServiceName());

		ca.setService(s);

		DeleteServiceResponse response = zas.deleteService(ca);
		assertSuccess(response);
	}

	@Test
	public void testDeleteService_DAC_ChannelsExist() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteService ca = new DeleteService();
		Service s = new Service();
		s.setDomain(domain.getDomainName());
		s.setServicename(service.getServiceName());

		ca.setService(s);

		DeleteServiceResponse response = zas.deleteService(ca);
		assertError(ErrorCode.ChannelAuthorizationExist, response);
	}

	@Test
	public void testDeleteService_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		removeChannels(domain, service);

		DeleteService ca = new DeleteService();
		Service s = new Service();
		s.setDomain(domain.getDomainName());
		s.setServicename(service.getServiceName());

		ca.setService(s);

		DeleteServiceResponse response = zas.deleteService(ca);
		assertSuccess(response);
	}

	@Test
	public void testDeleteAddress_ZAC_ok() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain(dac.getPublicCert().getCommonName());
		ucAddress.setLocalname("anewaddressname");

		CreateAddress ca = new CreateAddress();
		ca.setAddress(ucAddress);

		CreateAddressResponse response = zas.createAddress(ca);
		assertSuccess(response);

		DeleteAddress da = new DeleteAddress();
		da.setAddress(ucAddress);

		DeleteAddressResponse daRes = zas.deleteAddress(da);
		assertSuccess(daRes);
	}

	@Test
	public void testDeleteAddress_ZAC_UCsExist() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		Address a = new Address();
		a.setDomain(dac.getPublicCert().getCommonName());
		a.setLocalname(uc.getPublicCert().getCommonName());

		DeleteAddress request = new DeleteAddress();
		request.setAddress(a);

		DeleteAddressResponse response = zas.deleteAddress(request);
		assertError(ErrorCode.UserCredentialsExist, response);
	}

	@Test
	public void testSearchFlow_DAC_All() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchFlow req = new SearchFlow();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		FlowFilter ff = new FlowFilter();
		req.setFilter(ff);

		SearchFlowResponse response = zas.searchFlow(req);
		assertSuccess(response);
		assertEquals(1, response.getFlows().size());
	}

	@Test
	public void testSetChannelAuthorization_SendRecvSameDomain() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SetChannelAuthorization req = new SetChannelAuthorization();
		req.setDomain(domain.getDomainName());
		Currentchannelauthorization auth = new Currentchannelauthorization();

		Channel channel = new Channel();
		Destination dest = new Destination();
		dest.setDomain(domain.getDomainName());
		dest.setLocalname(uc.getPublicCert().getCommonName());
		dest.setServicename(service.getServiceName());
		dest.setServiceprovider("SP"); // TODO
		channel.setDestination(dest);

		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain(domain.getDomainName());
		origin.setLocalname(uc.getPublicCert().getCommonName());
		origin.setServiceprovider("SP"); // TODO
		channel.setOrigin(origin);
		auth.setChannel(channel);

		Date oneMonth = CalendarUtils.getDateWithOffset(new Date(), Calendar.MONTH, 1);
		EndpointPermission recvPermission = new EndpointPermission();
		recvPermission.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		recvPermission.setPermission(Permission.ALLOW);
		recvPermission.setValidUntil(CalendarUtils.cast(oneMonth));
		auth.setDestination(recvPermission);
		SignatureUtils.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_256_RSA, new Date(), channel,
				recvPermission);

		EndpointPermission sendPermission = new EndpointPermission();
		sendPermission.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		sendPermission.setPermission(Permission.ALLOW);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SetChannelAuthorization req = new SetChannelAuthorization();
		req.setDomain(domain.getDomainName());
		Currentchannelauthorization auth = new Currentchannelauthorization();

		Channel channel = new Channel();
		Destination dest = new Destination();
		dest.setDomain(domain.getDomainName());
		dest.setLocalname(uc.getPublicCert().getCommonName());
		dest.setServicename("gugus");
		dest.setServiceprovider("SP"); // TODO
		channel.setDestination(dest);

		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain(domain.getDomainName());
		origin.setLocalname(uc.getPublicCert().getCommonName());
		origin.setServiceprovider("SP"); // TODO
		channel.setOrigin(origin);
		auth.setChannel(channel);

		Date oneMonth = CalendarUtils.getDateWithOffset(new Date(), Calendar.MONTH, 1);
		EndpointPermission recvPermission = new EndpointPermission();
		recvPermission.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		recvPermission.setPermission(Permission.ALLOW);
		recvPermission.setValidUntil(CalendarUtils.cast(oneMonth));
		auth.setDestination(recvPermission);
		SignatureUtils.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_256_RSA, new Date(), channel,
				recvPermission);

		EndpointPermission sendPermission = new EndpointPermission();
		sendPermission.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		sendPermission.setPermission(Permission.ALLOW);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateUser ca = new CreateUser();
		ca.setStatus(CredentialStatus.ACTIVE);
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUserIdentity(u);
		CreateUserResponse response = zas.createUser(ca);
		assertError(ErrorCode.UserCredentialsExist, response);
	}

	@Test
	public void testCreateUser_ZAC_ok() {
		// create new ZAC credential
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateUser ca = new CreateUser();
		ca.setStatus(CredentialStatus.ACTIVE);
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc2.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUserIdentity(u);
		CreateUserResponse response = zas.createUser(ca);
		assertSuccess(response);

		// TODO check originating flows created by using ZAS#searchFlow
	}

	@Test
	public void testCreateUser_DAC_ok() {
		// create new ZAC credential
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateUser ca = new CreateUser();
		ca.setStatus(CredentialStatus.ACTIVE);
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc2.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUserIdentity(u);
		CreateUserResponse response = zas.createUser(ca);
		assertSuccess(response);

		// TODO check originating flows created by using ZAS#searchFlow
	}

	@Test
	public void testCreateUser_DAC_NOK_AddressNotFound() throws Exception {
		// create new credentials on unexisting address
		PKIXCredential uc3 = TestCredentialGenerator.createUC(dac, "gugus", 2);

		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateUser ca = new CreateUser();
		ca.setStatus(CredentialStatus.ACTIVE);
		UserIdentity u = new UserIdentity();
		u.setUsercertificate(uc3.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUserIdentity(u);
		CreateUserResponse response = zas.createUser(ca);
		assertError(ErrorCode.AddressNotFound, response);
	}

	@Test
	public void testSearchChannelAuthorization_ZAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchChannelAuthorization req = new SearchChannelAuthorization();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ChannelAuthorizationFilter uf = new ChannelAuthorizationFilter();
		req.setFilter(uf);

		SearchChannelAuthorizationResponse response = zas.searchChannelAuthorization(req);
		assertSuccess(response);
		assertEquals(1, response.getChannelauthorizations().size());
		// TODO alternatives
	}

	@Test
	public void testDeleteChannelAuthorization() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchChannelAuthorization req = new SearchChannelAuthorization();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ChannelAuthorizationFilter uf = new ChannelAuthorizationFilter();
		req.setFilter(uf);

		SearchChannelAuthorizationResponse response = zas.searchChannelAuthorization(req);
		assertSuccess(response);
		assertEquals(1, response.getChannelauthorizations().size());
		// TODO alternatives

		Channelauthorization caToDel = response.getChannelauthorizations().get(0);

		DeleteChannelAuthorization delReq = new DeleteChannelAuthorization();
		delReq.setDomain(dac.getPublicCert().getCommonName());
		delReq.setChannel(caToDel.getCurrent().getChannel());

		DeleteChannelAuthorizationResponse delRes = zas.deleteChannelAuthorization(delReq);
		assertSuccess(delRes);

		// check it's gone
		response = zas.searchChannelAuthorization(req);
		assertSuccess(response);
		assertEquals(0, response.getChannelauthorizations().size());
	}

	@Test
	public void testModifyAdministrator_ZAC_suspend() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ModifyAdministrator ca = new ModifyAdministrator();
		AdministratorIdentity u = new AdministratorIdentity();
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministratorIdentity(u);
		ca.setStatus(CredentialStatus.SUSPENDED);
		ModifyAdministratorResponse response = zas.modifyAdministrator(ca);
		assertSuccess(response);
		// TODO check susp.
	}

	@Test
	public void testDeleteAdministrator_ZAC() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteAdministrator ca = new DeleteAdministrator();
		AdministratorIdentity u = new AdministratorIdentity();
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministratorIdentity(u);
		DeleteAdministratorResponse response = zas.deleteAdministrator(ca);
		assertSuccess(response);
	}

	@Test
	public void testDeleteAdministrator_DAC_notAuthorized() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteAdministrator ca = new DeleteAdministrator();
		AdministratorIdentity u = new AdministratorIdentity();
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministratorIdentity(u);
		DeleteAdministratorResponse response = zas.deleteAdministrator(ca);
		assertError(ErrorCode.NonZoneAdministratorAccess, response);
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

	private void removeFlowTargets(Domain domain) {
		// delete any FlowTarget on the domain
		org.tdmx.lib.zone.domain.FlowTargetSearchCriteria ftSc = new org.tdmx.lib.zone.domain.FlowTargetSearchCriteria(
				new PageSpecifier(0, 1000));
		ftSc.getTarget().setDomainName(domain.getDomainName());
		List<FlowTarget> ftlist = flowTargetService.search(zone, ftSc);
		for (FlowTarget ft : ftlist) {
			flowTargetService.delete(ft);
		}
	}

	private void removeChannelAuthorizations(Domain domain) {
		// delete any ChannelAuthorizations on the domain
		org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria caSc = new org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria(
				new PageSpecifier(0, 1000));
		caSc.setDomainName(domain.getDomainName());
		List<ChannelAuthorization> calist = channelService.search(zone, caSc);
		for (ChannelAuthorization ca : calist) {
			channelService.delete(ca.getChannel());
		}
	}

	private void removeChannels(Domain domain, org.tdmx.lib.zone.domain.Service service) {
		// delete any ChannelAuthorizations on the domain
		org.tdmx.lib.zone.domain.ChannelSearchCriteria caSc = new org.tdmx.lib.zone.domain.ChannelSearchCriteria(
				new PageSpecifier(0, 1000));
		caSc.setDomainName(domain.getDomainName());
		caSc.getDestination().setServiceName(service.getServiceName());
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
