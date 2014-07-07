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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class KeyStoreUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private KeyStoreUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public static PKIXCredential getPrivateCredential(byte[] keystoreContents, String storeType, String storePassword,
			String alias) throws CryptoCertificateException {

		try {
			KeyStore store = loadKeyStore(keystoreContents, storeType, storePassword);

			PrivateKeyEntry pke = (PrivateKeyEntry) store.getEntry(alias,
					new KeyStore.PasswordProtection(storePassword.toCharArray()));

			PrivateKey key = pke.getPrivateKey();

			X509Certificate[] certChain = (X509Certificate[]) pke.getCertificateChain();
			PKIXCertificate[] certs = new PKIXCertificate[certChain.length];
			for (int i = 0; i < certChain.length; i++) {
				certs[i] = new PKIXCertificate(certChain[i]);
			}

			return new PKIXCredential(certs, key);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_ALGORITHM, e);
		} catch (UnrecoverableEntryException | KeyStoreException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_KEYSTORE_EXCEPTION, e);
		} catch (CertificateException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
		} catch (NoSuchProviderException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_PROVIDER, e);
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}
	}

	public static byte[] saveKeyStore(PKIXCredential credential, String storeType, String storePassword, String alias)
			throws CryptoCertificateException {

		try {
			KeyStore keystore = KeyStore.getInstance(storeType);
			keystore.load(null, storePassword.toCharArray());
			X509Certificate[] certChain = new X509Certificate[credential.getCertificateChain().length];
			for (int i = 0; i < certChain.length; i++) {
				certChain[i] = credential.getCertificateChain()[i].getCertificate();
			}
			keystore.setKeyEntry(alias, credential.getPrivateKey(), storePassword.toCharArray(), certChain);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			keystore.store(baos, storePassword.toCharArray());
			return baos.toByteArray();
		} catch (KeyStoreException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_KEYSTORE_EXCEPTION, e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_ALGORITHM, e);
		} catch (CertificateException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}

	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private static KeyStore loadKeyStore(byte[] keystoreContents, String storeType, String storePassword)
			throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException,
			NoSuchProviderException {

		InputStream inStream = new ByteArrayInputStream(keystoreContents);
		try {
			KeyStore keystore = KeyStore.getInstance(storeType);
			keystore.load(inStream, storePassword == null ? null : storePassword.toCharArray());
			return keystore;
		} finally {
			inStream.close();
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
