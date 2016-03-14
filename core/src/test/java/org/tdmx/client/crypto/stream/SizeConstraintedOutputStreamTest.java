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
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class SizeConstraintedOutputStreamTest {

	@Test
	public void testWriteBiggerThanCapacity() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		SizeConstrainedOutputStream scos = new SizeConstrainedOutputStream(1024, baos);

		byte[] content = StreamTestUtils.createRndArray(2048);
		try {
			scos.write(content);
			fail();
		} catch (IOException e) {
			// ok
		}
	}

	@Test
	public void testWriteSmallerThanCapacity() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
		SizeConstrainedOutputStream scos = new SizeConstrainedOutputStream(2048, baos);

		byte[] content = StreamTestUtils.createRndArray(1024);
		scos.write(content);
		scos.close();

		assertArrayEquals(content, baos.toByteArray());

	}

	@Test
	public void testWriteExactlyCapacity() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
		SizeConstrainedOutputStream scos = new SizeConstrainedOutputStream(2048, baos);

		byte[] content = StreamTestUtils.createRndArray(2028);
		scos.write(content);
		scos.close();
		assertArrayEquals(content, baos.toByteArray());

	}

	@Test
	public void testWriteExactlyCapacityOffset() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
		SizeConstrainedOutputStream scos = new SizeConstrainedOutputStream(2048, baos);

		byte[] content = StreamTestUtils.createRndArray(2048);
		scos.write(content, 0, content.length / 2);
		scos.write(content, content.length / 2, content.length / 2);
		scos.close();
		assertArrayEquals(content, baos.toByteArray());

	}

	@Test
	public void testWritePartsCapacityEnough() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
		SizeConstrainedOutputStream scos = new SizeConstrainedOutputStream(4096, baos);

		byte[] content = StreamTestUtils.createRndArray(2048);
		for (int i = 0; i < content.length / 2; i++) {
			scos.write(content, i * 2, 2);
		}
		scos.close();
		assertArrayEquals(content, baos.toByteArray());
	}

	@Test
	public void testWritePartsCapacityExactMatch() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
		SizeConstrainedOutputStream scos = new SizeConstrainedOutputStream(2048, baos);

		byte[] content = StreamTestUtils.createRndArray(2048);
		for (int i = 0; i < content.length / 2; i++) {
			scos.write(content, i * 2, 2);
		}
		scos.close();
		assertArrayEquals(content, baos.toByteArray());
	}

	@Test
	public void testWritePartsCapacityExceeded() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
		SizeConstrainedOutputStream scos = new SizeConstrainedOutputStream(2047, baos); // -1

		byte[] content = StreamTestUtils.createRndArray(2048);
		for (int i = 0; i < content.length / 2 - 1; i++) {
			scos.write(content, i * 2, 2);
		}
		try {
			scos.write(content, content.length - 2, 2);
			fail();
		} catch (IOException e) {
			// ok
		}
	}

	@Test
	public void testWritePartsCapacityExceededBiggerChunks() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
		SizeConstrainedOutputStream scos = new SizeConstrainedOutputStream(2047, baos); // -1

		byte[] content = StreamTestUtils.createRndArray(2048);
		for (int i = 0; i < content.length / 8 - 1; i++) {
			scos.write(content, i * 8, 8);
		}
		try {
			scos.write(content, content.length - 8, 8);
			fail();
		} catch (IOException e) {
			// ok
		}
	}
}
