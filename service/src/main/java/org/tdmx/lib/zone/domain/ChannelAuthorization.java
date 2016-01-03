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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;

/**
 * An ChannelAuthorization part of a Channel
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "ChannelAuthorization")
public class ChannelAuthorization implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	// TODO CA#ProcessingState: update after successful/failed relay of its changed state

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ChannelAuthorizationIdGen")
	@TableGenerator(name = "ChannelAuthorizationIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "channelauthObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@OneToOne(optional = false, fetch = FetchType.LAZY, mappedBy = "authorization")
	private Channel channel;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "grant", column = @Column(name = "sendGrant", length = EndpointPermissionGrant.MAX_PERMISSION_LEN) ),
			@AttributeOverride(name = "maxPlaintextSizeBytes", column = @Column(name = "sendMaxPlaintextBytes") ),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "sendSignDate") ),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "sendSignerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN) ),
			@AttributeOverride(name = "signature.value", column = @Column(name = "sendSignature", length = AgentSignature.MAX_SIGNATURE_LEN) ),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "sendSignAlg", length = AgentSignature.MAX_SIG_ALG_LEN) ) })
	private EndpointPermission sendAuthorization;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "grant", column = @Column(name = "recvGrant", length = EndpointPermissionGrant.MAX_PERMISSION_LEN) ),
			@AttributeOverride(name = "maxPlaintextSizeBytes", column = @Column(name = "recvMaxPlaintextBytes") ),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "recvSignDate") ),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "recvSignerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN) ),
			@AttributeOverride(name = "signature.value", column = @Column(name = "recvSignature", length = AgentSignature.MAX_SIGNATURE_LEN) ),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "recvSignAlg", length = AgentSignature.MAX_SIG_ALG_LEN) ) })
	private EndpointPermission recvAuthorization;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "grant", column = @Column(name = "reqSendGrant", length = EndpointPermissionGrant.MAX_PERMISSION_LEN) ),
			@AttributeOverride(name = "maxPlaintextSizeBytes", column = @Column(name = "reqSendMaxPlaintextBytes") ),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "reqSendSignDate") ),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "reqSendSignerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN) ),
			@AttributeOverride(name = "signature.value", column = @Column(name = "reqSendSignature", length = AgentSignature.MAX_SIGNATURE_LEN) ),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "reqSendSignAlg", length = AgentSignature.MAX_SIG_ALG_LEN) ) })
	private EndpointPermission reqSendAuthorization;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "grant", column = @Column(name = "reqRecvGrant", length = EndpointPermissionGrant.MAX_PERMISSION_LEN) ),
			@AttributeOverride(name = "maxPlaintextSizeBytes", column = @Column(name = "reqRecvMaxPlaintextBytes") ),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "reqRecvSignDate") ),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "reqRecvSignerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN) ),
			@AttributeOverride(name = "signature.value", column = @Column(name = "reqRecvSignature", length = AgentSignature.MAX_SIGNATURE_LEN) ),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "reqRecvSignAlg", length = AgentSignature.MAX_SIG_ALG_LEN) ) })
	private EndpointPermission reqRecvAuthorization;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "highMarkBytes", column = @Column(name = "limitHighBytes") ),
			@AttributeOverride(name = "lowMarkBytes", column = @Column(name = "limitLowBytes") ) })
	private FlowLimit limit;

	// the signature can be null when a requested ca is relayed in before being set by the local DAC.
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "signatureDate", column = @Column(name = "signatureDate") ),
			@AttributeOverride(name = "certificateChainPem", column = @Column(name = "signerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN) ),
			@AttributeOverride(name = "value", column = @Column(name = "signature", length = AgentSignature.MAX_SIGNATURE_LEN) ),
			@AttributeOverride(name = "algorithm", column = @Column(name = "signatureAlg", length = AgentSignature.MAX_SIG_ALG_LEN) ) })
	private AgentSignature signature;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "status", column = @Column(name = "processingStatus", length = ProcessingStatus.MAX_PROCESSINGSTATUS_LEN, nullable = false) ),
			@AttributeOverride(name = "timestamp", column = @Column(name = "processingTimestamp", nullable = false) ),
			@AttributeOverride(name = "errorCode", column = @Column(name = "processingErrorCode") ),
			@AttributeOverride(name = "errorMessage", column = @Column(name = "processingErrorMessage", length = ProcessingState.MAX_ERRORMESSAGE_LEN) ) })
	private ProcessingState processingState;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ChannelAuthorization() {
	}

	public ChannelAuthorization(Channel channel) {
		setChannel(channel);
		channel.setAuthorization(this);
	}

	public ChannelAuthorization(Channel channel, ChannelAuthorization other) {
		this(channel);
		if (other != null) {
			setProcessingState(other.getProcessingState());
			setRecvAuthorization(other.getRecvAuthorization());
			setReqRecvAuthorization(other.getReqRecvAuthorization());
			setReqSendAuthorization(other.getReqSendAuthorization());
			setSendAuthorization(other.getSendAuthorization());
			setSignature(other.getSignature());
			setLimit(other.getLimit());
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelAuthorization [id=");
		builder.append(id);
		builder.append(", sendAuthorization=").append(sendAuthorization);
		builder.append(", recvAuthorization=").append(recvAuthorization);
		builder.append(", reqSendAuthorization=").append(reqSendAuthorization);
		builder.append(", reqRecvAuthorization=").append(reqRecvAuthorization);
		builder.append(", limit=").append(limit);
		builder.append(", signature=").append(signature);
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

	private void setChannel(Channel channel) {
		this.channel = channel;
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

	public EndpointPermission getSendAuthorization() {
		return sendAuthorization;
	}

	public void setSendAuthorization(EndpointPermission sendAuthorization) {
		this.sendAuthorization = sendAuthorization;
	}

	public EndpointPermission getRecvAuthorization() {
		return recvAuthorization;
	}

	public void setRecvAuthorization(EndpointPermission recvAuthorization) {
		this.recvAuthorization = recvAuthorization;
	}

	public EndpointPermission getReqSendAuthorization() {
		return reqSendAuthorization;
	}

	public void setReqSendAuthorization(EndpointPermission reqSendAuthorization) {
		this.reqSendAuthorization = reqSendAuthorization;
	}

	public EndpointPermission getReqRecvAuthorization() {
		return reqRecvAuthorization;
	}

	public void setReqRecvAuthorization(EndpointPermission reqRecvAuthorization) {
		this.reqRecvAuthorization = reqRecvAuthorization;
	}

	public FlowLimit getLimit() {
		return limit;
	}

	public void setLimit(FlowLimit limit) {
		this.limit = limit;
	}

	public AgentSignature getSignature() {
		return signature;
	}

	public void setSignature(AgentSignature signature) {
		this.signature = signature;
	}

	public ProcessingState getProcessingState() {
		return processingState;
	}

	public void setProcessingState(ProcessingState processingState) {
		this.processingState = processingState;
	}

}
