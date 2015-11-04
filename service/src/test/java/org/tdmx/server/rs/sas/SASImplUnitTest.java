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

import java.util.List;
import java.util.Random;

import javax.ws.rs.ValidationException;

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
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.lib.control.domain.DatabaseType;
import org.tdmx.lib.control.job.MockJobScheduler;
import org.tdmx.lib.control.service.AccountService;
import org.tdmx.lib.control.service.AccountZoneAdministrationCredentialService;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.control.service.DatabasePartitionService;
import org.tdmx.lib.control.service.LockService;
import org.tdmx.lib.control.service.MaxValueService;
import org.tdmx.lib.control.service.SegmentService;
import org.tdmx.lib.control.service.UniqueIdService;
import org.tdmx.lib.zone.service.MockZonePartitionIdInstaller;
import org.tdmx.server.rs.sas.resource.AccountResource;
import org.tdmx.server.rs.sas.resource.AccountZoneResource;
import org.tdmx.server.rs.sas.resource.DatabasePartitionResource;
import org.tdmx.server.rs.sas.resource.SegmentResource;
import org.tdmx.server.ws.ErrorCode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SASImplUnitTest {

	private static final Logger log = LoggerFactory.getLogger(SASImplUnitTest.class);

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
	private MockJobScheduler jobScheduler;

	@Autowired
	private SAS sas;

	private SegmentResource segmentResource;
	private DatabasePartitionResource partitionResource;
	private AccountResource accountResource;
	private AccountZoneResource accountZoneResource;

	private String accountEmail;
	private String segmentName;

	@Before
	public void doSetup() throws Exception {

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
		accountZoneResource.setSegment(MockZonePartitionIdInstaller.S1);
		accountZoneResource.setZoneApex(accountResource.getAccountId() + ".zone.apex"); // make it unique.
		accountZoneResource.setAccessStatus("ACTIVE");

		accountZoneResource = sas.createAccountZone(accountResource.getId(), accountZoneResource);

		assertNotNull(accountZoneResource.getId());
		assertNotNull(accountZoneResource.getJobId());
		assertNotNull(jobScheduler.getLastImmediateScheduledJob());

	}

	@After
	public void doTeardown() {
		jobScheduler.clearLastImmediateScheduledJob();

	}

	@Test
	public void testAutowired() {
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
		} catch (ValidationException e) {
			log.info("VE :" + e.getViolations().get(0).getMessage());
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
		// TODO
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
	public void testCreateAccountZoneAdministrationCredential() {
		// TODO
	}

	@Test
	public void testSearchAccountZoneAdministrationCredential() {
		// TODO
	}

	@Test
	public void testGetAccountZoneAdministrationCredential() {
		// TODO
	}

	@Test
	public void testUpdateAccountZoneAdministrationCredential() {
		// TODO
	}

	@Test
	public void testDeleteAccountZoneAdministrationCredential() {
		// TODO
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
