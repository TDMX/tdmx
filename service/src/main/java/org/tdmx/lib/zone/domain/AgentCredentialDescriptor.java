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

import org.tdmx.client.crypto.certificate.PKIXCertificate;

/**
 * An AgentCredentialDescriptor describes an AgentCredential derived from the PKIXCertificate of the Agent.
 * 
 * @author Peter Klauser
 * 
 */
public class AgentCredentialDescriptor {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private AgentCredentialType credentialType;

	private String fingerprint;

	private String zoneApex; // set when ZAC, DAC or UC

	private String domainName; // set when DAC or UC, null if ZAC

	private String addressName; // set when UC, null if ZAC or DAC

	private PKIXCertificate[] certificateChain;

	private String certificateChainPem;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AgentCredentialDescriptor [");
		builder.append(" fingerprint=").append(fingerprint);
		builder.append(" ,type=").append(credentialType);
		builder.append(" ,zoneApex=").append(zoneApex);
		builder.append(" ,domainName=").append(domainName);
		builder.append(" ,addressName=").append(addressName);
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

	public AgentCredentialType getCredentialType() {
		return credentialType;
	}

	public void setCredentialType(AgentCredentialType credentialType) {
		this.credentialType = credentialType;
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

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getAddressName() {
		return addressName;
	}

	public void setAddressName(String addressName) {
		this.addressName = addressName;
	}

	public String getCertificateChainPem() {
		return certificateChainPem;
	}

	public void setCertificateChainPem(String certificateChainPem) {
		this.certificateChainPem = certificateChainPem;
	}

	public PKIXCertificate[] getCertificateChain() {
		return certificateChain;
	}

	public void setCertificateChain(PKIXCertificate[] certificateChain) {
		this.certificateChain = certificateChain;
	}

}
