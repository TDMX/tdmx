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

/**
 * JUnit unit tests for BCrypt routines
 * 
 * @author Damien Miller
 * @version 0.2
 */
public class StringEncrypterUnitTest extends TestCase {
	private final Logger log = LoggerFactory.getLogger(StringEncrypterUnitTest.class);

	protected String clearText = "This is a simple string which will be encrypted into some other string - silly";
	protected String cipherText = "PmDt6ocl/NfPlYL2XQtDq3MfnDHRuXP3IxuPzlPbzWHe23AVCcvK2xDGKp4S5gcl+bf5HQW7oddXfFCAlw4eMmi5HA35jTCQGtGpSKkwZkw=";
	protected StringEncrypter staticEncrypter = new StringEncrypter("This is a Reused object!!");
	protected StringEncrypter devEncrypter = new StringEncrypter("W1v3i6vo7F");

	/**
	 * Test method for 'BCrypt.hashpw(String, String)'
	 */
	public void testEncrypt() {

		log.debug("+----------------------------------------+");
		log.debug("|  -- Test Using Pass Phrase Method --   |");
		log.debug("+----------------------------------------+");

		String secretString = "Attack at dawn!";
		String passPhrase = "My Pass Phrase";

		// Create encrypter/decrypter class
		StringEncrypter desEncrypter = new StringEncrypter(passPhrase);

		// Encrypt the string
		String desEncrypted = desEncrypter.encrypt(secretString);

		// Decrypt the string
		String desDecrypted = desEncrypter.decrypt(desEncrypted);

		// Print out values
		log.debug("    Original String  : " + secretString);
		log.debug("    Encrypted String : " + desEncrypted);
		log.debug("    Decrypted String : " + desDecrypted);

		assertEquals(secretString, desDecrypted);
	}

	public void testDecrypt() throws Exception {
		assertEquals("secret", devEncrypter.decrypt("sKU8zbk9jHc=="));
		assertNull(devEncrypter.decrypt("sKU8zabk9jHc=")); // corrupted "secret"
	}

	/**
	 * The idea of this test is to verify the re-initialization of the cipher after an Exception has been thrown
	 */
	public void testReInitCipher() {
		StringEncrypter enc = new StringEncrypter("n8chtwAy");

		assertEquals("DFSzPUdWU5g=", enc.encrypt("secret"));

		// No error
		String plain = enc.decrypt("DFSzPUdWU5g=");
		assertNotNull(plain);
		// BadPaddingException -> Cipher re-initialization
		plain = enc.decrypt("FilBpgxX4Sc=");
		assertNull(plain);
		// No error
		plain = enc.decrypt("DFSzPUdWU5g=");
		assertNotNull(plain);
	}
}
