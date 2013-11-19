package org.tdmx.client.crypto.scheme;

import java.security.KeyPair;
import java.security.PublicKey;

import org.tdmx.client.crypto.algorithm.StreamCipherAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryBufferFactory;
import org.tdmx.client.crypto.scheme.ecdh.PF_ECDHContextCascadeDecrypter;
import org.tdmx.client.crypto.scheme.ecdh.PF_ECDHContextCascadeEncrypter;
import org.tdmx.client.crypto.scheme.ecdh.PF_ECDHContextDecrypter;
import org.tdmx.client.crypto.scheme.ecdh.PF_ECDHContextEncrypter;
import org.tdmx.client.crypto.scheme.ecdh.PF_ECDHPayloadDecrypter;
import org.tdmx.client.crypto.scheme.ecdh.PF_ECDHPayloadEncrypter;
import org.tdmx.client.crypto.scheme.ecdh.PF_RSA_ECDHCascadePayloadDecrypter;
import org.tdmx.client.crypto.scheme.ecdh.PF_RSA_ECDHCascadePayloadEncrypter;
import org.tdmx.client.crypto.scheme.ecdh.PF_RSA_ECDHPayloadDecrypter;
import org.tdmx.client.crypto.scheme.ecdh.PF_RSA_ECDHPayloadEncrypter;
import org.tdmx.client.crypto.scheme.none.PF_PayloadDecrypter;
import org.tdmx.client.crypto.scheme.none.PF_PayloadEncrypter;
import org.tdmx.client.crypto.scheme.none.PGP_PayloadDecrypter;
import org.tdmx.client.crypto.scheme.none.PGP_PayloadEncrypter;
import org.tdmx.client.crypto.scheme.none.SK_PayloadDecrypter;
import org.tdmx.client.crypto.scheme.none.SK_PayloadEncrypter;

/**
 * A Factory for CryptoScheme instances which can be used for encryption or decryption
 * of individual messages.
 * 
 * @author Peter
 *
 */
public class CryptoSchemeFactory {

	private KeyPair ownSigningKey;
	private PublicKey otherSigningKey;
	private TemporaryBufferFactory bufferFactory;
	
	public CryptoSchemeFactory( KeyPair ownSigningKey, PublicKey otherSigningKey, TemporaryBufferFactory bufferFactory ) {
		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;
		this.bufferFactory = bufferFactory;
	}

