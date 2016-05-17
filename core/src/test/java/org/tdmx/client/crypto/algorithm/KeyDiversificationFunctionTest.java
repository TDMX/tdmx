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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.converters.StringToUtf8;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;

public class KeyDiversificationFunctionTest {

	static {
		JCAProviderInitializer.init();
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_160bitOutput() throws CryptoException {
		byte[] salt = EntropySource.getRandomBytes(16);
		byte[] output = KeyDiversificationFunction.PBKDF2WithHmacSHA1(StringToUtf8.toBytes("hello world!"), salt,
				20000, 160);
		assertEquals(20, output.length);
	}

	@Test
	public void test_384bitOutput() throws CryptoException {
		byte[] salt = EntropySource.getRandomBytes(16);
		byte[] output = KeyDiversificationFunction.PBKDF2WithHmacSHA1(StringToUtf8.toBytes("hello world!"), salt,
				20000, 384);
		assertEquals(48, output.length);
	}
}
