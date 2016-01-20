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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;

/**
 * An Channel (within a Domain of a Zone) is used as a point to associate a ChannelAuthorization and any
 * ChannelFlowSessions.
 * 
 * The Channel is created on {@link ZAS#setChannelAuthorization(org.tdmx.core.api.v01.zas.SetChannelAuthorization)} by
 * own agents or relayed in via conversion of TemporaryChannel to Channel through
 * {@link org.tdmx.lib.zone.service.ChannelService#relayInitialAuthorization(Zone, Long, EndpointPermission)}.
 * 
 * The Channel is deleted on
 * {@link ZAS#deleteChannelAuthorization(org.tdmx.core.api.v01.zas.DeleteChannelAuthorization)}.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "Channel")
public class Channel implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ChannelIdGen")
	@TableGenerator(name = "ChannelIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "channelObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Domain domain;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "localName", column = @Column(name = "originAddress", nullable = false) ),
			@AttributeOverride(name = "domainName", column = @Column(name = "originDomain", nullable = false) ) })
	private ChannelOrigin origin;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "localName", column = @Column(name = "destAddress", nullable = false) ),
			@AttributeOverride(name = "domainName", column = @Column(name = "destDomain", nullable = false) ),
			@AttributeOverride(name = "serviceName", column = @Column(name = "destService", nullable = false) ) })
	private ChannelDestination destination;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "encryptionContextId", column = @Column(name = "dsIdentifier", length = DestinationSession.MAX_IDENTIFIER_LEN) ),
			@AttributeOverride(name = "scheme", column = @Column(name = "dsScheme", length = DestinationSession.MAX_SCHEME_LEN) ),
			@AttributeOverride(name = "sessionKey", column = @Column(name = "dsSession", length = DestinationSession.MAX_SESSION_KEY_LEN) ),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "dsSignatureDate") ),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "dsTargetPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN) ),
			@AttributeOverride(name = "signature.value", column = @Column(name = "dsSignature", length = AgentSignature.MAX_SIGNATURE_LEN) ),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "dsSignatureAlgorithm", length = AgentSignature.MAX_SIG_ALG_LEN) ) })
	private DestinationSession session;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "status", column = @Column(name = "processingStatus", length = ProcessingStatus.MAX_PROCESSINGSTATUS_LEN, nullable = false) ),
			@AttributeOverride(name = "timestamp", column = @Column(name = "processingTimestamp", nullable = false) ),
			@AttributeOverride(name = "errorCode", column = @Column(name = "processingErrorCode") ),
			@AttributeOverride(name = "errorMessage", column = @Column(name = "processingErrorMessage", length = ProcessingState.MAX_ERRORMESSAGE_LEN) ) })
	private ProcessingState processingState = ProcessingState.none(); // of relay of DestinationSession

	// TODO #93 relay DS and toggle PS to pending()

	/**
	 * The quota association is "owned" ie. managed through this Channel
	 */
	@OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private FlowQuota quota;

	/**
	 * The authorization owned by this Channel.
	 */
	@OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private ChannelAuthorization authorization;

	// we don't make getters nor setters for CMs because there can be too many, but we do want cascade of deletions
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ChannelMessage> channelFlowMessages;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	Channel() {
	}

	public Channel(Domain domain, ChannelOrigin origin, ChannelDestination destination) {
		setDomain(domain);
		setOrigin(origin);
		setDestination(destination);
		setQuota(new FlowQuota(this));
	}

	public Channel(Domain domain, Channel other) {
		setDomain(domain);
		setOrigin(other.getOrigin());
		setDestination(other.getDestination());
		setQuota(new FlowQuota(this, other.getQuota()));
		setProcessingState(other.getProcessingState());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public ChannelName getChannelName() {
		return new ChannelName(origin, destination);
	}

	/**
	 * When the same domain is both sender and receiver, we don't need to relay effectively via the MRS but can shortcut
	 * the relay entirely.
	 * 
	 * @return whether the same domain is at both sender and receiver ends of the channel.
	 */
	public boolean isSameDomain() {
		return isSend() && isRecv();
	}

	public boolean isSend() {
		return domain.getDomainName().equals(origin.getDomainName());
	}

	public boolean isRecv() {
		return domain.getDomainName().equals(destination.getDomainName());
	}

	public boolean isOpen() {
		return (authorization.getSendAuthorization() != null
				&& EndpointPermissionGrant.ALLOW == authorization.getSendAuthorization().getGrant())
				|| (authorization.getRecvAuthorization() != null
						&& EndpointPermissionGrant.ALLOW == authorization.getRecvAuthorization().getGrant());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Channel [id=");
		builder.append(id);
		builder.append(" origin=").append(origin);
		builder.append(", destination=").append(destination);
		builder.append(", ds=").append(session);
		builder.append(", processingState=").append(processingState);
		builder.append("]");
		return builder.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setQuota(FlowQuota quota) {
		this.quota = quota;
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

	public Domain getDomain() {
		return domain;
	}

	/**
	 * IMPORTANT!!! only use if you really know what you're doing.
	 * 
	 * @param domain
	 */
	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public ChannelOrigin getOrigin() {
		return origin;
	}

	private void setOrigin(ChannelOrigin origin) {
		this.origin = origin;
	}

	public ChannelDestination getDestination() {
		return destination;
	}

	private void setDestination(ChannelDestination destination) {
		this.destination = destination;
	}

	public DestinationSession getSession() {
		return session;
	}

	public void setSession(DestinationSession session) {
		this.session = session;
	}

	public ProcessingState getProcessingState() {
		return processingState;
	}

	public void setProcessingState(ProcessingState processingState) {
		this.processingState = processingState;
	}

	public FlowQuota getQuota() {
		return quota;
	}

	public ChannelAuthorization getAuthorization() {
		return authorization;
	}

	public void setAuthorization(ChannelAuthorization authorization) {
		this.authorization = authorization;
	}

}
