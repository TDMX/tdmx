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
package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.Date;

import org.tdmx.console.application.domain.CertificateStatus;
import org.tdmx.console.application.domain.ConnectionStatus;

public class ConnectionTestResult implements Serializable {

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url;
	private String certificateChainId;
	private CertificateStatus trustStatus;
	private ConnectionStatus connectionStatus;
	private String subject;
	private Date testedDate;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ConnectionTestResult(org.tdmx.console.application.domain.ConnectionTestResultVO o) {
	}

	public ConnectionTestResult() {
	}

	public org.tdmx.console.application.domain.ConnectionTestResultVO domain() {
		org.tdmx.console.application.domain.ConnectionTestResultVO o = new org.tdmx.console.application.domain.ConnectionTestResultVO();
		return o;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
