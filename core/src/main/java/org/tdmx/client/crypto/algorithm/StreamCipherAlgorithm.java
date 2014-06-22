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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public enum StreamCipherAlgorithm {

	Aes256_CTR("AES", "AES/CTR/NoPadding", 32, 16),
	Twofish256_CTR("Twofish", "Twofish/CTR/NoPadding", 32, 16),
	Serpent256_CTR("Serpent", "Serpent/CTR/NoPadding", 32, 16), ;

	private String algorithm;
	private String transform;
	private int keyLength;
	private int ivLength;

	private StreamCipherAlgorithm(String algorithm, String transform, int keyLength, int ivLength) {
		this.algorithm = algorithm;
		this.transform = transform;
		this.keyLength = keyLength;
		this.ivLength = ivLength;
	}

	public SecretKeySpec convertKey(byte[] key) throws CryptoException {
		if (key == null || key.length != getKeyLength()) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_KEY_INVALID);
		}
		return new SecretKeySpec(key, algorithm);
	}

	public SecretKeySpec generateNewKey() throws CryptoException {
		byte[] key = EntropySource.getRandomBytes(keyLength);
		return convertKey(key);
	}

	public IvParameterSpec convertIv(byte[] iv) throws CryptoException {
		if (iv == null || iv.length != getIvLength()) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_IV_INVALID);
		}
		return new IvParameterSpec(iv);
	}

	public IvParameterSpec generateNewIv() throws CryptoException {
		byte[] iv = EntropySource.getRandomBytes(ivLength);
		return convertIv(iv);
	}

	public Cipher getEncrypter(SecretKeySpec key, IvParameterSpec iv) throws CryptoException {
		try {
			Cipher cipher = Cipher.getInstance(transform);
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			return cipher;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_ALGORITHM_MISSING, e);
		} catch (NoSuchPaddingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_PADDING_MISSING, e);
		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_KEY_INVALID, e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_ALGORITHM_PARAMETER_INVALID, e);
		}
	}

	public byte[] encrypt(SecretKeySpec key, IvParameterSpec iv, byte[] plaintext) throws CryptoException {
		try {
			Cipher cipher = Cipher.getInstance(transform);
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			return cipher.doFinal(plaintext);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_ALGORITHM_MISSING, e);
		} catch (NoSuchPaddingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_PADDING_MISSING, e);
		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_KEY_INVALID, e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_ALGORITHM_PARAMETER_INVALID, e);
		} catch (IllegalBlockSizeException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_BLOCKSIZE_INVALID, e);
		} catch (BadPaddingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_PADDING_INVALID, e);
		}
	}

	public Cipher getDecrypter(SecretKeySpec key, IvParameterSpec iv) throws CryptoException {
		try {
			Cipher cipher = Cipher.getInstance(transform);
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			return cipher;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_ALGORITHM_MISSING, e);
		} catch (NoSuchPaddingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_PADDING_MISSING, e);
		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_KEY_INVALID, e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_ALGORITHM_PARAMETER_INVALID, e);
		}
	}

	public byte[] decrypt(SecretKeySpec key, IvParameterSpec iv, byte[] ciphertext) throws CryptoException {
		try {
			Cipher cipher = Cipher.getInstance(transform);
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			return cipher.doFinal(ciphertext);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_ALGORITHM_MISSING, e);
		} catch (NoSuchPaddingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_PADDING_MISSING, e);
		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_KEY_INVALID, e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_ALGORITHM_PARAMETER_INVALID, e);
		} catch (IllegalBlockSizeException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_BLOCKSIZE_INVALID, e);
		} catch (BadPaddingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SK_PADDING_INVALID, e);
		}
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	public String getTransform() {
		return this.transform;
	}

	public int getKeyLength() {
		return keyLength;
	}

	public int getIvLength() {
		return ivLength;
	}

}
