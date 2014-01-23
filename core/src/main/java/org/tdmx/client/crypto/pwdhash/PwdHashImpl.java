package org.tdmx.client.crypto.pwdhash;

import org.tdmx.client.crypto.entropy.EntropySource;

/**
 * A more secure alternative to MD5 hashing, based on jBCrypt.
 */
public class PwdHashImpl implements PwdHash {

	public static String hashpw(String password) {
		return BCrypt_v03.hashpw(password,  BCrypt_v03.gensalt(12, EntropySource.getSecureRandom()));
	}
	
	public static boolean checkpw(String plaintext, String hashed) {
		return BCrypt_v03.checkpw(plaintext, hashed);
	}

	@Override
	public String hash(String password){
		return hashpw(password);
	}
	
	@Override
	public boolean check(String plaintext, String hashed) {
		return checkpw(plaintext, hashed);
	}
	
}
