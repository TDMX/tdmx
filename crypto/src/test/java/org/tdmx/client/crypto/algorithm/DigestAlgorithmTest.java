package org.tdmx.client.crypto.algorithm;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;

public class DigestAlgorithmTest {

	static {
		JCAProviderInitializer.init();	
	}
	
	@Test
	public void testSHA1() throws CryptoException {
		byte[] hash = DigestAlgorithm.SHA_1.kdf(EntropySource.getRandomBytes(1000));
		assertNotNull(hash);
		assertEquals(20, hash.length);
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
