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
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.zone.domain.Address;
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

	private String domainName;
	private String localName;
	private ZoneReference zone;

	@Before
	public void doSetup() throws Exception {

		zone = new ZoneReference(new Random().nextLong(), "ZONE.ROOT.TEST");
		domainName = "SUBDOMAIN." + zone.getZoneApex();
		localName = "addressName";

		Zone az = ZoneFacade.createZone(zone);
		zoneService.createOrUpdate(az);

		Address d = ZoneFacade.createAddress(zone, domainName, localName);
		addressService.createOrUpdate(d);
	}

	@After
	public void doTeardown() {
		Address d = addressService.findByName(zone, domainName, localName);
		if (d != null) {
			addressService.delete(d);
		}
		Zone az = zoneService.findByZoneApex(zone);
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
		Address a = addressService.findByName(zone, domainName, localName);
		assertNotNull(a);
		assertEquals(zone, a.getZoneReference());
		assertEquals(domainName, a.getDomainName());
		assertEquals(localName, a.getLocalName());
	}

	@Test
	public void testSearch_Zone() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		List<Address> addresss = addressService.search(zone, criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address a = addresss.get(0);
		assertNotNull(a);
		assertEquals(zone, a.getZoneReference());
		assertEquals(domainName, a.getDomainName());
		assertEquals(localName, a.getLocalName());
	}

	@Test
	public void testSearch_ZoneAndAddress() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setLocalName(localName);

		List<Address> addresss = addressService.search(zone, criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address a = addresss.get(0);
		assertNotNull(a);
		assertEquals(zone, a.getZoneReference());
		assertEquals(domainName, a.getDomainName());
		assertEquals(localName, a.getLocalName());
	}

	@Test
	public void testSearch_ZoneAndDomainAndAddress() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(domainName);
		criteria.setLocalName(localName);

		List<Address> addresss = addressService.search(zone, criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address a = addresss.get(0);
		assertNotNull(a);
		assertEquals(zone, a.getZoneReference());
		assertEquals(domainName, a.getDomainName());
		assertEquals(localName, a.getLocalName());
	}

	@Test
	public void testSearch_ZoneAndDomainOnly() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(domainName);

		List<Address> addresss = addressService.search(zone, criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address a = addresss.get(0);
		assertNotNull(a);
		assertEquals(zone, a.getZoneReference());
		assertEquals(domainName, a.getDomainName());
		assertEquals(localName, a.getLocalName());
	}

	@Test
	public void testSearch_UnknownZoneAndAddress() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setLocalName(localName);

		ZoneReference gugus = new ZoneReference(zone.getTenantId(), "gugus");
		List<Address> addresss = addressService.search(gugus, criteria);
		assertNotNull(addresss);
		assertEquals(0, addresss.size());

		gugus = new ZoneReference(new Random().nextLong(), zone.getZoneApex());
		addresss = addressService.search(gugus, criteria);
		assertNotNull(addresss);
		assertEquals(0, addresss.size());
	}

	@Test
	public void testSearch_UnknownAddress() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setLocalName("gugus");

		List<Address> addresss = addressService.search(zone, criteria);
		assertNotNull(addresss);
		assertEquals(0, addresss.size());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		Address d = addressService.findByName(zone, domainName, "gugus");
		assertNull(d);
	}

	@Test
	public void testModify() throws Exception {
		Address d = addressService.findByName(zone, domainName, localName);
		addressService.createOrUpdate(d);

		Address d2 = addressService.findByName(zone, domainName, localName);

		assertEquals(d.getId(), d2.getId());
	}

}