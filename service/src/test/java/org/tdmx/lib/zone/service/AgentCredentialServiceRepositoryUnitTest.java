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
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialID;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.domain.AgentCredentialType;

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

		AgentCredential zoneAC = factory.createAgentCredential(zac.getCertificateChain());
		zoneAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(zoneAC);
		assertEquals(zoneApex, zoneAC.getId().getZoneApex());
		assertNull(zoneAC.getDomainName());
		assertNull(zoneAC.getAddressName());
		assertEquals(AgentCredentialType.ZAC, zoneAC.getCredentialType());
		service.createOrUpdate(zoneAC);

		AgentCredential domainAC = factory.createAgentCredential(dac.getCertificateChain());
		domainAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(domainAC);
		assertNotNull(domainAC.getId().getZoneApex());
		assertNotNull(domainAC.getDomainName());
		assertNull(domainAC.getAddressName());
		service.createOrUpdate(domainAC);

		AgentCredential userAC = factory.createAgentCredential(uc.getCertificateChain());
		userAC.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		assertNotNull(userAC);
		assertEquals(zoneApex, userAC.getId().getZoneApex());
		assertNotNull(userAC.getDomainName());
		assertNotNull(userAC.getAddressName());
		service.createOrUpdate(userAC);
	}

	@After
	public void doTeardown() {
		List<AgentCredential> list = service.search(zoneApex,
				new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(new PageSpecifier(0, 1000)));
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
		AgentCredentialID id = new AgentCredentialID(zoneApex, zac.getPublicCert().getFingerprint());
		AgentCredential zoneAC = service.findById(id);
		assertNotNull(zoneAC);
		assertEquals(zoneApex, zoneAC.getId().getZoneApex());

		id = new AgentCredentialID(zoneApex, dac.getPublicCert().getFingerprint());
		AgentCredential domainAC = service.findById(id);
		assertNotNull(domainAC);
		assertEquals(zoneApex, domainAC.getId().getZoneApex());

		id = new AgentCredentialID(zoneApex, uc.getPublicCert().getFingerprint());
		AgentCredential userAC = service.findById(id);
		assertNotNull(userAC);
		assertEquals(zoneApex, userAC.getId().getZoneApex());
	}

	@Test
	public void testLookupByDomain() throws Exception {
		AgentCredentialSearchCriteria sc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		sc.setDomainName(dac.getPublicCert().getCommonName());
		List<AgentCredential> domainCerts = service.search(zoneApex, sc);
		assertNotNull(domainCerts);
		assertEquals(2, domainCerts.size()); // DAC, UC
	}

	@Test
	public void testLookupByDomainAndType() throws Exception {
		AgentCredentialSearchCriteria dsc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dsc.setDomainName(dac.getPublicCert().getCommonName());
		dsc.setType(AgentCredentialType.DAC);

		List<AgentCredential> dacCerts = service.search(zoneApex, dsc);
		assertNotNull(dacCerts);
		assertEquals(1, dacCerts.size()); // DAC
		// TODO prove dac

		AgentCredentialSearchCriteria usc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		usc.setDomainName(dac.getPublicCert().getCommonName());
		usc.setType(AgentCredentialType.UC);

		List<AgentCredential> ucCerts = service.search(zoneApex, usc);
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

		List<AgentCredential> ucCerts = service.search(zoneApex, sc);
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

		List<AgentCredential> ucCerts = service.search(zoneApex, sc);
		assertNotNull(ucCerts);
		assertEquals(1, ucCerts.size()); // UC
		// TODO prove uc

		sc.setStatus(AgentCredentialStatus.SUSPENDED);
		ucCerts = service.search(zoneApex, sc);
		assertNotNull(ucCerts);
		assertEquals(0, ucCerts.size());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		AgentCredentialID id = new AgentCredentialID(zoneApex, "gugus");

		AgentCredential az = service.findById(id);
		assertNull(az);
	}

	@Test
	public void testModify_Status() throws Exception {
		AgentCredentialID id = new AgentCredentialID(zoneApex, uc.getPublicCert().getFingerprint());

		AgentCredential userAC = service.findById(id);
		assertNotNull(userAC);
		assertEquals(AgentCredentialStatus.ACTIVE, userAC.getCredentialStatus());
		assertEquals(zoneApex, userAC.getId().getZoneApex());

		userAC.setCredentialStatus(AgentCredentialStatus.SUSPENDED);
		service.createOrUpdate(userAC);

		userAC = service.findById(id);
		assertNotNull(userAC);
		assertEquals(AgentCredentialStatus.SUSPENDED, userAC.getCredentialStatus());
		assertEquals(zoneApex, userAC.getId().getZoneApex());
	}

}