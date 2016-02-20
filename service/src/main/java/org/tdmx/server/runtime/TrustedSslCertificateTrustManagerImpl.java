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
package org.tdmx.server.runtime;

import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.TrustStatus;
import org.tdmx.lib.control.domain.TrustedSslCertificate;
import org.tdmx.lib.control.service.TrustedSslCertificateService;
import org.tdmx.server.pcs.CacheInvalidationListener;

public class TrustedSslCertificateTrustManagerImpl implements X509TrustManager, CacheInvalidationListener {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(TrustedSslCertificateTrustManagerImpl.class);

	private TrustedSslCertificateService trustedCertificateService;

	// internal
	private Map<ByteBuffer, PKIXCertificate> distrustedCerts;
	private X509TrustManager delegateTrustManager = null;
	
	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void invalidateCache(String key) {
		if (TrustedSslCertificateService.CACHE_KEY.equals(key)) {
			log.debug("Invalidating cache " + key);
			loadCerts();
		}
	}

	public void init() {
		loadCerts();
	}
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		checkDistrusted( chain );
		if ( delegateTrustManager != null ) { 
			delegateTrustManager.checkClientTrusted(chain, authType);
		} else {
			log.warn("No delegateTrustManager.");
		}
	}


	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		checkDistrusted( chain );
		if ( delegateTrustManager != null ) { 
			delegateTrustManager.checkServerTrusted(chain, authType);
		} else {
			log.warn("No delegateTrustManager.");
		}
	}


	@Override
	public X509Certificate[] getAcceptedIssuers() {
		if ( delegateTrustManager != null ) { 
			return delegateTrustManager.getAcceptedIssuers();
		} else {
			log.warn("No delegateTrustManager.");
		}
		return new X509Certificate[0];
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
	private void checkDistrusted(X509Certificate[] chain) throws CertificateException {
		for( X509Certificate cert : chain ) {
			PKIXCertificate badCert = distrustedCerts.get(ByteBuffer.wrap(cert.getEncoded()));
			if ( badCert != null ) {
				throw new CertificateException("Use of untrusted certificate [" + badCert.getSubject()+ "]");
			}
		}
	}
	
	private synchronized void loadCerts() {
			List<TrustedSslCertificate> allCerts = trustedCertificateService.findAll();
			
			Map<ByteBuffer,PKIXCertificate> distrusted = new HashMap<>();
			List<PKIXCertificate> trusted = new ArrayList<>();
			
			for( TrustedSslCertificate cert : allCerts ) {
				if ( TrustStatus.DISTRUSTED == cert.getTrustStatus() ) {
					distrusted.put(ByteBuffer.wrap(cert.getCertificate().getX509Encoded()),cert.getCertificate());
				} else if ( TrustStatus.TRUSTED == cert.getTrustStatus() ) {
					trusted.add(cert.getCertificate());
				}
			}
			
			distrustedCerts = distrusted;
			try {
				KeyStore keyStore = KeyStoreUtils.createTrustStore(trusted.toArray(new PKIXCertificate[0]), "jks");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				tmf.init(keyStore);
				TrustManager[] tms = tmf.getTrustManagers();
				for (int i = 0; tms != null && i < tms.length; i++) {
					if (tms[i] instanceof X509TrustManager) {
						delegateTrustManager = (X509TrustManager) tms[i];
						break;
					}
				}
			} catch (KeyStoreException | NoSuchAlgorithmException e) {
				log.warn("Unable to initialize TrustManager.", e);
			} catch (CryptoCertificateException e) {
				log.warn("Unable to initialize KeyStore for use by TrustManager.", e);
			}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public TrustedSslCertificateService getTrustedCertificateService() {
		return trustedCertificateService;
	}

	public void setTrustedCertificateService(TrustedSslCertificateService trustedCertificateService) {
		this.trustedCertificateService = trustedCertificateService;
	}


}
