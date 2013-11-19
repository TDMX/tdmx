package org.tdmx.client.crypto.scheme;

import java.io.OutputStream;

/**
 * Encryption is performed by calling {@link #getOutputStream()} and writing all
 * plaintext to this stream. The output stream must be flushed, then closed before
 * calling {@link #getResult()}.
 * 
 * An Encrypter can be used once only.
 * 
 * The result's {@link CryptoContext} method must eventually be called after the caller
 * has finished using the encrypted data and context.
 * 
 * @author Peter
 *
 */
public interface Encrypter {

	public OutputStream getOutputStream() throws CryptoException;

	public CryptoContext getResult() throws CryptoException;
}
