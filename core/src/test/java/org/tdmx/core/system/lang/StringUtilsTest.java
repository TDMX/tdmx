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
package org.tdmx.core.system.lang;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtilsTest {
	private final Logger log = LoggerFactory.getLogger(StringUtilsTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testIsUpperCase() {
		assertFalse(StringUtils.isLowerCase("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-_.;:"));
		assertTrue(StringUtils.isLowerCase("abcdefghijklmnopqrstuvwxyz0123456789.-_.;:"));
		assertFalse(StringUtils.isLowerCase("Abcdefghijklmnopqrstuvwxyz0123456789.-_.;:"));
	}

	@Test
	public void testIsSuffix() {
		assertTrue(StringUtils.isSuffix("abcdefg", "efg"));
		assertFalse(StringUtils.isSuffix("abcdefg", "abc"));
	}
}
