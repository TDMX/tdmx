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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.KeyAgreementAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryFileManagerImpl;
import org.tdmx.client.crypto.certificate.TrustStoreCertificateIOUtilsTest;

public class IntegratedCryptoSchemeTester {
	private final Logger log = LoggerFactory.getLogger(TrustStoreCertificateIOUtilsTest.class);

	static {
		JCAProviderInitializer.init();
	}

	private IntegratedCryptoSchemeFactory ownFactory;
	private IntegratedCryptoSchemeFactory otherFactory;
	private TemporaryFileManagerImpl bufferFactory;

	@Before
	public void setup() throws CryptoException {
		bufferFactory = new TemporaryFileManagerImpl();
		bufferFactory.setChunkSize(33333);

		KeyPair ownSigningKeyPair = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
		KeyPair otherSigningKeyPair = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();

		ownFactory = new IntegratedCryptoSchemeFactory(ownSigningKeyPair, otherSigningKeyPair.getPublic(),
				bufferFactory);
		assertNotNull(ownFactory);
		otherFactory = new IntegratedCryptoSchemeFactory(otherSigningKeyPair, ownSigningKeyPair.getPublic(),
				bufferFactory);
		assertNotNull(otherFactory);
	}

	@Test
	public void testAllSchemes() throws CryptoException, IOException {

		IntegratedCryptoScheme[] css = IntegratedCryptoScheme.values();
		for (IntegratedCryptoScheme cs : css) {
			log.info("Testing " + cs.getName());

			KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
			byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());

			Encrypter e = ownFactory.getEncrypter(cs, encodedSessionKey);
			assertNotNull(e);
			Decrypter d = otherFactory.getDecrypter(cs, session);
			assertNotNull(d);

			SchemeTester.testFixedSizeSmallTransfer(e, d, 100000);

		}

	}

}
