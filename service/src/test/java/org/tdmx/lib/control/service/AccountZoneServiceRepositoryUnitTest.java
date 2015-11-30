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
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneFacade;
import org.tdmx.lib.control.domain.AccountZoneSearchCriteria;
import org.tdmx.lib.control.domain.AccountZoneStatus;
import org.tdmx.lib.control.domain.CredentialFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AccountZoneServiceRepositoryUnitTest {

	@Autowired
	private AccountZoneService service;

	private AccountZone az;

	@Before
	public void doSetup() throws Exception {
		PKIXCredential za = CredentialFacade.createZAC("zone.root.test");
		az = AccountZoneFacade.createAccountZone("1234", za.getPublicCert().getTdmxZoneInfo().getZoneRoot(), "test",
				"partitionId");
		service.createOrUpdate(az);
		assertNotNull(az.getId());
	}

	@After
	public void doTeardown() {
		AccountZone a = service.findById(az.getId());
		if (a != null) {
			service.delete(a);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		AccountZone a = service.findByZoneApex(az.getZoneApex());
		assertNotNull(a);
		assertNotNull(a.getAccountId());
		assertNotNull(a.getStatus());
		assertNotNull(a.getSegment());
		assertEquals(az.getZoneApex(), a.getZoneApex());
		assertNotNull(a.getZonePartitionId());
		assertEquals(az.getZonePartitionId(), a.getZonePartitionId());
	}

	@Test
	public void testLookup_NotFound_Zone() throws Exception {
		AccountZone a = service.findByZoneApex("gugus");
		assertNull(a);
	}

	@Test
	public void testModify() throws Exception {
		AccountZone a = service.findByZoneApex(az.getZoneApex());
		a.setStatus(AccountZoneStatus.BLOCKED);
		service.createOrUpdate(a);

		AccountZone a2 = service.findByZoneApex(az.getZoneApex());
		assertEquals(AccountZoneStatus.BLOCKED, a2.getStatus());

		assertEquals(a.getAccountId(), a2.getAccountId());
		assertEquals(a.getSegment(), a2.getSegment());
		assertEquals(a.getZoneApex(), a2.getZoneApex());
		assertEquals(a.getZonePartitionId(), a2.getZonePartitionId());
	}

	@Test
	public void testSearch_None() throws Exception {
		AccountZoneSearchCriteria sc = new AccountZoneSearchCriteria(new PageSpecifier(0, 100));
		List<AccountZone> accounts = service.search(sc);

		assertNotNull(accounts);
		assertTrue(accounts.size() > 0);
	}

	@Test
	public void testSearch_AccountId() throws Exception {
		AccountZoneSearchCriteria sc = new AccountZoneSearchCriteria(new PageSpecifier(0, 100));
		sc.setAccountId(az.getAccountId());
		List<AccountZone> accounts = service.search(sc);

		assertNotNull(accounts);
		assertEquals(1, accounts.size());
	}

	@Test
	public void testSearch_AccountIdStatus() throws Exception {
		AccountZoneSearchCriteria sc = new AccountZoneSearchCriteria(new PageSpecifier(0, 100));
		sc.setAccountId(az.getAccountId());
		sc.setStatus(az.getStatus());
		List<AccountZone> accounts = service.search(sc);

		assertNotNull(accounts);
		assertEquals(1, accounts.size());
	}

	@Test
	public void testSearch_AccountIdStatusApex() throws Exception {
		AccountZoneSearchCriteria sc = new AccountZoneSearchCriteria(new PageSpecifier(0, 100));
		sc.setAccountId(az.getAccountId());
		sc.setStatus(az.getStatus());
		sc.setZoneApex(az.getZoneApex());
		List<AccountZone> accounts = service.search(sc);

		assertNotNull(accounts);
		assertEquals(1, accounts.size());
	}

	@Test
	public void testSearch_AccountIdStatusApexSegment() throws Exception {
		AccountZoneSearchCriteria sc = new AccountZoneSearchCriteria(new PageSpecifier(0, 100));
		sc.setAccountId(az.getAccountId());
		sc.setStatus(az.getStatus());
		sc.setZoneApex(az.getZoneApex());
		sc.setSegment(az.getSegment());
		List<AccountZone> accounts = service.search(sc);

		assertNotNull(accounts);
		assertEquals(1, accounts.size());
	}

	@Test
	public void testSearch_AccountIdStatusApexSegmentPartitionId() throws Exception {
		AccountZoneSearchCriteria sc = new AccountZoneSearchCriteria(new PageSpecifier(0, 100));
		sc.setAccountId(az.getAccountId());
		sc.setStatus(az.getStatus());
		sc.setZoneApex(az.getZoneApex());
		sc.setSegment(az.getSegment());
		sc.setZonePartitionId(az.getZonePartitionId());
		List<AccountZone> accounts = service.search(sc);

		assertNotNull(accounts);
		assertEquals(1, accounts.size());
	}
}