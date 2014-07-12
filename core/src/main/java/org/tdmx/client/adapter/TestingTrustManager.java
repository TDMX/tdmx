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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Trust's all remote server certificates, and tests the remote server with the delegate TrustManager providing the test
 * result after connection testing with {@link #getTestResult()}
 * 
 * @author Peter
 * 
 */
class TestingTrustManager implements X509TrustManager {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final X509TrustManager delegateTrustManager;
	private X509Certificate[] serverCertificate;
	private CertificateException trustException;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public TestingTrustManager(X509TrustManager delegateTrustManager) {
		this.delegateTrustManager = delegateTrustManager;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		throw new UnsupportedOperationException();
	}

	@Override
	/**
	 * Check the supplied server's certificate chain with the delegate TrustManager and record the result for later analysis.
	 */
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		this.serverCertificate = chain;
		if (delegateTrustManager != null) {
			try {
				delegateTrustManager.checkServerTrusted(chain, authType);
			} catch (CertificateException e) {
				trustException = e;
			}
		}
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

	public X509Certificate[] getServerCertificate() {
		return serverCertificate;
	}

	public CertificateException getTrustException() {
		return trustException;
	}

}