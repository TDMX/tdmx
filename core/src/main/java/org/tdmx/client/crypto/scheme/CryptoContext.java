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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.core.api.v01.msg.Chunk;
import org.tdmx.core.system.lang.StreamUtils;

public class CryptoContext {

	private final InputStream encryptedData;
	private final byte[] encryptionContext;
	private final long plaintextLength;
	private final long ciphertextLength;
	private final int chunkSize;
	private final List<byte[]> macList;
	private final byte[] macOfMacs;

	public CryptoContext(InputStream encryptedData, byte[] encryptionContext, long plaintextLength,
			long ciphertextLength, int chunkSize, List<byte[]> macs, byte[] macOfMacs) {
		this.encryptedData = encryptedData;
		this.encryptionContext = encryptionContext;
		this.plaintextLength = plaintextLength;
		this.ciphertextLength = ciphertextLength;
		this.chunkSize = chunkSize;
		this.macOfMacs = macOfMacs;
		this.macList = macs;
	}

	public class ChunkSequentialReader implements AutoCloseable {
		private int chunkNo = 0;

		public ChunkSequentialReader() {
		}

		public Chunk getNextChunk(String msgId) throws IOException {
			Chunk c = new Chunk();
			c.setMsgId(msgId);
			if (chunkNo >= macList.size()) {
				return null;
			} else if (chunkNo == macList.size() - 1) {
				// last chunk is partial
				int sizeLeft = (int) ciphertextLength % chunkSize;
				byte[] buf = new byte[sizeLeft];
				StreamUtils.fill(encryptedData, buf, 0, sizeLeft);
				c.setData(buf);
				c.setPos(chunkNo);
				c.setMac(ByteArray.asHex(macList.get(chunkNo)));

			} else {
				byte[] buf = new byte[chunkSize];
				StreamUtils.fill(encryptedData, buf, 0, chunkSize);
				c.setData(buf);
				c.setPos(chunkNo);
				c.setMac(ByteArray.asHex(macList.get(chunkNo)));
			}
			chunkNo++;
			return c;
		}

		@Override
		public void close() throws IOException {
			encryptedData.close();
		}
	}

	/**
	 * Once the ChunkSequentialReader has been iterated through, remember to use call {@link #close()}
	 * 
	 * @return get a iterator for the chunks.
	 */
	public ChunkSequentialReader getChunkReader() {
		return new ChunkSequentialReader();
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

	public byte[] getMacOfMacs() {
		return macOfMacs;
	}

}
