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
package org.tdmx.server.rs.sas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.ws.rs.BadRequestException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialStatus;
import org.tdmx.lib.control.domain.DatabaseType;
import org.tdmx.lib.control.service.AccountService;
import org.tdmx.lib.control.service.AccountZoneAdministrationCredentialService;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.control.service.DatabasePartitionService;
import org.tdmx.lib.control.service.DnsResolverGroupService;
import org.tdmx.lib.control.service.LockService;
import org.tdmx.lib.control.service.MaxValueService;
import org.tdmx.lib.control.service.MockDatabasePartitionInstaller;
import org.tdmx.lib.control.service.SegmentService;
import org.tdmx.lib.control.service.UniqueIdService;
import org.tdmx.server.rs.sas.resource.AccountResource;
import org.tdmx.server.rs.sas.resource.AccountZoneAdministrationCredentialResource;
import org.tdmx.server.rs.sas.resource.AccountZoneResource;
import org.tdmx.server.rs.sas.resource.DatabasePartitionResource;
import org.tdmx.server.rs.sas.resource.DnsResolverGroupResource;
import org.tdmx.server.rs.sas.resource.SegmentResource;
import org.tdmx.server.ws.ErrorCode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/tdmx/test-context.xml")
public class SASImplUnitTest {

	private static final Logger log = LoggerFactory.getLogger(SASImplUnitTest.class);

	@Autowired
	private DnsResolverGroupService dnsResolverGroupService;
	@Autowired
	private SegmentService segmentService;
	@Autowired
	private DatabasePartitionService databasePartitionService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private AccountZoneService accountZoneService;
	@Autowired
	private AccountZoneAdministrationCredentialService accountZoneAdministrationCredentialService;
	@Autowired
	private LockService lockService;
	@Autowired
	private MaxValueService maxValueService;
	@Autowired
	@Qualifier("tdmx.lib.control.AccountIdService")
	private UniqueIdService objectIdService;

	@Autowired
	private SAS sas;

	private DnsResolverGroupResource dnsResolverGroupResource;
	private SegmentResource segmentResource;
	private DatabasePartitionResource partitionResource;
	private AccountResource accountResource;
	private AccountZoneResource accountZoneResource;
	private AccountZoneAdministrationCredentialResource zacResource;

	private String accountEmail;
	private String segmentName;
	private String dnsResolverGroupName;

