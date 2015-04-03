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
package org.tdmx.client.crypto.certificate;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.converters.StringToUtf8;
import org.tdmx.client.crypto.scheme.CryptoException;

public class StringSigningUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final static Logger log = LoggerFactory.getLogger(StringSigningUtils.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public static String getHexSignature(PrivateKey privateKey, SignatureAlgorithm alg, String valueToSign) {
		try {
			Signature signature = alg.getSignature(privateKey);

			signature.update(StringToUtf8.toBytes(valueToSign));

			byte[] signatureByes = signature.sign();
			return ByteArray.asHex(signatureByes);
		} catch (CryptoException | SignatureException e) {
			log.warn("Signature creation failed.", e);
			return null;
		}
	}

	public static boolean checkHexSignature(PublicKey signingPublicCert, SignatureAlgorithm alg, String valueToSign,
			String signatureAsHex) {
		try {
			Signature signature = alg.getVerifier(signingPublicCert);

			signature.update(StringToUtf8.toBytes(valueToSign));

			byte[] signatureBytes = ByteArray.fromHex(signatureAsHex.toCharArray());
			return signature.verify(signatureBytes);
		} catch (Exception e) {
			log.warn("Signature validation failed.", e);
			return false;
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
