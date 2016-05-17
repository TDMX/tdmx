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
package org.tdmx.client.adapter;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;

public class ServerTrustManagerFactoryImpl implements ServerTrustManagerFactory {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ServerTrustManagerFactoryImpl.class);

	private TrustedServerCertificateProvider certificateProvider;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public X509TrustManager getTrustManager() {
		PKIXCertificate[] trustedCertificates = certificateProvider.getTrustedCertificates();

		try {
			KeyStore keyStore = KeyStoreUtils.createTrustStore(trustedCertificates, "jks");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStore);
			TrustManager[] tms = tmf.getTrustManagers();
			for (int i = 0; tms != null && i < tms.length; i++) {
				if (tms[i] instanceof X509TrustManager) {
					return (X509TrustManager) tms[i];
				}
			}
		} catch (KeyStoreException | NoSuchAlgorithmException e) {
			log.warn("Unable to initialize TrustManager.", e);
		} catch (CryptoCertificateException e) {
			log.warn("Unable to initialize KeyStore for use by TrustManager.", e);
		}

		return null;
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

	public TrustedServerCertificateProvider getCertificateProvider() {
		return certificateProvider;
	}

	public void setCertificateProvider(TrustedServerCertificateProvider certificateProvider) {
		this.certificateProvider = certificateProvider;
	}

}
