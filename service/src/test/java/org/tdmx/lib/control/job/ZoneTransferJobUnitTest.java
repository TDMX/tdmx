/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud executor providers.
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
import static org.junit.Assert.fail;

import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.control.service.ControlJobService;
import org.tdmx.lib.control.service.MockDatabasePartitionInstaller;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.ZoneService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/tdmx/test-context.xml")
public class ZoneTransferJobUnitTest {

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private ControlJobService jobService;
	@Autowired
	@Named("tdmx.lib.control.job.ZoneTransferJobExecutor")
	private JobExecutor jobExecutor;
	@Autowired
	private AccountZoneService accountZoneService;
	@Autowired
	@Named("tdmx.lib.zone.ThreadLocalPartitionIdProvider")
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	@Autowired
	private ZoneService zoneService;

	private String segmentName = "default";
	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;
	private Account account;
	private Long accountZoneId;

	private ControlJob j;

	@Before
	public void doSetup() throws Exception {
		// create the mock data in the old zone
		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockDatabasePartitionInstaller.ZP1_S1);
		input.setNumZACs(3);
		input.setNumDomains(2);
		input.setNumDACsPerDomain(2);
		input.setNumAddressesPerDomain(5);
		input.setNumUsersPerAddress(2);

		data = dataGenerator.setUp(input);

		account = data.getAccount();

		AccountZone az = data.getAccountZone();
		accountZoneId = az.getId();
	}

	@After
	public void doTeardown() {
		dataGenerator.tearDown(input, data);
		// delete any job created during the test.
		if (j != null && j.getId() != null) {
			jobService.delete(j);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(dataGenerator);
		assertNotNull(accountZoneService);
		assertNotNull(zonePartitionIdProvider);
		assertNotNull(zoneService);
	}

	@Test
	public void test_Success() throws Exception {
		String newPartitionId = MockDatabasePartitionInstaller.ZP1_S2;

		j = ControlJob.createZoneTransferJob(segmentName, accountZoneId, newPartitionId);

		jobService.createOrUpdate(j);
		assertNotNull(j.getId());

		// execute the job
		jobExecutor.execute(j);

		AccountZone storedAZ = accountZoneService.findById(accountZoneId);
		assertNotNull(storedAZ);
		assertEquals(newPartitionId, storedAZ.getZonePartitionId());

		zonePartitionIdProvider.setPartitionId(newPartitionId);
		Zone z = zoneService.findByZoneApex(data.getAccountZone().getZoneApex());
		assertNotNull(z);

		// TODO check ALL the generated data is in the NEW partition.

	}

	@Test
	public void test_Failure_SameZone() throws Exception {
		String oldPartitionId = data.getAccountZone().getZonePartitionId();
		j = ControlJob.createZoneTransferJob(segmentName, accountZoneId, oldPartitionId);

		jobService.createOrUpdate(j);
		assertNotNull(j.getId());

		try {
			jobExecutor.execute(j);
			fail();
		} catch (IllegalArgumentException e) {

		}
	}

};
