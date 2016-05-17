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
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ZoneServiceRepositoryUnitTest {

	@Autowired
	private ZoneService service;

	private Zone zone;

	@Before
	public void doSetup() throws Exception {
		zone = ZoneFacade.createZone(new Random().nextLong(), "zone.root.test");

		service.createOrUpdate(zone);
		assertNotNull(zone.getId());
	}

	@After
	public void doTeardown() {
		Zone az = service.findById(zone.getId());
		if (az != null) {
			service.delete(az);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		Zone az = service.findByZoneApex(zone.getZoneApex());
		assertNotNull(az);
		assertEquals(zone.getId(), az.getId());
		assertEquals(zone.getAccountZoneId(), az.getAccountZoneId());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		Zone az = service.findByZoneApex("gugus");
		assertNull(az);
	}

	@Test
	public void testModify() throws Exception {
		Zone az = service.findByZoneApex(zone.getZoneApex());
		service.createOrUpdate(az);

		Zone az2 = service.findByZoneApex(zone.getZoneApex());

		assertTrue(az != az2);
		assertEquals(az.getAccountZoneId(), az2.getAccountZoneId());
		assertEquals(az.getZoneApex(), az2.getZoneApex());
		assertEquals(az.getId(), az2.getId());
	}

}