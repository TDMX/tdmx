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

public class ZoneAdministrationCredentialSpecifier {
	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final TdmxZoneInfo zoneInfo;

	private String cn;
	private String telephoneNumber;
	private String emailAddress;
	private String orgUnit;
	private String org;
	private String location;
	private String country;

	private Calendar notBefore;
	private Calendar notAfter;
	private PublicKeyAlgorithm keyAlgorithm;
	private SignatureAlgorithm signatureAlgorithm;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	// TODO remove mrsUrl add serial#
	public ZoneAdministrationCredentialSpecifier(int version, String zoneRoot) {
		if (!StringUtils.hasText(zoneRoot)) {
			throw new IllegalArgumentException("Missing zoneRoot.");
		}
		if (!StringUtils.isLowerCase(zoneRoot)) {
			throw new IllegalArgumentException("Uppercase zoneRoot.");
		}
		this.zoneInfo = new TdmxZoneInfo(version, zoneRoot, TdmxCertificateType.ZAC);
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

	public TdmxZoneInfo getZoneInfo() {
		return zoneInfo;
	}

	public PublicKeyAlgorithm getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(PublicKeyAlgorithm keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getOrgUnit() {
		return orgUnit;
	}

	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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
