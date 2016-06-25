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
package org.tdmx.lib.control.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.control.domain.DnsDomainZone;
import org.tdmx.lib.control.domain.DnsDomainZoneFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/tdmx/test-context.xml")
public class DnsDomainZoneRepositoryUnitTest {

	@Autowired
	private DnsDomainZoneService service;

	private Date d1_to;
	private Date d1_from;
	private Date d2_to;
	private Date d2_from;

	@Before
	public void doSetup() throws Exception {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1); // +1day
		d1_to = cal.getTime();
		cal.add(Calendar.DATE, -2);
		d1_from = cal.getTime();
		cal.add(Calendar.DATE, -1);
		d2_to = cal.getTime();
		cal.add(Calendar.DATE, -1);
		d2_from = cal.getTime();

		DnsDomainZone d11 = DnsDomainZoneFacade.createDnsDomainZone("domain1.zone.com", "zone.com",
				"scs.serviceprovider.com", d1_from, d1_to);
		service.createOrUpdate(d11);

		DnsDomainZone d12 = DnsDomainZoneFacade.createDnsDomainZone("domain1.zone.com", "zone.com",
				"scs.serviceprovider.com", d2_from, d2_to);
		service.createOrUpdate(d12);

		DnsDomainZone d2 = DnsDomainZoneFacade.createDnsDomainZone("domain2.zone.com", "zone.com",
				"scs.serviceprovider.com", d2_from, d2_to);
		service.createOrUpdate(d2);
	}

	@After
	public void doTeardown() {
		List<DnsDomainZone> records = service.findByDomain("domain1.zone.com");
		for (DnsDomainZone d : records) {
			service.delete(d);
		}
		records = service.findByDomain("domain2.zone.com");
		for (DnsDomainZone d : records) {
			service.delete(d);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookupAll() throws Exception {
		List<DnsDomainZone> records = service.findByDomain("domain1.zone.com");
		assertNotNull(records);
		assertEquals(2, records.size());
	}

	@Test
	public void testLookup_CurrentNotFound() throws Exception {
		DnsDomainZone zp1 = service.findCurrentByDomain("gugus");
		assertNull(zp1);
	}

	@Test
	public void testLookup_CurrentFound() throws Exception {
		DnsDomainZone zp1 = service.findCurrentByDomain("domain1.zone.com");
		assertNotNull(zp1);
		assertEquals(d1_from, zp1.getValidFromTime());
		assertEquals(d1_to, zp1.getValidUntilTime());

		// domain2 is not currently active
		DnsDomainZone zp2 = service.findCurrentByDomain("domain2.zone.com");
		assertNull(zp2);
	}

	@Test
	public void testModify_Current() throws Exception {
		DnsDomainZone zp1 = service.findCurrentByDomain("domain1.zone.com");
		assertNotNull(zp1);
		assertEquals(d1_from, zp1.getValidFromTime());
		assertEquals(d1_to, zp1.getValidUntilTime());

		zp1.setScsUrl(new URL("https://scsHost/url"));
		service.createOrUpdate(zp1);

		zp1 = service.findCurrentByDomain("domain1.zone.com");
		assertNotNull(zp1);
		assertEquals(d1_from, zp1.getValidFromTime());
		assertEquals(d1_to, zp1.getValidUntilTime());
		assertEquals("https://scsHost/url", zp1.getScsUrl().toString());
		assertEquals("scsHost", zp1.getScsHostname());

	}

	// TODO modify first (existing)
}