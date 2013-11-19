package org.tdmx.client.crypto.scheme;

import java.io.InputStream;


public class CryptoContext {

	public InputStream encryptedData;
	public byte[] encryptionContext;
	public long plaintextLength;
	public long ciphertextLength;
	
	public CryptoContext(InputStream encryptedData, byte[] encryptionContext, long plaintextLength, long ciphertextLength) {
		this.encryptedData = encryptedData;
		this.encryptionContext = encryptionContext;
		this.plaintextLength = plaintextLength;
		this.ciphertextLength = ciphertextLength;
	}

	/**
	 * @return the encryptedData
	 */
	public InputStream getEncryptedData() {
		return encryptedData;
	}

	/**
	 * @return the encryptionContext
	 */
	public byte[] getEncryptionContext() {
		return encryptionContext;
	}

	/**
	 * @return the plaintextLength
	 */
	public long getPlaintextLength() {
		return plaintextLength;
	}

	/**
	 * @return the ciphertextLength
	 */
	public long getCiphertextLength() {
		return ciphertextLength;
	}

}
