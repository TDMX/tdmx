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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.control.domain.Lock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager="tdmx.lib.control.TransactionManager")
// @Transactional("ControlDB")
public class LockServiceRepositoryUnitTest {

	@Autowired
	private LockService service;

	// @Autowired
	// private AuthorizedAgentDao dao;

	private String lockName;

	@Before
	public void doSetup() throws Exception {
		lockName = UUID.randomUUID().toString();

		Lock l = new Lock();
		l.setId(new Random().nextLong());
		l.setLockName(lockName);

		service.createOrUpdate(l);
	}

	@After
	public void doTeardown() {
		Lock l = service.findByName(lockName);
		if (l != null) {
			service.delete(l);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		Lock l = service.findByName(lockName);
		assertNotNull(l);
		assertNotNull(l.getId());
		assertNotNull(l.getLockName());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		Lock l = service.findByName("gugus");
		assertNull(l);
	}

	@Test
	public void testModify() throws Exception {
		Date d = new Date();
		Lock l = service.findByName(lockName);
		l.setLockedBy("me");
		l.setLockedUntilTime(d);
		service.createOrUpdate(l);

		Lock l2 = service.findByName(lockName);

		assertEquals(d, l2.getLockedUntilTime());
		assertEquals(l.getId(), l2.getId());
		assertEquals(l.getLockName(), l2.getLockName());
		assertEquals(l.getLockedBy(), l2.getLockedBy());
	}

	@Test
	public void testAquireLock() throws Exception {
		String holderIdentitifier = UUID.randomUUID().toString();

		assertTrue(service.acquireLock(lockName, holderIdentitifier));

		String holderIdentitifier2 = UUID.randomUUID().toString();

		assertFalse(service.acquireLock(lockName, holderIdentitifier2));

		service.releaseLock(lockName, holderIdentitifier, null);

		assertTrue(service.acquireLock(lockName, holderIdentitifier2));

		Calendar futureDate = Calendar.getInstance();
		futureDate.add(Calendar.DATE, 1);

		service.releaseLock(lockName, holderIdentitifier2, futureDate.getTime());

		// cannot get lock because of time locking
		assertFalse(service.acquireLock(lockName, holderIdentitifier));

		// release for some time in the past
		Calendar pastDate = Calendar.getInstance();
		pastDate.add(Calendar.DATE, -1);
		service.releaseLock(lockName, holderIdentitifier2, pastDate.getTime());

		assertTrue(service.acquireLock(lockName, holderIdentitifier));
	}

}