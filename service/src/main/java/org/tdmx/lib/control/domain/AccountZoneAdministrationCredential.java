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
package org.tdmx.lib.control.domain;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;

/**
 * An AgentCredential is the public certificate of a ZAC, DAC or UC.
 * 
 * The AgentCredential is identified by it's SHA1 fingerprint of the public certificate ( first in chain ).
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "AccountZoneAdministrationCredential")
public class AccountZoneAdministrationCredential implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_SHA1FINGERPRINT_LEN = 64;
	public static final int MAX_CERTIFICATECHAIN_LEN = 12000;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -988419614813872556L;

	@Id
	private AccountZoneAdministrationCredentialID id;

	@Enumerated(EnumType.STRING)
	@Column(length = AccountZoneAdministrationCredentialStatus.MAX_CREDENTIALSTATUS_LEN, nullable = false)
	private AccountZoneAdministrationCredentialStatus credentialStatus;

	@Column(length = MAX_CERTIFICATECHAIN_LEN, nullable = false)
	private String certificateChainPem;

	@Column(length = AccountZone.MAX_ZONEAPEX_LEN, nullable = false)
	private String zoneApex;

	@Transient
	private PKIXCertificate[] certificateChain;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public AccountZoneAdministrationCredential() {
	}

	public AccountZoneAdministrationCredential(AccountZoneAdministrationCredentialID id) {
		this.id = id;
	}

	public AccountZoneAdministrationCredential(String accountId, String pem) throws CryptoCertificateException {
		setCertificateChainPem(pem);

		PKIXCertificate[] certificateChain = CertificateIOUtils.safePemToX509certs(pem);
		if (certificateChain != null) {
			setCertificateChain(certificateChain);

			PKIXCertificate publicKey = getPublicKey();

			AccountZoneAdministrationCredentialID id = new AccountZoneAdministrationCredentialID(accountId,
					publicKey.getFingerprint());
			setId(id);
			// an invalid cert might be missing the ZI
			if (publicKey.getTdmxZoneInfo() != null) {
				setZoneApex(publicKey.getTdmxZoneInfo().getZoneRoot());
				setCredentialStatus(AccountZoneAdministrationCredentialStatus.PENDING);
			} else {
				setZoneApex(null);
				setCredentialStatus(AccountZoneAdministrationCredentialStatus.INVALID_TDMX);
			}
		} else {
			AccountZoneAdministrationCredentialID id = new AccountZoneAdministrationCredentialID(accountId, UUID
					.randomUUID().toString());
			setId(id);
			setZoneApex(null);
			setCredentialStatus(AccountZoneAdministrationCredentialStatus.INVALID_PEM);
		}

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Get the PEM certificate chain in PKIXCertificate form, converting and caching on the first call.
	 * 
	 * @return
	 * @throws CryptoCertificateException
	 */
	public PKIXCertificate[] getCertificateChain() {
		if (certificateChain == null && getCertificateChainPem() != null) {
			certificateChain = CertificateIOUtils.safePemToX509certs(getCertificateChainPem());
			return certificateChain;
		}
		return certificateChain;
	}

	public PKIXCertificate getPublicKey() {
		if (getCertificateChain() != null && getCertificateChain().length > 0) {
			return getCertificateChain()[0];
		}
		return null;
	}

	public PKIXCertificate getIssuerPublicKey() {
		if (getCertificateChain() != null && getCertificateChain().length > 1) {
			return getCertificateChain()[1];
		}
		return null;
	}

	public PKIXCertificate getZoneRootPublicKey() {
		if (getCertificateChain() != null && getCertificateChain().length > 1) {
			if (getCertificateChain().length > 2) {
				return getCertificateChain()[2];
			}
			return getCertificateChain()[1];
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setCertificateChain(PKIXCertificate[] certificateChain) throws CryptoCertificateException {
		this.certificateChain = certificateChain;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AccountZoneAdministrationCredentialID getId() {
		return id;
	}

	private void setId(AccountZoneAdministrationCredentialID id) {
		this.id = id;
	}

	public String getZoneApex() {
		return zoneApex;
	}

	public void setZoneApex(String zoneApex) {
		this.zoneApex = zoneApex;
	}

	public AccountZoneAdministrationCredentialStatus getCredentialStatus() {
		return credentialStatus;
	}

	public void setCredentialStatus(AccountZoneAdministrationCredentialStatus credentialStatus) {
		this.credentialStatus = credentialStatus;
	}

	public String getCertificateChainPem() {
		return certificateChainPem;
	}

	public void setCertificateChainPem(String certificateChainPem) {
		this.certificateChainPem = certificateChainPem;
	}

}