	@Before
	public void doSetup() throws Exception {

		dnsResolverGroupName = "drg" + System.currentTimeMillis();

		dnsResolverGroupResource = new DnsResolverGroupResource();
		dnsResolverGroupResource.setGroupName(dnsResolverGroupName);
		dnsResolverGroupResource.setIpAddressList("8.8.8.8,4.4.4.4");
		dnsResolverGroupResource = sas.createDnsResolverGroup(dnsResolverGroupResource);
		assertNotNull(dnsResolverGroupResource.getId());

		segmentName = "segment" + System.currentTimeMillis();

		segmentResource = new SegmentResource();
		segmentResource.setSegment(segmentName);
		segmentResource.setScsUrl("https://" + segmentName + ".scs.tdmx.org/sp/v1.0/scs");
		segmentResource = sas.createSegment(segmentResource);
		assertNotNull(segmentResource.getId());

		partitionResource = new DatabasePartitionResource();
		partitionResource.setPartitionId("partitionId" + System.currentTimeMillis());
		partitionResource.setSegment(segmentName);
		partitionResource.setDbType(DatabaseType.ZONE.toString());
		partitionResource = sas.createDatabasePartition(partitionResource);
		assertNotNull(partitionResource.getId());

		accountEmail = "email" + System.currentTimeMillis() + "@gmail.com";

		accountResource = new AccountResource();
		accountResource.setEmail(accountEmail);
		accountResource.setFirstname("firstName");
		accountResource.setLastname("ln");
		accountResource = sas.createAccount(accountResource);

		assertNotNull(accountResource.getId());
		assertNotNull(accountResource.getAccountId());

		accountZoneResource = new AccountZoneResource();
		accountZoneResource.setAccountId(accountResource.getAccountId());
		accountZoneResource.setSegment(MockDatabasePartitionInstaller.S1);
		accountZoneResource.setZoneApex(accountResource.getAccountId() + ".zone.apex"); // make it unique.
		accountZoneResource.setAccessStatus("ACTIVE");

		accountZoneResource = sas.createAccountZone(accountResource.getId(), accountZoneResource);

		assertNotNull(accountZoneResource.getId());
		/*
		 * assertNotNull(accountZoneResource.getJobId()); assertNotNull(jobScheduler.getLastImmediateScheduledJob());
		 * jobScheduler.clearLastImmediateScheduledJob();
		 */
		Calendar validFrom = Calendar.getInstance();
		Calendar validTo = Calendar.getInstance();
		validTo.add(Calendar.YEAR, 10);

		ZoneAdministrationCredentialSpecifier adminSpec = new ZoneAdministrationCredentialSpecifier(1,
				accountZoneResource.getZoneApex());
		adminSpec.setEmailAddress("name@email.com");
		adminSpec.setCountry("CH");
		adminSpec.setLocation("Zug");
		adminSpec.setOrg("Organization");
		adminSpec.setOrgUnit("OrgUnit");
		adminSpec.setSerialNumber(1);
		adminSpec.setTelephoneNumber("041...");
		adminSpec.setSignatureAlgorithm(SignatureAlgorithm.SHA_384_RSA);
		adminSpec.setKeyAlgorithm(PublicKeyAlgorithm.RSA4096);
		adminSpec.setNotBefore(validFrom);
		adminSpec.setNotAfter(validTo);
		PKIXCredential zac = CredentialUtils.createZoneAdministratorCredential(adminSpec);

		zacResource = new AccountZoneAdministrationCredentialResource();
		zacResource.setAccountId(accountResource.getAccountId());
		zacResource.setZoneApex(accountZoneResource.getZoneApex());
		zacResource.setCertificatePem(CertificateIOUtils.safeX509certsToPem(zac.getCertificateChain()));

		zacResource = sas.createAccountZoneAdministrationCredential(accountResource.getId(),
				accountZoneResource.getId(), zacResource);
		assertNotNull(zacResource.getId());
		assertEquals(AccountZoneAdministrationCredentialStatus.PENDING_INSTALLATION.toString(),
				zacResource.getStatus());
	}

	@After
	public void doTeardown() {
	}

	@Test
	public void testAutowired() {
		assertNotNull(dnsResolverGroupService);
		assertNotNull(segmentService);
		assertNotNull(databasePartitionService);
		assertNotNull(accountService);
		assertNotNull(accountZoneService);
		assertNotNull(accountZoneAdministrationCredentialService);
		assertNotNull(lockService);
		assertNotNull(maxValueService);
		assertNotNull(objectIdService);
		// the service under test...
		assertNotNull(sas);
	}

	@Test
	public void testGetDnsResolverGroup() {
		DnsResolverGroupResource r = sas.getDnsResolverGroup(dnsResolverGroupResource.getId());
		assertNotNull(r);
	}

	@Test
	public void testGetSegment() {
		SegmentResource r = sas.getSegment(segmentResource.getId());
		assertNotNull(r);
	}

	@Test
	public void testGetDatabasePartition() {
		DatabasePartitionResource r = sas.getDatabasePartition(partitionResource.getId());
		assertNotNull(r);
	}

	@Test
	public void testSearchDnsResolverGroup_GroupName() {
		List<DnsResolverGroupResource> dnsResolverGroups = sas.searchDnsResolverGroup(0, 10,
				dnsResolverGroupResource.getGroupName());
		assertEquals(1, dnsResolverGroups.size());
	}

	@Test
	public void testSearchDnsResolverGroup_All() {
		List<DnsResolverGroupResource> dnsResolverGroups = sas.searchDnsResolverGroup(0, 10, null);
		assertFalse(dnsResolverGroups.isEmpty());
	}

	@Test
	public void testGetAccount() {
		AccountResource r = sas.getAccount(accountResource.getId());
		assertNotNull(r);
	}

	@Test
	public void testCreateAccount_InvalidId() {
		AccountResource ar = new AccountResource();
		ar.setId(new Random().nextLong());

		try {
			sas.createAccount(ar);
			fail();
		} catch (BadRequestException e) {
			log.info("VE :" + e.getMessage());
		}
	}

