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
package org.tdmx.client.crypto.pwdhash;

import junit.framework.TestCase;

/**
 * 
 */
public class BCryptPerformanceUnitTest extends TestCase {

	/**
	 * Entry point for unit tests
	 * 
	 * @param args
	 *            unused
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(BCryptPerformanceUnitTest.class);
	}

	/**
	 * 
	 */
	public void testCheckPerformance() {
		int MULT = 1;
		for (int i = 4; i < 15; i++) {
			String salt = BCrypt_v03.gensalt(i);
			String hashpw = BCrypt_v03.hashpw("my pwd", salt);
			long startTs = System.currentTimeMillis();
			for (int mult = 0; mult < MULT; mult++) {
				assertTrue(BCrypt_v03.checkpw("my pwd", hashpw));
			}
			long endTs = System.currentTimeMillis();
			// TODO System.out.println("" + i + ": " + ((endTs - startTs) / MULT));
		}
	}

}
