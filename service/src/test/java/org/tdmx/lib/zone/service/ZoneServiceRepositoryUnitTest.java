package org.tdmx.lib.zone.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

//@TransactionConfiguration(transactionManager="tdmx.lib.zone.TransactionManager")
//@Transactional("ZoneDB")
public class ZoneServiceRepositoryUnitTest {

	@Autowired
	private ZoneService service;
	
//	@Autowired
//	private AuthorizedAgentDao dao;
	
	private String zoneApex;
	
	@Before
	public void doSetup() throws Exception {
		zoneApex = "zone.root.test";
		
		Zone az = ZoneFacade.createZone(zoneApex);
		
		service.createOrUpdate(az);
	}
	
	@After
	public void doTeardown() {
		Zone az = service.findByZoneApex(zoneApex);
		if ( az != null ) {
			service.delete(az);
		}
	}
	
	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		Zone az = service.findByZoneApex(zoneApex);
		assertNotNull(az);
		assertEquals(zoneApex,az.getZoneApex());
	}
	
	@Test
	public void testLookup_NotFound() throws Exception {
		Zone az = service.findByZoneApex("gugus");
		assertNull(az);
	}
	
	@Test
	public void testModify() throws Exception {
		Zone az = service.findByZoneApex(zoneApex);
		service.createOrUpdate(az);

		Zone az2 = service.findByZoneApex(zoneApex);

		assertEquals(az.getZoneApex(),az2.getZoneApex());
	}

}