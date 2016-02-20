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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.system.lang.StringUtils;

/**
 * A trusted or distrusted SSL root certificate.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "TrustedSslCertificate")
public class TrustedSslCertificate implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_DESCRIPTION_LEN = 12000;
	public static final int MAX_COMMENT_LEN = 2000;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TrustedSslCertificateIdGen")
	@TableGenerator(name = "TrustedSslCertificateIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "controlObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@Column(length = AccountZoneAdministrationCredential.MAX_SHA256FINGERPRINT_LEN, nullable = false, unique = true)
	private String fingerprint;

	@Enumerated(EnumType.STRING)
	@Column(length = TrustStatus.MAX_TRUSTSTATUS_LEN, nullable = false)
	private TrustStatus trustStatus;

	@Column(length = MAX_COMMENT_LEN, nullable = true)
	private String comment;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date validFrom;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date validTo;

	@Column(length = MAX_DESCRIPTION_LEN, nullable = false)
	private String description;

	@Column(length = AccountZoneAdministrationCredential.MAX_CERTIFICATECHAIN_LEN, nullable = false)
	private String certificatePem;

	@Transient
	private PKIXCertificate certificate;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public TrustedSslCertificate() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Get the PEM certificate in PKIXCertificate form, converting and caching on the first call.
	 * 
	 * @return
	 * @throws CryptoCertificateException
	 */
	public PKIXCertificate getCertificate() {
		if (certificate == null && getCertificatePem() != null) {
			PKIXCertificate[] certificateChain = CertificateIOUtils.safePemToX509certs(getCertificatePem());
			certificate = certificateChain != null && certificateChain.length == 1 ? certificateChain[0] : null;
		}
		return certificate;
	}

	public void setCertificate(PKIXCertificate cert) {
		certificate = cert;
		certificatePem = CertificateIOUtils.safeX509certsToPem(new PKIXCertificate[] { cert });
		fingerprint = cert.getFingerprint();
		validFrom = cert.getNotBefore().getTime();
		validTo = cert.getNotAfter().getTime();
		description = StringUtils.truncateToMaxLen(cert.toString(), MAX_DESCRIPTION_LEN);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrustedSslCertificate [id=");
		builder.append(id);
		builder.append(", fingerprint=");
		builder.append(fingerprint);
		builder.append(", trustStatus=");
		builder.append(trustStatus);
		builder.append("]");
		return builder.toString();
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public String getCertificatePem() {
		return certificatePem;
	}

	public TrustStatus getTrustStatus() {
		return trustStatus;
	}

	public void setTrustStatus(TrustStatus trustStatus) {
		this.trustStatus = trustStatus;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

	public String getDescription() {
		return description;
	}

}
