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
package org.tdmx.client.crypto.algorithm;

import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.Signature;
import java.security.SignatureException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.scheme.CryptoException;

public class SignatureAlgorithmTest {
	private final Logger log = LoggerFactory.getLogger(SignatureAlgorithmTest.class);

	static {
		JCAProviderInitializer.init();
	}

	@Test
	public void testOkRsa2048Signature() throws CryptoException, SignatureException {
		KeyPair kp = PublicKeyAlgorithm.RSA2048.generateNewKeyPair();
		testSignature(kp, SignatureAlgorithm.SHA_1_RSA);
		testSignature(kp, SignatureAlgorithm.SHA_256_RSA);
		testSignature(kp, SignatureAlgorithm.SHA_384_RSA);
		testSignature(kp, SignatureAlgorithm.SHA_512_RSA);
	}

	@Test
	public void testOkRsa4096Signature() throws CryptoException, SignatureException {
		KeyPair kp = PublicKeyAlgorithm.RSA4096.generateNewKeyPair();
		testSignature(kp, SignatureAlgorithm.SHA_1_RSA);
		testSignature(kp, SignatureAlgorithm.SHA_256_RSA);
		testSignature(kp, SignatureAlgorithm.SHA_384_RSA);
		testSignature(kp, SignatureAlgorithm.SHA_512_RSA);
	}

	@Test
	public void testOkEcdsa384Signature() throws CryptoException, SignatureException {
		KeyPair kp = PublicKeyAlgorithm.ECDSA384.generateNewKeyPair();
		testSignature(kp, SignatureAlgorithm.SHA_1_ECDSA);
		testSignature(kp, SignatureAlgorithm.SHA_256_ECDSA);
		testSignature(kp, SignatureAlgorithm.SHA_384_ECDSA);
		testSignature(kp, SignatureAlgorithm.SHA_512_ECDSA);
	}

	@Test
	public void testOkEcdsa256Signature() throws CryptoException, SignatureException {
		KeyPair kp = PublicKeyAlgorithm.ECDSA256.generateNewKeyPair();
		testSignature(kp, SignatureAlgorithm.SHA_1_ECDSA);
		testSignature(kp, SignatureAlgorithm.SHA_256_ECDSA);
		testSignature(kp, SignatureAlgorithm.SHA_384_ECDSA);
		testSignature(kp, SignatureAlgorithm.SHA_512_ECDSA);
	}

	private void testSignature(KeyPair kp, SignatureAlgorithm alg) throws CryptoException, SignatureException {
		Signature signature = alg.getSignature(kp.getPrivate());
		// Read the string into a buffer
		// @formatter:off
		String data = "{\n" 
				+ "  \"schemas\":[\"urn:scim:schemas:core:1.0\"],\n" 
				+ "  \"userName\":\"bjensen\",\n"
				+ "  \"externalId\":\"bjensen\",\n" 
				+ "  \"name\":{\n"
				+ "    \"formatted\":\"Ms. Barbara J Jensen III\",\n" 
				+ "    \"familyName\":\"Jensen\",\n"
				+ "    \"givenName\":\"Barbara\"\n" 
				+ "  }\n" 
				+ "}";
		// @formatter:on

		byte[] dataInBytes = data.getBytes();

		// update signature with data to be signed
		signature.update(dataInBytes);

		// sign the data
		byte[] signedInfo = signature.sign();

		log.debug("Alg: " + alg + " signature len " + signedInfo.length);

		Signature verifier = alg.getVerifier(kp.getPublic());
		verifier.update(dataInBytes);
		assertTrue(verifier.verify(signedInfo));
		assertTrue(signedInfo.length <= SignatureAlgorithm.getMaxSignatureSizeBytes(signature, kp.getPublic()));
	}

}
