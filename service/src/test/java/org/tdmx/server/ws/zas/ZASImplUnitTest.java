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

import java.util.List;
import java.util.Random;

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
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.sp.zas.CreateAddress;
import org.tdmx.core.api.v01.sp.zas.CreateAddressResponse;
import org.tdmx.core.api.v01.sp.zas.CreateAdministrator;
import org.tdmx.core.api.v01.sp.zas.CreateAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.CreateDomain;
import org.tdmx.core.api.v01.sp.zas.CreateDomainResponse;
import org.tdmx.core.api.v01.sp.zas.CreateService;
import org.tdmx.core.api.v01.sp.zas.CreateServiceResponse;
import org.tdmx.core.api.v01.sp.zas.CreateUser;
import org.tdmx.core.api.v01.sp.zas.CreateUserResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteAddress;
import org.tdmx.core.api.v01.sp.zas.DeleteAddressResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteAdministrator;
import org.tdmx.core.api.v01.sp.zas.DeleteAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteDomain;
import org.tdmx.core.api.v01.sp.zas.DeleteDomainResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteService;
import org.tdmx.core.api.v01.sp.zas.DeleteServiceResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteUser;
import org.tdmx.core.api.v01.sp.zas.DeleteUserResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyAdministrator;
import org.tdmx.core.api.v01.sp.zas.ModifyAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyService;
import org.tdmx.core.api.v01.sp.zas.ModifyServiceResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyUser;
import org.tdmx.core.api.v01.sp.zas.ModifyUserResponse;
import org.tdmx.core.api.v01.sp.zas.SearchAddress;
import org.tdmx.core.api.v01.sp.zas.SearchAddressResponse;
import org.tdmx.core.api.v01.sp.zas.SearchAdministrator;
import org.tdmx.core.api.v01.sp.zas.SearchAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.SearchDomain;
import org.tdmx.core.api.v01.sp.zas.SearchDomainResponse;
import org.tdmx.core.api.v01.sp.zas.SearchService;
import org.tdmx.core.api.v01.sp.zas.SearchServiceResponse;
import org.tdmx.core.api.v01.sp.zas.SearchUser;
import org.tdmx.core.api.v01.sp.zas.SearchUserResponse;
import org.tdmx.core.api.v01.sp.zas.common.Acknowledge;
import org.tdmx.core.api.v01.sp.zas.common.Page;
import org.tdmx.core.api.v01.sp.zas.msg.Address;
import org.tdmx.core.api.v01.sp.zas.msg.AddressFilter;
import org.tdmx.core.api.v01.sp.zas.msg.Administrator;
import org.tdmx.core.api.v01.sp.zas.msg.AdministratorFilter;
import org.tdmx.core.api.v01.sp.zas.msg.CredentialStatus;
import org.tdmx.core.api.v01.sp.zas.msg.DomainFilter;
import org.tdmx.core.api.v01.sp.zas.msg.Service;
import org.tdmx.core.api.v01.sp.zas.msg.ServiceFilter;
import org.tdmx.core.api.v01.sp.zas.msg.User;
import org.tdmx.core.api.v01.sp.zas.msg.UserFilter;
import org.tdmx.core.api.v01.sp.zas.ws.ZAS;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneStatus;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.server.ws.security.service.AgentCredentialAuthorizationService.AuthorizationResult;
import org.tdmx.server.ws.security.service.AuthenticatedAgentService;
import org.tdmx.server.ws.zas.ZASImpl.ErrorCode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ZASImplUnitTest {

	private static final Logger log = LoggerFactory.getLogger(ZASImplUnitTest.class);

	@Autowired
	private AgentCredentialService agentCredentialService;
	@Autowired
	private AgentCredentialFactory agentCredentialFactory;

	@Autowired
	private AuthenticatedAgentService authenticatedAgentService;
	@Autowired
	private DomainService domainService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private ServiceService serviceService;

	@Autowired
	private ZAS zas;

	private ZoneReference zone;
	private AccountZone accountZone;
	private PKIXCredential zac;
	private PKIXCredential dac;
	private PKIXCredential uc;
	private String localName;
	private String serviceName;
	private String domainName;

	@Before
	public void doSetup() throws Exception {
		// uc/dac/zac.keystore created by KeyStoreUtilsTest#storeCreateClientKeystores
		byte[] zacFile = FileUtils.getFileContents("src/test/resources/zac.keystore");
		assertNotNull(zacFile);
		zac = KeyStoreUtils.getPrivateCredential(zacFile, "jks", "changeme", "client");
		// ZAC, DAC and UC need to be all on the same zoneApex

		zone = new ZoneReference(new Random().nextLong(), zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());

		byte[] dacFile = FileUtils.getFileContents("src/test/resources/dac.keystore");
		assertNotNull(dacFile);
		dac = KeyStoreUtils.getPrivateCredential(dacFile, "jks", "changeme", "client");

		byte[] ucFile = FileUtils.getFileContents("src/test/resources/uc.keystore");
		assertNotNull(ucFile);
		uc = KeyStoreUtils.getPrivateCredential(ucFile, "jks", "changeme", "client");

		AgentCredential zoneAC = agentCredentialFactory.createAgentCredential(zone, zac.getCertificateChain());
		zoneAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(zoneAC);
		assertEquals(zone, zoneAC.getZoneReference());
		agentCredentialService.createOrUpdate(zoneAC);

		AgentCredential domainAC = agentCredentialFactory.createAgentCredential(zone, dac.getCertificateChain());
		domainAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(domainAC);
		assertEquals(zone, domainAC.getZoneReference());
		agentCredentialService.createOrUpdate(domainAC);

		// we create the domain of the dac
		Domain dacDomain = new Domain(zone);
		domainName = dac.getPublicCert().getCommonName();
		dacDomain.setDomainName(domainName);
		domainService.createOrUpdate(dacDomain);

		AgentCredential userAC = agentCredentialFactory.createAgentCredential(zone, uc.getCertificateChain());
		userAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(userAC);
		assertEquals(zone, userAC.getZoneReference());
		agentCredentialService.createOrUpdate(userAC);

		localName = uc.getPublicCert().getCommonName();
		org.tdmx.lib.zone.domain.Address userAddress = new org.tdmx.lib.zone.domain.Address(zone);
		userAddress.setDomainName(domainName);
		userAddress.setLocalName(localName);
		addressService.createOrUpdate(userAddress);

		serviceName = "service";
		org.tdmx.lib.zone.domain.Service s = new org.tdmx.lib.zone.domain.Service(zone);
		s.setDomainName(domainName);
		s.setServiceName(serviceName);
		s.setConcurrencyLimit(10);
		serviceService.createOrUpdate(s);

		accountZone = new AccountZone();
		accountZone.setId(zone.getTenantId());
		accountZone.setAccountId("TEST");
		accountZone.setZoneApex(zone.getZoneApex());
		accountZone.setStatus(AccountZoneStatus.ACTIVE);
		accountZone.setSegment("test");
		accountZone.setZonePartitionId("default");
	}

	@After
	public void doTeardown() {
		if (authenticatedAgentService.getAuthenticatedAgent() != null) {
			authenticatedAgentService.clearAuthenticatedAgent();
		}

		List<AgentCredential> list = agentCredentialService.search(zone,
				new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(new PageSpecifier(0, 1000)));
		for (AgentCredential ac : list) {
			agentCredentialService.delete(ac);
		}

		List<org.tdmx.lib.zone.domain.Address> addresses = addressService.search(zone,
				new org.tdmx.lib.zone.domain.AddressSearchCriteria(new PageSpecifier(0, 1000)));
		for (org.tdmx.lib.zone.domain.Address a : addresses) {
			log.info("Cleanup " + a);
			addressService.delete(a);
		}

		List<org.tdmx.lib.zone.domain.Service> services = serviceService.search(zone,
				new org.tdmx.lib.zone.domain.ServiceSearchCriteria(new PageSpecifier(0, 1000)));
		for (org.tdmx.lib.zone.domain.Service s : services) {
			log.info("Cleanup " + s);
			serviceService.delete(s);
		}

		List<Domain> domains = domainService.search(zone, new DomainSearchCriteria(new PageSpecifier(0, 1000)));
		for (Domain d : domains) {
			log.info("Cleanup " + d);
			domainService.delete(d);
		}
	}

	@Test
	public void testAutowired() {
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		assertEquals(1, response.getUserstates().size());
	}

	@Test
	public void testSearchAddress_ZAC_all() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		assertEquals(1, response.getUserstates().size());
	}

	@Test
	public void testSearchUser_ZAC_getUser() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		User u = new User();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(uc.getIssuerPublicCert().getX509Encoded());
		u.setRootcertificate(uc.getZoneRootPublicCert().getX509Encoded());
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertSuccess(response);
		assertEquals(1, response.getUserstates().size());
	}

	@Test
	public void testSearchUser_ZAC_invalidZone() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		assertEquals(1, response.getUserstates().size());
	}

	@Test
	public void testSearchAddress_DAC_all() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		assertEquals(1, response.getUserstates().size());
	}

	@Test
	public void testSearchAddress_DAC_addressName() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchService req = new SearchService();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ServiceFilter uf = new ServiceFilter();
		uf.setServicename(serviceName);
		req.setFilter(uf);

		SearchServiceResponse response = zas.searchService(req);
		assertSuccess(response);
		assertEquals(1, response.getServicestates().size());
	}

	@Test
	public void testSearchUser_DAC_suspended() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		assertEquals(0, response.getUserstates().size());
	}

	@Test
	public void testSearchUser_DAC_invalidDomain() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SearchUser req = new SearchUser();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		UserFilter uf = new UserFilter();
		User u = new User();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(uc.getIssuerPublicCert().getX509Encoded());
		u.setRootcertificate(uc.getZoneRootPublicCert().getX509Encoded());
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertSuccess(response);
		assertEquals(1, response.getUserstates().size());
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain(dac.getPublicCert().getCommonName()); // DAC's cn is the domain
		CreateDomainResponse response = zas.createDomain(req);
		assertError(ErrorCode.NonZoneAdministratorAccess, response);
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_ZAC_nonsubdomain() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain("not.a.subdomain.com"); // ZAC can only create subdomains of their root
		CreateDomainResponse response = zas.createDomain(req);
		assertError(ErrorCode.OutOfZoneAccess, response);
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_wrongDomain() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain("UPPERCASE." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		CreateDomainResponse response = zas.createDomain(req);
		assertError(ErrorCode.NotNormalizedDomain, response);
	}

	@Test
	public void testCreateDomain_Success() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain("lowercasesubdomain." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		CreateDomainResponse response = zas.createDomain(req);
		assertSuccess(response);
	}

	@Test
	public void testSearchAdministrator_ZAC_all() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		assertEquals(1, response.getAdministratorstates().size());
	}

	@Test
	public void testSearchAdministrator_ZAC_domain() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		assertEquals(1, response.getAdministratorstates().size());
	}

	@Test
	public void testSearchAdministrator_ZAC_suspended() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		assertEquals(0, response.getAdministratorstates().size());
	}

	@Test
	public void testSearchAdministrator_ZAC_nonsubdomainFails() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
	public void testDeleteChannelAuthorization() {
		fail("Not yet implemented"); // TODO
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
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteDomain ca = new DeleteDomain();

		ca.setDomain(domainName);
		DeleteDomainResponse response = zas.deleteDomain(ca);
		assertError(ErrorCode.DomainAdministratorCredentialsExist, response);
	}

	@Test
	public void testDeleteDomain_DAC_notAuthorized() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteDomain ca = new DeleteDomain();

		ca.setDomain(domainName);
		DeleteDomainResponse response = zas.deleteDomain(ca);
		assertError(ErrorCode.NonZoneAdministratorAccess, response);
	}

	@Test
	public void testDeleteDomain_ZAC_AddressesExist() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// delete any DAC on the domain
		org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria dacSc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dacSc.setDomainName(domainName);
		List<AgentCredential> list = agentCredentialService.search(zone, dacSc);
		for (AgentCredential ac : list) {
			agentCredentialService.delete(ac);
		}

		DeleteDomain ca = new DeleteDomain();

		ca.setDomain(domainName);
		DeleteDomainResponse response = zas.deleteDomain(ca);
		assertError(ErrorCode.AddressesExist, response);
	}

	@Test
	public void testDeleteDomain_ZAC_ServicesExist() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// delete any DAC on the domain
		org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria dacSc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dacSc.setDomainName(domainName);
		List<AgentCredential> list = agentCredentialService.search(zone, dacSc);
		for (AgentCredential ac : list) {
			agentCredentialService.delete(ac);
		}

		// delete any address on the domain
		org.tdmx.lib.zone.domain.AddressSearchCriteria adSc = new org.tdmx.lib.zone.domain.AddressSearchCriteria(
				new PageSpecifier(0, 1000));
		adSc.setDomainName(domainName);
		List<org.tdmx.lib.zone.domain.Address> addresses = addressService.search(zone, adSc);
		for (org.tdmx.lib.zone.domain.Address ad : addresses) {
			addressService.delete(ad);
		}

		DeleteDomain ca = new DeleteDomain();

		ca.setDomain(domainName);
		DeleteDomainResponse response = zas.deleteDomain(ca);
		assertError(ErrorCode.ServicesExist, response);
	}

	@Test
	public void testDeleteDomain_ZAC_ok() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// delete any DAC on the domain
		org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria dacSc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dacSc.setDomainName(domainName);
		List<AgentCredential> list = agentCredentialService.search(zone, dacSc);
		for (AgentCredential ac : list) {
			agentCredentialService.delete(ac);
		}

		// delete any address on the domain
		org.tdmx.lib.zone.domain.AddressSearchCriteria adSc = new org.tdmx.lib.zone.domain.AddressSearchCriteria(
				new PageSpecifier(0, 1000));
		adSc.setDomainName(domainName);
		List<org.tdmx.lib.zone.domain.Address> addresses = addressService.search(zone, adSc);
		for (org.tdmx.lib.zone.domain.Address ad : addresses) {
			addressService.delete(ad);
		}

		// delete any services on the domain
		org.tdmx.lib.zone.domain.ServiceSearchCriteria sSc = new org.tdmx.lib.zone.domain.ServiceSearchCriteria(
				new PageSpecifier(0, 1000));
		sSc.setDomainName(domainName);
		List<org.tdmx.lib.zone.domain.Service> services = serviceService.search(zone, sSc);
		for (org.tdmx.lib.zone.domain.Service s : services) {
			serviceService.delete(s);
		}

		DeleteDomain ca = new DeleteDomain();

		ca.setDomain(domainName);
		DeleteDomainResponse response = zas.deleteDomain(ca);
		assertSuccess(response);
	}

	@Test
	@Ignore
	public void testReport() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testGetChannelAuthorization() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDeleteUser_ZAC() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteUser ca = new DeleteUser();
		User u = new User();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUser(u);
		DeleteUserResponse response = zas.deleteUser(ca);
		assertSuccess(response);
	}

	@Test
	public void testModiyUser_ZAC_suspended() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ModifyUser ca = new ModifyUser();
		User u = new User();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUser(u);
		ca.setStatus(CredentialStatus.SUSPENDED);
		ModifyUserResponse response = zas.modifyUser(ca);
		assertSuccess(response);
		// TODO check susp.
	}

	@Test
	public void testModiyService_ZAC() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ModifyService ca = new ModifyService();
		Service u = new Service();
		u.setDomain(domainName);
		u.setServicename(serviceName);

		ca.setService(u);
		ca.setConcurrencyLimit(100);
		ModifyServiceResponse response = zas.modifyService(ca);
		assertSuccess(response);
		// TODO check susp.
	}

	@Test
	public void testModiyService_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ModifyService ca = new ModifyService();
		Service u = new Service();
		u.setDomain(domainName);
		u.setServicename(serviceName);

		ca.setService(u);
		ca.setConcurrencyLimit(99);
		ModifyServiceResponse response = zas.modifyService(ca);
		assertSuccess(response);
		// TODO check limit.
	}

	@Test
	public void testDeleteUser_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteUser ca = new DeleteUser();
		User u = new User();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUser(u);
		DeleteUserResponse response = zas.deleteUser(ca);
		assertSuccess(response);
	}

	@Test
	public void testModiyUser_DAC_suspended() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ModifyUser ca = new ModifyUser();
		User u = new User();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUser(u);
		ca.setStatus(CredentialStatus.SUSPENDED);
		ModifyUserResponse response = zas.modifyUser(ca);
		assertSuccess(response);
	}

	@Test
	@Ignore
	public void testModifyFlowTargetState() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testModifyIpZone() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testGetFlowState() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateAdministrator_Success() {
		AgentCredential dAC = agentCredentialFactory.createAgentCredential(zone, dac.getCertificateChain());
		AgentCredential domainAC = agentCredentialService.findByFingerprint(zone, dAC.getSha1fingerprint());
		agentCredentialService.delete(domainAC);

		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateAdministrator ca = new CreateAdministrator();
		ca.setStatus(CredentialStatus.ACTIVE);
		Administrator a = new Administrator();
		a.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministrator(a);
		CreateAdministratorResponse response = zas.createAdministrator(ca);
		assertSuccess(response);
	}

	@Test
	public void testCreateAdministrator_Success_DefaultStatus() {
		AgentCredential dAC = agentCredentialFactory.createAgentCredential(zone, dac.getCertificateChain());
		AgentCredential domainAC = agentCredentialService.findByFingerprint(zone, dAC.getSha1fingerprint());
		agentCredentialService.delete(domainAC);

		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateAdministrator ca = new CreateAdministrator();
		Administrator a = new Administrator();
		a.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministrator(a);
		CreateAdministratorResponse response = zas.createAdministrator(ca);
		assertSuccess(response);
	}

	@Test
	public void testCreateAdministrator_DACExists() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateAdministrator ca = new CreateAdministrator();
		ca.setStatus(CredentialStatus.ACTIVE);
		Administrator a = new Administrator();
		a.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministrator(a);
		CreateAdministratorResponse response = zas.createAdministrator(ca);
		assertError(ErrorCode.DomainAdministratorCredentialsExist, response);
	}

	@Test
	public void testCreateAdministrator_DomainNotExists() {
		// we delete the domain of the dac and the dac.
		Domain dacDomain = domainService.findByDomainName(zone, domainName);
		domainService.delete(dacDomain);

		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateAdministrator ca = new CreateAdministrator();
		ca.setStatus(CredentialStatus.ACTIVE);
		Administrator a = new Administrator();
		a.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministrator(a);
		CreateAdministratorResponse response = zas.createAdministrator(ca);
		assertError(ErrorCode.DomainNotFound, response);
	}

	@Test
	@Ignore
	public void testSearchFlowTargetState() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDeleteService_ZAC() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteService ca = new DeleteService();
		Service s = new Service();
		s.setDomain(domainName);
		s.setServicename(serviceName);

		ca.setService(s);

		DeleteServiceResponse response = zas.deleteService(ca);
		assertSuccess(response);
	}

	@Test
	public void testDeleteService_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteService ca = new DeleteService();
		Service s = new Service();
		s.setDomain(domainName);
		s.setServicename(serviceName);

		ca.setService(s);

		DeleteServiceResponse response = zas.deleteService(ca);
		assertSuccess(response);
	}

	@Test
	@Ignore
	public void testGetFlowTargetState() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDeleteAddress_ZAC_ok() {
		// remove UC credentials
		AgentCredential uAC = agentCredentialFactory.createAgentCredential(zone, uc.getCertificateChain());
		AgentCredential userAC = agentCredentialService.findByFingerprint(zone, uAC.getSha1fingerprint());
		agentCredentialService.delete(userAC);

		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		Address a = new Address();
		a.setDomain(dac.getPublicCert().getCommonName());
		a.setLocalname(uc.getPublicCert().getCommonName());

		DeleteAddress request = new DeleteAddress();
		request.setAddress(a);

		DeleteAddressResponse response = zas.deleteAddress(request);
		assertSuccess(response);
	}

	@Test
	public void testDeleteAddress_ZAC_UCsExist() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
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
	@Ignore
	public void testSearchFlowState() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testSetChannelAuthorization() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateUser_ZAC_UCExists() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateUser ca = new CreateUser();
		ca.setStatus(CredentialStatus.ACTIVE);
		User u = new User();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUser(u);
		CreateUserResponse response = zas.createUser(ca);
		assertError(ErrorCode.UserCredentialsExist, response);
	}

	@Test
	public void testCreateUser_ZAC_ok() {
		// delete the UC setup
		AgentCredential uAC = agentCredentialFactory.createAgentCredential(zone, uc.getCertificateChain());
		AgentCredential userAC = agentCredentialService.findByFingerprint(zone, uAC.getSha1fingerprint());
		agentCredentialService.delete(userAC);

		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateUser ca = new CreateUser();
		ca.setStatus(CredentialStatus.ACTIVE);
		User u = new User();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUser(u);
		CreateUserResponse response = zas.createUser(ca);
		assertSuccess(response);
	}

	@Test
	public void testCreateUser_AddressNotExists() {
		org.tdmx.lib.zone.domain.Address userAddress = addressService.findByName(zone, domainName, localName);
		addressService.delete(userAddress);

		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateUser ca = new CreateUser();
		ca.setStatus(CredentialStatus.ACTIVE);
		User u = new User();
		u.setUsercertificate(uc.getPublicCert().getX509Encoded());
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setUser(u);
		CreateUserResponse response = zas.createUser(ca);
		assertError(ErrorCode.AddressNotFound, response);
	}

	@Test
	@Ignore
	public void testSearchChannelAuthorization() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testModifyAdministrator_ZAC_suspend() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ModifyAdministrator ca = new ModifyAdministrator();
		Administrator u = new Administrator();
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministrator(u);
		ca.setStatus(CredentialStatus.SUSPENDED);
		ModifyAdministratorResponse response = zas.modifyAdministrator(ca);
		assertSuccess(response);
		// TODO check susp.
	}

	@Test
	public void testDeleteAdministrator_ZAC() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteAdministrator ca = new DeleteAdministrator();
		Administrator u = new Administrator();
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministrator(u);
		DeleteAdministratorResponse response = zas.deleteAdministrator(ca);
		assertSuccess(response);
	}

	@Test
	public void testDeleteAdministrator_DAC_notAuthorized() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		DeleteAdministrator ca = new DeleteAdministrator();
		Administrator u = new Administrator();
		u.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		u.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministrator(u);
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

}
