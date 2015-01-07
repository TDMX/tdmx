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

import java.util.Calendar;

import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.core.system.lang.StringUtils;

public class DomainAdministrationCredentialSpecifier {
	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final PKIXCredential zoneAdministratorCredential;

	private final String domainName;

	private Calendar notBefore;
	private Calendar notAfter;
	private PublicKeyAlgorithm keyAlgorithm;
	private SignatureAlgorithm signatureAlgorithm;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	/**
	 * Construct a DomainAdministrationCredentialSpecifier for a Domain.
	 * 
	 * @param subdomainName
	 *            will be normalized to uppercase and prefixed onto the issuing zoneApex.
	 */
	public DomainAdministrationCredentialSpecifier(String subdomainName, PKIXCredential zoneAdministratorCredential) {
		if (!StringUtils.hasText(subdomainName)) {
			throw new IllegalArgumentException("Missing domainName.");
		}
		if (!StringUtils.isLowerCase(subdomainName)) {
			throw new IllegalArgumentException("Uppercase domainName.");
		}
		if (zoneAdministratorCredential == null || zoneAdministratorCredential.getPublicCert() == null) {
			throw new IllegalArgumentException("Missing ZAC.");
		}
		if (!zoneAdministratorCredential.getPublicCert().isTdmxZoneAdminCertificate()) {
			throw new IllegalArgumentException("DAC must be specified using a ZAC.");
		}
		PKIXCertificate issuer = zoneAdministratorCredential.getPublicCert();
		String zoneApex = issuer.getTdmxZoneInfo().getZoneRoot();
		this.domainName = subdomainName + "." + zoneApex;
		this.zoneAdministratorCredential = zoneAdministratorCredential;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public PKIXCredential getZoneAdministratorCredential() {
		return zoneAdministratorCredential;
	}

	public String getDomainName() {
		return domainName;
	}

	public PublicKeyAlgorithm getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(PublicKeyAlgorithm keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public Calendar getNotBefore() {
		return notBefore;
	}

	public void setNotBefore(Calendar notBefore) {
		this.notBefore = notBefore;
	}

	public Calendar getNotAfter() {
		return notAfter;
	}

	public void setNotAfter(Calendar notAfter) {
		this.notAfter = notAfter;
	}

	public SignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

}
