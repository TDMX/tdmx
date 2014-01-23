package org.tdmx.client.crypto.scheme;

import java.io.InputStream;

/**
 * The Decrypter provides an InputStream which provides plaintext stream to the caller of {@link #getInputStream(InputStream, byte[])}.
 * 
 * A Decrypter can be used once only.
 * 
 * The InputStream must be read fully until {@link InputStream#read()} gives -1. Only if the
 * stream is fully consumed, are all signatures and data integrity checks performed.
 * 
 * There are no further data integrity checks performed on {@link InputStream#close()}.
 * 
 * @author Peter
 *
 */
public interface Decrypter {

	/**
	 * The caller should always call {@link InputStream#close()} on the returned plaintext input stream.
	 * 
	 * @param encryptedData
	 * @param encryptionContext
	 * @return plaintext inputstream
	 * @throws CryptoException
	 */
	public InputStream getInputStream( InputStream encryptedData, byte[] encryptionContext ) throws CryptoException;
	
}
