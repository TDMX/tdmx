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
package org.tdmx.lib.zone.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.tdmx.lib.control.domain.AuthorizedAgent;

/**
 * A ZoneCredential is the PublicKey and status of an ZoneAdministrator.
 * 
 * The ZoneCredential (ZAC) is identified by it's SHA1 fingerprint of the public certificate. For each ZAC in any
 * ZoneDB, there is a corresponding {@link AuthorizedAgent} in the centralized ControlDB.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "ZoneCredential")
public class ZoneCredential implements Serializable {

	public static final int MAX_SHA1FINGERPRINT_LEN = 64;
	public static final int MAX_CERTIFICATE_LEN = 4000;

	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@Column(length = MAX_SHA1FINGERPRINT_LEN)
	private String sha1fingerprint;

	@Enumerated(EnumType.STRING)
	@Column(length = CredentialType.MAX_CREDENTIALTYPE_LEN, nullable = false)
	private CredentialType credentialType;

	@Enumerated(EnumType.STRING)
	@Column(length = CredentialStatus.MAX_CREDENTIALSTATUS_LEN, nullable = false)
	private CredentialStatus credentialStatus;

	@Column(length = Zone.MAX_NAME_LEN, nullable = false)
	private String name;

	@Column(length = MAX_CERTIFICATE_LEN, nullable = false)
	private String certificateChainPem;

	public String getSha1fingerprint() {
		return sha1fingerprint;
	}

	public void setSha1fingerprint(String sha1fingerprint) {
		this.sha1fingerprint = sha1fingerprint;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CredentialType getCredentialType() {
		return credentialType;
	}

	public void setCredentialType(CredentialType credentialType) {
		this.credentialType = credentialType;
	}

	public CredentialStatus getCredentialStatus() {
		return credentialStatus;
	}

	public void setCredentialStatus(CredentialStatus credentialStatus) {
		this.credentialStatus = credentialStatus;
	}

	public String getCertificateChainPem() {
		return certificateChainPem;
	}

	public void setCertificateChainPem(String certificateChainPem) {
		this.certificateChainPem = certificateChainPem;
	}

}
