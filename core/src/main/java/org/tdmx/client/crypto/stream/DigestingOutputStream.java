/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.client.crypto.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.scheme.CryptoException;

/**
 * DigestingOutputStream can create a MessageDigest value of the bytes written to it's wrapped OutputStream.
 * 
 * The size counts the number of bytes written to the delegate output stream. The digest value is available after
 * {@link #close()} is called.
 * 
 * @author Peter
 * 
 */
public class DigestingOutputStream extends OutputStream {

	private final OutputStream output;
	private final MessageDigest digest;

	private byte[] digestValue = null;

	public DigestingOutputStream(DigestAlgorithm digest, OutputStream output) throws CryptoException {
		this.digest = digest.getMessageDigest();
		this.output = output;
	}

	@Override
	public void write(int b) throws IOException {
		output.write(b);
		digest.update((byte) b);
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
