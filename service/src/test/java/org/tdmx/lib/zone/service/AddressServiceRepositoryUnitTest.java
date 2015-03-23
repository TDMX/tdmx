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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AddressServiceRepositoryUnitTest {

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private AddressService addressService;
	@Autowired
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;

	private Zone zone;
	private Domain domain;
	private Address address;

	@Before
	public void doSetup() throws Exception {

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockZonePartitionIdInstaller.ZP1_S1);
		input.setNumZACs(1);
		input.setNumDomains(1);
		input.setNumDACsPerDomain(0);
		input.setNumAddressesPerDomain(1);
		input.setNumUsersPerAddress(0);

		data = dataGenerator.setUp(input);

		zone = data.getZone();
		domain = data.getDomains().get(0).getDomain();
		address = data.getDomains().get(0).getAddresses().get(0).getAddress();

		zonePartitionIdProvider.setPartitionId(input.getZonePartitionId());
	}

	@After
	public void doTeardown() {
		zonePartitionIdProvider.clearPartitionId();

		dataGenerator.tearDown(input, data);
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(addressService);
	}

	@Test
	public void testLookup() throws Exception {
		Address a = addressService.findByName(domain, address.getLocalName());
		assertNotNull(a);
		assertEquals(domain.getDomainName(), a.getDomain().getDomainName());
		assertEquals(address.getLocalName(), a.getLocalName());
	}

	@Test
	public void testSearch_Zone() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		List<Address> addresss = addressService.search(zone, criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address a = addresss.get(0);
		assertNotNull(a);
		assertEquals(domain.getDomainName(), a.getDomain().getDomainName());
		assertEquals(address.getLocalName(), a.getLocalName());
	}

	@Test
	public void testSearch_ZoneAndAddress() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setLocalName(address.getLocalName());

		List<Address> addresss = addressService.search(zone, criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address a = addresss.get(0);
		assertNotNull(a);
		assertEquals(domain.getDomainName(), a.getDomain().getDomainName());
		assertEquals(address.getLocalName(), a.getLocalName());
	}

	@Test
	public void testSearch_ZoneAndDomainAndAddress() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(domain.getDomainName());
		criteria.setLocalName(address.getLocalName());

		List<Address> addresss = addressService.search(zone, criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address a = addresss.get(0);
		assertNotNull(a);
		assertEquals(domain.getDomainName(), a.getDomain().getDomainName());
		assertEquals(address.getLocalName(), a.getLocalName());
	}

	@Test
	public void testSearch_ZoneAndDomainOnly() throws Exception {
		AddressSearchCriteria criteria = new AddressSearchCriteria(new PageSpecifier(0, 10));
		criteria.setDomainName(domain.getDomainName());

		List<Address> addresss = addressService.search(zone, criteria);
		assertNotNull(addresss);
		assertEquals(1, addresss.size());
		Address a = addresss.get(0);
		assertNotNull(a);
		assertEquals(domain.getDomainName(), a.getDomain().getDomainName());
		assertEquals(address.getLocalName(), a.getLocalName());
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
		Address d = addressService.findByName(domain, "gugus");
		assertNull(d);
	}

	@Test
	public void testModify() throws Exception {
		Address d = addressService.findByName(domain, address.getLocalName());
		addressService.createOrUpdate(d);

		Address d2 = addressService.findByName(domain, address.getLocalName());

		assertEquals(d.getId(), d2.getId());
	}

}