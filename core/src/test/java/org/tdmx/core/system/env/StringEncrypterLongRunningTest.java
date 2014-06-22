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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.AsyncTestRunner;

public class StringEncrypterLongRunningTest extends TestCase {
	private final Logger log = LoggerFactory.getLogger(StringEncrypterLongRunningTest.class);

	protected String clearText = "This is a simple string which will be encrypted into some other string - silly";
	protected String cipherText = "xuqqNQPgUw/+XB26DLH1rukbS0BINoJkv4FyKq+aMtUxVfsFN9YP9aW61u6a092gt/QU0+fwrRUAjnnFPz4I5QjhH6WfUevRSj7w3pgE6rI=";

	protected StringEncrypter encrypter = new StringEncrypter("This is a Reused object!!");

	public void doTestDecrypt() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Ciphertext of " + clearText + " is " + encrypter.encrypt(clearText));
		}
		assertEquals(clearText, encrypter.decrypt(cipherText));
	}

	public void testMultiThreadedDecrypt() throws Exception {
		AsyncTestRunner.runDuration(this, "doTestDecrypt", 5, 60000);
	}

	public void testMultipleSingleThreaded() {
		for (int i = 0; i < 100000; i++) {
			String t1 = encrypter.encrypt(clearText);
			assertEquals(cipherText, t1);
			String t2 = encrypter.decrypt(t1);
			assertEquals(clearText, t2);
		}
	}

	public void testSingleThreadedGuarded() {
		for (int i = 0; i < 100000; i++) {
			synchronized (this) {
				String t1 = encrypter.encrypt(clearText);
				assertEquals(cipherText, t1);
			}
			synchronized (this) {
				String t2 = encrypter.decrypt(cipherText);
				assertEquals(clearText, t2);
			}
		}
	}

	public void testMultiThreadedGuarded() throws Exception {
		AsyncTestRunner.runDuration(this, "testSingleThreadedGuarded", 5, 60000);
	}

	public void testMultiThreaded() throws Exception {
		AsyncTestRunner.runDuration(this, "testMultipleSingleThreaded", 5, 60000);
	}
}
