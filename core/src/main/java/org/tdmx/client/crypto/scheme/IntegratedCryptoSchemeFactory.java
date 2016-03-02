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

import java.security.KeyPair;
import java.security.PublicKey;

import org.tdmx.client.crypto.algorithm.StreamCipherAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryBufferFactory;
import org.tdmx.client.crypto.scheme.ecdh.RSA_ECDHContextDecrypter;
import org.tdmx.client.crypto.scheme.ecdh.RSA_ECDHContextEncrypter;
import org.tdmx.client.crypto.scheme.ecdh.RSA_ECDHPayloadDecrypter;
import org.tdmx.client.crypto.scheme.ecdh.RSA_ECDHPayloadEncrypter;

/**
 * A Factory for CryptoScheme instances which can be used for encryption or decryption of individual messages.
 * 
 * @author Peter
 * 
 */
public class IntegratedCryptoSchemeFactory {

	private final KeyPair ownSigningKey;
	private final PublicKey otherSigningKey;
	private final TemporaryBufferFactory bufferFactory;

	public IntegratedCryptoSchemeFactory(KeyPair ownSigningKey, PublicKey otherSigningKey,
			TemporaryBufferFactory bufferFactory) {
		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;
		this.bufferFactory = bufferFactory;
	}

	/**
	 * IMPORTANT: encodedSessionKey must be proven to have originated from the destination by checking it's signature
	 * with the otherSigningKey prior to calling.
	 * 
	 * @param scheme
	 * @param encodedSessionKey
	 * @param messageKey
	 * @return
	 * @throws CryptoException
	 */
	public Encrypter getEncrypter(IntegratedCryptoScheme scheme, byte[] encodedSessionKey) throws CryptoException {
		if (scheme == null || encodedSessionKey == null) {
			throw new IllegalArgumentException();
		}
		switch (scheme) {
		case ECDH384_RSA_SLASH_AES256:
			return new RSA_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Aes256_CTR);
		case ECDH384_RSA_SLASH_TWOFISH256:
			return new RSA_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Twofish256_CTR);
		case ECDH384_RSA_SLASH_SERPENT256:
			return new RSA_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Serpent256_CTR);

		case ECDH384_AES256plusRSA_SLASH_AES256:
			return new RSA_ECDHContextEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Aes256_CTR);
		case ECDH384_AES256plusRSA_SLASH_TWOFISH256:
			return new RSA_ECDHContextEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR);
		case ECDH384_AES256plusRSA_SLASH_SERPENT256:
			return new RSA_ECDHContextEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR);

		case ECDH384_TWOFISH256plusRSA_SLASH_AES256:
			return new RSA_ECDHContextEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR);
		case ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256:
			return new RSA_ECDHContextEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Twofish256_CTR);
		case ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256:
			return new RSA_ECDHContextEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR);

		case ECDH384_SERPENT256plusRSA_SLASH_AES256:
			return new RSA_ECDHContextEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR);
		case ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256:
			return new RSA_ECDHContextEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR);
		case ECDH384_SERPENT256plusRSA_SLASH_SERPENT256:
			return new RSA_ECDHContextEncrypter(ownSigningKey, otherSigningKey, encodedSessionKey, bufferFactory,
					StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Serpent256_CTR);
		default:
			throw new CryptoException(CryptoResultCode.UNKNOWN_CRYPTOSCHEME_NAME);
		}

	}

	/**
	 * 
	 * @param scheme
	 * @param sessionKey
	 * @param encryptionContext
	 * @return
	 * @throws CryptoException
	 */
	public Decrypter getDecrypter(IntegratedCryptoScheme scheme, KeyPair sessionKey) throws CryptoException {
		if (scheme == null || sessionKey == null) { // encryptionContext could be null depending
													// on the scheme
			throw new IllegalArgumentException();
		}
		switch (scheme) {
		case ECDH384_RSA_SLASH_AES256:
			return new RSA_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Aes256_CTR);
		case ECDH384_RSA_SLASH_TWOFISH256:
			return new RSA_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Twofish256_CTR);
		case ECDH384_RSA_SLASH_SERPENT256:
			return new RSA_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Serpent256_CTR);

		case ECDH384_AES256plusRSA_SLASH_AES256:
			return new RSA_ECDHContextDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Aes256_CTR);
		case ECDH384_AES256plusRSA_SLASH_TWOFISH256:
			return new RSA_ECDHContextDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR);
		case ECDH384_AES256plusRSA_SLASH_SERPENT256:
			return new RSA_ECDHContextDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR);

		case ECDH384_TWOFISH256plusRSA_SLASH_AES256:
			return new RSA_ECDHContextDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR);
		case ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256:
			return new RSA_ECDHContextDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Twofish256_CTR);
		case ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256:
			return new RSA_ECDHContextDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR);

		case ECDH384_SERPENT256plusRSA_SLASH_AES256:
			return new RSA_ECDHContextDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR);
		case ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256:
			return new RSA_ECDHContextDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR);
		case ECDH384_SERPENT256plusRSA_SLASH_SERPENT256:
			return new RSA_ECDHContextDecrypter(ownSigningKey, otherSigningKey, sessionKey,
					StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Serpent256_CTR);

		default:
			throw new CryptoException(CryptoResultCode.UNKNOWN_CRYPTOSCHEME_NAME);
		}

	}
}
