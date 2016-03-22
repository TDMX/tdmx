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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.buffer.TemporaryFileManagerImpl;

public class CompressionStreamTest {

	FileBackedOutputStream fbos = null;

	@Before
	public void setup() {
		TemporaryFileManagerImpl mgr = new TemporaryFileManagerImpl();

		fbos = mgr.getOutputStream(100);
	}

	@After
	public void tearDown() {
		if (fbos != null) {
			fbos.discard();
		}
	}

	@Test
	public void testCompressDecompress_Compress() throws IOException {

		DeflaterOutputStream zos = new DeflaterOutputStream(fbos, new Deflater(Deflater.DEFAULT_COMPRESSION, false),
				512, false);

		PrintWriter pw = new PrintWriter(zos);
		int repeats = 10000;
		for (int i = 0; i < repeats; i++) {
			String contentLine = "NUM" + i;
			pw.write(contentLine);
			pw.write("\n");
		}
		pw.flush();
		pw.close();

		assertTrue(fbos.isClosed());

		InputStream fbis = fbos.getInputStream();
		try {
			InflaterInputStream zis = new InflaterInputStream(fbis, new Inflater(false));

			BufferedReader br = new BufferedReader(new InputStreamReader(zis));

			String strLine;
			int i = 0;
			while ((strLine = br.readLine()) != null) {
				String contentLine = "NUM" + i;

				assertEquals(contentLine, strLine);
				i++;
			}
			assertEquals(repeats, i);
		} finally {
			fbis.close();
		}
	}

	@Test
	public void testCompressDecompress_Zlib() throws IOException {

		DeflaterOutputStream zos = new DeflaterOutputStream(fbos, new Deflater(Deflater.DEFAULT_COMPRESSION, true), 512,
				false);

		PrintWriter pw = new PrintWriter(zos);
		int repeats = 10000;
		for (int i = 0; i < repeats; i++) {
			String contentLine = "NUM" + i;
			pw.write(contentLine);
			pw.write("\n");
		}
		pw.flush();
		pw.close();

		assertTrue(fbos.isClosed());

		InputStream fbis = fbos.getInputStream();
		try {
			InflaterInputStream zis = new InflaterInputStream(fbis, new Inflater(true));

			BufferedReader br = new BufferedReader(new InputStreamReader(zis));

			String strLine;
			int i = 0;
			while ((strLine = br.readLine()) != null) {
				String contentLine = "NUM" + i;

				assertEquals(contentLine, strLine);
				i++;
			}
			assertEquals(repeats, i);
		} finally {
			fbis.close();
		}
	}

	@Test
	public void testGZipGunzip() throws IOException {

		GZIPOutputStream zos = new GZIPOutputStream(fbos, false);

		PrintWriter pw = new PrintWriter(zos);
		int repeats = 10000;
		for (int i = 0; i < repeats; i++) {
			String contentLine = "NUM" + i;
			pw.write(contentLine);
			pw.write("\n");
		}
		pw.flush();
		pw.close();

		assertTrue(fbos.isClosed());

		InputStream fbis = fbos.getInputStream();
		try {
			GZIPInputStream zis = new GZIPInputStream(fbis);

			BufferedReader br = new BufferedReader(new InputStreamReader(zis));

			String strLine;
			int i = 0;
			while ((strLine = br.readLine()) != null) {
				String contentLine = "NUM" + i;

				assertEquals(contentLine, strLine);
				i++;
			}
			assertEquals(repeats, i);
		} finally {
			fbis.close();
		}
	}
}
