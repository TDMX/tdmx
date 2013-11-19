package org.tdmx.client.crypto.algorithm;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public class KeyDiversificationFunction {

	static {
		JCAProviderInitializer.init();	
	}
	
	public static byte[] PBKDF2WithHmacSHA1(byte[] password, byte[] salt, int iterations, int derivedKeyLengthBits)
			throws CryptoException {
		char[] chars = new char[password.length];
		for( int i = 0; i < chars.length; i++) {
			chars[i] = (char)password[i];
		}
		
		// PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
		// specifically names SHA-1 as an acceptable hashing algorithm for
		// PBKDF2
		String algorithm = "PBKDF2WithHmacSHA1";
		// SHA-1 generates 160 bit hashes, so that's what makes sense here
		// Pick an iteration count that works for you. The NIST recommends at
		// least 1,000 iterations:
		// http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
		// iOS 4.x reportedly uses 10,000:
		// http://blog.crackpassword.com/2010/09/smartphone-forensics-cracking-blackberry-backup-passwords/

		KeySpec spec = new PBEKeySpec(chars, salt, iterations,
				derivedKeyLengthBits);

		try {
			SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
			return f.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(
					CryptoResultCode.ERROR_PBE_ALGORITHM_MISSING, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PBE_KEY_INVALID, e);
		}

	}

}
