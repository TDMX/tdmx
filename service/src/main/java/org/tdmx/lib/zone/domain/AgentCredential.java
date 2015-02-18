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

	@Column(length = DomainID.MAX_NAME_LEN)
	private String domainName; // set when DAC or UC, null if ZAC

	@Column(length = AddressID.MAX_NAME_LEN)
	private String addressName; // set when UC, null if ZAC or DAC

	@Transient
	private PKIXCertificate[] certificateChain;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public AgentCredential() {
	}

	public AgentCredential(PKIXCertificate[] certificateChain) throws CryptoCertificateException {
		setCertificateChain(certificateChain);
		PKIXCertificate publicKey = getPublicKey();
		// TODO fixme
		AgentCredentialID id = new AgentCredentialID(publicKey.getTdmxZoneInfo().getZoneRoot(),
				publicKey.getFingerprint());
		setId(id);

		if (publicKey.isTdmxZoneAdminCertificate()) {
			setCredentialType(AgentCredentialType.ZAC);
		} else if (publicKey.isTdmxDomainAdminCertificate()) {
			setCredentialType(AgentCredentialType.DAC);
			setDomainName(publicKey.getCommonName());
		} else if (publicKey.isTdmxUserCertificate()) {
			setCredentialType(AgentCredentialType.UC);
			setAddressName(publicKey.getCommonName());

			PKIXCertificate issuerKey = getIssuerPublicKey();
			setDomainName(issuerKey.getCommonName());
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
		setCertificateChainPem(CertificateIOUtils.x509certsToPem(certificateChain));
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AgentCredentialID getId() {
		return id;
	}

	private void setId(AgentCredentialID id) {
		this.id = id;
	}

	public AgentCredentialType getCredentialType() {
		return credentialType;
	}

	private void setCredentialType(AgentCredentialType credentialType) {
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

	public String getDomainName() {
		return domainName;
	}

	private void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getAddressName() {
		return addressName;
	}

	private void setAddressName(String addressName) {
		this.addressName = addressName;
	}

}
