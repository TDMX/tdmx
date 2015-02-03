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

import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.control.domain.Account;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager="tdmx.lib.control.TransactionManager")
// @Transactional("ControlDB")
public class AccountServiceRepositoryUnitTest {

	@Autowired
	private AccountService service;

	// @Autowired
	// private AuthorizedAgentDao dao;

	private String accountId;

	@Before
	public void doSetup() throws Exception {
		accountId = UUID.randomUUID().toString();
		Account a = new Account();
		a.setAccountId(accountId);
		service.createOrUpdate(a);
	}

	@After
	public void doTeardown() {
		Account a = service.findById(accountId);
		if (a != null) {
			service.delete(a);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

}