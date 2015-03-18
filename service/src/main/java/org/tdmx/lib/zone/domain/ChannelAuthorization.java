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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.tdmx.lib.control.job.ZoneTransferJobExecutorImpl;

/**
 * An ChannelAuthorization (within a Zone) managed by a ServiceProvider
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

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ChannelAuthorizationIdGen")
	@TableGenerator(name = "ChannelAuthorizationIdGen", table = "MaxValueEntry", pkColumnName = "NAME", pkColumnValue = "channelauthObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Zone zone;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "localName", column = @Column(name = "originAddress", nullable = false)),
			@AttributeOverride(name = "domainName", column = @Column(name = "originDomain", nullable = false)),
			@AttributeOverride(name = "serviceProvider", column = @Column(name = "originSP", nullable = false)) })
	private ChannelOrigin origin;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "localName", column = @Column(name = "destAddress", nullable = false)),
			@AttributeOverride(name = "domainName", column = @Column(name = "destDomain", nullable = false)),
			@AttributeOverride(name = "serviceName", column = @Column(name = "destService", nullable = false)),
			@AttributeOverride(name = "serviceProvider", column = @Column(name = "destSP", nullable = false)) })
	private ChannelDestination destination;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "grant", column = @Column(name = "sendGrant", length = EndpointPermissionGrant.MAX_PERMISSION_LEN)),
			@AttributeOverride(name = "maxPlaintextSizeBytes", column = @Column(name = "sendMaxPlaintextBytes")),
			@AttributeOverride(name = "validUntil", column = @Column(name = "sendValidUntil")),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "sendSignDate")),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "sendSignerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN)),
			@AttributeOverride(name = "signature.value", column = @Column(name = "sendSignature", length = AgentSignature.MAX_SIGNATURE_LEN)),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "sendSignAlg", length = AgentSignature.MAX_SIG_ALG_LEN)) })
	private EndpointPermission sendAuthorization;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "grant", column = @Column(name = "recvGrant", length = EndpointPermissionGrant.MAX_PERMISSION_LEN)),
			@AttributeOverride(name = "maxPlaintextSizeBytes", column = @Column(name = "recvMaxPlaintextBytes")),
			@AttributeOverride(name = "validUntil", column = @Column(name = "recvValidUntil")),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "recvSignDate")),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "recvSignerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN)),
			@AttributeOverride(name = "signature.value", column = @Column(name = "recvSignature", length = AgentSignature.MAX_SIGNATURE_LEN)),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "recvSignAlg", length = AgentSignature.MAX_SIG_ALG_LEN)) })
	private EndpointPermission recvAuthorization;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "grant", column = @Column(name = "reqSendGrant", length = EndpointPermissionGrant.MAX_PERMISSION_LEN)),
			@AttributeOverride(name = "maxPlaintextSizeBytes", column = @Column(name = "reqSendMaxPlaintextBytes")),
			@AttributeOverride(name = "validUntil", column = @Column(name = "reqSendValidUntil")),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "reqSendSignDate")),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "reqSendSignerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN)),
			@AttributeOverride(name = "signature.value", column = @Column(name = "reqSendSignature", length = AgentSignature.MAX_SIGNATURE_LEN)),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "reqSendSignAlg", length = AgentSignature.MAX_SIG_ALG_LEN)) })
	private EndpointPermission reqSendAuthorization;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "grant", column = @Column(name = "reqRecvGrant", length = EndpointPermissionGrant.MAX_PERMISSION_LEN)),
			@AttributeOverride(name = "maxPlaintextSizeBytes", column = @Column(name = "reqRecvMaxPlaintextBytes")),
			@AttributeOverride(name = "validUntil", column = @Column(name = "reqRecvValidUntil")),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "reqRecvSignDate")),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "reqRecvSignerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN)),
			@AttributeOverride(name = "signature.value", column = @Column(name = "reqRecvSignature", length = AgentSignature.MAX_SIGNATURE_LEN)),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "reqRecvSignAlg", length = AgentSignature.MAX_SIG_ALG_LEN)) })
	private EndpointPermission reqRecvAuthorization;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "highMarkBytes", column = @Column(name = "unsentHigh")),
			@AttributeOverride(name = "lowMarkBytes", column = @Column(name = "unsentLow")) })
	private FlowLimit unsentBuffer;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "highMarkBytes", column = @Column(name = "undeliveredHigh")),
			@AttributeOverride(name = "lowMarkBytes", column = @Column(name = "undeliveredLow")) })
	private FlowLimit undeliveredBuffer;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "signatureDate", column = @Column(name = "signatureDate", nullable = false)),
			@AttributeOverride(name = "certificateChainPem", column = @Column(name = "signerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN, nullable = false)),
			@AttributeOverride(name = "value", column = @Column(name = "signature", length = AgentSignature.MAX_SIGNATURE_LEN, nullable = false)),
			@AttributeOverride(name = "algorithm", column = @Column(name = "signatureAlg", length = AgentSignature.MAX_SIG_ALG_LEN, nullable = false)) })
	private AgentSignature signature;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	ChannelAuthorization() {
	}

	public ChannelAuthorization(Zone zone) {
		this.zone = zone;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelAuthorization [id=");
		builder.append(id);
		builder.append(" origin=").append(origin);
		builder.append(", destination=").append(destination);
		builder.append(", sendAuthorization=").append(sendAuthorization);
		builder.append(", recvAuthorization=").append(recvAuthorization);
		builder.append(", reqSendAuthorization=").append(reqSendAuthorization);
		builder.append(", reqRecvAuthorization=").append(reqRecvAuthorization);
		builder.append(", undeliveredBuffer=").append(undeliveredBuffer);
		builder.append(", unsentBuffer=").append(unsentBuffer);
		builder.append(", signature=").append(signature);
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

	public Zone getZone() {
		return zone;
	}

	/**
	 * Should only be used for ZoneDB partition transfer. {@link ZoneTransferJobExecutorImpl}
	 * 
	 * @param zone
	 */
	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public ChannelOrigin getOrigin() {
		return origin;
	}

	public void setOrigin(ChannelOrigin origin) {
		this.origin = origin;
	}

	public ChannelDestination getDestination() {
		return destination;
	}

	public void setDestination(ChannelDestination destination) {
		this.destination = destination;
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

	public FlowLimit getUnsentBuffer() {
		return unsentBuffer;
	}

	public void setUnsentBuffer(FlowLimit unsentBuffer) {
		this.unsentBuffer = unsentBuffer;
	}

	public FlowLimit getUndeliveredBuffer() {
		return undeliveredBuffer;
	}

	public void setUndeliveredBuffer(FlowLimit undeliveredBuffer) {
		this.undeliveredBuffer = undeliveredBuffer;
	}

	public AgentSignature getSignature() {
		return signature;
	}

	public void setSignature(AgentSignature signature) {
		this.signature = signature;
	}

}
