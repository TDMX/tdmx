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
import java.math.BigInteger;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
 * An FlowQuota is separated as it's own instance from the Channel so that it can be updated with a higher frequency
 * without incurring the penalty of the data of the Channel not changing fast.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "FlowQuota")
public class FlowQuota implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -1L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "FlowQuotaIdGen")
	@TableGenerator(name = "FlowQuotaIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "zoneObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@OneToOne(optional = false, fetch = FetchType.LAZY, mappedBy = "quota")
	private Channel channel;

	@Enumerated(EnumType.STRING)
	@Column(length = ChannelAuthorizationStatus.MAX_AUTH_STATUS_LEN, nullable = false)
	private ChannelAuthorizationStatus authorizationStatus;

	@Enumerated(EnumType.STRING)
	@Column(length = FlowControlStatus.MAX_FLOWCONTROL_STATUS_LEN, nullable = false)
	private FlowControlStatus relayStatus;

	@Enumerated(EnumType.STRING)
	@Column(length = FlowControlStatus.MAX_FLOWCONTROL_STATUS_LEN, nullable = false)
	private FlowControlStatus flowStatus;

	@Column(nullable = false)
	private BigInteger usedBytes;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "highMarkBytes", column = @Column(name = "limitHighBytes") ),
			@AttributeOverride(name = "lowMarkBytes", column = @Column(name = "limitLowBytes") ) })
	private FlowLimit limit;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "status", column = @Column(name = "processingStatus", length = ProcessingStatus.MAX_PROCESSINGSTATUS_LEN, nullable = false) ),
			@AttributeOverride(name = "timestamp", column = @Column(name = "processingTimestamp", nullable = false) ),
			@AttributeOverride(name = "errorCode", column = @Column(name = "processingErrorCode") ),
			@AttributeOverride(name = "errorMessage", column = @Column(name = "processingErrorMessage", length = ProcessingState.MAX_ERRORMESSAGE_LEN) ) })
	private ProcessingState processingState = ProcessingState.none(); // of relay of FlowControl "OPEN" event

	// max size of messages denormalized from min(send+recv) authorization
	@Column
	private BigInteger maxPlaintextSizeBytes;

	// max redelivery count - new part of destination authorization
	@Column
	private Integer maxRedeliveryCount;

	// redeliver after wait seconds - new part of destination authorization
	@Column
	private Integer redeliveryDelaySec;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	FlowQuota() {
	}

	public FlowQuota(Channel channel) {
		setChannel(channel);
		setUsedBytes(BigInteger.ZERO);
		setFlowStatus(FlowControlStatus.OPEN);
		setRelayStatus(FlowControlStatus.OPEN);
		updateAuthorizationInfo();
	}

	public FlowQuota(Channel channel, FlowQuota other) {
		setChannel(channel);
		setUsedBytes(other.getUsedBytes());
		setRelayStatus(other.getRelayStatus());
		setFlowStatus(other.getFlowStatus());
		setAuthorizationStatus(other.getAuthorizationStatus());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Update the FlowQuota's denormalized data regarding the authorization state and flow control limits.
	 */
	public void updateAuthorizationInfo() {
		if (channel != null && channel.getAuthorization() != null) {
			setAuthorizationStatus(
					channel.isOpen() ? ChannelAuthorizationStatus.OPEN : ChannelAuthorizationStatus.CLOSED);

			// denormalized info from the CA.
			ChannelAuthorization ca = channel.getAuthorization();
			setLimit(ca.getLimit());
			setMaxPlaintextSizeBytes(ca.getMaxPlaintextPayloadSize());
			setRedeliveryDelaySec(ca.getRedeliveryDelaySec());
			setMaxRedeliveryCount(ca.getMaxRedeliveryCount());
		}
	}

	/**
	 * Adapt flow control to increase of bytes to be sent out.
	 * 
	 * @param payloadSizeBytes
	 */
	public void incrementBufferOnSend(long payloadSizeBytes) {
		setUsedBytes(getUsedBytes().add(BigInteger.valueOf(payloadSizeBytes)));
		if (getUsedBytes().subtract(getLimit().getHighMarkBytes()).compareTo(BigInteger.ZERO) > 0) {
			// quota exceeded, close send
			setFlowStatus(FlowControlStatus.CLOSED);
		}
	}

	/**
	 * Adapt flow control to the bytes which are incoming over the relay.
	 * 
	 * @param payloadSizeBytes
	 */
	public void incrementBufferOnRelay(long payloadSizeBytes) {
		setUsedBytes(getUsedBytes().add(BigInteger.valueOf(payloadSizeBytes)));
		if (getUsedBytes().subtract(getLimit().getHighMarkBytes()).compareTo(BigInteger.ZERO) > 0) {
			// quota exceeded, close relay
			setFlowStatus(FlowControlStatus.CLOSED);
			setRelayStatus(FlowControlStatus.CLOSED);
		}
	}

	/**
	 * Return true if there is quota available for the payload size requested.
	 * 
	 * @param payloadSizeBytes
	 * @return true if there is quota available for the payload size requested.
	 */
	public boolean hasAvailableQuotaFor(long payloadSizeBytes) {
		return (getUsedBytes().add(BigInteger.valueOf(payloadSizeBytes)).subtract(getLimit().getHighMarkBytes())
				.compareTo(BigInteger.ZERO) <= 0);
	}

	/**
	 * Adapt flow control to the bytes which are consumed by receiving.
	 * 
	 * @param payloadSizeBytes
	 * @return true if the FC status changes from closed to open.
	 */
	public boolean reduceBufferOnReceive(long payloadSizeBytes) {
		FlowControlStatus oldFC = getFlowStatus();
		setUsedBytes(getUsedBytes().subtract(BigInteger.valueOf(payloadSizeBytes)));
		if (getUsedBytes().subtract(getLimit().getLowMarkBytes()).compareTo(BigInteger.ZERO) < 0) {
			// quota below low limit, open relay
			setFlowStatus(FlowControlStatus.OPEN);
			setRelayStatus(FlowControlStatus.OPEN);
		}
		return FlowControlStatus.CLOSED == oldFC && FlowControlStatus.OPEN == getFlowStatus();
	}

	/**
	 * Adapt flow control to the bytes which are relayed out to the destination via the relay.
	 * 
	 * @param payloadSizeBytes
	 * @return true if the FC status changes from closed to open.
	 */
	public boolean reduceBuffer(long payloadSizeBytes) {
		FlowControlStatus oldFC = getFlowStatus();
		setUsedBytes(getUsedBytes().subtract(BigInteger.valueOf(payloadSizeBytes)));
		if (getUsedBytes().subtract(getLimit().getLowMarkBytes()).compareTo(BigInteger.ZERO) < 0) {
			// quota below low limit
			setFlowStatus(FlowControlStatus.OPEN);
		}
		return FlowControlStatus.CLOSED == oldFC && FlowControlStatus.OPEN == getFlowStatus();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FlowQuota [id=");
		builder.append(id);
		builder.append(", authorizationStatus=").append(authorizationStatus);
		builder.append(", relayStatus=").append(relayStatus);
		builder.append(", flowStatus=").append(flowStatus);
		builder.append(", usedBytes=").append(usedBytes);
		builder.append(", limit=").append(limit);
		builder.append(", maxPlaintextSizeBytes=").append(maxPlaintextSizeBytes);
		builder.append(", maxRedeliveryCount=").append(maxRedeliveryCount);
		builder.append(", redeliveryDelaySec=").append(redeliveryDelaySec);
		builder.append(", ps=").append(processingState);
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

	public ChannelAuthorizationStatus getAuthorizationStatus() {
		return authorizationStatus;
	}

	public void setAuthorizationStatus(ChannelAuthorizationStatus authorizationStatus) {
		this.authorizationStatus = authorizationStatus;
	}

	public FlowControlStatus getRelayStatus() {
		return relayStatus;
	}

	public void setRelayStatus(FlowControlStatus relayStatus) {
		this.relayStatus = relayStatus;
	}

	public FlowControlStatus getFlowStatus() {
		return flowStatus;
	}

	public void setFlowStatus(FlowControlStatus flowStatus) {
		this.flowStatus = flowStatus;
	}

	public BigInteger getUsedBytes() {
		return usedBytes;
	}

	public void setUsedBytes(BigInteger usedBytes) {
		this.usedBytes = usedBytes;
	}

	public FlowLimit getLimit() {
		return limit;
	}

	public void setLimit(FlowLimit limit) {
		this.limit = limit;
	}

	public ProcessingState getProcessingState() {
		return processingState;
	}

	public void setProcessingState(ProcessingState processingState) {
		this.processingState = processingState;
	}

	public long getMaxPlaintextSizeBytes() {
		return maxPlaintextSizeBytes != null ? maxPlaintextSizeBytes.longValue() : 0L;
	}

	public void setMaxPlaintextSizeBytes(BigInteger maxPlaintextSizeBytes) {
		this.maxPlaintextSizeBytes = maxPlaintextSizeBytes;
	}

	public int getMaxRedeliveryCount() {
		return maxRedeliveryCount != null ? maxRedeliveryCount : 0;
	}

	public void setMaxRedeliveryCount(Integer maxRedeliveryCount) {
		this.maxRedeliveryCount = maxRedeliveryCount;
	}

	public int getRedeliveryDelaySec() {
		return redeliveryDelaySec != null ? redeliveryDelaySec : 0;
	}

	public void setRedeliveryDelaySec(Integer redeliveryDelaySec) {
		this.redeliveryDelaySec = redeliveryDelaySec;
	}

}
