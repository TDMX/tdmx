package org.tdmx.client.crypto.algorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public enum DigestAlgorithm {

	SHA_1("SHA-1"), 
	SHA_256("SHA-256"), 
	SHA_384("SHA-384"),
	SHA_512("SHA-512");

	private String algorithm;
	
	private DigestAlgorithm( String algorithm ) {
		this.algorithm = algorithm;
	}
	
	public String getAlgorithm() {
		return this.algorithm;
	}
	
	public MessageDigest getMessageDigest() throws CryptoException {
		try {
			return MessageDigest.getInstance(getAlgorithm());
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_DIGEST_ALGORITHM_MISSING, e);
		}
	}
	
	public byte[] kdf( byte[] input ) throws CryptoException {
		MessageDigest d = getMessageDigest();
		return d.digest(input);
	}
}
