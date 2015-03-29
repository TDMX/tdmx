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
import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;

/**
 * A Signature of either a User, DomainAdministrator or ZoneAdministrator.
 * 
 * NOTE: the signing public certificate chain is stored as a PEM.
 * 
 * @author Peter Klauser
 * 
 */
@Embeddable
public class AgentSignature implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	// large enough for SHA512 as hex
	public static final int MAX_SIGNATURE_LEN = 128;
	public static final int MAX_SIG_ALG_LEN = 16;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	/**
	 * The public certificate of the Agent ( UC or DAC or ZAC ).
	 * 
	 * NOTE: Maximum length is defined by {@link AgentCredential#MAX_CERTIFICATECHAIN_LEN}
	 */
	private String certificateChainPem;

	@Temporal(TemporalType.TIMESTAMP)
	private Date signatureDate;

	/**
	 * The hex representation of the signature.
	 */
	private String value;

	/**
	 * The signature algorithm.
	 */
	@Enumerated(EnumType.STRING)
	private SignatureAlgorithm algorithm;

	@Transient
	private PKIXCertificate[] certificateChain;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public AgentSignature() {

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AgentSignature [");
		builder.append("signatureDate=").append(signatureDate);
		builder.append(", algorithm=").append(algorithm);
		builder.append(", value=").append(value);
		if (certificateChainPem != null) {
			builder.append(", certificateChainPem.length=").append(certificateChainPem.length());
		}

		builder.append("]");
		return builder.toString();
	}

	/**
	 * Get the PEM certificate chain in PKIXCertificate form, converting and caching on the first call.
	 * 
	 * @return
	 * @throws CryptoCertificateException
	 */
	public PKIXCertificate[] getCertificateChain() {
		if (certificateChain == null && getCertificateChainPem() != null) {
			certificateChain = CertificateIOUtils.safePemToX509certs(getCertificateChainPem());
		}
		return certificateChain;
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

	public String getCertificateChainPem() {
		return certificateChainPem;
	}

	public void setCertificateChainPem(String certificateChainPem) {
		this.certificateChainPem = certificateChainPem;
	}

	public Date getSignatureDate() {
		return signatureDate;
	}

	public void setSignatureDate(Date signatureDate) {
		this.signatureDate = signatureDate;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public SignatureAlgorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(SignatureAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

}
