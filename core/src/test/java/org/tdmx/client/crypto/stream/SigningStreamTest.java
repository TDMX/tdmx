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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.Signature;
import java.security.SignatureException;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryBufferFactory;
import org.tdmx.client.crypto.buffer.TemporaryFileManagerImpl;
import org.tdmx.client.crypto.converters.NumberToOctetString;
import org.tdmx.client.crypto.scheme.CryptoException;

public class SigningStreamTest {

	TemporaryBufferFactory factory = new TemporaryFileManagerImpl();
	KeyPair kp = null;

	@Before
	public void setUp() throws Exception {
		kp = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
	}

	@Test
	public void testPlainSign_ArrayWritesInclLength_OutputSignature() throws CryptoException, IOException,
			SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		Signature check = SignatureAlgorithm.SHA_1_RSA.getSignature(kp.getPrivate());

		try {
			int reps = 1024;
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), true,
					true, fbos);

			byte[] content = StreamTestUtils.createRndArray(chunklen);
			StreamTestUtils.writeArrayAsArray(sos, content, reps); // test
			StreamTestUtils.signArray(check, content, reps); // check

			sos.close();
			assertTrue(fbos.getSize() > reps * chunklen); // +signature length which for RSA is dep. on the RSA key
															// length in bytes.

			byte[] sizeBytes = NumberToOctetString.longToBytes(sos.getSize());
			check.update(sizeBytes);
			assertArrayEquals(sos.getSignatureValue(), check.sign());

		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainSign_ArrayWritesInclLength() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		Signature check = SignatureAlgorithm.SHA_1_RSA.getSignature(kp.getPrivate());

		try {
			int reps = 1024;
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), false,
					true, fbos);

			byte[] content = StreamTestUtils.createRndArray(chunklen);
			StreamTestUtils.writeArrayAsArray(sos, content, reps); // test
			StreamTestUtils.signArray(check, content, reps); // check

			sos.close();
			assertEquals(reps * chunklen, fbos.getSize());

			byte[] sizeBytes = NumberToOctetString.longToBytes(sos.getSize());
			check.update(sizeBytes);
			assertArrayEquals(sos.getSignatureValue(), check.sign());

		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainSign_ArrayWrites() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		Signature check = SignatureAlgorithm.SHA_1_RSA.getSignature(kp.getPrivate());

		try {
			int reps = 1024;
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), false,
					false, fbos);

			byte[] content = StreamTestUtils.createRndArray(chunklen);
			StreamTestUtils.writeArrayAsArray(sos, content, reps); // test
			StreamTestUtils.signArray(check, content, reps); // check

			sos.close();
			assertEquals(reps * chunklen, fbos.getSize());
			assertArrayEquals(sos.getSignatureValue(), check.sign());

		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainVerify_SingleArray() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		try {
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), false,
					false, fbos);

			byte[] content = StreamTestUtils.createRndArray(chunklen);
			sos.write(content);

			sos.close();
			assertEquals(chunklen, fbos.getSize());
			byte[] signatureValue = sos.getSignatureValue();
			long contentLen = sos.getSize();

			InputStream fbis = fbos.getInputStream();
			SignatureVerifyingInputStream svis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_1_RSA,
					kp.getPublic(), contentLen, false, signatureValue, fbis);
			byte[] buffer = new byte[content.length];
			StreamTestUtils.readArrayAsArray(svis, buffer, content, 1);
			assertEquals(-1, svis.read());
			svis.close();
			assertTrue(svis.isSignatureValid());
			assertArrayEquals(signatureValue, svis.getSignatureValue());

		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainVerify_SingleArray_AppendedExclLength() throws CryptoException, IOException,
			SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		try {
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), true,
					false, fbos);

			byte[] content = StreamTestUtils.createRndArray(chunklen);
			sos.write(content);

			sos.close();
			assertTrue(fbos.getSize() > chunklen);
			byte[] signatureValue = sos.getSignatureValue();
			long contentLen = sos.getSize();

			InputStream fbis = fbos.getInputStream();
			SignatureVerifyingInputStream svis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_1_RSA,
					kp.getPublic(), contentLen, false, fbis);
			byte[] buffer = new byte[content.length];
			StreamTestUtils.readArrayAsArray(svis, buffer, content, 1);
			assertEquals(-1, svis.read());
			svis.close();
			assertTrue(svis.isSignatureValid());
			assertArrayEquals(signatureValue, svis.getSignatureValue());

		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainVerify_SingleArray_AppendedInclLength() throws CryptoException, IOException,
			SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		try {
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), true,
					true, fbos);

			byte[] content = StreamTestUtils.createRndArray(chunklen);
			sos.write(content);

			sos.close();
			assertTrue(fbos.getSize() > chunklen);
			byte[] signatureValue = sos.getSignatureValue();
			long contentLen = sos.getSize();

			InputStream fbis = fbos.getInputStream();
			SignatureVerifyingInputStream svis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_1_RSA,
					kp.getPublic(), contentLen, true, fbis);
			byte[] buffer = new byte[content.length];
			StreamTestUtils.readArrayAsArray(svis, buffer, content, 1);
			assertEquals(-1, svis.read());
			svis.close();
			assertTrue(svis.isSignatureValid());
			assertArrayEquals(signatureValue, svis.getSignatureValue());

		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainVerify_ArrayWrites() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		try {
			int reps = 1024;
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), false,
					false, fbos);

			byte[] content = StreamTestUtils.createRndArray(chunklen);
			StreamTestUtils.writeArrayAsArray(sos, content, reps); // test

			sos.close();
			assertEquals(reps * chunklen, fbos.getSize());
			byte[] signatureValue = sos.getSignatureValue();
			long contentLen = sos.getSize();

			InputStream fbis = fbos.getInputStream();
			SignatureVerifyingInputStream svis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_1_RSA,
					kp.getPublic(), contentLen, false, signatureValue, fbis);
			byte[] buffer = new byte[content.length];
			StreamTestUtils.readArrayAsArray(svis, buffer, content, reps);
			assertEquals(-1, svis.read());
			svis.close();
			assertTrue(svis.isSignatureValid());
			assertArrayEquals(signatureValue, svis.getSignatureValue());

		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainVerify_ArrayWrites_AppendedInclLength() throws CryptoException, IOException,
			SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		try {
			int reps = 1024;
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), true,
					true, fbos);

			byte[] content = StreamTestUtils.createRndArray(chunklen);
			StreamTestUtils.writeArrayAsArray(sos, content, reps); // test

			sos.close();
			assertTrue(fbos.getSize() > reps * chunklen);
			byte[] signatureValue = sos.getSignatureValue();
			long contentLen = sos.getSize();

			InputStream fbis = fbos.getInputStream();
			SignatureVerifyingInputStream svis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_1_RSA,
					kp.getPublic(), contentLen, true, fbis);
			byte[] buffer = new byte[content.length];
			StreamTestUtils.readArrayAsArray(svis, buffer, content, reps);
			assertEquals(-1, svis.read());
			svis.close();
			assertTrue(svis.isSignatureValid());
			assertArrayEquals(signatureValue, svis.getSignatureValue());

		} finally {
			fbos.discard();
		}
	}
}
