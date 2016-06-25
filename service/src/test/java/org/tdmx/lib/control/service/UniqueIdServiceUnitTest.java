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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/tdmx/test-context.xml")
public class UniqueIdServiceUnitTest {

	@Autowired
	@Qualifier("tdmx.lib.control.AccountIdService")
	private UniqueIdService service;

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testSequentialLookup() throws Exception {
		String initialValue = service.getNextId();
		assertTrue(service.isValid(initialValue));

		for (int i = 0; i < 10000; i++) {
			String nextValue = service.getNextId();

			assertTrue(Long.parseLong(nextValue) > Long.parseLong(initialValue));
			assertTrue(service.isValid(nextValue));
			initialValue = nextValue;
		}
	}

	@Test
	public void testValidate() throws Exception {
		String badValue = "12345678";
		assertFalse(service.isValid(badValue));

	}

}