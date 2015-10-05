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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
@Table(name = "AgentCredential")
public class AgentCredential implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_SHA256FINGERPRINT_LEN = 64;
	public static final int MAX_CERTIFICATECHAIN_LEN = 12000;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "CredentialIdGen")
	@TableGenerator(name = "CredentialIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "zoneObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Zone zone;

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	private Domain domain;

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	private Address address;

	// TODO DB: index fingerprint
	@Column(length = MAX_SHA256FINGERPRINT_LEN, nullable = false)
	private String fingerprint;

	@Enumerated(EnumType.STRING)
	@Column(length = AgentCredentialType.MAX_CREDENTIALTYPE_LEN, nullable = false)
	private AgentCredentialType credentialType;

	@Enumerated(EnumType.STRING)
	@Column(length = AgentCredentialStatus.MAX_CREDENTIALSTATUS_LEN, nullable = false)
	private AgentCredentialStatus credentialStatus = AgentCredentialStatus.ACTIVE;

	@Column(length = MAX_CERTIFICATECHAIN_LEN, nullable = false)
	private String certificateChainPem;

	@Transient
	private PKIXCertificate[] certificateChain;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	AgentCredential() {
	}

	/**
	 * Constructor for a ZAC.
	 * 
	 * @param zone
	 * @param other
	 */
	public AgentCredential(Zone zone, AgentCredentialDescriptor other) {
		this(zone, null, null, other);
	}

	/**
	 * Constructor for a DAC.
	 * 
	 * @param zone
	 * @param domain
	 * @param other
	 */
	public AgentCredential(Zone zone, Domain domain, AgentCredentialDescriptor other) {
		this(zone, domain, null, other);
	}

	/**
	 * Constructor for a UC.
	 * 
	 * @param zone
	 * @param domain
	 * @param address
	 * @param other
	 */
	public AgentCredential(Zone zone, Domain domain, Address address, AgentCredentialDescriptor other) {
		setZone(zone);
		setDomain(domain);
		setAddress(address);
		setCredentialType(other.getCredentialType());
		setCertificateChainPem(other.getCertificateChainPem());
		setFingerprint(other.getFingerprint());
	}

	/**
	 * Clone ZAC for ZoneDB partition transfer.
	 * 
	 * @param zone
	 * @param domain
	 * @param address
	 * @param other
	 */
	public AgentCredential(Zone zone, AgentCredential other) {
		this(zone, null, null, other);
	}

	/**
	 * Clone DAC for ZoneDB partition transfer.
	 * 
	 * @param zone
	 * @param domain
	 * @param address
	 * @param other
	 */
	public AgentCredential(Zone zone, Domain domain, AgentCredential other) {
		this(zone, domain, null, other);
	}

	/**
	 * Clone UC for ZoneDB partition transfer.
	 * 
	 * @param zone
	 * @param domain
	 * @param address
	 * @param other
	 */
	public AgentCredential(Zone zone, Domain domain, Address address, AgentCredential other) {
		setZone(zone);
		setDomain(domain);
		setAddress(address);
		setCredentialType(other.getCredentialType());
		setCertificateChainPem(other.getCertificateChainPem());
		setCredentialStatus(other.getCredentialStatus());
		setFingerprint(other.getFingerprint());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AgentCredential [id=");
		builder.append(id);
		builder.append(" ,type=").append(credentialType);
		builder.append(", fingerprint=").append(fingerprint);
		builder.append(" ,status=").append(credentialStatus);
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

	/**
	 * Get the agent's public certificate.
	 * 
	 * @return the Agent's public certificate.
	 */
	public PKIXCertificate getPublicCertificate() {
		PKIXCertificate[] certs = getCertificateChain();
		if (certs != null && certs.length > 0) {
			return certs[0];
		}
		return null;
	}

	/**
	 * Reverse map the AgentCredential to a Descriptor which can be mapped easier to other entities.
	 * 
	 * @return a AgentCredentialDescriptor describing this AgentCredential.
	 */
	public AgentCredentialDescriptor getDescriptor(Zone zone) {
		AgentCredentialDescriptor result = new AgentCredentialDescriptor();
		result.setZoneApex(zone.getZoneApex());
		if (getAddress() != null) {
			result.setAddressName(getAddress().getLocalName());
		}
		result.setCertificateChainPem(getCertificateChainPem());
		result.setCredentialType(getCredentialType());
		if (getDomain() != null) {
			result.setDomainName(getDomain().getDomainName());
		}
		result.setFingerprint(getFingerprint());
		return result;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setZone(Zone zone) {
		this.zone = zone;
	}

	private void setDomain(Domain domain) {
		this.domain = domain;
	}

	private void setAddress(Address address) {
		this.address = address;
	}

	private void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	private void setCredentialType(AgentCredentialType credentialType) {
		this.credentialType = credentialType;
	}

	private void setCertificateChainPem(String certificateChainPem) {
		this.certificateChainPem = certificateChainPem;
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

	public Zone getZone() {
		return zone;
	}

	public Domain getDomain() {
		return domain;
	}

	public Address getAddress() {
		return address;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public AgentCredentialType getCredentialType() {
		return credentialType;
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

}
