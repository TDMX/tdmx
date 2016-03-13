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
package org.tdmx.client.crypto.scheme.ecdh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.KeyAgreementAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryFileManagerImpl;
import org.tdmx.client.crypto.converters.StringToUtf8;
import org.tdmx.client.crypto.scheme.CryptoContext;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoScheme;
import org.tdmx.client.crypto.scheme.CryptoSchemeFactory;
import org.tdmx.client.crypto.scheme.Decrypter;
import org.tdmx.client.crypto.scheme.Encrypter;
import org.tdmx.client.crypto.scheme.SchemeTester;

public class PF_ECDHPayloadSchemeTest {

	static {
		JCAProviderInitializer.init();
	}

	private CryptoSchemeFactory ownFactory;
	private CryptoSchemeFactory otherFactory;
	private TemporaryFileManagerImpl bufferFactory;

	@Before
	public void setup() throws CryptoException {
		bufferFactory = new TemporaryFileManagerImpl();
		bufferFactory.setChunkSize(1024);

		KeyPair ownSigningKeyPair = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
		KeyPair otherSigningKeyPair = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();

		ownFactory = new CryptoSchemeFactory(ownSigningKeyPair, otherSigningKeyPair.getPublic(), bufferFactory);
		assertNotNull(ownFactory);
		otherFactory = new CryptoSchemeFactory(otherSigningKeyPair, ownSigningKeyPair.getPublic(), bufferFactory);
		assertNotNull(otherFactory);
	}

	@Test
	public void testEncrypt() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());

		byte[] passphrase = StringToUtf8.toBytes("wtf");

		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.NONE_SLASH_PF_ECDH384_AES256, passphrase,
				encodedSessionKey);
		assertNotNull(e);

		OutputStream os = e.getOutputStream();
		os.write(101);
		os.flush();
		os.close();

		CryptoContext result = e.getResult();
		assertNotNull(result);
		assertEquals(1, result.getPlaintextLength());
		assertNotNull(result.getEncryptedData());
		assertNotNull(result.getEncryptionContext());
		assertNotNull(result.getCiphertextLength());
	}

	@Test
	public void testEncryptDecrypt_SingleByte() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());

		byte[] passphrase = StringToUtf8.toBytes("wtf");

		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.NONE_SLASH_PF_ECDH384_AES256, passphrase,
				encodedSessionKey);
		assertNotNull(e);

		byte p = 101;

		OutputStream os = e.getOutputStream();
		os.write(p);
		os.flush();
		os.close();

		CryptoContext result = e.getResult();

		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.NONE_SLASH_PF_ECDH384_AES256, passphrase, session);

		InputStream is = d.getInputStream(result.getEncryptedData(), result.getEncryptionContext());
		try {
			byte dp = (byte) is.read();

			assertEquals(p, dp);

			assertEquals(-1, is.read());
		} finally {
			is.close();
		}
	}

	@Test
	public void testEncryptDecrypt_Aes_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());

		byte[] passphrase = StringToUtf8.toBytes("wtf");

		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.NONE_SLASH_PF_ECDH384_AES256, passphrase,
				encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.NONE_SLASH_PF_ECDH384_AES256, passphrase, session);
		assertNotNull(d);

		SchemeTester.testLargeCompressable(e, d);
	}

	@Test
	public void testEncryptDecrypt_Twofish_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());

		byte[] passphrase = StringToUtf8.toBytes("wtf");

		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.NONE_SLASH_PF_ECDH384_TWOFISH256, passphrase,
				encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.NONE_SLASH_PF_ECDH384_TWOFISH256, passphrase, session);
		assertNotNull(d);

		SchemeTester.testLargeCompressable(e, d);
	}

	@Test
	public void testEncryptDecrypt_Serpent_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());

		byte[] passphrase = StringToUtf8.toBytes("wtf");

		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.NONE_SLASH_PF_ECDH384_SERPENT256, passphrase,
				encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.NONE_SLASH_PF_ECDH384_SERPENT256, passphrase, session);
		assertNotNull(d);

		SchemeTester.testLargeCompressable(e, d);
	}

	@Test
	public void testEncryptDecrypt_Aes_RSA_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());

		byte[] passphrase = StringToUtf8.toBytes("wtf");

		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_ECDH384_AES256, passphrase,
				encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.RSA_SLASH_PF_ECDH384_AES256, passphrase, session);
		assertNotNull(d);

		SchemeTester.testLargeCompressable(e, d);
	}

	@Test
	public void testEncryptDecrypt_Twofish_RSA_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());

		byte[] passphrase = StringToUtf8.toBytes("wtf");

		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_ECDH384_TWOFISH256, passphrase,
				encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.RSA_SLASH_PF_ECDH384_TWOFISH256, passphrase, session);
		assertNotNull(d);

		SchemeTester.testLargeCompressable(e, d);
	}

	@Test
	public void testEncryptDecrypt_Serpent_RSA_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());

		byte[] passphrase = StringToUtf8.toBytes("wtf");

		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_ECDH384_SERPENT256, passphrase,
				encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.RSA_SLASH_PF_ECDH384_SERPENT256, passphrase, session);
		assertNotNull(d);

		SchemeTester.testLargeCompressable(e, d);
	}

}
