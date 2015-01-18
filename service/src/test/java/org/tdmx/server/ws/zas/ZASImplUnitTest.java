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
import org.tdmx.core.api.v01.sp.zas.CreateUser;
import org.tdmx.core.api.v01.sp.zas.CreateUserResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteAddress;
import org.tdmx.core.api.v01.sp.zas.DeleteAddressResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteAdministrator;
import org.tdmx.core.api.v01.sp.zas.DeleteAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteUser;
import org.tdmx.core.api.v01.sp.zas.DeleteUserResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyAdministrator;
import org.tdmx.core.api.v01.sp.zas.ModifyAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyUser;
import org.tdmx.core.api.v01.sp.zas.ModifyUserResponse;
import org.tdmx.core.api.v01.sp.zas.SearchAddress;
import org.tdmx.core.api.v01.sp.zas.SearchAddressResponse;
import org.tdmx.core.api.v01.sp.zas.SearchAdministrator;
import org.tdmx.core.api.v01.sp.zas.SearchAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.SearchDomain;
import org.tdmx.core.api.v01.sp.zas.SearchDomainResponse;
import org.tdmx.core.api.v01.sp.zas.SearchUser;
import org.tdmx.core.api.v01.sp.zas.SearchUserResponse;
import org.tdmx.core.api.v01.sp.zas.common.Page;
import org.tdmx.core.api.v01.sp.zas.msg.Address;
import org.tdmx.core.api.v01.sp.zas.msg.AddressFilter;
import org.tdmx.core.api.v01.sp.zas.msg.Administrator;
import org.tdmx.core.api.v01.sp.zas.msg.AdministratorFilter;
import org.tdmx.core.api.v01.sp.zas.msg.CredentialStatus;
import org.tdmx.core.api.v01.sp.zas.msg.DomainFilter;
import org.tdmx.core.api.v01.sp.zas.msg.User;
import org.tdmx.core.api.v01.sp.zas.msg.UserFilter;
import org.tdmx.core.api.v01.sp.zas.ws.ZAS;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneStatus;
import org.tdmx.lib.zone.domain.AddressID;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainID;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.DomainService;
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
	private ZAS zas;

	private String zoneApex;
	private AccountZone accountZone;
	private PKIXCredential zac;
	private PKIXCredential dac;
	private PKIXCredential uc;

	@Before
	public void doSetup() throws Exception {
		// uc/dac/zac.keystore created by KeyStoreUtilsTest#storeCreateClientKeystores

		byte[] zacFile = FileUtils.getFileContents("src/test/resources/zac.keystore");
		assertNotNull(zacFile);
		zac = KeyStoreUtils.getPrivateCredential(zacFile, "jks", "changeme", "client");
		// ZAC, DAC and UC need to be all on the same zoneApex
		zoneApex = zac.getPublicCert().getTdmxZoneInfo().getZoneRoot();

		byte[] dacFile = FileUtils.getFileContents("src/test/resources/dac.keystore");
		assertNotNull(dacFile);
		dac = KeyStoreUtils.getPrivateCredential(dacFile, "jks", "changeme", "client");

		byte[] ucFile = FileUtils.getFileContents("src/test/resources/uc.keystore");
		assertNotNull(ucFile);
		uc = KeyStoreUtils.getPrivateCredential(ucFile, "jks", "changeme", "client");

		AgentCredential zoneAC = agentCredentialFactory.createAgentCredential(zac.getCertificateChain());
		zoneAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(zoneAC);
		assertEquals(zoneApex, zoneAC.getId().getZoneApex());
		agentCredentialService.createOrUpdate(zoneAC);

		AgentCredential domainAC = agentCredentialFactory.createAgentCredential(dac.getCertificateChain());
		domainAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(domainAC);
		assertNotNull(domainAC.getId().getZoneApex());
		agentCredentialService.createOrUpdate(domainAC);

		// we create the domain of the dac
		DomainID domId = new DomainID(dac.getPublicCert().getCommonName(), zoneApex);
		Domain dacDomain = new Domain(domId);
		domainService.createOrUpdate(dacDomain);

		AgentCredential userAC = agentCredentialFactory.createAgentCredential(uc.getCertificateChain());
		userAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(userAC);
		assertEquals(zoneApex, userAC.getId().getZoneApex());
		agentCredentialService.createOrUpdate(userAC);

		AddressID aid = new AddressID(uc.getPublicCert().getCommonName(), domainAC.getDomainName(), zoneApex);
		org.tdmx.lib.zone.domain.Address userAddress = new org.tdmx.lib.zone.domain.Address(aid);
		addressService.createOrUpdate(userAddress);

		accountZone = new AccountZone();
		accountZone.setAccountId("TEST");
		accountZone.setZoneApex(zoneAC.getId().getZoneApex());
		accountZone.setStatus(AccountZoneStatus.ACTIVE);
		accountZone.setSegment("test");
		accountZone.setZonePartitionId("default");
	}

	@After
	public void doTeardown() {
		List<AgentCredential> list = agentCredentialService.search(zoneApex,
				new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(new PageSpecifier(0, 1000)));
		for (AgentCredential ac : list) {
			agentCredentialService.delete(ac);
		}

		List<org.tdmx.lib.zone.domain.Address> addresses = addressService.search(zoneApex,
				new org.tdmx.lib.zone.domain.AddressSearchCriteria(new PageSpecifier(0, 1000)));
		for (org.tdmx.lib.zone.domain.Address a : addresses) {
			log.info("Cleanup " + a);
			addressService.delete(a);
		}

		List<Domain> domains = domainService.search(zoneApex, new DomainSearchCriteria(new PageSpecifier(0, 1000)));
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.OutOfZoneAccess, response.getError());
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.NonZoneAdministratorAccess, response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.OutOfZoneAccess, response.getError());
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.OutOfZoneAccess, response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
		assertEquals(1, response.getAddresses().size());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		uf.setDomain("unknownsubdomain." + zoneApex);
		req.setFilter(uf);

		SearchUserResponse response = zas.searchUser(req);
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.OutOfDomainAccess, response.getError());
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
		uf.setDomain("unknownsubdomain." + zoneApex);
		req.setFilter(uf);

		SearchAddressResponse response = zas.searchAddress(req);
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.OutOfDomainAccess, response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
		assertEquals(1, response.getUserstates().size());
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_DAC() {
		AuthorizationResult r = new AuthorizationResult(dac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain(dac.getPublicCert().getCommonName()); // DAC's cn is the domain
		CreateDomainResponse response = zas.createDomain(req);
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.NonZoneAdministratorAccess, response.getError());
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_ZAC_nonsubdomain() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain("not.a.subdomain.com"); // ZAC can only create subdomains of their root
		CreateDomainResponse response = zas.createDomain(req);
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.OutOfZoneAccess, response.getError());
	}

	@Test
	public void testCreateDomain_AuthorizationFailure_wrongDomain() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain("UPPERCASE." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		CreateDomainResponse response = zas.createDomain(req);
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.NotNormalizedDomain, response.getError());
	}

	@Test
	public void testCreateDomain_Success() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateDomain req = new CreateDomain();
		req.setDomain("lowercasesubdomain." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		CreateDomainResponse response = zas.createDomain(req);
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.OutOfZoneAccess, response.getError());
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.NonZoneAdministratorAccess, response.getError());
	}

	@Test
	@Ignore
	public void testModifyUser() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testSearchIpZone() {
		fail("Not yet implemented");
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
	}

	@Test
	public void testCreateAddress_MissingDomain() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain("unknownsubdomain." + zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());
		ucAddress.setLocalname(uc.getPublicCert().getCommonName());

		CreateAddress ca = new CreateAddress();
		ca.setAddress(ucAddress);

		CreateAddressResponse response = zas.createAddress(ca);
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.DomainNotFound, response.getError());
	}

	@Test
	@Ignore
	public void testDeleteChannelAuthorization() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testIncident() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testCreateIpZone() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testDeleteIpZone() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testDeleteDomain() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testReport() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetChannelAuthorization() {
		fail("Not yet implemented");
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
		// TODO check susp.
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
	}

	@Test
	@Ignore
	public void testModifyFlowTargetState() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testModifyIpZone() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testModifyService() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetFlowState() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testSearchAddress() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateAdministrator_Success() {
		AgentCredential domainAC = agentCredentialFactory.createAgentCredential(dac.getCertificateChain());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
	}

	@Test
	public void testCreateAdministrator_Success_DefaultStatus() {
		AgentCredential domainAC = agentCredentialFactory.createAgentCredential(dac.getCertificateChain());
		agentCredentialService.delete(domainAC);

		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		CreateAdministrator ca = new CreateAdministrator();
		Administrator a = new Administrator();
		a.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		a.setRootcertificate(dac.getIssuerPublicCert().getX509Encoded());

		ca.setAdministrator(a);
		CreateAdministratorResponse response = zas.createAdministrator(ca);
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.DomainAdministratorCredentialsExist, response.getError());
	}

	@Test
	public void testCreateAdministrator_DomainNotExists() {
		// we delete the domain of the dac and the dac.
		DomainID domId = new DomainID(dac.getPublicCert().getCommonName(), zoneApex);
		Domain dacDomain = new Domain(domId);
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.DomainNotFound, response.getError());
	}

	@Test
	@Ignore
	public void testSearchFlowTargetState() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testDeleteService() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetFlowTargetState() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testSearchService() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteAddress_ZAC_ok() {
		// remove UC credentials
		AgentCredential userAC = agentCredentialFactory.createAgentCredential(uc.getCertificateChain());
		agentCredentialService.delete(userAC);

		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		Address a = new Address();
		a.setDomain(dac.getPublicCert().getCommonName());
		a.setLocalname(uc.getPublicCert().getCommonName());

		DeleteAddress request = new DeleteAddress();
		request.setAddress(a);

		DeleteAddressResponse response = zas.deleteAddress(request);
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.UserCredentialsExist, response.getError());
	}

	@Test
	@Ignore
	public void testModifyAdministrator() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testSearchFlowState() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testSetChannelAuthorization() {
		fail("Not yet implemented");
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.UserCredentialsExist, response.getError());
	}

	@Test
	public void testCreateUser_ZAC_ok() {
		// delete the UC setup
		AgentCredential userAC = agentCredentialFactory.createAgentCredential(uc.getCertificateChain());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
	}

	@Test
	public void testCreateUser_AddressNotExists() {
		AddressID aid = new AddressID(uc.getPublicCert().getCommonName(), dac.getPublicCert().getCommonName(), zoneApex);
		org.tdmx.lib.zone.domain.Address userAddress = new org.tdmx.lib.zone.domain.Address(aid);
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.AddressNotFound, response.getError());
	}

	@Test
	@Ignore
	public void testSearchChannelAuthorization() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testCreateService() {
		fail("Not yet implemented");
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertError(ErrorCode.NonZoneAdministratorAccess, response.getError());
	}

	private void assertError(ErrorCode expected, org.tdmx.core.api.v01.sp.zas.common.Error error) {
		assertNotNull(error);
		assertEquals(expected.getErrorCode(), error.getCode());
		assertEquals(expected.getErrorDescription(), error.getDescription());
	}

}
