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
package org.tdmx.client.cli.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCredential;

/**
 * login as
 * 
 * 1) ZoneAdministrator - pwd
 * 
 * 2) DomainAdministrator - domain + pwd
 * 
 * 3) User - user@domain + pwd
 * 
 * @author Peter
 *
 */
public class AuthenticatedUserContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(AuthenticatedUserContext.class);

	private final PKIXCredential credential;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	AuthenticatedUserContext(PKIXCredential credential) {
		this.credential = credential;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public boolean isUC() {
		return credential != null ? credential.getPublicCert().isTdmxUserCertificate() : false;
	}

	public boolean isDAC() {
		return credential != null ? credential.getPublicCert().isTdmxDomainAdminCertificate() : false;
	}

	public boolean isZAC() {
		return credential != null ? credential.getPublicCert().isTdmxZoneAdminCertificate() : false;
	}

	public String getDomain() {
		return credential != null ? credential.getPublicCert().getTdmxDomainName() : null;
	}

	public String getUserName() {
		return credential != null ? credential.getPublicCert().getTdmxUserName() : null;
	}

	public String getZoneApex() {
		return credential != null ? credential.getZoneRootPublicCert().getTdmxZoneInfo().getZoneRoot() : null;
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

	public PKIXCredential getCredential() {
		return credential;
	}

}
