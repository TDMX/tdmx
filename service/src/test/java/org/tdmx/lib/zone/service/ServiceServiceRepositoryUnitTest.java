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
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.ServiceID;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager="tdmx.lib.zone.TransactionManager")
// @Transactional("ZoneDB")
public class ServiceServiceRepositoryUnitTest {

	@Autowired
	private ZoneService zoneService;

	@Autowired
	private ServiceService serviceService;

	private ServiceID id = null;

	@Before
	public void doSetup() throws Exception {
		id = new ServiceID();
		id.setZoneApex("ZONE.ROOT.TEST");
		id.setDomainName("SUBDOMAIN." + id.getZoneApex());
		id.setServiceName("serviceName");

		Zone az = ZoneFacade.createZone(id.getZoneApex());

		zoneService.createOrUpdate(az);

		Service d = ZoneFacade.createService(id);
		serviceService.createOrUpdate(d);
	}

	@After
	public void doTeardown() {
		Service d = serviceService.findById(id);
		if (d != null) {
			serviceService.delete(d);
		}
		Zone az = zoneService.findByZoneApex(id.getZoneApex());
		if (az != null) {
			zoneService.delete(az);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(zoneService);
		assertNotNull(serviceService);
	}

	@Test
	public void testLookup() throws Exception {
		Service d = serviceService.findById(id);
		assertNotNull(d);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
		assertEquals(id.getServiceName(), d.getId().getServiceName());
	}

	@Test
	public void testSearch_Zone() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		List<Service> services = serviceService.search(id.getZoneApex(), criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service d = services.get(0);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
		assertEquals(id.getServiceName(), d.getId().getServiceName());
	}

	@Test
	public void testSearch_ZoneAndService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setServiceName(id.getServiceName());

		List<Service> services = serviceService.search(id.getZoneApex(), criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service d = services.get(0);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
		assertEquals(id.getServiceName(), d.getId().getServiceName());
	}

	@Test
	public void testSearch_ZoneAndDomainAndService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(id.getDomainName());
		criteria.setServiceName(id.getServiceName());

		List<Service> services = serviceService.search(id.getZoneApex(), criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service d = services.get(0);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
		assertEquals(id.getServiceName(), d.getId().getServiceName());
	}

	@Test
	public void testSearch_ZoneAndDomainOnly() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(id.getDomainName());

		List<Service> services = serviceService.search(id.getZoneApex(), criteria);
		assertNotNull(services);
		assertEquals(1, services.size());
		Service d = services.get(0);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
		assertEquals(id.getServiceName(), d.getId().getServiceName());
	}

	@Test
	public void testSearch_UnknownZoneAndService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setServiceName(id.getServiceName());

		List<Service> services = serviceService.search("gugusZone", criteria);
		assertNotNull(services);
		assertEquals(0, services.size());
	}

	@Test
	public void testSearch_UnknownZoneAndUnknownService() throws Exception {
		ServiceSearchCriteria criteria = new ServiceSearchCriteria(new PageSpecifier(0, 10));
		criteria.setServiceName(id.getServiceName());

		List<Service> services = serviceService.search("gugusZone", criteria);
		assertNotNull(services);
		assertEquals(0, services.size());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		ServiceID gugusID = new ServiceID("gugus", id.getDomainName(), id.getZoneApex());
		Service d = serviceService.findById(gugusID);
		assertNull(d);
	}

	@Test
	public void testModify() throws Exception {
		Service d = serviceService.findById(id);
		serviceService.createOrUpdate(d);

		Service d2 = serviceService.findById(id);

		assertEquals(d.getId(), d2.getId());
	}

}