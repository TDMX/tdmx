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
import org.tdmx.lib.control.domain.DnsResolverGroup;
import org.tdmx.lib.control.domain.DnsResolverGroupFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/tdmx/test-context.xml")
public class DnsResolverGroupRepositoryUnitTest {

	@Autowired
	private DnsResolverGroupService service;

	@Before
	public void doSetup() throws Exception {

		{
			DnsResolverGroup g1 = DnsResolverGroupFacade.createDnsResolverGroup("unittest-drg-1");
			service.createOrUpdate(g1);

			DnsResolverGroup g2 = DnsResolverGroupFacade.createDnsResolverGroup("unittest-drg-2");
			service.createOrUpdate(g2);
		}
	}

	@After
	public void doTeardown() {
		DnsResolverGroup g1 = service.findByName("unittest-drg-1");
		if (g1 != null) {
			service.delete(g1);
		}
		DnsResolverGroup g2 = service.findByName("unittest-drg-2");
		if (g2 != null) {
			service.delete(g2);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		DnsResolverGroup zp1 = service.findByName("unittest-drg-1");
		assertNotNull(zp1);
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		DnsResolverGroup zp1 = service.findByName("gugus");
		assertNull(zp1);
	}

	@Test
	public void testModify() throws Exception {
		DnsResolverGroup zp1 = service.findByName("unittest-drg-1");
		assertNotNull(zp1);

		service.createOrUpdate(zp1);

		DnsResolverGroup zp2 = service.findByName("unittest-drg-1");
		assertNotNull(zp2);

		assertTrue(zp1 != zp2);
	}

	@Test
	public void testLookupAll() throws Exception {
		List<DnsResolverGroup> l = service.findAll();
		assertNotNull(l);
		assertTrue(l.size() >= 2);
	}

}