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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.domain.SegmentFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SegmentRepositoryUnitTest {

	@Autowired
	private SegmentService service;

	@Before
	public void doSetup() throws Exception {

		{
			Segment s1 = SegmentFacade.createSegment("unittest-segment-1",
					"https://unittest-segment-1.scs.tdmx.org/sp/v1.0/scs");
			service.createOrUpdate(s1);

			Segment s2 = SegmentFacade.createSegment("unittest-segment-2",
					"https://unittest-segment-2.scs.tdmx.org/sp/v1.0/scs");
			service.createOrUpdate(s2);
		}
	}

	@After
	public void doTeardown() {
		Segment s1 = service.findBySegment("unittest-segment-1");
		if (s1 != null) {
			service.delete(s1);
		}
		Segment s2 = service.findBySegment("unittest-segment-2");
		if (s2 != null) {
			service.delete(s2);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		Segment zp1 = service.findBySegment("unittest-segment-1");
		assertNotNull(zp1);
		assertNotNull(zp1.getScsUrl());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		Segment zp1 = service.findBySegment("gugus");
		assertNull(zp1);
	}

	@Test
	public void testModify() throws Exception {
		Segment zp1 = service.findBySegment("unittest-segment-1");
		assertNotNull(zp1);
		assertNotNull(zp1.getScsUrl());

		zp1.setScsUrl("new hostname");
		service.createOrUpdate(zp1);

		Segment zp2 = service.findBySegment("unittest-segment-1");
		assertNotNull(zp2);
		assertNotNull(zp2.getScsUrl());

		assertTrue(zp1 != zp2);
		assertEquals("new hostname", zp2.getScsUrl());
	}

	@Test
	public void testLookupAll() throws Exception {
		List<Segment> l = service.findAll();
		assertNotNull(l);
		assertTrue(l.size() >= 2);
	}

}