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
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.domain.AccountSearchCriteria;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager="tdmx.lib.control.TransactionManager")
// @Transactional("ControlDB")
public class AccountServiceRepositoryUnitTest {

	@Autowired
	private AccountService service;

	private Account a;

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Before
	public void doSetup() throws Exception {
		a = new Account();
		a.setAccountId(UUID.randomUUID().toString());
		a.setFirstName("peter");
		a.setLastName("Klauser");
		a.setEmail("pjklauser@gmail.com");

		service.createOrUpdate(a);
		// id is created on commit of service
		assertNotNull(a.getId());
	}

	@After
	public void doTeardown() {
		Account ac = service.findByAccountId(a.getAccountId());
		if (ac != null) {
			service.delete(ac);
		}
	}

	@Test
	public void testLookup_NotFoundAccountId() throws Exception {
		Account accounts = service.findByAccountId(UUID.randomUUID().toString());

		assertNull(accounts);
	}

	@Test
	public void testSearch_NotFoundAccountId() throws Exception {
		AccountSearchCriteria sc = new AccountSearchCriteria(new PageSpecifier(0, 10));
		sc.setAccountId(UUID.randomUUID().toString());
		List<Account> accounts = service.search(sc);

		assertNotNull(accounts);
		assertEquals(0, accounts.size());
	}

	@Test
	public void testSearch_FirstName() throws Exception {
		AccountSearchCriteria sc = new AccountSearchCriteria(new PageSpecifier(0, 10));
		sc.setFirstName(a.getFirstName());
		List<Account> accounts = service.search(sc);

		assertNotNull(accounts);
		assertEquals(1, accounts.size());
		assertEquals(a.getAccountId(), accounts.get(0).getAccountId());
	}

	@Test
	public void testSearch_LastName() throws Exception {
		AccountSearchCriteria sc = new AccountSearchCriteria(new PageSpecifier(0, 10));
		sc.setLastName(a.getLastName());
		List<Account> accounts = service.search(sc);

		assertNotNull(accounts);
		assertEquals(1, accounts.size());
		assertEquals(a.getAccountId(), accounts.get(0).getAccountId());
	}

	@Test
	public void testSearch_AllFields() throws Exception {
		AccountSearchCriteria sc = new AccountSearchCriteria(new PageSpecifier(0, 10));
		sc.setLastName(a.getLastName());
		sc.setFirstName(a.getFirstName());
		sc.setEmail(a.getEmail());
		sc.setAccountId(a.getAccountId());
		List<Account> accounts = service.search(sc);

		assertNotNull(accounts);
		assertEquals(1, accounts.size());
		assertEquals(a.getAccountId(), accounts.get(0).getAccountId());
	}

	@Test
	public void testSearch_Email() throws Exception {
		AccountSearchCriteria sc = new AccountSearchCriteria(new PageSpecifier(0, 10));
		sc.setEmail(a.getEmail());
		List<Account> accounts = service.search(sc);

		assertNotNull(accounts);
		assertEquals(1, accounts.size());
		assertEquals(a.getAccountId(), accounts.get(0).getAccountId());
	}

	@Test
	public void testSearch_None() throws Exception {
		AccountSearchCriteria sc = new AccountSearchCriteria(new PageSpecifier(0, 10));
		List<Account> accounts = service.search(sc);

		assertNotNull(accounts);
		assertTrue(accounts.size() > 0);
	}

}
