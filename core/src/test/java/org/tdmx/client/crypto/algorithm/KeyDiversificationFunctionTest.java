package org.tdmx.client.crypto.algorithm;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

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
		byte[] output = KeyDiversificationFunction.PBKDF2WithHmacSHA1(StringToUtf8.toBytes("hello world!"), salt, 20000, 160 );
		assertEquals(20, output.length);
	}

	@Test
	public void test_384bitOutput() throws CryptoException {
		byte[] salt = EntropySource.getRandomBytes(16);
		byte[] output = KeyDiversificationFunction.PBKDF2WithHmacSHA1(StringToUtf8.toBytes("hello world!"), salt, 20000, 384 );
		assertEquals(48, output.length);
	}
}
