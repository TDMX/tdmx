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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;

/**
 * An ChannelFlowTarget is a copy of a FlowTarget held within each ChannelAuthorization.
 * 
 * ChannelFlowTargets are created when FlowTargets are set by target Agents on all open Channels which have that target.
 * ChannelFlowTargets are created on the receiving side ( from existing FlowTargets ) on Channel open when
 * ChannelAuthorization is relayed in or authorization set by the receiving side.
 * 
 * ChannelFlowTargets ( including their ChannelFlowOrigins ) are deleted on the receiving side when the target agent is
 * deleted on the receiving side. They are deleted also when a ChannelAuthorization declares a Channel "closed".
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "ChannelFlowTarget")
public class ChannelFlowTarget implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	// TODO "Relay" Processingstatus, relay to include any existing destination FlowTargetSessions which are valid at
	// opening time.

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ChannelFlowTargetIdGen")
	@TableGenerator(name = "ChannelFlowTargetIdGen", table = "MaxValueEntry", pkColumnName = "NAME", pkColumnValue = "channelflowtargetObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Channel channel;

	@Column(length = AgentCredential.MAX_SHA256FINGERPRINT_LEN, nullable = false)
	private String targetFingerprint;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "primary.identifier", column = @Column(name = "primaryIdentifier", length = FlowSession.MAX_IDENTIFIER_LEN)),
			@AttributeOverride(name = "primary.scheme", column = @Column(name = "primaryScheme", length = FlowSession.MAX_SCHEME_LEN)),
			@AttributeOverride(name = "primary.validFrom", column = @Column(name = "primaryValidFrom")),
			@AttributeOverride(name = "primary.sessionKey", column = @Column(name = "primarySession", length = FlowSession.MAX_SESSION_KEY_LEN)),
			@AttributeOverride(name = "secondary.identifier", column = @Column(name = "secondaryIdentifier", length = FlowSession.MAX_IDENTIFIER_LEN)),
			@AttributeOverride(name = "secondary.scheme", column = @Column(name = "secondaryScheme", length = FlowSession.MAX_SCHEME_LEN)),
			@AttributeOverride(name = "secondary.validFrom", column = @Column(name = "secondaryValidFrom")),
			@AttributeOverride(name = "secondary.sessionKey", column = @Column(name = "secondarySession", length = FlowSession.MAX_SESSION_KEY_LEN)),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "signatureDate")),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "targetPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN)),
			@AttributeOverride(name = "signature.value", column = @Column(name = "signature", length = AgentSignature.MAX_SIGNATURE_LEN)),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "signatureAlgorithm", length = AgentSignature.MAX_SIG_ALG_LEN)) })
	private FlowTargetSession flowTargetSession;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "taskId", column = @Column(name = "processingId", length = ProcessingState.MAX_TASKID_LEN, nullable = false)),
			@AttributeOverride(name = "status", column = @Column(name = "processingStatus", length = ProcessingStatus.MAX_PROCESSINGSTATUS_LEN, nullable = false)),
			@AttributeOverride(name = "timestamp", column = @Column(name = "processingTimestamp", nullable = false)),
			@AttributeOverride(name = "errorCode", column = @Column(name = "processingErrorCode")),
			@AttributeOverride(name = "errorMessage", column = @Column(name = "processingErrorMessage", length = ProcessingState.MAX_ERRORMESSAGE_LEN)) })
	private ProcessingState processingState;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "flowTarget", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ChannelFlowOrigin> channelFlowOrigins;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	ChannelFlowTarget() {
	}

	public ChannelFlowTarget(Channel c) {
		setChannel(c);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelFlowTarget [id=");
		builder.append(id);
		builder.append(" targetFingerprint=").append(targetFingerprint);
		builder.append(" fts=").append(flowTargetSession);
		builder.append(", processingState=").append(processingState);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Get the PEM certificate chain in PKIXCertificate form, converting and caching on the first call.
	 * 
	 * @return
	 * @throws CryptoCertificateException
	 */
	public PKIXCertificate[] getTargetCertificateChain() {
		return flowTargetSession.getSignature().getCertificateChain();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setChannel(Channel c) {
		this.channel = c;
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

	public Channel getChannel() {
		return channel;
	}

	public String getTargetFingerprint() {
		return targetFingerprint;
	}

	public void setTargetFingerprint(String targetFingerprint) {
		this.targetFingerprint = targetFingerprint;
	}

	public FlowTargetSession getFlowTargetSession() {
		return flowTargetSession;
	}

	public void setFlowTargetSession(FlowTargetSession flowTargetSession) {
		this.flowTargetSession = flowTargetSession;
	}

	public ProcessingState getProcessingState() {
		return processingState;
	}

	public void setProcessingState(ProcessingState processingState) {
		this.processingState = processingState;
	}

	public Set<ChannelFlowOrigin> getChannelFlowOrigins() {
		if (channelFlowOrigins == null) {
			channelFlowOrigins = new HashSet<>();
		}
		return channelFlowOrigins;
	}

}
