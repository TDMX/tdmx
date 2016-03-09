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

import java.io.OutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.algorithm.KeyAgreementAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.algorithm.StreamCipherAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryBufferFactory;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.converters.NumberToOctetString;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoContext;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.Encrypter;
import org.tdmx.client.crypto.stream.ChunkMacCalculatingOutputStream;
import org.tdmx.client.crypto.stream.FileBackedOutputStream;
import org.tdmx.client.crypto.stream.SigningOutputStream;

/**
 * <pre>
 * encryption( M, PF, (K-A,K-a), K-B, A-B ) -> E, L
 * {
 * This scheme passes the secret keys used to decrypt the message payload to the 
 * destination in encrypted form in the encryption-context. 
 * 
 * validate A-B must be a 384bit X.509 encoded EC sessionKey.
 * EC key generate (A-A,A-a), an EC keypair on secp384r1
 * ECDH key agreement (A-a,A-B) => shared secret S
 * PFS := SHA384(PF) - convert the PF into a shared secret.
 * SKk || IVk:= SHA384(A-B||S||PFS), 
 *   where SKk is a 256bit AES encryption key, 
 *   IVk is a 128bit initialization vector for the AES encryption
 * SKe1 := PRNG(256bit)
 * IVe1 := PRNG(128bit)
 * SKe2 := PRNG(256bit)
 * IVe2 := PRNG(128bit)
 * E := Twofish256/CTR(SKe2,IVe2,AES256/CTR(SKe1,IVe1,ZLib-compress(M||Sign(K-a,M||long-byte-len(M)))))
 * L := long-byte-len(M) || short-byte-len(A-A) || A-A || AES256/CTR(SKk,IVk, SKe1 || IVe1 || SKe2 || IVe2 )
 *   where long-byte-len(M) is the length of M in bytes represented as 8-byte fixed length big-endian integer, and short-byte-len is a single byte representing the length of A-A in bytes.
 *   A-A is a X.509 encoded EC public key - aka the sender’s messageKey
 * }
 * </pre>
 * 
 * @author Peter
 * 
 */
public class PF_ECDHContextCascadeEncrypter implements Encrypter {

	private final TemporaryBufferFactory bufferFactory;

	private final KeyPair ownSigningKey;
	private final PublicKey otherSigningKey;
	private final PublicKey sessionKey;
	private final KeyPair messageKey;
	private final boolean rsaEnabled;

	private final StreamCipherAlgorithm keyEncryptionCipher;
	private final SecretKeySpec keyEncryptionKey;
	private final IvParameterSpec keyEncryptionIv;

	private FileBackedOutputStream fbos = null;
	private ChunkMacCalculatingOutputStream mcos = null;

	private final StreamCipherAlgorithm innerPayloadCipher;
	private final byte[] innerPayloadSecretKey;
	private final byte[] innerPayloadSecretIv;
	private final StreamCipherAlgorithm outerPayloadCipher;
	private final byte[] outerPayloadSecretKey;
	private final byte[] outerPayloadSecretIv;

