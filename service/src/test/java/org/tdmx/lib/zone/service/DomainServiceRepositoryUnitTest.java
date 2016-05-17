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
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DomainServiceRepositoryUnitTest {

	@Autowired
	private ZoneService zoneService;

	@Autowired
	private DomainService domainService;

	private Zone zone;
	private String domainName;

	@Before
	public void doSetup() throws Exception {
		zone

		= ZoneFacade.createZone(new Random().nextLong(), "ZONE.ROOT.TEST");

		zoneService.createOrUpdate(zone);

		domainName = "SUBDOMAIN." + zone.getZoneApex();
		Domain d = ZoneFacade.createDomain(zone, domainName);
		domainService.createOrUpdate(d);
	}

	@After
	public void doTeardown() {
		Domain d = domainService.findByName(zone, domainName);
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
	}

	@Test
	public void testLookup() throws Exception {
		Domain d = domainService.findByName(zone, domainName);
		assertNotNull(d);
		assertEquals(domainName, d.getDomainName());
	}

	@Test
	public void testLookup_NotFoundDomain() throws Exception {
		Domain d = domainService.findByName(zone, "gugus");
		assertNull(d);
	}

	@Test
	public void testLookup_NotFoundZone() throws Exception {
		Zone gugus = new Zone(zone.getAccountZoneId(), "gugus");
		gugus.setId(new Random().nextLong());

		Domain d = domainService.findByName(gugus, domainName);
		assertNull(d);
	}

	@Test
	public void testSearch_DomainName() throws Exception {
		DomainSearchCriteria criteria = new DomainSearchCriteria(new PageSpecifier(0, 10));
		List<Domain> domains = domainService.search(zone, criteria);
		assertNotNull(domains);
		assertEquals(1, domains.size());
		Domain d = domains.get(0);
		assertEquals(domainName, d.getDomainName());
	}

	@Test
	public void testSearch_UnknownDomain() throws Exception {
		DomainSearchCriteria criteria = new DomainSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName("gugusdomain");

		List<Domain> domains = domainService.search(zone, criteria);
		assertNotNull(domains);
		assertEquals(0, domains.size());
	}

	@Test
	public void testModify() throws Exception {
		Domain d = domainService.findByName(zone, domainName);
		domainService.createOrUpdate(d);

		Domain d2 = domainService.findByName(zone, domainName);

		assertEquals(d.getId(), d2.getId());
	}
}