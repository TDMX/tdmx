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
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.MockZonePartitionIdInstaller;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.service.control.task.dao.ZoneTransferTask;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ZoneTransferJobUnitTest {

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private AccountZoneService accountZoneService;
	@Autowired
	private JobFactory jobFactory;
	@Autowired
	private JobExecutor<ZoneTransferTask> executor;
	@Autowired
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	@Autowired
	private ZoneService zoneService;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;
	private Long jobId;

	@Before
	public void doSetup() throws Exception {
		jobId = new Random().nextLong();

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockZonePartitionIdInstaller.ZP1_S1);
		input.setNumZACs(3);
		input.setNumDomains(2);
		input.setNumDACsPerDomain(2);
		input.setNumAddressesPerDomain(5);
		input.setNumUsersPerAddress(2);

		data = dataGenerator.setUp(input);

		AccountZone az = data.getAccountZone();
		az.setJobId(jobId);
		accountZoneService.createOrUpdate(az);
	}

	@After
	public void doTeardown() {
		dataGenerator.tearDown(input, data);
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(dataGenerator);
		assertNotNull(accountZoneService);
		assertNotNull(jobFactory);
		assertNotNull(executor);
		assertNotNull(zonePartitionIdProvider);
		assertNotNull(zoneService);
	}

	@Test
	public void test_Success() throws Exception {
		String newPartitionId = MockZonePartitionIdInstaller.ZP1_S2;
		ZoneTransferTask task = new ZoneTransferTask();
		task.setAccountId(data.getAccountZone().getAccountId());
		task.setZoneApex(data.getAccountZone().getZoneApex());
		task.setZoneDbPartitionId(newPartitionId);

		executor.execute(jobId, task);

		AccountZone storedAZ = accountZoneService.findByAccountIdZoneApex(data.getAccountZone().getAccountId(), data
				.getAccountZone().getZoneApex());
		assertNotNull(storedAZ);
		assertNull(storedAZ.getJobId());
		assertEquals(newPartitionId, storedAZ.getZonePartitionId());

		zonePartitionIdProvider.setPartitionId(newPartitionId);
		Zone z = zoneService.findByZoneApex(data.getAccountZone().getId(), data.getAccountZone().getZoneApex());
		assertNotNull(z);

		// TODO check ALL the generated data is in the NEW partition.

	}

	@Test
	public void test_Failure_AccountIdNotFound() throws Exception {
		ZoneTransferTask task = new ZoneTransferTask();
		task.setAccountId("gugus");
		task.setZoneApex(data.getAccountZone().getZoneApex());
		task.setZoneDbPartitionId(MockZonePartitionIdInstaller.ZP1_S2);

		try {
			executor.execute(jobId, task);
			fail();
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	public void test_Failure_JobIdMismatch() throws Exception {
		ZoneTransferTask task = new ZoneTransferTask();
		task.setAccountId(data.getAccountZone().getAccountId());
		task.setZoneApex(data.getAccountZone().getZoneApex());
		task.setZoneDbPartitionId(MockZonePartitionIdInstaller.ZP1_S2);

		try {
			executor.execute(new Random().nextLong(), task);
			fail();
		} catch (IllegalStateException e) {

		}
	}

	@Test
	public void test_Failure_SameZone() throws Exception {
		ZoneTransferTask task = new ZoneTransferTask();
		task.setAccountId(data.getAccountZone().getAccountId());
		task.setZoneApex(data.getAccountZone().getZoneApex());
		task.setZoneDbPartitionId(data.getAccountZone().getZonePartitionId());

		try {
			executor.execute(new Random().nextLong(), task);
			fail();
		} catch (IllegalStateException e) {

		}
	}

};
