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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.stream.StreamTestUtils;
import org.tdmx.core.system.lang.StreamUtils;

public class SchemeTester {

	public static void testFixedSizeSmallTransfer(Encrypter e, Decrypter d, int length)
			throws CryptoException, IOException {
		byte[] plaintext = EntropySource.getRandomBytes(length);

		OutputStream os = e.getOutputStream();
		os.write(plaintext);
		os.close();

		CryptoContext result = e.getResult();

		InputStream is = d.getInputStream(result.getEncryptedData(), result.getEncryptionContext());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(plaintext.length);
		try {
			StreamTestUtils.transfer(is, baos);
		} finally {
			is.close();
		}
		byte[] decryptedPlaintext = baos.toByteArray();
		assertArrayEquals(plaintext, decryptedPlaintext);
	}

	public static void testLargeCompressable(Encrypter e, Decrypter d) throws CryptoException, IOException {
		OutputStream os = e.getOutputStream();
		PrintWriter pw = new PrintWriter(os);
		int repeats = 20000000;
		for (int i = 0; i < repeats; i++) {
			String contentLine = "NUM" + i + "\n";
			pw.write(contentLine);
		}
		pw.close();

		CryptoContext result = e.getResult();

		InputStream is = d.getInputStream(result.getEncryptedData(), result.getEncryptionContext());
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String strLine;
			int i = 0;
			while ((strLine = br.readLine()) != null) {
				String contentLine = "NUM" + i;

				assertEquals(contentLine, strLine);
				i++;
			}
			assertEquals(repeats, i);

			assertEquals(-1, is.read());
		} finally {
			is.close();
		}
	}

	public static void testLargeUncompressable(Encrypter e, Decrypter d, byte[] data)
			throws CryptoException, IOException {
		OutputStream os = e.getOutputStream();
		os.write(data);
		os.close();

		CryptoContext result = e.getResult();

		InputStream is = d.getInputStream(result.getEncryptedData(), result.getEncryptionContext());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
		StreamUtils.transfer(is, baos);

		assertArrayEquals(data, baos.toByteArray());
	}
}
