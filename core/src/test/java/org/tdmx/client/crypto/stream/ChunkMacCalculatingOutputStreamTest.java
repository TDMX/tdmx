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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.scheme.CryptoException;

public class ChunkMacCalculatingOutputStreamTest {

	private ByteArrayOutputStream delegate;
	private ChunkMacCalculatingOutputStream sut;

	private byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };

	@Before
	public void setUp() throws Exception {
		delegate = new ByteArrayOutputStream();

	}

	@Test
	public void testChunkSizeBeforeFlush() throws IOException, CryptoException {

		sut = new ChunkMacCalculatingOutputStream(delegate, 10, DigestAlgorithm.SHA_256);
		sut.write((int) 1);
		assertEquals(1, sut.getSize());
		assertEquals(0, sut.getMacs().size());
	}

	@Test
	public void testWriteBytes() throws IOException, CryptoException {
		sut = new ChunkMacCalculatingOutputStream(delegate, 5, DigestAlgorithm.SHA_256);
		for (int i = 0; i < data.length; i++) {
			sut.write(data[i]);
		}
		sut.flush();
		assertEquals(10, sut.getSize());

		byte[] result = delegate.toByteArray();
		assertArrayEquals(data, result);

		assertEquals(2, sut.getMacs().size());
		assertArrayEquals(DigestAlgorithm.SHA_256.kdf(new byte[] { 1, 2, 3, 4, 5 }), sut.getMacs().get(0));
		assertArrayEquals(DigestAlgorithm.SHA_256.kdf(new byte[] { 6, 7, 8, 9, 0 }), sut.getMacs().get(1));
	}

	@Test
	public void testWriteOneChunk() throws IOException, CryptoException {
		sut = new ChunkMacCalculatingOutputStream(delegate, 10, DigestAlgorithm.SHA_256);
		sut.write(data);
		sut.flush();
		assertEquals(10, sut.getSize());

		byte[] result = delegate.toByteArray();
		assertArrayEquals(data, result);

		assertEquals(1, sut.getMacs().size());
		assertArrayEquals(DigestAlgorithm.SHA_256.kdf(data), sut.getMacs().get(0));
	}

	@Test
	public void testWriteTwoChunks() throws IOException, CryptoException {
		sut = new ChunkMacCalculatingOutputStream(delegate, 5, DigestAlgorithm.SHA_256);
		sut.write(data);
		sut.flush();
		assertEquals(10, sut.getSize());

		byte[] result = delegate.toByteArray();
		assertArrayEquals(data, result);

		assertEquals(2, sut.getMacs().size());
		assertArrayEquals(DigestAlgorithm.SHA_256.kdf(new byte[] { 1, 2, 3, 4, 5 }), sut.getMacs().get(0));
		assertArrayEquals(DigestAlgorithm.SHA_256.kdf(new byte[] { 6, 7, 8, 9, 0 }), sut.getMacs().get(1));
	}
}