	public PF_ECDHContextCascadeEncrypter(KeyPair ownSigningKey, PublicKey otherSigningKey, byte[] passphrase,
			byte[] encodedSessionKey, TemporaryBufferFactory bufferFactory, boolean rsaEnabled,
			StreamCipherAlgorithm keyEncryptionCipher, StreamCipherAlgorithm innerPayloadCipher,
			StreamCipherAlgorithm outerPayloadCipher) throws CryptoException {
		this.bufferFactory = bufferFactory;
		this.rsaEnabled = rsaEnabled;

		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;

		this.keyEncryptionCipher = keyEncryptionCipher;
		this.innerPayloadCipher = innerPayloadCipher;
		this.outerPayloadCipher = outerPayloadCipher;

		this.sessionKey = KeyAgreementAlgorithm.ECDH384.decodeX509EncodedPublicKey(encodedSessionKey);
		this.messageKey = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();

		byte[] sharedSecret = KeyAgreementAlgorithm.ECDH384.agreeKey(messageKey, sessionKey);

		byte[] passphraseSecret = DigestAlgorithm.SHA_384.kdf(passphrase);

		byte[] kdf = DigestAlgorithm.SHA_384.kdf(ByteArray.append(encodedSessionKey, sharedSecret, passphraseSecret));

		byte[] aesKey = ByteArray.subArray(kdf, 0, keyEncryptionCipher.getKeyLength());
		byte[] aesIv = ByteArray.subArray(kdf, keyEncryptionCipher.getKeyLength(), keyEncryptionCipher.getIvLength());

		keyEncryptionKey = new SecretKeySpec(aesKey, keyEncryptionCipher.getAlgorithm());
		keyEncryptionIv = new IvParameterSpec(aesIv);

		// TODO System.out.println("KeyEncryption KEY: " + ByteArray.asHex(aesKey));
		// TODO System.out.println("KeyEncryption IV: " + ByteArray.asHex(aesIv));

		innerPayloadSecretKey = EntropySource.getRandomBytes(innerPayloadCipher.getKeyLength());
		innerPayloadSecretIv = EntropySource.getRandomBytes(innerPayloadCipher.getIvLength());

		// TODO System.out.println("Inner Payload KEY: " + ByteArray.asHex(innerPayloadSecretKey));
		// TODO System.out.println("Inner Payload IV: " + ByteArray.asHex(innerPayloadSecretKey));

		outerPayloadSecretKey = EntropySource.getRandomBytes(outerPayloadCipher.getKeyLength());
		outerPayloadSecretIv = EntropySource.getRandomBytes(outerPayloadCipher.getIvLength());

		// TODO System.out.println("Outer Payload KEY: " + ByteArray.asHex(outerPayloadSecretKey));
		// TODO System.out.println("Outer Payload IV: " + ByteArray.asHex(outerPayloadSecretIv));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.tdmx.client.crypto.scheme.Encrypter#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws CryptoException {
		if (fbos != null) {
			throw new IllegalStateException();
		}
		fbos = bufferFactory.getOutputStream();

		SecretKeySpec innerPayloadKey = innerPayloadCipher.convertKey(innerPayloadSecretKey);
		IvParameterSpec innerPayloadIv = innerPayloadCipher.convertIv(innerPayloadSecretIv);

		Cipher ic = innerPayloadCipher.getEncrypter(innerPayloadKey, innerPayloadIv);
		CipherOutputStream icos = new CipherOutputStream(fbos, ic);

		SecretKeySpec outerPayloadKey = outerPayloadCipher.convertKey(outerPayloadSecretKey);
		IvParameterSpec outerPayloadIv = outerPayloadCipher.convertIv(outerPayloadSecretIv);

		Cipher oc = outerPayloadCipher.getEncrypter(outerPayloadKey, outerPayloadIv);
		CipherOutputStream ocos = new CipherOutputStream(icos, oc);

		DeflaterOutputStream zos = new DeflaterOutputStream(ocos, new Deflater(Deflater.DEFAULT_COMPRESSION, false),
				512, false);

		SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_384_RSA, ownSigningKey.getPrivate(),
				true, true, zos);

		mcos = new ChunkMacCalculatingOutputStream(sos, bufferFactory.getChunkSize());
		return mcos;
	}

	@Override
	public CryptoContext getResult() throws CryptoException {
		if (fbos == null) {
			throw new IllegalStateException();
		}
		if (!fbos.isClosed()) {
			throw new IllegalStateException();
		}
		byte[] msgKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(messageKey.getPublic());
		byte[] msgKeyLen = NumberToOctetString.intToByte(msgKey.length);
		byte[] plaintextLengthBytes = NumberToOctetString.longToBytes(mcos.getSize());
		byte[] payloadKeyBytes = ByteArray.append(innerPayloadSecretKey, innerPayloadSecretIv, outerPayloadSecretKey,
				outerPayloadSecretIv);

		if (rsaEnabled) {
			AsymmetricEncryptionAlgorithm rsa = AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey(otherSigningKey);
			payloadKeyBytes = rsa.encrypt(otherSigningKey, payloadKeyBytes);
		}
		byte[] encryptedKey = keyEncryptionCipher.encrypt(keyEncryptionKey, keyEncryptionIv, payloadKeyBytes);
		byte[] encryptionContext = ByteArray.append(plaintextLengthBytes, msgKeyLen, msgKey, encryptedKey);

		CryptoContext cc = new CryptoContext(fbos.getInputStream(), encryptionContext, mcos.getSize(), fbos.getSize(),
				mcos.getChunkSize(), mcos.getMacs());
		return cc;
	}

}
