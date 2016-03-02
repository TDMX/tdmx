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
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(KeyStoreUtils.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private KeyStoreUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Return trusted CA certs of the keystore provided.
	 * 
	 * @param keystoreContents
	 *            the keystore contents.
	 * @param storeType
	 *            the keystore type.
	 * @param storePassword
	 *            the keystore passphrase.
	 * @return the trusted CA certs, or emtpy list if there were problems.
	 * @throws CryptoCertificateException
	 */
	public static PKIXCertificate[] getTrustedCertificates(byte[] keystoreContents, String storeType,
			String storePassword) throws CryptoCertificateException {
		List<X509Certificate> certs = new ArrayList<>();
		try {
			KeyStore store = loadKeyStore(keystoreContents, storeType, storePassword);

			Enumeration<String> aliases = store.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				if (store.isCertificateEntry(alias)) {
					Certificate c = store.getCertificate(alias);
					if (c instanceof X509Certificate) {
						certs.add((X509Certificate) c);
					}
				}
			}
			PKIXCertificate[] result = new PKIXCertificate[certs.size()];
			for (int i = 0; i < result.length; i++) {
				result[i] = new PKIXCertificate(certs.get(i));
			}

			return result;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_ALGORITHM, e);
		} catch (KeyStoreException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_KEYSTORE_EXCEPTION, e);
		} catch (CertificateException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
		} catch (NoSuchProviderException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_PROVIDER, e);
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}
	}

	/**
	 * Return trusted CA certs of the keystore provided.
	 * 
	 * @param keystoreContents
	 *            the keystore contents.
	 * @param storeType
	 *            the keystore type.
	 * @param storePassword
	 *            the keystore passphrase.
	 * @param alias
	 *            the certificate alias
	 * @return the trusted CA cert with the alias.
	 * @throws CryptoCertificateException
	 */
	public static PKIXCertificate getTrustedCertificate(byte[] keystoreContents, String storeType, String storePassword,
			String alias) throws CryptoCertificateException {
		try {
			KeyStore store = loadKeyStore(keystoreContents, storeType, storePassword);

			if (store.isCertificateEntry(alias)) {
				Certificate c = store.getCertificate(alias);
				if (c instanceof X509Certificate) {
					return new PKIXCertificate((X509Certificate) c);
				}
			}
			return null;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_ALGORITHM, e);
		} catch (KeyStoreException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_KEYSTORE_EXCEPTION, e);
		} catch (CertificateException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
		} catch (NoSuchProviderException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_PROVIDER, e);
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}
	}

	public static KeyStore createTrustStore(PKIXCertificate[] trustedCerts, String storeType)
			throws CryptoCertificateException {
		try {
			KeyStore ks = KeyStore.getInstance(storeType);
			ks.load(null);
			for (int i = 0; trustedCerts != null && i < trustedCerts.length; i++) {
				ks.setCertificateEntry("cert[" + i + "]", trustedCerts[i].getCertificate());
			}
			return ks;
		} catch (KeyStoreException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_KEYSTORE_EXCEPTION, e);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			log.warn("Unexpected problem creating TrustStore.", e);
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
		}
	}

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
