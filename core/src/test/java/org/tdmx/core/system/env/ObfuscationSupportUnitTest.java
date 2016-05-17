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
package org.tdmx.core.system.env;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObfuscationSupportUnitTest {
	private final Logger log = LoggerFactory.getLogger(ObfuscationSupportUnitTest.class);

	protected String clearText = "This is a simple string which will be encrypted into some other string - silly";
	protected String obfuscatedText = "OBF:f3/AHJLmZ3SRoSs/T9L1RZMSW7pmW+Qw6dTz2R9q4+u/9CJHl3Mr9SKELbH4weM6gLnMd9Sxr9e521GO1saekVDOxR8zfes7mb6xesrkIy8=";

	@Test
	public void testObfuscate() {
		assertEquals(obfuscatedText, ObfuscationSupport.obfuscate(clearText));
	}

	@Test
	public void testDefuscate() {
		assertEquals(clearText, ObfuscationSupport.deobfuscate(obfuscatedText));
	}
}
