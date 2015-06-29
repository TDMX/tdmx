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
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;

/**
 * An ChannelFlowOrigin is a the source of a Flow which targets a ChannelFlowTarget.
 * 
 * Multiple ChannelFlowOrigins ( one per source Agent with the same address ) can share the same ChannelFlowTarget (
 * where there is one per destination Agent ).
 * 
 * ChannelFlowOrigins are created at the same ChannelFlowTargets are created for all "known" source Agents. This can be
 * on the sending side when a ChannelFlowTarget is relayed in from the receiving side, or when the first inbound
 * messages is relayed in on the receiving side. For Flows within the same domain, the ChannelFlowOrigin is created at
 * the same time the ChannelFlowTargets are created, on Channel open.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "ChannelFlowOrigin")
public class ChannelFlowOrigin implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	// TODO "Relay" Processingstatus of flowcontrolstatus

	// TODO Quota info

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ChannelFlowOriginIdGen")
	@TableGenerator(name = "ChannelFlowOriginIdGen", table = "MaxValueEntry", pkColumnName = "NAME", pkColumnValue = "channelfloworiginObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private ChannelFlowTarget flowTarget;

	@Column(length = AgentCredential.MAX_SHA256FINGERPRINT_LEN, nullable = false)
	private String sourceFingerprint;

	@Column(length = AgentCredential.MAX_CERTIFICATECHAIN_LEN, nullable = false)
	private String sourceCertificateChainPem;

	// we don't make getters nor setters for CFMs because there can be too many, but we do want cascade of deletions
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "flowOrigin", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ChannelFlowMessage> channelFlowMessages;

	@Transient
	private PKIXCertificate[] sourceCertificateChain;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	ChannelFlowOrigin() {
	}

	public ChannelFlowOrigin(ChannelFlowTarget cft) {
		setFlowTarget(cft);
		cft.getChannelFlowOrigins().add(this);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelFlowOrigin [id=");
		builder.append(id);
		builder.append(" sourceFingerprint=").append(sourceFingerprint);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Get the PEM certificate chain in PKIXCertificate form, converting and caching on the first call.
	 * 
	 * @return
	 * @throws CryptoCertificateException
	 */
	public PKIXCertificate[] getSourceCertificateChain() {
		if (sourceCertificateChain == null && getSourceCertificateChainPem() != null) {
			sourceCertificateChain = CertificateIOUtils.safePemToX509certs(getSourceCertificateChainPem());
		}
		return sourceCertificateChain;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setFlowTarget(ChannelFlowTarget flowTarget) {
		this.flowTarget = flowTarget;
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

	public ChannelFlowTarget getFlowTarget() {
		return flowTarget;
	}

	public String getSourceFingerprint() {
		return sourceFingerprint;
	}

	public void setSourceFingerprint(String sourceFingerprint) {
		this.sourceFingerprint = sourceFingerprint;
	}

	public String getSourceCertificateChainPem() {
		return sourceCertificateChainPem;
	}

	public void setSourceCertificateChainPem(String sourceCertificateChainPem) {
		this.sourceCertificateChainPem = sourceCertificateChainPem;
	}

}
