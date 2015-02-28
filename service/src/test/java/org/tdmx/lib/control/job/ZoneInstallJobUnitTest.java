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
package org.tdmx.lib.control.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.console.domain.AccountZoneFacade;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.MockZonePartitionIdInstaller;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.service.control.task.dao.ZoneInstallTask;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ZoneInstallJobUnitTest {

	@Autowired
	private AccountZoneService accountZoneService;
	@Autowired
	private JobFactory jobFactory;
	@Autowired
	private JobExecutor<ZoneInstallTask> executor;
	@Autowired
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	@Autowired
	private ZoneService zoneService;

	private AccountZone az;
	private Long jobId;

	@Before
	public void doSetup() throws Exception {
		jobId = new Random().nextLong();

		az = AccountZoneFacade.createAccountZone("1234", "zoneApex", MockZonePartitionIdInstaller.S1,
				MockZonePartitionIdInstaller.ZP1_S1);
		az.setJobId(jobId);
		accountZoneService.createOrUpdate(az);
	}

	@After
	public void doTeardown() {
		AccountZone storedAZ = accountZoneService.findByAccountIdZoneApex(az.getAccountId(), az.getZoneApex());
		if (storedAZ != null) {
			accountZoneService.delete(storedAZ);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(accountZoneService);
		assertNotNull(jobFactory);
		assertNotNull(executor);
		assertNotNull(zonePartitionIdProvider);
		assertNotNull(zoneService);
	}

	@Test
	public void test_Success() throws Exception {
		ZoneInstallTask task = new ZoneInstallTask();
		task.setAccountId(az.getAccountId());
		task.setZoneApex(az.getZoneApex());

		executor.execute(jobId, task);

		AccountZone storedAZ = accountZoneService.findByAccountIdZoneApex(az.getAccountId(), az.getZoneApex());
		assertNotNull(storedAZ);
		assertNull(storedAZ.getJobId());

		zonePartitionIdProvider.setPartitionId(az.getZonePartitionId());
		Zone z = zoneService.findByZoneApex(new ZoneReference(az.getId(), az.getZoneApex()));
		assertNotNull(z);
		assertEquals(az.getZoneReference(), z.getZoneReference());
	}

	@Test
	public void test_Failure_AccountIdNotFound() throws Exception {
		ZoneInstallTask task = new ZoneInstallTask();
		task.setAccountId("gugus");
		task.setZoneApex(az.getZoneApex());

		try {
			executor.execute(jobId, task);
			fail();
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	public void test_Failure_JobIdMismatch() throws Exception {
		ZoneInstallTask task = new ZoneInstallTask();
		task.setAccountId(az.getAccountId());
		task.setZoneApex(az.getZoneApex());

		try {
			executor.execute(new Random().nextLong(), task);
			fail();
		} catch (IllegalStateException e) {

		}
	}
};
