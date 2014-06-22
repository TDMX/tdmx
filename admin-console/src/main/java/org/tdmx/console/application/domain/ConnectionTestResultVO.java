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
package org.tdmx.console.application.domain;

import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * A Result of a ConnectionTest of a ServiceProvider's interface.
 * 
 * @author Peter
 * 
 */
public class ConnectionTestResultVO implements ValueObject {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private String url;
	private X509Certificate[] serverCertificateChain;
	private CertificateStatus trustStatus;
	private ConnectionStatus status;
	private Date testedDate;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public <E extends ValueObject> E copy() {
		// TODO
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public X509Certificate[] getServerCertificateChain() {
		return serverCertificateChain;
	}

	public void setServerCertificateChain(X509Certificate[] serverCertificateChain) {
		this.serverCertificateChain = serverCertificateChain;
	}

	public CertificateStatus getTrustStatus() {
		return trustStatus;
	}

	public void setTrustStatus(CertificateStatus trustStatus) {
		this.trustStatus = trustStatus;
	}

	public ConnectionStatus getStatus() {
		return status;
	}

	public void setStatus(ConnectionStatus status) {
		this.status = status;
	}

	public Date getTestedDate() {
		return testedDate;
	}

	public void setTestedDate(Date testedDate) {
		this.testedDate = testedDate;
	}
}
