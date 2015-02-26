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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.console.domain.AccountZoneFacade;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.service.AccountZoneService;
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

	private AccountZone az;
	private Long jobId;

	@Before
	public void doSetup() throws Exception {
		jobId = new Random().nextLong();

		az = AccountZoneFacade.createAccountZone("1234", "zoneApex", "segment", "partitionId");
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
	}

	@Test
	public void test_Success() throws Exception {
		ZoneInstallTask task = new ZoneInstallTask();
		task.setAccountId("1");
		task.setZoneApex("z");

		executor.execute(jobId, task);

		AccountZone storedAZ = accountZoneService.findByAccountIdZoneApex(az.getAccountId(), az.getZoneApex());
		assertNotNull(storedAZ);
		assertNull(storedAZ.getJobId());
	}

};
