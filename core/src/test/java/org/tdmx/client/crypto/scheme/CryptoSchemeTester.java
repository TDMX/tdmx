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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.security.KeyPair;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.KeyAgreementAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryBufferFactory;
import org.tdmx.client.crypto.buffer.TemporaryFileManagerImpl;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.converters.StringToUtf8;
import org.tdmx.client.crypto.entropy.EntropySource;

public class CryptoSchemeTester {

	static {
		JCAProviderInitializer.init();
	}

	private CryptoSchemeFactory ownFactory;
	private CryptoSchemeFactory otherFactory;
	private final TemporaryBufferFactory bufferFactory = new TemporaryFileManagerImpl(1024, 1024);

	@Before
	public void setup() throws CryptoException {
		KeyPair ownSigningKeyPair = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
		KeyPair otherSigningKeyPair = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();

		ownFactory = new CryptoSchemeFactory(ownSigningKeyPair, otherSigningKeyPair.getPublic(), bufferFactory);
		assertNotNull(ownFactory);
		otherFactory = new CryptoSchemeFactory(otherSigningKeyPair, ownSigningKeyPair.getPublic(), bufferFactory);
		assertNotNull(otherFactory);
	}

	@Test
	public void testAllSchemes() throws CryptoException, IOException {

		CryptoScheme[] css = CryptoScheme.values();
		for (CryptoScheme cs : css) {
			System.out.println("Testing " + cs.getName());
			if (cs.getName().contains("ecdh384")) {
				KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
				byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());

				byte[] passphrase = StringToUtf8.toBytes("hello world!");

				Encrypter e = ownFactory.getECDHEncrypter(cs, passphrase, encodedSessionKey);
				assertNotNull(e);
				Decrypter d = otherFactory.getECDHDecrypter(cs, passphrase, session);
				assertNotNull(d);

				SchemeTester.testFixedSizeSmallTransfer(e, d, 100000);
			} else {
				byte[] sessionKey = EntropySource.getRandomBytes(16);
				byte[] encodedSessionKey = ByteArray.clone(sessionKey);

				byte[] passphrase = StringToUtf8.toBytes("hello world!");
				if (!cs.getName().contains("pf-")) {
					passphrase = EntropySource.getRandomBytes(48); // typical 32+16 key+iv
				}
				Encrypter e = ownFactory.getPlainEncrypter(cs, passphrase, encodedSessionKey);
				assertNotNull(e);
				Decrypter d = otherFactory.getPlainDecrypter(cs, passphrase, sessionKey);
				assertNotNull(d);

				SchemeTester.testFixedSizeSmallTransfer(e, d, 100000);

			}

		}

	}

}
