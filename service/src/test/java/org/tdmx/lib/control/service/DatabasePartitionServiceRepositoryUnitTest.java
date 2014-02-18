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
import org.tdmx.lib.console.domain.DatabasePartitionFacade;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;

@RunWith(SpringJUnit4ClassRunner.class)

@ContextConfiguration

//@TransactionConfiguration(transactionManager="tdmx.lib.control.TransactionManager")
//@Transactional("ControlDB")
public class DatabasePartitionServiceRepositoryUnitTest {

	@Autowired
	private DatabasePartitionService service;
	
	@Before
	public void doSetup() throws Exception {

		{
			DatabasePartition zp1 = DatabasePartitionFacade.createDatabasePartition("z-segment1-id1", DatabaseType.ZONE, "segment1");
			service.createOrUpdate(zp1);
	
			DatabasePartition zp2 = DatabasePartitionFacade.createDatabasePartition("z-segment1-id2", DatabaseType.ZONE, "segment1");
			service.createOrUpdate(zp2);
	
			DatabasePartition zp3 = DatabasePartitionFacade.createDatabasePartition("z-segment1-id3", DatabaseType.ZONE, "segment1");
			service.createOrUpdate(zp3);
		}
		{
			DatabasePartition zp1 = DatabasePartitionFacade.createDatabasePartition("z-segment2-id1", DatabaseType.ZONE, "segment2");
			service.createOrUpdate(zp1);
	
			DatabasePartition zp2 = DatabasePartitionFacade.createDatabasePartition("z-segment2-id2", DatabaseType.ZONE, "segment2");
			service.createOrUpdate(zp2);
	
			DatabasePartition zp3 = DatabasePartitionFacade.createDatabasePartition("z-segment2-id3", DatabaseType.ZONE, "segment2");
			service.createOrUpdate(zp3);
		}

		{
			DatabasePartition p1 = DatabasePartitionFacade.createDatabasePartition("m-segment1-id1", DatabaseType.MESSAGE, "segment1");
			service.createOrUpdate(p1);
	
			DatabasePartition p2 = DatabasePartitionFacade.createDatabasePartition("m-segment1-id2", DatabaseType.MESSAGE, "segment1");
			service.createOrUpdate(p2);
	
			DatabasePartition p3 = DatabasePartitionFacade.createDatabasePartition("m-segment1-id3", DatabaseType.MESSAGE, "segment1");
			service.createOrUpdate(p3);
		}
	}
	
	@After
	public void doTeardown() {
		{
			DatabasePartition p = service.findById("z-segment1-id1");
			if ( p != null ) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findById("z-segment1-id2");
			if ( p != null ) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findById("z-segment1-id3");
			if ( p != null ) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findById("z-segment2-id1");
			if ( p != null ) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findById("z-segment2-id2");
			if ( p != null ) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findById("z-segment2-id3");
			if ( p != null ) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findById("m-segment1-id1");
			if ( p != null ) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findById("m-segment1-id2");
			if ( p != null ) {
				service.delete(p);
			}
		}
		{
			DatabasePartition p = service.findById("m-segment1-id3");
			if ( p != null ) {
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
		DatabasePartition zp1 = service.findById("z-segment1-id1");
		assertNotNull(zp1);
		assertNotNull(zp1.getPartitionId());
		assertEquals("z-segment1-id1",zp1.getPartitionId());
		assertEquals(DatabaseType.ZONE, zp1.getDbType());
		assertNotNull(zp1.getSegment());
		assertNotNull(zp1.getUrl());
		assertNotNull(zp1.getUsername());
		assertNotNull(zp1.getObfuscatedPassword());
		assertEquals(100, zp1.getSizeFactor());
		assertNotNull(zp1.getActivationTimestamp());
		assertNull(zp1.getDeactivationTimestamp());
	}
	
	@Test
	public void testLookup_NotFound() throws Exception {
		DatabasePartition zp1 = service.findById("gugus");
		assertNull(zp1);
	}
	
	@Test
	public void testModify() throws Exception {
		DatabasePartition zp1 = service.findById("z-segment1-id1");
		// only these 4 fields can be changed. ( activationDate too if null )
		zp1.setDeactivationTimestamp( new Date() );
		zp1.setUrl("new.url");
		zp1.setUsername("new.username");
		zp1.setObfuscatedPassword("new.pwd");
		
		assertNotNull(zp1);
		service.createOrUpdate(zp1);
		
		DatabasePartition zp2 = service.findById("z-segment1-id1");
		
		assertTrue( zp1 != zp2 );
		assertEquals(zp1.getPartitionId(), zp2.getPartitionId());
		assertEquals(zp1.getDbType(), zp2.getDbType());
		assertEquals(zp1.getSegment(), zp2.getSegment());
		assertEquals(zp1.getUrl(), zp2.getUrl());
		assertEquals(zp1.getUsername(), zp2.getUsername());
		assertEquals(zp1.getObfuscatedPassword(), zp2.getObfuscatedPassword());
		assertEquals(zp1.getSizeFactor(), zp2.getSizeFactor());
		assertEquals(zp1.getActivationTimestamp(), zp2.getActivationTimestamp());
		assertEquals(zp1.getDeactivationTimestamp(), zp2.getDeactivationTimestamp());
	}

	@Test
	public void testModify_Failure() throws Exception {
		DatabasePartition zp1 = service.findById("z-segment1-id1");
		assertNotNull(zp1);

		// several fields are immutable once active only
		zp1.setSizeFactor(101);
		
		try {
			service.createOrUpdate(zp1);
			fail();
		} catch ( IllegalStateException e ) {
			//ok
		}
	}


	@Test
	public void testLookupCache_ByTypeOnly() throws Exception {
		List<DatabasePartition> zonelist = service.findByType(DatabaseType.ZONE);
		assertNotNull(zonelist);
		assertEquals(6, zonelist.size());

		List<DatabasePartition> messagelist = service.findByType(DatabaseType.MESSAGE);
		assertNotNull(messagelist);
		assertEquals(3, messagelist.size());
	}
	
	@Test
	public void testLookupCache_ByTypeNotFound() throws Exception {
		List<DatabasePartition> l = service.findByType(DatabaseType.CONSOLE);
		assertNotNull(l);
		assertEquals(0, l.size());
	}
	
	@Test
	public void testLookupCache_ByTypeAndSegment() throws Exception {
		List<DatabasePartition> zonelist = service.findByTypeAndSegment(DatabaseType.ZONE, "segment1" );
		assertNotNull(zonelist);
		assertEquals(3, zonelist.size());
		
		List<DatabasePartition> zonelist2 = service.findByTypeAndSegment(DatabaseType.ZONE, "segment1" );
		assertNotNull(zonelist2);
		assertEquals(3, zonelist2.size());
		
		assertTrue( zonelist == zonelist2 ); //cached result
	}
	
	@Test
	public void testLookupCache_ByTypeAndSegmentNotFound() throws Exception {
		List<DatabasePartition> l = service.findByTypeAndSegment(DatabaseType.CONSOLE, "gugus");
		assertNotNull(l);
		assertEquals(0, l.size());
		
		l = service.findByTypeAndSegment(DatabaseType.ZONE, "gugus");
		assertNotNull(l);
		assertEquals(0, l.size());
	}
	
}