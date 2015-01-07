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
@Table(name = "AgentCredential")
public class AgentCredential implements Serializable {

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
	private AgentCredentialID id;

	@Enumerated(EnumType.STRING)
	@Column(length = AgentCredentialType.MAX_CREDENTIALTYPE_LEN, nullable = false)
	private AgentCredentialType credentialType;

	@Enumerated(EnumType.STRING)
	@Column(length = AgentCredentialStatus.MAX_CREDENTIALSTATUS_LEN, nullable = false)
	private AgentCredentialStatus credentialStatus;

	@Column(length = MAX_CERTIFICATECHAIN_LEN, nullable = false)
	private String certificateChainPem;

	@Transient
	private PKIXCertificate[] certificateChain;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public AgentCredential() {
	}

	public AgentCredential(AgentCredentialID id) {
		this.id = id;
	}

	public AgentCredential(PKIXCertificate[] certificateChain, AgentCredentialStatus status)
			throws CryptoCertificateException {
		PKIXCertificate publicKey = certificateChain[0];

		AgentCredentialID id = new AgentCredentialID(publicKey.getTdmxZoneInfo().getZoneRoot(),
				publicKey.getFingerprint());
		setId(id);
		setCredentialStatus(status);

		if (publicKey.isTdmxZoneAdminCertificate()) {
			setCredentialType(AgentCredentialType.ZAC);
		} else if (publicKey.isTdmxDomainAdminCertificate()) {
			setCredentialType(AgentCredentialType.DAC);
		} else if (publicKey.isTdmxUserCertificate()) {
			setCredentialType(AgentCredentialType.UC);
		}

		setCertificateChain(certificateChain);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// TODO getComain / getUserName as fields + DAO filter.
	public String getDomain() {
		try {
			PKIXCertificate publicKey = getPublicKey();
			if (publicKey != null) {
				if (AgentCredentialType.DAC == getCredentialType()) {
					return publicKey.getCommonName();
				} else if (AgentCredentialType.UC == getCredentialType()) {
					PKIXCertificate issuerKey = getCertificateChain()[1];
					return issuerKey.getCommonName();
				}
			}

		} catch (CryptoCertificateException e) {
			throw new IllegalStateException(e);
		}
		return null;
	}

	/**
	 * Get the PEM certificate chain in PKIXCertificate form, converting and caching on the first call.
	 * 
	 * @return
	 * @throws CryptoCertificateException
	 */
	public PKIXCertificate[] getCertificateChain() throws CryptoCertificateException {
		if (certificateChain == null && getCertificateChainPem() != null) {
			certificateChain = CertificateIOUtils.pemToX509certs(getCertificateChainPem());
			return certificateChain;
		}
		return certificateChain;
	}

	public PKIXCertificate getPublicKey() throws CryptoCertificateException {
		if (getCertificateChain() != null && getCertificateChain().length > 0) {
			return getCertificateChain()[0];
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
		setCertificateChainPem(CertificateIOUtils.x509certsToPem(certificateChain));
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AgentCredentialID getId() {
		return id;
	}

	public void setId(AgentCredentialID id) {
		this.id = id;
	}

	public AgentCredentialType getCredentialType() {
		return credentialType;
	}

	public void setCredentialType(AgentCredentialType credentialType) {
		this.credentialType = credentialType;
	}

	public AgentCredentialStatus getCredentialStatus() {
		return credentialStatus;
	}

	public void setCredentialStatus(AgentCredentialStatus credentialStatus) {
		this.credentialStatus = credentialStatus;
	}

	public String getCertificateChainPem() {
		return certificateChainPem;
	}

	public void setCertificateChainPem(String certificateChainPem) {
		this.certificateChainPem = certificateChainPem;
	}

}
