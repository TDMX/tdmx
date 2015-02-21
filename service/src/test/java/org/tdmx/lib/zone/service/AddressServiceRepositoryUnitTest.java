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
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AddressID;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager="tdmx.lib.zone.TransactionManager")
// @Transactional("ZoneDB")
public class AddressServiceRepositoryUnitTest {

	@Autowired
	private ZoneService zoneService;

	@Autowired
	private AddressService addressService;

	private AddressID id = null;
	private Long tenantId;

	@Before
	public void doSetup() throws Exception {
		tenantId = new Random().nextLong();

		id = new AddressID();
		id.setZoneApex("ZONE.ROOT.TEST");
		id.setDomainName("SUBDOMAIN." + id.getZoneApex());
		id.setLocalName("addressName");

		Zone az = ZoneFacade.createZone(tenantId, id.getZoneApex());

		zoneService.createOrUpdate(az);

		Address d = ZoneFacade.createAddress(id);
		addressService.createOrUpdate(d);
	}

	@After
	public void doTeardown() {
		Address d = addressService.findById(id);
		if (d != null) {
			addressService.delete(d);
		}
		Zone az = zoneService.findByZoneApex(tenantId, id.getZoneApex());
		if (az != null) {
			zoneService.delete(az);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(zoneService);
		assertNotNull(addressService);
	}

	@Test
	public void testLookup() throws Exception {
		Address d = addressService.findById(id);
		assertNotNull(d);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
		assertEquals(id.getLocalName(), d.getId().getLocalName());
	}

	@Test
	public void testSearch_Zone() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		List<Address> addresss = addressService.search(id.getZoneApex(), criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address d = addresss.get(0);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
		assertEquals(id.getLocalName(), d.getId().getLocalName());
	}

	@Test
	public void testSearch_ZoneAndAddress() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setLocalName(id.getLocalName());

		List<Address> addresss = addressService.search(id.getZoneApex(), criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address d = addresss.get(0);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
		assertEquals(id.getLocalName(), d.getId().getLocalName());
	}

	@Test
	public void testSearch_ZoneAndDomainAndAddress() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(id.getDomainName());
		criteria.setLocalName(id.getLocalName());

		List<Address> addresss = addressService.search(id.getZoneApex(), criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address d = addresss.get(0);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
		assertEquals(id.getLocalName(), d.getId().getLocalName());
	}

	@Test
	public void testSearch_ZoneAndDomainOnly() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(id.getDomainName());

		List<Address> addresss = addressService.search(id.getZoneApex(), criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address d = addresss.get(0);
		assertEquals(id.getZoneApex(), d.getId().getZoneApex());
		assertEquals(id.getDomainName(), d.getId().getDomainName());
		assertEquals(id.getLocalName(), d.getId().getLocalName());
	}

	@Test
	public void testSearch_UnknownZoneAndAddress() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setLocalName(id.getLocalName());

		List<Address> addresss = addressService.search("gugusZone", criteria);
		assertNotNull(addresss);
		assertEquals(0, addresss.size());
	}

	@Test
	public void testSearch_UnknownZoneAndUnknownAddress() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setLocalName(id.getLocalName());

		List<Address> addresss = addressService.search("gugusZone", criteria);
		assertNotNull(addresss);
		assertEquals(0, addresss.size());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		AddressID gugusID = new AddressID("gugus", id.getDomainName(), id.getZoneApex());
		Address d = addressService.findById(gugusID);
		assertNull(d);
	}

	@Test
	public void testModify() throws Exception {
		Address d = addressService.findById(id);
		addressService.createOrUpdate(d);

		Address d2 = addressService.findById(id);

		assertEquals(d.getId(), d2.getId());
	}

}