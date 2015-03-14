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
import java.util.Random;

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
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.domain.AgentCredentialType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AgentCredentialServiceRepositoryUnitTest {

	@Autowired
	private AgentCredentialService service;

	@Autowired
	private AgentCredentialFactory factory;

	private ZoneReference zone;
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
		zone = new ZoneReference(new Random().nextLong(), zac.getPublicCert().getTdmxZoneInfo().getZoneRoot());

		byte[] dacFile = FileUtils.getFileContents("src/test/resources/dac.keystore");
		assertNotNull(dacFile);
		dac = KeyStoreUtils.getPrivateCredential(dacFile, "jks", "changeme", "client");

		byte[] ucFile = FileUtils.getFileContents("src/test/resources/uc.keystore");
		assertNotNull(ucFile);
		uc = KeyStoreUtils.getPrivateCredential(ucFile, "jks", "changeme", "client");

		AgentCredential zoneAC = factory.createAgentCredential(zone, zac.getCertificateChain());
		zoneAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(zoneAC);
		assertNull(zoneAC.getId());
		assertEquals(zone, zoneAC.getZoneReference());
		assertNull(zoneAC.getDomainName());
		assertNull(zoneAC.getAddressName());
		assertEquals(AgentCredentialType.ZAC, zoneAC.getCredentialType());
		service.createOrUpdate(zoneAC);

		AgentCredential domainAC = factory.createAgentCredential(zone, dac.getCertificateChain());
		domainAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(domainAC);
		assertNull(domainAC.getId());
		assertEquals(zone, domainAC.getZoneReference());
		assertNotNull(domainAC.getDomainName());
		assertNull(domainAC.getAddressName());
		service.createOrUpdate(domainAC);

		AgentCredential userAC = factory.createAgentCredential(zone, uc.getCertificateChain());
		userAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(userAC);
		assertNull(userAC.getId());
		assertEquals(zone, userAC.getZoneReference());
		assertNotNull(userAC.getDomainName());
		assertNotNull(userAC.getAddressName());
		service.createOrUpdate(userAC);
	}

	@After
	public void doTeardown() {
		List<AgentCredential> list = service.search(zone, new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000)));
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
		AgentCredential zoneAC = service.findByFingerprint(zone, zac.getPublicCert().getFingerprint());
		assertNotNull(zoneAC);
		assertEquals(zone, zoneAC.getZoneReference());
		assertEquals(AgentCredentialType.ZAC, zoneAC.getCredentialType());

		AgentCredential domainAC = service.findByFingerprint(zone, dac.getPublicCert().getFingerprint());
		assertNotNull(domainAC);
		assertEquals(zone, domainAC.getZoneReference());
		assertEquals(AgentCredentialType.DAC, domainAC.getCredentialType());

		AgentCredential userAC = service.findByFingerprint(zone, uc.getPublicCert().getFingerprint());
		assertNotNull(userAC);
		assertEquals(zone, userAC.getZoneReference());
		assertEquals(AgentCredentialType.UC, userAC.getCredentialType());
	}

	@Test
	public void testLookupByDomain() throws Exception {
		AgentCredentialSearchCriteria sc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		sc.setDomainName(dac.getPublicCert().getCommonName());
		List<AgentCredential> domainCerts = service.search(zone, sc);
		assertNotNull(domainCerts);
		assertEquals(2, domainCerts.size()); // DAC, UC
	}

	@Test
	public void testLookupByDomainAndType() throws Exception {
		AgentCredentialSearchCriteria dsc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dsc.setDomainName(dac.getPublicCert().getCommonName());
		dsc.setType(AgentCredentialType.DAC);

		List<AgentCredential> dacCerts = service.search(zone, dsc);
		assertNotNull(dacCerts);
		assertEquals(1, dacCerts.size()); // DAC
		// TODO prove dac

		AgentCredentialSearchCriteria usc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		usc.setDomainName(dac.getPublicCert().getCommonName());
		usc.setType(AgentCredentialType.UC);

		List<AgentCredential> ucCerts = service.search(zone, usc);
		assertNotNull(ucCerts);
		assertEquals(1, ucCerts.size()); // UC
		// TODO prove uc
	}

	@Test
	public void testLookupByAddressAndType() throws Exception {
		AgentCredentialSearchCriteria sc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		sc.setDomainName(dac.getPublicCert().getCommonName());
		sc.setAddressName(uc.getPublicCert().getCommonName());
		sc.setType(AgentCredentialType.UC);

		List<AgentCredential> ucCerts = service.search(zone, sc);
		assertNotNull(ucCerts);
		assertEquals(1, ucCerts.size()); // UC
		// TODO prove uc
	}

	@Test
	public void testLookupByAddressAndTypeAndStatus() throws Exception {
		AgentCredentialSearchCriteria sc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		sc.setDomainName(dac.getPublicCert().getCommonName());
		sc.setAddressName(uc.getPublicCert().getCommonName());
		sc.setType(AgentCredentialType.UC);
		sc.setStatus(AgentCredentialStatus.ACTIVE);

		List<AgentCredential> ucCerts = service.search(zone, sc);
		assertNotNull(ucCerts);
		assertEquals(1, ucCerts.size()); // UC
		// TODO prove uc

		sc.setStatus(AgentCredentialStatus.SUSPENDED);
		ucCerts = service.search(zone, sc);
		assertNotNull(ucCerts);
		assertEquals(0, ucCerts.size());
	}

	@Test
	public void testSearch_ZoneNotFound() throws Exception {
		AgentCredentialSearchCriteria sc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		sc.setDomainName(dac.getPublicCert().getCommonName());

		ZoneReference gugus = new ZoneReference(zone.getTenantId(), "gugus");
		List<AgentCredential> domainCerts = service.search(gugus, sc);
		assertNotNull(domainCerts);
		assertEquals(0, domainCerts.size());

		gugus = new ZoneReference(new Random().nextLong(), zone.getZoneApex());
		domainCerts = service.search(gugus, sc);
		assertNotNull(domainCerts);
		assertEquals(0, domainCerts.size());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		AgentCredential az = service.findByFingerprint(zone, "gugus");
		assertNull(az);
	}

	@Test
	public void testModify_Status() throws Exception {
		AgentCredential userAC = service.findByFingerprint(zone, uc.getPublicCert().getFingerprint());
		assertNotNull(userAC);
		assertEquals(AgentCredentialStatus.ACTIVE, userAC.getCredentialStatus());
		assertEquals(zone, userAC.getZoneReference());

		userAC.setCredentialStatus(AgentCredentialStatus.SUSPENDED);
		service.createOrUpdate(userAC);

		userAC = service.findByFingerprint(zone, uc.getPublicCert().getFingerprint());
		assertNotNull(userAC);
		assertEquals(AgentCredentialStatus.SUSPENDED, userAC.getCredentialStatus());
		assertEquals(zone, userAC.getZoneReference());
	}
}