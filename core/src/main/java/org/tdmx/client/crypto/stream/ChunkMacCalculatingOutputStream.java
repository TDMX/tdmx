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
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper of a FileBackedOutputStream which calculates individual chunk checksums and records them.
 * 
 * The number of bytes written to the underlying stream is returned with {@link #getSize()}
 * 
 * The caller should take care to call {@link #close()}.
 * 
 * The caller should always call {@link #discard()}, especially if the {@link #getInputStream()} has not been called.
 * 
 * @author Peter
 * 
 */
public class ChunkMacCalculatingOutputStream extends OutputStream {

	private final OutputStream delegate;
	private long delegatedSize = 0;
	private int cachedSize = 0;
	private final byte[] chunk;
	private final List<byte[]> macs = new ArrayList<>();

	// TODO do the MAC calculation of each chunk

	public ChunkMacCalculatingOutputStream(OutputStream delegate, int chunkSize) {
		this.delegate = delegate;
		this.chunk = new byte[chunkSize];
	}

	@Override
	public void write(int b) throws IOException {
		chunk[cachedSize++] = (byte) b;
		if (chunk.length - cachedSize <= 0) {
			calculateMacAndFlush();
		}
	}

	@Override
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		int available = chunk.length - cachedSize;
		while (available < len) {
			write(b, off, available);
			off += available;
			len -= available;
			available = chunk.length - cachedSize;
		}
		if (available >= len) {
			System.arraycopy(b, off, chunk, cachedSize, len);
			cachedSize += len;
			if (available == len) {
				calculateMacAndFlush();
			}
		}
	}

	@Override
	public void flush() throws IOException {
		if (cachedSize > 0) {
			calculateMacAndFlush();
		}
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		flush();
		delegate.close();
	}

	private void calculateMacAndFlush() throws IOException {
		delegate.write(chunk, 0, cachedSize);
		delegatedSize += cachedSize;
		cachedSize = 0;

		// TODO
		macs.add(new byte[0]);
	}

	public int getChunkSize() {
		return this.chunk.length;
	}

	public List<byte[]> getMacs() {
		return this.macs;
	}

	public long getSize() {
		return delegatedSize + cachedSize;
	}

}
