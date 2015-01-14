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
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainID;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager="tdmx.lib.zone.TransactionManager")
// @Transactional("ZoneDB")
public class DomainServiceRepositoryUnitTest {

	@Autowired
	private ZoneService zoneService;

	@Autowired
	private DomainService domainService;

	private DomainID id = null;

	@Before
	public void doSetup() throws Exception {
		id = new DomainID();
		id.setZoneApex("ZONE.ROOT.TEST");
		id.setDomainName("SUBDOMAIN." + id.getZoneApex());

		Zone az = ZoneFacade.createZone(id.getZoneApex());

		zoneService.createOrUpdate(az);

		Domain d = ZoneFacade.createDomain(id);
		domainService.createOrUpdate(d);
	}

	@After
	public void doTeardown() {
		Domain d = domainService.findById(id);
		if (d != null) {
			domainService.delete(d);
		}
		Zone az = zoneService.findByZoneApex(id.getZoneApex());
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
		Domain d = domainService.findById(id);
		assertNotNull(d);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
	}

	@Test
	public void testSearch_Zone() throws Exception {
		DomainSearchCriteria criteria = new DomainSearchCriteria(new PageSpecifier(0, 10));
		List<Domain> domains = domainService.search(id.getZoneApex(), criteria);
		assertNotNull(domains);
		assertEquals(1, domains.size());
		Domain d = domains.get(0);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
	}

	@Test
	public void testSearch_ZoneAndDomain() throws Exception {
		DomainSearchCriteria criteria = new DomainSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(id.getDomainName());

		List<Domain> domains = domainService.search(id.getZoneApex(), criteria);
		assertNotNull(domains);
		assertEquals(1, domains.size());
		Domain d = domains.get(0);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
	}

	@Test
	public void testSearch_UnknownZoneAndDomain() throws Exception {
		DomainSearchCriteria criteria = new DomainSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(id.getDomainName());

		List<Domain> domains = domainService.search("gugusZone", criteria);
		assertNotNull(domains);
		assertEquals(0, domains.size());
	}

	@Test
	public void testSearch_UnknownZoneAndUnknownDomain() throws Exception {
		DomainSearchCriteria criteria = new DomainSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName("gugusdomain");

		List<Domain> domains = domainService.search("gugusZone", criteria);
		assertNotNull(domains);
		assertEquals(0, domains.size());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		DomainID gugusID = new DomainID("gugus", id.getZoneApex());
		Domain d = domainService.findById(gugusID);
		assertNull(d);
	}

	@Test
	public void testModify() throws Exception {
		Domain d = domainService.findById(id);
		domainService.createOrUpdate(d);

		Domain d2 = domainService.findById(id);

		assertEquals(d.getId(), d2.getId());
	}
}