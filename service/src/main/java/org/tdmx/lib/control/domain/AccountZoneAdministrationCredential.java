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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
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
	public static final int MAX_CERTIFICATECHAIN_LEN = 12000;
	public static final int MAX_SHA256FINGERPRINT_LEN = 64;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "AccountZoneCredentialIdGen")
	@TableGenerator(name = "AccountZoneCredentialIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "controlObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@Column(length = Account.MAX_ACCOUNTID_LEN, nullable = false)
	private String accountId;

	@Column(length = DnsDomainZone.MAX_DOMAINNAME_LEN, nullable = false)
	private String zoneApex;

	@Column(length = MAX_SHA256FINGERPRINT_LEN, nullable = false, unique = true)
	private String fingerprint;

	@Enumerated(EnumType.STRING)
	@Column(length = AccountZoneAdministrationCredentialStatus.MAX_CREDENTIALSTATUS_LEN, nullable = false)
	private AccountZoneAdministrationCredentialStatus credentialStatus;

	@Column(length = MAX_CERTIFICATECHAIN_LEN, nullable = false)
	private String certificateChainPem;

	@Transient
	private PKIXCertificate[] certificateChain;

	/**
	 * If a Job is pending on the Account's ZoneCredential (like check/setup ) then this is the link to the jobId. The
	 * Job is responsible to remove this record once it has completed.
	 */
	@Column
	private Long jobId;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public AccountZoneAdministrationCredential() {
	}

	public AccountZoneAdministrationCredential(String accountId, String pem) throws CryptoCertificateException {
		setCertificateChainPem(pem);
		setAccountId(accountId);

		PKIXCertificate[] certChain = CertificateIOUtils.safePemToX509certs(pem);
		if (certChain != null) {
			setCertificateChain(certChain);

			PKIXCertificate publicKey = getPublicKey();
			setFingerprint(publicKey.getFingerprint());
			// an invalid cert might be missing the ZI
			if (publicKey.getTdmxZoneInfo() != null) {
				setZoneApex(publicKey.getTdmxZoneInfo().getZoneRoot());
				setCredentialStatus(AccountZoneAdministrationCredentialStatus.PENDING);
			} else {
				setZoneApex(null);
				setCredentialStatus(AccountZoneAdministrationCredentialStatus.INVALID_TDMX);
			}
		} else {
			setFingerprint(UUID.randomUUID().toString());
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AccountZoneAdministrationCredential [id=");
		builder.append(id);
		builder.append(", accountId=");
		builder.append(accountId);
		builder.append(", zoneApex=");
		builder.append(zoneApex);
		builder.append(", fingerprint=");
		builder.append(fingerprint);
		builder.append(", credentialStatus=");
		builder.append(credentialStatus);
		builder.append("]");
		return builder.toString();
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
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
		this.certificateChain = null;
		this.certificateChainPem = certificateChainPem;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

}
