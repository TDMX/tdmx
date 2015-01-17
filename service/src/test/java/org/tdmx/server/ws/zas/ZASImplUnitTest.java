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
import org.tdmx.core.api.v01.sp.zas.msg.Address;
import org.tdmx.core.api.v01.sp.zas.msg.Administrator;
import org.tdmx.core.api.v01.sp.zas.msg.CredentialStatus;
import org.tdmx.core.api.v01.sp.zas.msg.User;
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

		AgentCredential userAC = agentCredentialFactory.createAgentCredential(uc.getCertificateChain());
		userAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(userAC);
		assertEquals(zoneApex, userAC.getId().getZoneApex());
		agentCredentialService.createOrUpdate(userAC);

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
	@Ignore
	public void testSearchDomain() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testSearchUser() {
		fail("Not yet implemented");
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
	@Ignore
	public void testSearchAdministrator() {
		fail("Not yet implemented");
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

		// we need the domain to exist before we can create addresses on it.
		DomainID domId = new DomainID(dac.getPublicCert().getCommonName(), zoneApex);
		Domain dacDomain = new Domain(domId);
		domainService.createOrUpdate(dacDomain);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain(dac.getPublicCert().getCommonName());
		ucAddress.setLocalname(uc.getPublicCert().getCommonName());

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

		// we need the domain to exist before we can create addresses on it.
		DomainID domId = new DomainID(dac.getPublicCert().getCommonName(), zoneApex);
		Domain dacDomain = new Domain(domId);
		domainService.createOrUpdate(dacDomain);

		// create the address
		Address ucAddress = new Address();
		ucAddress.setDomain(dac.getPublicCert().getCommonName());
		ucAddress.setLocalname(uc.getPublicCert().getCommonName());

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
	public void testDeleteUser_ZAS() {
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
	@Ignore
	public void testCreateAdministrator_Success() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateAdministrator_DACExists() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// we cannot create a DAC which already exists, setup before
		DomainID domId = new DomainID(dac.getPublicCert().getCommonName(), zoneApex);
		Domain dacDomain = new Domain(domId);
		domainService.createOrUpdate(dacDomain);

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
	public void testDeleteAddress() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// we cannot create a DAC which already exists, setup before
		DomainID domId = new DomainID(dac.getPublicCert().getCommonName(), zoneApex);
		Domain dacDomain = new Domain(domId);
		domainService.createOrUpdate(dacDomain);

		// create the address so we can delete it
		AddressID aId = new AddressID("localname", dac.getPublicCert().getCommonName(), zoneApex);
		org.tdmx.lib.zone.domain.Address ad = new org.tdmx.lib.zone.domain.Address(aId);
		addressService.createOrUpdate(ad);

		Address a = new Address();
		a.setDomain(dac.getPublicCert().getCommonName());
		a.setLocalname("localname");

		DeleteAddress request = new DeleteAddress();
		request.setAddress(a);

		DeleteAddressResponse response = zas.deleteAddress(request);
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getError());
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
	public void testCreateUser_UCExists() {
		AuthorizationResult r = new AuthorizationResult(zac.getPublicCert(), accountZone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		// we cannot create a UC which already exists, setup before
		AddressID addressId = new AddressID(uc.getPublicCert().getCommonName(), dac.getPublicCert().getCommonName(),
				zoneApex);
		org.tdmx.lib.zone.domain.Address address = new org.tdmx.lib.zone.domain.Address(addressId);
		addressService.createOrUpdate(address);

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
	public void testCreateUser_AddressNotExists() {
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
	@Ignore
	public void testGetAgentService() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testSetAgentService() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetDomainService() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testSetDomainService() {
		fail("Not yet implemented");
	}

	private void assertError(ErrorCode expected, org.tdmx.core.api.v01.sp.zas.common.Error error) {
		assertNotNull(error);
		assertEquals(expected.getErrorCode(), error.getCode());
		assertEquals(expected.getErrorDescription(), error.getDescription());
	}

}