	@Test
	public void testSearchAccount_AccountId() {
		List<AccountResource> accounts = sas.searchAccount(0, 10, null, accountResource.getAccountId());
		assertEquals(1, accounts.size());
	}

	@Test
	public void testSearchAccount_Email() {
		List<AccountResource> accounts = sas.searchAccount(0, 10, accountResource.getEmail(), null);
		assertEquals(1, accounts.size());
	}

	@Test
	public void testSearchAccount_AccountIdAndEmail() {
		List<AccountResource> accounts = sas.searchAccount(0, 10, accountResource.getEmail(),
				accountResource.getAccountId());
		assertEquals(1, accounts.size());
	}

	@Test
	public void testUpdateAccount() {
		// TODO
	}

	@Test
	public void testDeleteAccount() {
		// TODO
	}

	@Test
	public void testCreateAccountZone() {
		// TODO
	}

	@Test
	public void testSearchAccountZone() {
		List<AccountZoneResource> aczs = sas.searchAccountZone(0, 100, null, accountZoneResource.getZoneApex(), null,
				null, null);
		assertFalse(aczs.isEmpty());
	}

	@Test
	public void testGetAccountZone() {
		// TODO
	}

	@Test
	public void testUpdateAccountZone() {
		// TODO
	}

	@Test
	public void testDeleteAccountZone() {
		// TODO
	}

	@Test
	public void testCreateAccountZoneAdministrationCredential_WrongZoneInCert() throws Exception {
		Calendar validFrom = Calendar.getInstance();
		Calendar validTo = Calendar.getInstance();
		validTo.add(Calendar.YEAR, 10);

		ZoneAdministrationCredentialSpecifier adminSpec = new ZoneAdministrationCredentialSpecifier(1, "gugus.com");
		adminSpec.setEmailAddress("name@email.com");
		adminSpec.setCountry("CH");
		adminSpec.setLocation("Zug");
		adminSpec.setOrg("Organization");
		adminSpec.setOrgUnit("OrgUnit");
		adminSpec.setSerialNumber(1);
		adminSpec.setTelephoneNumber("041...");
		adminSpec.setSignatureAlgorithm(SignatureAlgorithm.SHA_384_RSA);
		adminSpec.setKeyAlgorithm(PublicKeyAlgorithm.RSA4096);
		adminSpec.setNotBefore(validFrom);
		adminSpec.setNotAfter(validTo);
		PKIXCredential zac = CredentialUtils.createZoneAdministratorCredential(adminSpec);

		AccountZoneAdministrationCredentialResource zacRes = new AccountZoneAdministrationCredentialResource();
		zacRes.setAccountId(accountResource.getAccountId());
		zacRes.setZoneApex(accountZoneResource.getZoneApex()); // fake! - real is gugus.com in cert.
		zacRes.setCertificatePem(CertificateIOUtils.safeX509certsToPem(zac.getCertificateChain()));

		try {
			sas.createAccountZoneAdministrationCredential(accountResource.getId(), accountZoneResource.getId(), zacRes);
			fail();
		} catch (BadRequestException ve) {

		}
	}

	@Test
	public void testSearchAccountZoneAdministrationCredential() {
		List<AccountZoneAdministrationCredentialResource> azcrs = sas.searchAccountZoneAdministrationCredential(0, 100,
				accountResource.getId(), accountZoneResource.getId());
		assertFalse(azcrs.isEmpty());
		assertEquals(1, azcrs.size());
		assertEquals(zacResource.getFingerprint(), azcrs.get(0).getFingerprint());
	}

	@Test
	public void testGetAccountZoneAdministrationCredential() {
		AccountZoneAdministrationCredentialResource res = sas.getAccountZoneAdministrationCredential(
				accountResource.getId(), accountZoneResource.getId(), zacResource.getId());
		assertNotNull(res);
	}

	@Test
	public void testUpdateAccountZoneAdministrationCredential() {
		// TODO
	}

	@Test
	public void testDeleteAccountZoneAdministrationCredential() {
		// TODO delete after update to deinstall
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