	/**
	 * IMPORTANT: encodedSessionKey must be proven to have originated from the destination by
	 * checking it's signature with the otherSigningKey prior to calling.
	 * 
	 * @param scheme
	 * @param encodedSessionKey
	 * @param messageKey
	 * @return
	 * @throws CryptoException
	 */
	public Encrypter getPlainEncrypter( CryptoScheme scheme, byte[] passphrase, byte[] encodedSessionKey  ) throws CryptoException {
		if ( scheme == null || passphrase == null || encodedSessionKey == null) {
			throw new IllegalArgumentException();
		}
		switch ( scheme ) { 
		
		case NONE_SLASH_PF_AES256 :
			return new PF_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Aes256_CTR );
		case NONE_SLASH_PF_TWOFISH256 :
			return new PF_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Twofish256_CTR );
		case NONE_SLASH_PF_SERPENT256 :
			return new PF_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Serpent256_CTR );
		case NONE_SLASH_AES256 :
			return new SK_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, bufferFactory, StreamCipherAlgorithm.Aes256_CTR );
		case NONE_SLASH_TWOFISH256 :
			return new SK_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, bufferFactory, StreamCipherAlgorithm.Twofish256_CTR );
		case NONE_SLASH_SERPENT256 :
			return new SK_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, bufferFactory, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_AES256 :
			return new PF_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_PF_TWOFISH256 :
			return new PF_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR );
		case RSA_SLASH_PF_SERPENT256 :
			return new PF_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_AES256 :
			return new PGP_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_TWOFISH256 :
			return new PGP_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Twofish256_CTR );
		case RSA_SLASH_SERPENT256 :
			return new PGP_PayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Serpent256_CTR );
		}
		throw new CryptoException( CryptoResultCode.UNKNOWN_CRYPTOSCHEME_NAME );
	}
	
	/**
	 * IMPORTANT: encodedSessionKey must be proven to have originated from the destination by
	 * checking it's signature with the otherSigningKey prior to calling.
	 * 
	 * @param scheme
	 * @param encodedSessionKey
	 * @param messageKey
	 * @return
	 * @throws CryptoException
	 */
	public Encrypter getECDHEncrypter( CryptoScheme scheme, byte[] passphrase, byte[] encodedSessionKey  ) throws CryptoException {
		if ( scheme == null || passphrase == null || encodedSessionKey == null) {
			throw new IllegalArgumentException();
		}
		switch ( scheme ) { 
		
		case NONE_SLASH_PF_ECDH384_AES256 :
			return new PF_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Aes256_CTR );
		case NONE_SLASH_PF_ECDH384_TWOFISH256 :
			return new PF_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Twofish256_CTR );
		case NONE_SLASH_PF_ECDH384_SERPENT256 :
			return new PF_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_ECDH384_AES256:
			return new PF_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_PF_ECDH384_TWOFISH256:
			return new PF_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR );
		case RSA_SLASH_PF_ECDH384_SERPENT256:
			return new PF_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_AES256:
			return new PF_RSA_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_TWOFISH256:
			return new PF_RSA_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Twofish256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_SERPENT256:
			return new PF_RSA_ECDHPayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_AES256plusTWOFISH256:
			return new PF_RSA_ECDHCascadePayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_AES256plusSERPENT256:
			return new PF_RSA_ECDHCascadePayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_TWOFISH256plusAES256:
			return new PF_RSA_ECDHCascadePayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_TWOFISH256plusSERPENT256:
			return new PF_RSA_ECDHCascadePayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_SERPENT256plusAES256:
			return new PF_RSA_ECDHCascadePayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_SERPENT256plusTWOFISH256:
			return new PF_RSA_ECDHCascadePayloadEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_AES256_SLASH_AES256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_AES256_SLASH_TWOFISH256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_AES256_SLASH_SERPENT256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_TWOFISH256_SLASH_AES256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_TWOFISH256_SLASH_TWOFISH256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_TWOFISH256_SLASH_SERPENT256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_SERPENT256_SLASH_AES256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_SERPENT256_SLASH_TWOFISH256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_SERPENT256_SLASH_SERPENT256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, false, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_AES256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_TWOFISH256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_SERPENT256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_AES256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_AES256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_SERPENT256:
			return new PF_ECDHContextEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_AES256plusTWOFISH256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_AES256plusSERPENT256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_TWOFISH256plusAES256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_TWOFISH256plusSERPENT256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_SERPENT256plusAES256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_SERPENT256plusTWOFISH256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_AES256plusTWOFISH256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_AES256plusSERPENT256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256plusAES256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256plusSERPENT256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256plusAES256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256plusTWOFISH256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_AES256plusTWOFISH256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_AES256plusSERPENT256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256plusAES256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256plusSERPENT256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_SERPENT256plusAES256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_SERPENT256plusTWOFISH256:
			return new PF_ECDHContextCascadeEncrypter(ownSigningKey, otherSigningKey, passphrase, encodedSessionKey, bufferFactory, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
 		}
		throw new CryptoException( CryptoResultCode.UNKNOWN_CRYPTOSCHEME_NAME );
	}
	
	/**
	 * 
	 * @param scheme
	 * @param sessionKey
	 * @param encryptionContext
	 * @return
	 * @throws CryptoException
	 */
	public Decrypter getPlainDecrypter( CryptoScheme scheme, byte[] passphrase, byte[] sessionKey ) throws CryptoException {
		if ( scheme == null || passphrase == null || sessionKey == null ) { // encryptionContext could be null depending on the scheme
			throw new IllegalArgumentException();
		}
		switch ( scheme ) {
		case NONE_SLASH_PF_AES256 :
			return new PF_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Aes256_CTR );
		case NONE_SLASH_PF_TWOFISH256 :
			return new PF_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Twofish256_CTR );
		case NONE_SLASH_PF_SERPENT256 :
			return new PF_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Serpent256_CTR );
		case NONE_SLASH_AES256 :
			return new SK_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, StreamCipherAlgorithm.Aes256_CTR );
		case NONE_SLASH_TWOFISH256 :
			return new SK_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, StreamCipherAlgorithm.Twofish256_CTR );
		case NONE_SLASH_SERPENT256 :
			return new SK_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_AES256 :
			return new PF_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_PF_TWOFISH256 :
			return new PF_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR );
		case RSA_SLASH_PF_SERPENT256 :
			return new PF_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_AES256 :
			return new PGP_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_TWOFISH256 :
			return new PGP_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Twofish256_CTR );
		case RSA_SLASH_SERPENT256 :
			return new PGP_PayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Serpent256_CTR );
		}
		throw new CryptoException( CryptoResultCode.UNKNOWN_CRYPTOSCHEME_NAME );
	}
	
	/**
	 * 
	 * @param scheme
	 * @param sessionKey
	 * @param encryptionContext
	 * @return
	 * @throws CryptoException
	 */
	public Decrypter getECDHDecrypter( CryptoScheme scheme, byte[] passphrase, KeyPair sessionKey ) throws CryptoException {
		if ( scheme == null || passphrase == null || sessionKey == null ) { // encryptionContext could be null depending on the scheme
			throw new IllegalArgumentException();
		}
		switch ( scheme ) {
		case NONE_SLASH_PF_ECDH384_AES256 :
			return new PF_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Aes256_CTR );
		case NONE_SLASH_PF_ECDH384_TWOFISH256 :
			return new PF_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Twofish256_CTR );
		case NONE_SLASH_PF_ECDH384_SERPENT256 :
			return new PF_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_ECDH384_AES256:
			return new PF_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_PF_ECDH384_TWOFISH256:
			return new PF_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR );
		case RSA_SLASH_PF_ECDH384_SERPENT256:
			return new PF_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_AES256:
			return new PF_RSA_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_TWOFISH256:
			return new PF_RSA_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Twofish256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_SERPENT256:
			return new PF_RSA_ECDHPayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_AES256plusTWOFISH256:
			return new PF_RSA_ECDHCascadePayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_AES256plusSERPENT256:
			return new PF_RSA_ECDHCascadePayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_TWOFISH256plusAES256:
			return new PF_RSA_ECDHCascadePayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_TWOFISH256plusSERPENT256:
			return new PF_RSA_ECDHCascadePayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_SERPENT256plusAES256:
			return new PF_RSA_ECDHCascadePayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case RSA_SLASH_PF_RSA_ECDH384_SERPENT256plusTWOFISH256:
			return new PF_RSA_ECDHCascadePayloadDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_AES256_SLASH_AES256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_AES256_SLASH_TWOFISH256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_AES256_SLASH_SERPENT256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_TWOFISH256_SLASH_AES256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_TWOFISH256_SLASH_TWOFISH256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_TWOFISH256_SLASH_SERPENT256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_SERPENT256_SLASH_AES256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_SERPENT256_SLASH_TWOFISH256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_SERPENT256_SLASH_SERPENT256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, false, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_AES256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_TWOFISH256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_SERPENT256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_AES256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_AES256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_SERPENT256:
			return new PF_ECDHContextDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_AES256plusTWOFISH256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_AES256plusSERPENT256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_TWOFISH256plusAES256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_TWOFISH256plusSERPENT256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_SERPENT256plusAES256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_AES256plusRSA_SLASH_SERPENT256plusTWOFISH256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_AES256plusTWOFISH256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_AES256plusSERPENT256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256plusAES256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256plusSERPENT256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256plusAES256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256plusTWOFISH256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_AES256plusTWOFISH256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_AES256plusSERPENT256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256plusAES256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256plusSERPENT256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR, StreamCipherAlgorithm.Serpent256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_SERPENT256plusAES256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Aes256_CTR );
		case PF_ECDH384_SERPENT256plusRSA_SLASH_SERPENT256plusTWOFISH256:
			return new PF_ECDHContextCascadeDecrypter(ownSigningKey, otherSigningKey, passphrase, sessionKey, true, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Serpent256_CTR, StreamCipherAlgorithm.Twofish256_CTR );
		}
		throw new CryptoException( CryptoResultCode.UNKNOWN_CRYPTOSCHEME_NAME );
	}
}
