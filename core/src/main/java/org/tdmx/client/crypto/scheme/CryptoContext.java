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
package org.tdmx.client.crypto.scheme;

import java.io.InputStream;
import java.util.List;

public class CryptoContext {

	public final InputStream encryptedData;
	public final byte[] encryptionContext;
	public final long plaintextLength;
	public final long ciphertextLength;
	public final long chunkSize;
	public final List<byte[]> macList;

	public CryptoContext(InputStream encryptedData, byte[] encryptionContext, long plaintextLength,
			long ciphertextLength, long chunkSize, List<byte[]> macs) {
		this.encryptedData = encryptedData;
		this.encryptionContext = encryptionContext;
		this.plaintextLength = plaintextLength;
		this.ciphertextLength = ciphertextLength;
		this.chunkSize = chunkSize;
		this.macList = macs;
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

	public long getChunkSize() {
		return chunkSize;
	}

	public List<byte[]> getMacList() {
		return macList;
	}

}
