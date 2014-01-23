package org.tdmx.client.crypto.entropy;

import java.security.SecureRandom;

public class EntropySource {

	private static final SecureRandom sr = new SecureRandom();
	
	public static byte[] getRandomBytes( int length ) {
		byte[] result = new byte[length];
		sr.nextBytes(result);
		return result;
	}
	
	public static SecureRandom getSecureRandom() {
		return sr;
	}
}
