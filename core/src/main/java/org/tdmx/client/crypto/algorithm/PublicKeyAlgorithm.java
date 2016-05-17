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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public enum PublicKeyAlgorithm {

	RSA2048(2048, "RSA", null),
	RSA4096(4096, "RSA", null),
	ECDSA256(384, "EC", "secp256r1"),
	ECDSA384(384, "EC", "secp384r1"), ;

	private int keyLength;
	private String algorithm;
	private String parameter;

	private PublicKeyAlgorithm(int keyLength, String algorithm, String parameter) {
		this.keyLength = keyLength;
		this.algorithm = algorithm;
		this.parameter = parameter;
	}

	public KeyPair generateNewKeyPair() throws CryptoException {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);
			if (parameter != null) {
				ECGenParameterSpec ecSpec = new ECGenParameterSpec(parameter);
				kpg.initialize(ecSpec, EntropySource.getSecureRandom());
			} else {
				kpg.initialize(keyLength, EntropySource.getSecureRandom());
			}
			KeyPair kp = kpg.generateKeyPair();
			return kp;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING, e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_PARAMETER_INVALID, e);
		}
	}

	public PrivateKey decodePKCS8EncodedKey(byte[] privateKeyBytes) throws CryptoException {
		try {
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			PrivateKey privateKey = kf.generatePrivate(privateKeySpec);
			if (privateKey instanceof RSAPrivateKey) {
				int bitLen = ((RSAPrivateKey) privateKey).getModulus().bitLength();
				if (bitLen != keyLength) {
					throw new CryptoException(CryptoResultCode.ERROR_PK_PRIVATE_KEY_SPEC_INVALID);
				}
			} else {
				throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISMATCH);
			}
			return privateKey;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_PRIVATE_KEY_SPEC_INVALID, e);
		}
	}

	public PublicKey decodeX509EncodedKey(byte[] publicKeyBytes) throws CryptoException {
		try {
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			EncodedKeySpec eks = new X509EncodedKeySpec(publicKeyBytes);
			PublicKey publicKey = kf.generatePublic(eks);
			if (publicKey instanceof RSAPublicKey) {
				int bitLen = ((RSAPublicKey) publicKey).getModulus().bitLength();
				if (bitLen != keyLength) {
					throw new CryptoException(CryptoResultCode.ERROR_PK_PRIVATE_KEY_SPEC_INVALID);
				}
			} else {
				throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISMATCH);
			}
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_PUBLIC_KEY_SPEC_INVALID, e);
		}
	}

	public byte[] encodeX509PublicKey(PublicKey publicKey) throws CryptoException {
		if (!"X.509".equals(publicKey.getFormat())) {
			throw new CryptoException(CryptoResultCode.ERROR_ENCODED_KEY_FORMAT_INVALID);
		}
		return publicKey.getEncoded();
	}

	public static PublicKeyAlgorithm getAlgorithmMatchingKey(PublicKey k) throws CryptoException {
		if (k instanceof RSAPublicKey) {
			int bitLen = ((RSAPublicKey) k).getModulus().bitLength();
			switch (bitLen) {
			case 2048:
				return PublicKeyAlgorithm.RSA2048;
			case 4096:
				return PublicKeyAlgorithm.RSA4096;
			}
		}
		throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING);
	}

	public static PublicKeyAlgorithm getAlgorithmMatchingKey(PrivateKey k) throws CryptoException {
		if (k instanceof RSAPrivateKey) {
			int bitLen = ((RSAPrivateKey) k).getModulus().bitLength();
			switch (bitLen) {
			case 2048:
				return PublicKeyAlgorithm.RSA2048;
			case 4096:
				return PublicKeyAlgorithm.RSA4096;
			}
		}
		throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING);
	}

	public int getKeyLength() {
		return keyLength;
	}

	public int getKeyLengthInBytes() {
		return keyLength / 8;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getParameter() {
		return parameter;
	}
}
