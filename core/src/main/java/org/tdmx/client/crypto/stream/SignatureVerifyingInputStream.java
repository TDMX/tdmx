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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.converters.NumberToOctetString;
import org.tdmx.client.crypto.scheme.CryptoException;

/**
 * SignatureVerifyingInputStream reads from an underlying InputStream and checks a digital signature read from the input
 * stream after a known length of bytes ( {@link #expectedSize} ).
 * 
 * If {@link #lengthInSignature} then the expectedLength is computed as part of the signature before validating the
 * digital signature.
 * 
 * If the signature is valid, the signature value is set and retrievable by {@link #getSignatureValue()}.
 * 
 * @author Peter
 * 
 */
public class SignatureVerifyingInputStream extends InputStream {

	private final PublicKey publicKey;
	private final Signature signature;
	private final boolean lengthInSignature;
	private boolean signatureAppended = false;
	private final long expectedSize;
	private final InputStream input;

	private long size = 0;

	private boolean signatureValid;
	private byte[] signatureValue;

	private final byte[] buffer = new byte[1024];

	public SignatureVerifyingInputStream(SignatureAlgorithm signatureAlgorithm, PublicKey publicKey, long expectedSize,
			boolean lengthInSignature, InputStream input) throws CryptoException {
		this.signature = signatureAlgorithm.getVerifier(publicKey);
		this.publicKey = publicKey;
		this.input = input;
		this.expectedSize = expectedSize;
		this.lengthInSignature = lengthInSignature;
		this.signatureAppended = true;
		this.signatureValue = null; // when appended we read the signature from the underlying input stream.
	}

	public SignatureVerifyingInputStream(SignatureAlgorithm signatureAlgorithm, PublicKey publicKey, long expectedSize,
			boolean lengthInSignature, byte[] signatureValue, InputStream input) throws CryptoException {
		this.signature = signatureAlgorithm.getVerifier(publicKey);
		this.publicKey = publicKey;
		this.input = input;
		this.expectedSize = expectedSize;
		this.lengthInSignature = lengthInSignature;
		this.signatureAppended = false;
		this.signatureValue = signatureValue;
	}

	@Override
	public int read() throws IOException {
		int result = input.read();
		if (result != -1 && ++size == expectedSize) {
			// we should compute
			try {
				signature.update((byte) result);
				checkSignature(new byte[0]);
			} catch (SignatureException e) {
				throw new IOException("Unable to update Signature.", e);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int result = -1;
		int readBytes = input.read(buffer, 0, Math.min(buffer.length, len));
		if (readBytes != -1) {
			size += readBytes;
			long excess = size - expectedSize;

			if (excess >= 0) {
				// we exceed the expectedSize, so compute the signature
				size -= excess;
				int effectiveReadBytes = readBytes - (int) excess;
				System.arraycopy(buffer, 0, b, off, effectiveReadBytes);
				try {
					signature.update(buffer, 0, readBytes);
				} catch (SignatureException e) {
					throw new IOException("Unable to update Signature.", e);
				}
				result = effectiveReadBytes;

				byte[] signaturePrefix = new byte[(int) excess];
				System.arraycopy(buffer, effectiveReadBytes, signaturePrefix, 0, (int) excess);
				checkSignature(signaturePrefix);
			} else {
				System.arraycopy(buffer, 0, b, off, readBytes);
				try {
					signature.update(buffer, 0, readBytes);
				} catch (SignatureException e) {
					throw new IOException("Unable to update Signature.", e);
				}
				result = readBytes;
			}
		} else {
			result = readBytes; // no output just return -1
		}
		return result;
	}

	/**
	 * Call this after reading exactly expectedSize bytes from the underlying input stream.
	 * 
	 * @param signatureValuePrefix
	 * @throws IOException
	 */
	private void checkSignature(byte[] signatureValuePrefix) throws IOException {
		try {
			if (lengthInSignature) {
				byte[] length = NumberToOctetString.longToBytes(expectedSize);
				signature.update(length);
			}
			if (signatureAppended) {
				int maxSignatureSize = SignatureAlgorithm.getMaxSignatureSizeBytes(signature, publicKey);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(maxSignatureSize);
				SizeConstrainedOutputStream os = new SizeConstrainedOutputStream(maxSignatureSize, baos);
				try {
					os.write(signatureValuePrefix); // signature bytes read previously from the underlying input stream
					int b = -1;
					while ((b = input.read()) != -1) { // all signature bytes left on underlying input stream.
						os.write(b);
					}
					signatureValue = baos.toByteArray();

				} finally {
					if (os != null) {
						os.close();
					}
				}
			} else {
				if (signatureValuePrefix.length != 0) {
					throw new IOException("Signature bytes appended.");
				}
				if (input.read() != -1) {
					throw new IOException("EOF not reached at contentLength.");
				}
			}
			signatureValid = signature.verify(signatureValue);

		} catch (SignatureException | CryptoException e) {
			throw new IOException("Unable to update Signature.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {
		return input.skip(n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return input.available();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		input.close();
	}

	/**
	 * @return the lengthInSignature
	 */
	public boolean isLengthInSignature() {
		return lengthInSignature;
	}

	/**
	 * @return the expectedSize
	 */
	public long getExpectedSize() {
		return expectedSize;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @return the signatureValid
	 */
	public boolean isSignatureValid() {
		return signatureValid;
	}

	/**
	 * @return the signatureValue
	 */
	public byte[] getSignatureValue() {
		return signatureValue;
	}

}
