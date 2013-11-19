/**
 * 
 */
package org.tdmx.client.crypto.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.scheme.CryptoException;

/**
 * DigestingOutputStream can create a MessageDigest value
 * of the bytes written to it's wrapped OutputStream.
 * 
 * The size counts the number of bytes written to the delegate output stream.
 * The digest value is available after {@link #close()} is called.
 * 
 * @author Peter
 *
 */
public class DigestingOutputStream extends OutputStream {

	private OutputStream output;
	private MessageDigest digest;
	
	private byte[] digestValue = null;
	
	public DigestingOutputStream( DigestAlgorithm digest, OutputStream output ) throws CryptoException {
		this.digest = digest.getMessageDigest();
		this.output = output;
	}

	@Override
	public void write(int b) throws IOException {
		output.write(b);
		digest.update((byte)b);
	}

	@Override
    public void write(byte b[]) throws IOException {
        output.write(b, 0, b.length);
        digest.update(b);
    }
	
	@Override
    public void write(byte b[], int off, int len) throws IOException {
		output.write(b, off, len);
		digest.update(b, off, len);
    }
	
	@Override
    public void flush() throws IOException {
		output.flush();
    }

	@Override
    public void close() throws IOException {
		output.close();
		digestValue = digest.digest();
    }

	/**
	 * @return the digestValue
	 */
	public byte[] getDigestValue() {
		return digestValue;
	}

}
