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
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.zone.domain.ChannelAuthorization;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ChannelAuthorizationServiceRepositoryUnitTest {

	@Autowired
	private TestDataGenerator dataGenerator;

	@Autowired
	private ChannelAuthorizationService channelAuthorizationService;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;

	private ZoneReference zone;

	@Before
	public void doSetup() throws Exception {

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockZonePartitionIdInstaller.ZP1_S1);
		input.setNumZACs(1);
		input.setNumDomains(1);
		input.setNumDACsPerDomain(1);
		input.setNumAddressesPerDomain(1);
		input.setNumUsersPerAddress(1);

		data = dataGenerator.generate(input);

		zone = data.getAccountZone().getZoneReference();
	}

	@After
	public void doTeardown() {
		dataGenerator.tearDown(data.getAccount());
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(dataGenerator);
		assertNotNull(channelAuthorizationService);
	}

	@Test
	public void testFindById_NotFound() throws Exception {
		ChannelAuthorization c = channelAuthorizationService.findById(new Random().nextLong());
		assertNull(c);
	}

	//@formatter:off
/*
	@Test
	public void testLookup() throws Exception {
		Service s = serviceService.findByName(zone, domainName, serviceName);
		assertNotNull(s);
		assertEquals(zone, s.getZoneReference());
		assertEquals(domainName, s.getDomainName());
		assertEquals(serviceName, s.getServiceName());
	}

	@Test
	public void testSearch_Zone() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		List<Service> services = serviceService.search(zone, criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service s = services.get(0);
		assertEquals(zone, s.getZoneReference());
		assertEquals(domainName, s.getDomainName());
		assertEquals(serviceName, s.getServiceName());
	}

	@Test
	public void testSearch_ZoneAndService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setServiceName(serviceName);

		List<Service> services = serviceService.search(zone, criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service s = services.get(0);
		assertEquals(zone, s.getZoneReference());
		assertEquals(domainName, s.getDomainName());
		assertEquals(serviceName, s.getServiceName());
	}

	@Test
	public void testSearch_ZoneAndDomainAndService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(domainName);
		criteria.setServiceName(serviceName);

		List<Service> services = serviceService.search(zone, criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service s = services.get(0);
		assertEquals(zone, s.getZoneReference());
		assertEquals(domainName, s.getDomainName());
		assertEquals(serviceName, s.getServiceName());
	}

	@Test
	public void testSearch_ZoneAndDomainOnly() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(domainName);

		List<Service> services = serviceService.search(zone, criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service s = services.get(0);
		assertEquals(zone, s.getZoneReference());
		assertEquals(domainName, s.getDomainName());
		assertEquals(serviceName, s.getServiceName());
	}

	@Test
	public void testSearch_UnknownZoneAndService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setServiceName(serviceName);

		ZoneReference gugus = new ZoneReference(zone.getTenantId(), "gugus");
		List<Service> services = serviceService.search(gugus, criteria);
		assertNotNull(services);
		assertEquals(0, services.size());
	}

	@Test
	public void testSearch_UnknownZoneAndUnknownService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setServiceName(serviceName);

		ZoneReference gugus = new ZoneReference(new Random().nextLong(), zone.getZoneApex());
		List<Service> services = serviceService.search(gugus, criteria);
		assertNotNull(services);
		assertEquals(0, services.size());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		Service d = serviceService.findByName(zone, domainName, "gugus");
		assertNull(d);
	}

	@Test
	public void testModify() throws Exception {
		Service d = serviceService.findByName(zone, domainName, serviceName);
		d.setConcurrencyLimit(20);
		serviceService.createOrUpdate(d);

		Service d2 = serviceService.findByName(zone, domainName, serviceName);

		assertEquals(d.getZoneReference(), d2.getZoneReference());
		assertEquals(d.getDomainName(), d2.getDomainName());
		assertEquals(d.getServiceName(), d2.getServiceName());
		assertEquals(d.getConcurrencyLimit(), d2.getConcurrencyLimit());
	}
*/
	//@formatter:on
}