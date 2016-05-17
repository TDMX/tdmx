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

import junit.framework.TestCase;

public class EnvironmentSupportUnitTest extends TestCase {

	public void testExpandVars() {
		for (int i = 0; i < testStrings.length; i++) {
			String testValue = testStrings[i][0];
			String expectedValue = testStrings[i][1];
			String testResult = EnvironmentSupport.expandVars(testValue);

			assertEquals(expectedValue, testResult);
		}
	}

	private static final String jb = System.getenv("JAVA_HOME");

	private static final String[][] testStrings = {
			new String[] { "hello $${JAVA_HOME} world ${JAVA_HOME} hello again",
					"hello ${JAVA_HOME} world " + jb + " hello again" },
			new String[] { "$${JAVA_HOME}world", "${JAVA_HOME}world" },
			new String[] { "${JAVA_HOME}world", "" + jb + "world" },
			new String[] { "hello${JAVA_HOME}", "hello" + jb + "" },
			new String[] { "hello$${JAVA_HOME}", "hello${JAVA_HOME}" },
			new String[] { "${JAVA_HOME}${JAVA_HOME}", "" + jb + "" + jb + "" },
			new String[] { "hello ${JAVA_HOME world", "hello ${JAVA_HOME world" },
			new String[] { "hello ${", "hello ${" },
			new String[] { "${ hello", "${ hello" },
			new String[] { "hello $${", "hello ${" },
			new String[] { "hello $$${JAVA_HOME} world ${JAVA_HOME} hello again",
					"hello $${JAVA_HOME} world " + jb + " hello again" },
			new String[] { "hello ${DOESNOTEXIST} world", "hello ${DOESNOTEXIST} world" },
			new String[] { "hello $${${JAVA_HOME}} world", "hello ${" + jb + "} world" },
			new String[] { "hello ${user.name} world",
					"hello " + System.getProperty("user.name").toLowerCase() + " world" },

			// tests for new escaped values
			new String[] { "$\\{JAVA_HOME\\}", "${JAVA_HOME}" }, new String[] { "$\\{\\}", "${}" },
			new String[] { "$\\{HELLO\\}$\\{WORLD\\}", "${HELLO}${WORLD}" },
			new String[] { "$\\{\\}$\\{\\}", "${}${}" },
			new String[] { "$\\{HELLO\\}\\$\\{WORLD\\}", "${HELLO}\\${WORLD}" }, };

}
