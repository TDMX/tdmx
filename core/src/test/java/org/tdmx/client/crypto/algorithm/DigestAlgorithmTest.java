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
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;

public class DigestAlgorithmTest {

	static {
		JCAProviderInitializer.init();
	}

	@Test
	public void testMD5() throws CryptoException {
		byte[] hash = DigestAlgorithm.MD5.kdf(EntropySource.getRandomBytes(1000));
		assertNotNull(hash);
		assertEquals(16, hash.length);
	}

	@Test
	public void testMD5Repeated() throws CryptoException {
		byte[] bytes = EntropySource.getRandomBytes(1000);
		byte[] expectedHash = DigestAlgorithm.MD5.kdf(bytes);
		for (int i = 0; i < 10000; i++) {
			byte[] hash = DigestAlgorithm.MD5.kdf(bytes);
			assertNotNull(hash);
			assertEquals(16, hash.length);
			assertArrayEquals(expectedHash, hash);
		}
	}

	@Test
	public void testSHA1() throws CryptoException {
		byte[] hash = DigestAlgorithm.SHA_1.kdf(EntropySource.getRandomBytes(1000));
		assertNotNull(hash);
		assertEquals(20, hash.length);
	}

	@Test
	public void testSHA1Repeated() throws CryptoException {
		byte[] bytes = EntropySource.getRandomBytes(1000);
		byte[] expectedHash = DigestAlgorithm.SHA_1.kdf(bytes);
		for (int i = 0; i < 10000; i++) {
			byte[] hash = DigestAlgorithm.SHA_1.kdf(bytes);
			assertNotNull(hash);
			assertEquals(20, hash.length);
			assertArrayEquals(expectedHash, hash);
		}
	}

	@Test
	public void testSHA256() throws CryptoException {
		byte[] hash = DigestAlgorithm.SHA_256.kdf(EntropySource.getRandomBytes(1000));
		assertNotNull(hash);
		assertEquals(32, hash.length);
	}

	@Test
	public void testSHA384() throws CryptoException {
		byte[] hash = DigestAlgorithm.SHA_384.kdf(EntropySource.getRandomBytes(1000));
		assertNotNull(hash);
		assertEquals(48, hash.length);
	}

	@Test
	public void testSHA512() throws CryptoException {
		byte[] hash = DigestAlgorithm.SHA_512.kdf(EntropySource.getRandomBytes(1000));
		assertNotNull(hash);
		assertEquals(64, hash.length);
	}
}
