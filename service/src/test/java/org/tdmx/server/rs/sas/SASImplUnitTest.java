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
import org.tdmx.core.api.v01.sp.zas.common.Acknowledge;
import org.tdmx.lib.console.domain.DatabasePartitionFacade;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;
import org.tdmx.lib.control.job.MockJobScheduler;
import org.tdmx.lib.control.service.AccountService;
import org.tdmx.lib.control.service.AccountZoneAdministrationCredentialService;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.control.service.DatabasePartitionService;
import org.tdmx.lib.control.service.LockService;
import org.tdmx.lib.control.service.MaxValueService;
import org.tdmx.lib.control.service.UniqueIdService;
import org.tdmx.server.ws.zas.ZASImpl.ErrorCode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SASImplUnitTest {

	private static final Logger log = LoggerFactory.getLogger(SASImplUnitTest.class);

	@Autowired
	private AccountService accountService;
	@Autowired
	private AccountZoneService accountZoneService;
	@Autowired
	private AccountZoneAdministrationCredentialService accountZoneAdministrationCredentialService;
	@Autowired
	private DatabasePartitionService databasePartitionService;
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

	private AccountResource accountResource;
	private AccountZoneResource accountZoneResource;

	@Before
	public void doSetup() throws Exception {
		DatabasePartition zp1 = DatabasePartitionFacade.createDatabasePartition("z-segment-id1", DatabaseType.ZONE,
				"segment");
		databasePartitionService.createOrUpdate(zp1);

		DatabasePartition zp2 = DatabasePartitionFacade.createDatabasePartition("z-segment-id2", DatabaseType.ZONE,
				"segment");
		databasePartitionService.createOrUpdate(zp2);

		DatabasePartition zp3 = DatabasePartitionFacade.createDatabasePartition("z-segment-id3", DatabaseType.ZONE,
				"segment");
		databasePartitionService.createOrUpdate(zp3);

		accountResource = new AccountResource();
		accountResource.setEmail("email@gmail.com");
		accountResource.setFirstname("firstName");
		accountResource.setLastname("ln");
		accountResource = sas.createAccount(accountResource);

		assertNotNull(accountResource.getId());
		assertNotNull(accountResource.getAccountId());

		accountZoneResource = new AccountZoneResource();
		accountZoneResource.setAccountId(accountResource.getAccountId());
		accountZoneResource.setSegment("segment");
		accountZoneResource.setZoneApex("zone.apex");
		accountZoneResource.setAccessStatus("ACTIVE");

		accountZoneResource = sas.createAccountZone(accountResource.getId(), accountZoneResource);

		assertNotNull(accountZoneResource.getId());
		assertNotNull(accountZoneResource.getJobId());
		assertNotNull(jobScheduler.getLastImmediateScheduledJob());
	}

	@After
	public void doTeardown() {
		jobScheduler.clearLastImmediateScheduledJob();

		DatabasePartition zp1 = databasePartitionService.findByPartitionId("z-segment-id1");
		if (zp1 != null) {
			databasePartitionService.delete(zp1);
		}
		DatabasePartition zp2 = databasePartitionService.findByPartitionId("z-segment-id2");
		if (zp2 != null) {
			databasePartitionService.delete(zp2);
		}
		DatabasePartition zp3 = databasePartitionService.findByPartitionId("z-segment-id3");
		if (zp3 != null) {
			databasePartitionService.delete(zp3);
		}
	}

	@Test
	public void testAutowired() {
		assertNotNull(accountService);
		assertNotNull(accountZoneService);
		assertNotNull(accountZoneAdministrationCredentialService);
		assertNotNull(databasePartitionService);
		assertNotNull(lockService);
		assertNotNull(maxValueService);
		assertNotNull(objectIdService);
		// the service under test...
		assertNotNull(sas);
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
	public void testSearchAccount() {
		// TODO
	}

	@Test
	public void testGetAccount() {
		// TODO
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
