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
package org.tdmx.client.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientCliUtilsTest {

	private static final Logger log = LoggerFactory.getLogger(ClientCliUtilsTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_getAddressLocalName() {

		assertEquals("abc", ClientCliUtils.getAddressLocalName("abc@dom"));
		assertEquals("abc", ClientCliUtils.getAddressLocalName("abc@dom#svc"));
	}

	@Test
	public void test_getAddressDomainName() {

		assertNull(ClientCliUtils.getAddressDomainName("localnameonly"));
		assertEquals("dom", ClientCliUtils.getAddressDomainName("abc@dom"));
		assertEquals("dom", ClientCliUtils.getAddressDomainName("abc@dom#svc"));

	}

	@Test
	public void test_getAddressServiceName() {

		assertNull(ClientCliUtils.getAddressServiceName("localnameonly"));
		assertNull(ClientCliUtils.getAddressServiceName("abc@dom"));
		assertEquals("svc", ClientCliUtils.getAddressServiceName("abc@dom#svc"));

	}
}
