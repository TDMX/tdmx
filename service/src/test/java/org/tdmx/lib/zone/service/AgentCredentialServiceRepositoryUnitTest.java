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

import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.control.service.MockDatabasePartitionInstaller;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AgentCredentialServiceRepositoryUnitTest {

	@Autowired
	private AgentCredentialService service;
	@Autowired
	private ZoneService zoneService;

	@Autowired
	private AgentCredentialFactory factory;

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private AddressService addressService;
	@Autowired
	@Named("tdmx.lib.zone.ThreadLocalPartitionIdProvider")
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;

	private Zone zone;
	private AgentCredential zac;
	private Domain domain;
	private AgentCredential dac;
	private Address address;
	private AgentCredential uc;

	@Before
	public void doSetup() throws Exception {

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockDatabasePartitionInstaller.ZP1_S1);
		input.setNumZACs(1);
		input.setNumDomains(1);
		input.setNumDACsPerDomain(1);
		input.setNumAddressesPerDomain(1);
		input.setNumUsersPerAddress(1);

		data = dataGenerator.setUp(input);

		zone = data.getZone();
		zac = data.getZacs().get(0).getAg();
		domain = data.getDomains().get(0).getDomain();
		dac = data.getDomains().get(0).getDacs().get(0).getAg();
		address = data.getDomains().get(0).getAddresses().get(0).getAddress();
		uc = data.getDomains().get(0).getAddresses().get(0).getUcs().get(0).getAg();

		zonePartitionIdProvider.setPartitionId(input.getZonePartitionId());

	}

	@After
	public void doTeardown() {
		zonePartitionIdProvider.clearPartitionId();

		dataGenerator.tearDown(input, data);
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(addressService);
	}

	@Test
	public void testLookupById() throws Exception {
		AgentCredential zoneAC = service.findByFingerprint(zac.getFingerprint());
		assertNotNull(zoneAC);
		assertEquals(AgentCredentialType.ZAC, zoneAC.getCredentialType());

		AgentCredential domainAC = service.findByFingerprint(dac.getFingerprint());
		assertNotNull(domainAC);
		assertEquals(AgentCredentialType.DAC, domainAC.getCredentialType());

		AgentCredential userAC = service.findByFingerprint(uc.getFingerprint());
		assertNotNull(userAC);
		assertEquals(AgentCredentialType.UC, userAC.getCredentialType());
	}

	@Test
	public void testLookupByDomain() throws Exception {
		AgentCredentialSearchCriteria sc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		sc.setDomainName(domain.getDomainName());
		List<AgentCredential> domainCerts = service.search(zone, sc);
		assertNotNull(domainCerts);
		assertEquals(2, domainCerts.size()); // DAC, UC
	}

	@Test
	public void testLookupByDomainAndType() throws Exception {
		AgentCredentialSearchCriteria dsc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dsc.setDomainName(domain.getDomainName());
		dsc.setType(AgentCredentialType.DAC);

		List<AgentCredential> dacCerts = service.search(zone, dsc);
		assertNotNull(dacCerts);
		assertEquals(1, dacCerts.size()); // DAC
		// TODO prove dac

		AgentCredentialSearchCriteria usc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		usc.setDomainName(domain.getDomainName());
		usc.setType(AgentCredentialType.UC);

		List<AgentCredential> ucCerts = service.search(zone, usc);
		assertNotNull(ucCerts);
		assertEquals(1, ucCerts.size()); // UC
		// TODO prove uc

		AgentCredentialSearchCriteria zsc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		zsc.setDomainName(domain.getDomainName());
		zsc.setType(AgentCredentialType.ZAC);

		List<AgentCredential> zacCerts = service.search(zone, zsc);
		assertNotNull(zacCerts);
		assertEquals(0, zacCerts.size()); // ZAC not found since ZAC doesn't have domainName set.
	}

	@Test
	public void testLookupByAddressAndType() throws Exception {
		AgentCredentialSearchCriteria sc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		sc.setDomainName(domain.getDomainName());
		sc.setAddressName(address.getLocalName());
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
		sc.setDomainName(domain.getDomainName());
		sc.setAddressName(address.getLocalName());
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
	public void testLookup_NotFound() throws Exception {
		AgentCredential az = service.findByFingerprint("gugus");
		assertNull(az);
	}

	@Test
	public void testModify_Status() throws Exception {
		AgentCredential userAC = service.findByFingerprint(uc.getFingerprint());
		assertNotNull(userAC);
		assertEquals(AgentCredentialStatus.ACTIVE, userAC.getCredentialStatus());

		userAC.setCredentialStatus(AgentCredentialStatus.SUSPENDED);
		service.createOrUpdate(userAC);

		userAC = service.findByFingerprint(uc.getFingerprint());
		assertNotNull(userAC);
		assertEquals(AgentCredentialStatus.SUSPENDED, userAC.getCredentialStatus());
	}
}