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
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabasePartitionFacade;
import org.tdmx.lib.control.domain.DatabasePartitionSearchCriteria;
import org.tdmx.lib.control.domain.DatabaseType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DatabasePartitionServiceRepositoryUnitTest {

	@Autowired
	private DatabasePartitionService service;

	@Before
	public void doSetup() throws Exception {

		{
			DatabasePartition zp1 = DatabasePartitionFacade.createDatabasePartition("z-segment1-id1", DatabaseType.ZONE,
					"unittest-segment-1");
			service.createOrUpdate(zp1);

			DatabasePartition zp2 = DatabasePartitionFacade.createDatabasePartition("z-segment1-id2", DatabaseType.ZONE,
					"unittest-segment-1");
			service.createOrUpdate(zp2);

			DatabasePartition zp3 = DatabasePartitionFacade.createDatabasePartition("z-segment1-id3", DatabaseType.ZONE,
					"unittest-segment-1");
			service.createOrUpdate(zp3);
		}
		{
			DatabasePartition zp1 = DatabasePartitionFacade.createDatabasePartition("z-segment2-id1", DatabaseType.ZONE,
					"unittest-segment-2");
			service.createOrUpdate(zp1);

			DatabasePartition zp2 = DatabasePartitionFacade.createDatabasePartition("z-segment2-id2", DatabaseType.ZONE,
					"unittest-segment-2");
			service.createOrUpdate(zp2);

			DatabasePartition zp3 = DatabasePartitionFacade.createDatabasePartition("z-segment2-id3", DatabaseType.ZONE,
					"unittest-segment-2");
			service.createOrUpdate(zp3);
		}

		{
			DatabasePartition p1 = DatabasePartitionFacade.createDatabasePartition("m-segment1-id1",
					DatabaseType.MESSAGE, "unittest-segment-1");
			service.createOrUpdate(p1);

			DatabasePartition p2 = DatabasePartitionFacade.createDatabasePartition("m-segment1-id2",
					DatabaseType.MESSAGE, "unittest-segment-1");
			service.createOrUpdate(p2);

			DatabasePartition p3 = DatabasePartitionFacade.createDatabasePartition("m-segment1-id3",
					DatabaseType.MESSAGE, "unittest-segment-1");
			service.createOrUpdate(p3);
		}
	}

	@After
	public void doTeardown() {
		{
			DatabasePartition p = service.findByPartitionId("z-segment1-id1");
			if (p != null) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findByPartitionId("z-segment1-id2");
			if (p != null) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findByPartitionId("z-segment1-id3");
			if (p != null) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findByPartitionId("z-segment2-id1");
			if (p != null) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findByPartitionId("z-segment2-id2");
			if (p != null) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findByPartitionId("z-segment2-id3");
			if (p != null) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findByPartitionId("m-segment1-id1");
			if (p != null) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findByPartitionId("m-segment1-id2");
			if (p != null) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findByPartitionId("m-segment1-id3");
			if (p != null) {
				service.delete(p);
			}
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		DatabasePartition zp1 = service.findByPartitionId("z-segment1-id1");
		assertNotNull(zp1);
		assertNotNull(zp1.getPartitionId());
		assertEquals("z-segment1-id1", zp1.getPartitionId());
		assertEquals(DatabaseType.ZONE, zp1.getDbType());
		assertNotNull(zp1.getSegment());
		assertNotNull(zp1.getUrl());
		assertNotNull(zp1.getUsername());
		assertNotNull(zp1.getPassword());
		assertEquals(100, zp1.getSizeFactor());
		assertNotNull(zp1.getActivationTimestamp());
		assertNull(zp1.getDeactivationTimestamp());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		DatabasePartition zp1 = service.findByPartitionId("gugus");
		assertNull(zp1);
	}

	@Test
	public void testModify() throws Exception {
		DatabasePartition zp1 = service.findByPartitionId("z-segment1-id1");
		// only these 4 fields can be changed. ( activationDate too if null )
		zp1.setDeactivationTimestamp(new Date());
		zp1.setUrl("new.url");
		zp1.setUsername("new.username");
		zp1.setPassword("new.pwd");

		assertNotNull(zp1);
		service.createOrUpdate(zp1);

		DatabasePartition zp2 = service.findByPartitionId("z-segment1-id1");

		assertTrue(zp1 != zp2);
		assertEquals(zp1.getPartitionId(), zp2.getPartitionId());
		assertEquals(zp1.getDbType(), zp2.getDbType());
		assertEquals(zp1.getSegment(), zp2.getSegment());
		assertEquals(zp1.getUrl(), zp2.getUrl());
		assertEquals(zp1.getUsername(), zp2.getUsername());
		assertEquals(zp1.getPassword(), zp2.getPassword());
		assertEquals(zp1.getSizeFactor(), zp2.getSizeFactor());
		assertEquals(zp1.getActivationTimestamp(), zp2.getActivationTimestamp());
		assertEquals(zp1.getDeactivationTimestamp(), zp2.getDeactivationTimestamp());
	}

	@Test
	public void testModify_Failure() throws Exception {
		DatabasePartition zp1 = service.findByPartitionId("z-segment1-id1");
		assertNotNull(zp1);

		// several fields are immutable once active only
		zp1.setSizeFactor(101);

		try {
			service.createOrUpdate(zp1);
			fail();
		} catch (IllegalStateException e) {
			// ok
		}
	}

	@Test
	public void testLookupCache_ByTypeOnly() throws Exception {
		DatabasePartitionSearchCriteria sc = new DatabasePartitionSearchCriteria(new PageSpecifier(0, 100));
		sc.setDbType(DatabaseType.ZONE);
		List<DatabasePartition> zonelist = service.search(sc);
		assertNotNull(zonelist);
		assertTrue(zonelist.size() > 6);

		sc.setDbType(DatabaseType.MESSAGE);
		List<DatabasePartition> messagelist = service.search(sc);
		assertNotNull(messagelist);
		assertEquals(3, messagelist.size());
	}

	@Test
	public void testLookupCache_ByTypeNotFound() throws Exception {
		DatabasePartitionSearchCriteria sc = new DatabasePartitionSearchCriteria(new PageSpecifier(0, 100));
		sc.setDbType(DatabaseType.CONSOLE);
		List<DatabasePartition> l = service.search(sc);
		assertNotNull(l);
		assertEquals(0, l.size());
	}

	@Test
	public void testLookupCache_ByTypeAndSegment() throws Exception {
		DatabasePartitionSearchCriteria sc = new DatabasePartitionSearchCriteria(new PageSpecifier(0, 100));
		sc.setDbType(DatabaseType.ZONE);
		sc.setSegment("unittest-segment-1");
		List<DatabasePartition> zonelist = service.search(sc);
		assertNotNull(zonelist);
		assertEquals(3, zonelist.size());

		sc.setDbType(DatabaseType.ZONE);
		sc.setSegment("unittest-segment-2");
		List<DatabasePartition> zonelist2 = service.search(sc);
		assertNotNull(zonelist2);
		assertEquals(3, zonelist2.size());
	}

	@Test
	public void testLookupCache_ByTypeAndSegmentNotFound() throws Exception {
		DatabasePartitionSearchCriteria sc = new DatabasePartitionSearchCriteria(new PageSpecifier(0, 100));
		sc.setDbType(DatabaseType.CONSOLE);
		sc.setSegment("gugus");
		List<DatabasePartition> l = service.search(sc);
		assertNotNull(l);
		assertEquals(0, l.size());

		sc.setDbType(DatabaseType.ZONE);
		sc.setSegment("gugus");
		l = service.search(sc);
		assertNotNull(l);
		assertEquals(0, l.size());
	}

}