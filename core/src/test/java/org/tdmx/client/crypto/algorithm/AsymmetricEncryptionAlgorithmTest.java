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
package org.tdmx.client.crypto.algorithm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public class AsymmetricEncryptionAlgorithmTest {

	static {
		JCAProviderInitializer.init();
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testKeyInfos() throws CryptoException {
		KeyPair kp = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
		assertEquals("RSA", kp.getPrivate().getAlgorithm());
		assertEquals("PKCS#8", kp.getPrivate().getFormat());

		assertEquals("RSA", kp.getPublic().getAlgorithm());
		assertEquals("X.509", kp.getPublic().getFormat());
	}

	@Test
	public void testGetAlgorithmFromKey() throws CryptoException, NoSuchAlgorithmException {
		KeyPair kp2048 = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();

		assertEquals(AsymmetricEncryptionAlgorithm.RSA2048,
				AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey(kp2048.getPublic()));

	}

	@Test
	public void testEncryptDecrypt() throws CryptoException, NoSuchAlgorithmException {
		KeyPair kp2048 = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();

		byte[] content = EntropySource.getRandomBytes(214);
		byte[] ciphertext = AsymmetricEncryptionAlgorithm.RSA2048.encrypt(kp2048.getPublic(), content);
		assertEquals(AsymmetricEncryptionAlgorithm.RSA2048.getKeyLengthInBytes(), ciphertext.length);
		byte[] plaintext = AsymmetricEncryptionAlgorithm.RSA2048.decrypt(kp2048.getPrivate(), ciphertext);
		assertArrayEquals(content, plaintext);

	}

	@Test
	public void testEncryptTooLong() throws CryptoException, NoSuchAlgorithmException {
		KeyPair kp2048 = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();

		byte[] content = EntropySource.getRandomBytes(246);
		try {
			AsymmetricEncryptionAlgorithm.RSA2048.encrypt(kp2048.getPublic(), content);
			fail();
		} catch (CryptoException e) {
			assertEquals(CryptoResultCode.ERROR_PK_BLOCKSIZE_INVALID, e.getRc());
		}

	}
}
