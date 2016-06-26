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
import java.util.Date;

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

import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.lib.zone.domain.AgentSignature;

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
	public static final int MAX_STRING_LEN = 255;
	public static final int MAX_PUBLIC_KEY_ALG_LEN = 16;

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

	@Column(length = MAX_CERTIFICATECHAIN_LEN, nullable = false)
	private String certificateChainPem;

	/*
	 * Denormalized(Read-only) from PEM
	 */
	@Column(nullable = false)
	private int tdmxVersionNumber;
	@Column(nullable = false)
	private int serialNumber;
	@Column(length = MAX_STRING_LEN, nullable = false)
	private String cn;
	@Column(length = MAX_STRING_LEN)
	private String telephoneNumber;
	@Column(length = MAX_STRING_LEN)
	private String emailAddress;
	@Column(length = MAX_STRING_LEN)
	private String orgUnit;
	@Column(length = MAX_STRING_LEN)
	private String org;
	@Column(length = MAX_STRING_LEN)
	private String location;
	@Column(length = MAX_STRING_LEN)
	private String country;
	@Column(nullable = false)
	private Date notBefore;
	@Column(nullable = false)
	private Date notAfter;
	@Enumerated(EnumType.STRING)
	@Column(length = MAX_PUBLIC_KEY_ALG_LEN, nullable = false)
	private PublicKeyAlgorithm keyAlgorithm;
	@Enumerated(EnumType.STRING)
	@Column(length = AgentSignature.MAX_SIG_ALG_LEN, nullable = false)
	private SignatureAlgorithm signatureAlgorithm;

	@Transient
	private PKIXCertificate[] certificateChain;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public AccountZoneAdministrationCredential() {
	}

	/**
	 * Create a ZAC for a specific account using the PEM representation.
	 * 
	 * @param accountId
	 * @param pem
	 * @throws IllegalArgumentException
	 *             if the PEM is not valid or not a ZAC.
	 */
	public AccountZoneAdministrationCredential(String accountId, String pem) {
		setAccountId(accountId);
		initializePEM(pem);
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
		builder.append("]");
		return builder.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setCertificateChain(PKIXCertificate[] certificateChain) {
		this.certificateChain = certificateChain;
	}

	private void initializePEM(String pem) {
		setCertificateChainPem(pem);

		PKIXCertificate[] certChain = CertificateIOUtils.safePemToX509certs(pem);
		if (certChain != null) {
			setCertificateChain(certChain);

			PKIXCertificate publicKey = getPublicKey();
			setFingerprint(publicKey.getFingerprint());
			if (publicKey.isTdmxZoneAdminCertificate()) {
				ZoneAdministrationCredentialSpecifier spec = CredentialUtils
						.describeZoneAdministratorCertificate(publicKey);

				setZoneApex(publicKey.getTdmxZoneInfo().getZoneRoot());
				setTdmxVersionNumber(spec.getZoneInfo().getVersion());
				setSerialNumber(spec.getSerialNumber());
				setCn(spec.getCn());
				setTelephoneNumber(spec.getTelephoneNumber());
				setEmailAddress(spec.getEmailAddress());
				setOrgUnit(spec.getOrgUnit());
				setOrg(spec.getOrg());
				setLocation(spec.getLocation());
				setCountry(spec.getCountry());
				setNotBefore(CalendarUtils.cast(spec.getNotBefore()));
				setNotAfter(CalendarUtils.cast(spec.getNotAfter()));
				setKeyAlgorithm(spec.getKeyAlgorithm());
				setSignatureAlgorithm(spec.getSignatureAlgorithm());
			} else {
				throw new IllegalArgumentException("pem");
			}
		} else {
			throw new IllegalArgumentException("pem");
		}
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

	public String getCertificateChainPem() {
		return certificateChainPem;
	}

	public void setCertificateChainPem(String certificateChainPem) {
		this.certificateChain = null;
		this.certificateChainPem = certificateChainPem;
	}

	public int getTdmxVersionNumber() {
		return tdmxVersionNumber;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public String getCn() {
		return cn;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getOrgUnit() {
		return orgUnit;
	}

	public String getOrg() {
		return org;
	}

	public String getLocation() {
		return location;
	}

	public String getCountry() {
		return country;
	}

	public Date getNotBefore() {
		return notBefore;
	}

	public Date getNotAfter() {
		return notAfter;
	}

	public PublicKeyAlgorithm getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public SignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	private void setTdmxVersionNumber(int tdmxVersionNumber) {
		this.tdmxVersionNumber = tdmxVersionNumber;
	}

	private void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}

	private void setCn(String cn) {
		this.cn = cn;
	}

	private void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	private void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	private void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}

	private void setOrg(String org) {
		this.org = org;
	}

	private void setLocation(String location) {
		this.location = location;
	}

	private void setCountry(String country) {
		this.country = country;
	}

	private void setNotBefore(Date notBefore) {
		this.notBefore = notBefore;
	}

	private void setNotAfter(Date notAfter) {
		this.notAfter = notAfter;
	}

	private void setKeyAlgorithm(PublicKeyAlgorithm keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	private void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

}
