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
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/tdmx/test-context.xml")
public class ServiceServiceRepositoryUnitTest {

	@Autowired
	private ZoneService zoneService;
	@Autowired
	private DomainService domainService;

	@Autowired
	private ServiceService serviceService;

	private Domain domain;
	private String serviceName;
	private Zone zone;

	@Before
	public void doSetup() throws Exception {

		zone = ZoneFacade.createZone(new Random().nextLong(), "ZONE.ROOT.TEST");

		serviceName = "serviceName";

		zoneService.createOrUpdate(zone);

		domain = new Domain(zone, "SUBDOMAIN." + zone.getZoneApex());
		domainService.createOrUpdate(domain);

		Service s = ZoneFacade.createService(domain, serviceName);
		serviceService.createOrUpdate(s);
	}

	@After
	public void doTeardown() {
		Service s = serviceService.findByName(domain, serviceName);
		if (s != null) {
			serviceService.delete(s);
		}
		Domain d = domainService.findById(domain.getId());
		if (d != null) {
			domainService.delete(d);
		}
		Zone az = zoneService.findById(zone.getId());
		if (az != null) {
			zoneService.delete(az);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(zoneService);
		assertNotNull(domainService);
		assertNotNull(serviceService);
	}

	@Test
	public void testLookup() throws Exception {
		Service s = serviceService.findByName(domain, serviceName);
		assertNotNull(s);
		assertEquals(domain.getDomainName(), s.getDomain().getDomainName());
		assertEquals(serviceName, s.getServiceName());
	}

	@Test
	public void testSearch_Zone() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		List<Service> services = serviceService.search(zone, criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service s = services.get(0);
		assertEquals(domain.getDomainName(), s.getDomain().getDomainName());
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
		assertEquals(domain.getDomainName(), s.getDomain().getDomainName());
		assertEquals(serviceName, s.getServiceName());
	}

	@Test
	public void testSearch_ZoneAndDomainAndService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(domain.getDomainName());
		criteria.setServiceName(serviceName);

		List<Service> services = serviceService.search(zone, criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service s = services.get(0);
		assertEquals(domain.getDomainName(), s.getDomain().getDomainName());
		assertEquals(serviceName, s.getServiceName());
	}

	@Test
	public void testSearch_ZoneAndDomainOnly() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(domain.getDomainName());

		List<Service> services = serviceService.search(zone, criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service s = services.get(0);
		assertEquals(domain.getDomainName(), s.getDomain().getDomainName());
		assertEquals(serviceName, s.getServiceName());
	}

	@Test
	public void testSearch_UnknownZoneAndService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setServiceName(serviceName);

		Zone gugus = new Zone(zone.getAccountZoneId(), "gugus");
		gugus.setId(new Random().nextLong());

		List<Service> services = serviceService.search(gugus, criteria);
		assertNotNull(services);
		assertEquals(0, services.size());
	}

	@Test
	public void testSearch_UnknownService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setServiceName("gugus");

		List<Service> services = serviceService.search(zone, criteria);
		assertNotNull(services);
		assertEquals(0, services.size());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		Service d = serviceService.findByName(domain, "gugus");
		assertNull(d);
	}

}