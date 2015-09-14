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
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.control.domain.PartitionControlServer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PartitionControlServerServiceRepositoryUnitTest {

	@Autowired
	private PartitionControlServerService service;

	private PartitionControlServer pcs;

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Before
	public void doSetup() throws Exception {
		pcs = new PartitionControlServer();
		pcs.setIpAddress("0.0.0.0");
		pcs.setPort(1);
		pcs.setSegment("UNIT-TEST-SEG");
		pcs.setServerModulo(0);
		service.createOrUpdate(pcs);
	}

	@After
	public void doTeardown() {
		PartitionControlServer p = service.findByIpEndpoint(pcs.getIpAddress(), pcs.getPort());
		if (p != null) {
			service.delete(p);
		}
	}

	@Test
	public void testLookup_NotFoundSegment() throws Exception {
		List<PartitionControlServer> pcss = service.findBySegment(UUID.randomUUID().toString());

		assertNotNull(pcss);
		assertTrue(pcss.isEmpty());
	}

	@Test
	public void testSearch_NotFoundIpAddress() throws Exception {
		PartitionControlServer p = service.findByIpEndpoint("x.y.z.a", 1);

		assertNull(p);
	}

	@Test
	public void testFindByIp_Success() throws Exception {
		PartitionControlServer p = service.findByIpEndpoint(pcs.getIpAddress(), pcs.getPort());

		assertNotNull(p);
		assertEquals(pcs.getSegment(), p.getSegment());
	}

	@Test
	public void testFindAll_Success() throws Exception {
		List<PartitionControlServer> pcss = service.findAll();

		assertNotNull(pcss);
		assertTrue(!pcss.isEmpty());
	}

}
