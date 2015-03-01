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

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.control.domain.MaxValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MaxValueServiceRepositoryUnitTest {

	@Autowired
	private MaxValueService service;

	// @Autowired
	// private AuthorizedAgentDao dao;

	private String key;
	private Long initialValue;

	@Before
	public void doSetup() throws Exception {
		key = UUID.randomUUID().toString().substring(0, 8);
		initialValue = 1000l;

		MaxValue m = new MaxValue();
		m.setKey(key);
		m.setValue(initialValue);

		service.createOrUpdate(m);
	}

	@After
	public void doTeardown() {
		MaxValue m = service.findById(key);
		if (m != null) {
			service.delete(m);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		MaxValue l = service.findById(key);
		assertNotNull(l);
		assertNotNull(l.getKey());
		assertEquals(key, l.getKey());
		assertEquals(initialValue, l.getValue());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		MaxValue m = service.findById("gugus");
		assertNull(m);
	}

	@Test
	public void testModify() throws Exception {
		MaxValue l = service.findById(key);
		l.setValue(2000l);
		service.createOrUpdate(l);

		MaxValue l2 = service.findById(key);

		assertEquals(l.getKey(), l2.getKey());
		assertEquals(l.getValue(), l2.getValue());
	}

	@Test
	public void testIncrement() throws Exception {
		int increment = 1000;
		Long initialValue = service.findById(key).getValue();
		Long expectedValue = initialValue + increment;

		service.increment(key, 1000);

		Long incrementedValue = service.findById(key).getValue();

		assertEquals(expectedValue, incrementedValue);
	}

	@Test
	public void testRepeatedIncrement() throws Exception {
		int increment = 100;
		int loops = 1000;
		Long initialValue = service.findById(key).getValue();
		Long expectedValue = initialValue + (increment * loops);

		for (int i = 0; i < loops; i++) {
			service.increment(key, increment);
		}
		Long incrementedValue = service.findById(key).getValue();

		assertEquals(expectedValue, incrementedValue);
	}
}