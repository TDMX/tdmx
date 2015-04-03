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
package org.tdmx.core.system.env;

// -----------------------------------------------------------------------------
// StringEncrypter.java
// -----------------------------------------------------------------------------

/*
 * Copyright (c) 1998-2008 Jeffrey M. Hunter. All rights reserved.
 * 
 * All source code and material located at the Internet address of http://www.idevelopment.info is the copyright of
 * Jeffrey M. Hunter and is protected under copyright laws of the United States. This source code may not be hosted on
 * any other site without my express, prior, written permission. Application to host any of the material elsewhere can
 * be made by contacting me at jhunter@idevelopment.info.
 * 
 * I have made every effort and taken great care in making sure that the source code and other content included on my
 * web site is technically accurate, but I disclaim any and all responsibility for any loss, damage or destruction of
 * data or any other property which may arise from relying on it. I will in no case be liable for any monetary damages
 * arising from such loss, damage or destruction.
 * 
 * As with any code, ensure to test this code in a development environment before attempting to run it in production.
 */

// CIPHER / GENERATORS
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The StringEncrypter implements a class for encrypting and decrypting strings using several Cipher algorithms. The
 * class is created with a key and can be used repeatedly to encrypt and decrypt strings using that key. Some of the
 * more popular algorithms are: Blowfish DES DESede PBEWithMD5AndDES PBEWithMD5AndTripleDES TripleDES
 */
public class StringEncrypter {

	private Cipher ecipher;
	private Cipher dcipher;
	private SecretKey key;
	AlgorithmParameterSpec paramSpec;

	private static final Logger log = LoggerFactory.getLogger(StringEncrypter.class);

	/**
	 * Constructor used to create this object. Responsible for setting and initializing this object's encrypter and
	 * decrypter Cipher instances given a Pass Phrase and algorithm.
	 * 
	 * @param passPhrase
	 *            Pass Phrase used to initialize both the encrypter and decrypter instances.
	 */
	public StringEncrypter(String passPhrase) {

		// 8-bytes Salt
		byte[] salt = { (byte) 0xA4, (byte) 0x9B, (byte) 0xC8, (byte) 0x72, (byte) 0x46, (byte) 0x45, (byte) 0xE3,
				(byte) 0x93 };

		// Iteration count
		int iterationCount = 1024;
		try {
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
			key = SecretKeyFactory.getInstance("PBEWithSHA1AndDESede").generateSecret(keySpec);

			ecipher = Cipher.getInstance(key.getAlgorithm());
			dcipher = Cipher.getInstance(key.getAlgorithm());

			// Prepare the parameters to the ciphers
			paramSpec = new PBEParameterSpec(salt, iterationCount);

			initCipher();
		} catch (InvalidKeySpecException e) {
			log.error("EXCEPTION: InvalidKeySpecException", e);
		} catch (NoSuchPaddingException e) {
			log.error("EXCEPTION: NoSuchPaddingException", e);
		} catch (NoSuchAlgorithmException e) {
			log.error("EXCEPTION: NoSuchAlgorithmException", e);
		}
	}

	/**
	 * Takes a single String as an argument and returns an Encrypted version of that String.
	 * 
	 * @param str
	 *            String to be encrypted
	 * @return <code>String</code> Encrypted version of the provided String
	 */
	public synchronized String encrypt(String str) {
		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return new String(Base64.encodeBase64(enc), "UTF8");

		} catch (UnsupportedEncodingException e) {
			log.warn("Encrypt Exception:", e);
		} catch (Exception e) {
			log.warn("Encrypt Exception:", e);
			initCipher();
		}
		log.warn("encrypt returns null");
		return null;
	}

	/**
	 * Takes a encrypted String as an argument, decrypts and returns the decrypted String.
	 * 
	 * @param str
	 *            Encrypted String to be decrypted
	 * @return <code>String</code> Decrypted version of the provided String
	 */
	public synchronized String decrypt(String str) {
		log.info("decrypt [" + str + "]");
		try {
			// Decode base64 to get bytes
			byte[] dec = Base64.decodeBase64(str.getBytes("UTF8"));

			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);

			// Decode using utf-8
			return new String(utf8, "UTF8");

		} catch (UnsupportedEncodingException e) {
			log.warn("Decrypt Exception:", e);
		} catch (Exception e) {
			log.warn("Decrypt Exception:", e);
			initCipher();
		}
		log.warn("decrypt returns null");
		return null;
	}

	private void initCipher() {
		try {
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
		} catch (InvalidAlgorithmParameterException e) {
			log.error("initCipher InvalidAlgorithmParameterException:", e);
		} catch (InvalidKeyException e) {
			log.error("initCipher InvalidKeyException:", e);
		}

	}

}
