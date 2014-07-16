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
package org.tdmx.lib.zone.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager="tdmx.lib.zone.TransactionManager")
// @Transactional("ZoneDB")
public class AgentCredentialServiceRepositoryUnitTest {

	@Autowired
	private AgentCredentialService service;

	@Autowired
	private AgentCredentialFactory factory;

	private String zoneApex;
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

		AgentCredential zoneAC = factory.createAgentCredential(zac.getCertificateChain(), AgentCredentialStatus.ACTIVE);
		assertNotNull(zoneAC);
		assertEquals(zoneApex, zoneAC.getZoneApex());
		service.createOrUpdate(zoneAC);

		AgentCredential domainAC = factory.createAgentCredential(dac.getCertificateChain(),
				AgentCredentialStatus.ACTIVE);
		assertNotNull(domainAC);
		assertNotNull(domainAC.getZoneApex());
		service.createOrUpdate(domainAC);

		AgentCredential userAC = factory.createAgentCredential(uc.getCertificateChain(), AgentCredentialStatus.ACTIVE);
		assertNotNull(userAC);
		assertEquals(zoneApex, userAC.getZoneApex());
		service.createOrUpdate(userAC);
	}

	@After
	public void doTeardown() {
		List<AgentCredential> list = service.findByZoneApex(zoneApex);
		for (AgentCredential ac : list) {
			service.delete(ac);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookupById() throws Exception {
		AgentCredential zoneAC = service.findByFingerprint(zac.getCertificateChain()[0].getFingerprint());
		assertNotNull(zoneAC);
		assertEquals(zoneApex, zoneAC.getZoneApex());

		AgentCredential domainAC = service.findByFingerprint(dac.getCertificateChain()[0].getFingerprint());
		assertNotNull(domainAC);
		assertEquals(zoneApex, domainAC.getZoneApex());

		AgentCredential userAC = service.findByFingerprint(uc.getCertificateChain()[0].getFingerprint());
		assertNotNull(userAC);
		assertEquals(zoneApex, userAC.getZoneApex());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		AgentCredential az = service.findByFingerprint("gugus");
		assertNull(az);
	}

	@Test
	public void testModify_Status() throws Exception {
		AgentCredential userAC = service.findByFingerprint(uc.getCertificateChain()[0].getFingerprint());
		assertNotNull(userAC);
		assertEquals(AgentCredentialStatus.ACTIVE, userAC.getCredentialStatus());
		assertEquals(zoneApex, userAC.getZoneApex());

		userAC.setCredentialStatus(AgentCredentialStatus.SUSPENDED);
		service.createOrUpdate(userAC);

		userAC = service.findByFingerprint(uc.getCertificateChain()[0].getFingerprint());
		assertNotNull(userAC);
		assertEquals(AgentCredentialStatus.SUSPENDED, userAC.getCredentialStatus());
		assertEquals(zoneApex, userAC.getZoneApex());
	}

}