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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.lib.console.domain.AccountZoneFacade;
import org.tdmx.lib.console.domain.CredentialFacade;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager="tdmx.lib.control.TransactionManager")
// @Transactional("ControlDB")
public class AccountZoneServiceRepositoryUnitTest {

	@Autowired
	private AccountZoneService service;

	// @Autowired
	// private AuthorizedAgentDao dao;

	private String zoneApex;

	@Before
	public void doSetup() throws Exception {
		zoneApex = "zone.root.test";
		PKIXCredential za = CredentialFacade.createZAC(zoneApex);

		AccountZone az = AccountZoneFacade.createAccountZone(za.getPublicCert());

		service.createOrUpdate(az);
	}

	@After
	public void doTeardown() {
		AccountZone az = service.findByZoneApex(zoneApex);
		if (az != null) {
			service.delete(az);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		AccountZone az = service.findByZoneApex(zoneApex);
		assertNotNull(az);
		assertNotNull(az.getAccountId());
		assertNotNull(az.getStatus());
		assertNotNull(az.getSegment());
		assertEquals(zoneApex, az.getZoneApex());
		assertNotNull(az.getZonePartitionId());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		AccountZone az = service.findByZoneApex("gugus");
		assertNull(az);
	}

	@Test
	public void testModify() throws Exception {
		AccountZone az = service.findByZoneApex(zoneApex);
		az.setStatus(AccountZoneStatus.BLOCKED);
		service.createOrUpdate(az);

		AccountZone az2 = service.findByZoneApex(zoneApex);
		assertEquals(AccountZoneStatus.BLOCKED, az2.getStatus());

		assertEquals(az.getAccountId(), az2.getAccountId());
		assertEquals(az.getSegment(), az2.getSegment());
		assertEquals(az.getZoneApex(), az2.getZoneApex());
		assertEquals(az.getZonePartitionId(), az2.getZonePartitionId());
	}

}