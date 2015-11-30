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
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialStatus;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.service.AccountZoneAdministrationCredentialService;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.MockZonePartitionIdInstaller;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.service.control.task.dao.ZACInstallTask;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ZACInstallJobUnitTest {

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private AccountZoneService accountZoneService;
	@Autowired
	private AccountZoneAdministrationCredentialService accountZoneAdministrationCredentialService;
	@Autowired
	private JobFactory jobFactory;
	@Autowired
	private JobExecutor<ZACInstallTask> executor;
	@Autowired
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	@Autowired
	private ZoneService zoneService;
	@Autowired
	private AgentCredentialService agentCredentialService;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;
	private Long jobId;
	private String fingerprint;
	private Zone zone;
	private String partitionId;

	@Before
	public void doSetup() throws Exception {
		jobId = new Random().nextLong();

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockZonePartitionIdInstaller.ZP1_S1);
		input.setNumZACs(1);
		input.setNumDomains(1);
		input.setNumDACsPerDomain(1);
		input.setNumAddressesPerDomain(0);
		input.setNumUsersPerAddress(0);

		data = dataGenerator.setUp(input);

		AccountZoneAdministrationCredential zac = data.getZacs().get(0).getAc();
		zac.setJobId(jobId);
		accountZoneAdministrationCredentialService.createOrUpdate(zac);

		zone = data.getZone();
		fingerprint = data.getZacs().get(0).getAc().getFingerprint();
		partitionId = data.getAccountZone().getZonePartitionId();

		zonePartitionIdProvider.setPartitionId(partitionId);
		try {
			AgentCredential generatedAC = agentCredentialService.findByFingerprint(fingerprint);
			agentCredentialService.delete(generatedAC);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
	}

	@After
	public void doTeardown() {
		zonePartitionIdProvider.clearPartitionId();

		dataGenerator.tearDown(input, data);
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(accountZoneService);
		assertNotNull(accountZoneAdministrationCredentialService);
		assertNotNull(jobFactory);
		assertNotNull(executor);
		assertNotNull(zonePartitionIdProvider);
		assertNotNull(zoneService);
		assertNotNull(agentCredentialService);
	}

	@Test
	public void test_Success() throws Exception {
		ZACInstallTask task = new ZACInstallTask();
		task.setAccountId(data.getAccount().getId());
		task.setAccountZoneId(zone.getAccountZoneId());
		task.setFingerprint(fingerprint);

		executor.execute(jobId, task);

		AccountZoneAdministrationCredential storedZAC = accountZoneAdministrationCredentialService
				.findByFingerprint(fingerprint);
		assertNotNull(storedZAC);
		assertNull(storedZAC.getJobId());
		assertEquals(AccountZoneAdministrationCredentialStatus.INSTALLED, storedZAC.getCredentialStatus());

		zonePartitionIdProvider.setPartitionId(partitionId);
		try {
			AgentCredential zac = agentCredentialService.findByFingerprint(task.getFingerprint());
			assertNotNull(zac);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
	}

	@Test
	public void test_DAC_Invalid() throws Exception {
		PKIXCredential dac = data.getDomains().get(0).getDacs().get(0).getCredential();
		// replace the ZAC PEM with a DAC's PEM.
		AccountZoneAdministrationCredential storedZAC = accountZoneAdministrationCredentialService
				.findByFingerprint(fingerprint);
		storedZAC.setCertificateChainPem(CertificateIOUtils.x509certToPem(dac.getPublicCert()));
		accountZoneAdministrationCredentialService.createOrUpdate(storedZAC);

		ZACInstallTask task = new ZACInstallTask();
		task.setAccountId(data.getAccount().getId());
		task.setAccountZoneId(zone.getAccountZoneId());
		task.setFingerprint(fingerprint);

		executor.execute(jobId, task);

		storedZAC = accountZoneAdministrationCredentialService.findByFingerprint(fingerprint);
		assertNotNull(storedZAC);
		assertNull(storedZAC.getJobId());
		assertEquals(AccountZoneAdministrationCredentialStatus.NON_ZAC, storedZAC.getCredentialStatus());
	}

	@Test
	public void test_Failure_FingerprintMissing() throws Exception {
		ZACInstallTask task = new ZACInstallTask();
		task.setAccountId(data.getAccount().getId());
		task.setAccountZoneId(zone.getAccountZoneId());
		task.setFingerprint(null);

		try {
			executor.execute(jobId, task);
			fail();
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	public void test_Failure_FingerprintNotFound() throws Exception {
		ZACInstallTask task = new ZACInstallTask();
		task.setAccountId(data.getAccount().getId());
		task.setAccountZoneId(zone.getAccountZoneId());
		task.setFingerprint("gugus");

		try {
			executor.execute(jobId, task);
			fail();
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	public void test_Failure_JobIdMismatch() throws Exception {
		ZACInstallTask task = new ZACInstallTask();
		task.setAccountId(data.getAccount().getId());
		task.setAccountZoneId(zone.getAccountZoneId());
		task.setFingerprint(fingerprint);

		try {
			executor.execute(new Random().nextLong(), task);
			fail();
		} catch (IllegalStateException e) {

		}
	}
};
