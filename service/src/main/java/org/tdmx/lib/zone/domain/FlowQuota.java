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

import javax.persistence.Column;
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

/**
 * An FlowQuota is separated as it's own instance from the ChannelFlowOrigin so that it can be updated with a higher
 * frequency without incurring the penalty of the data of the ChannelFlowOrigin not changing fast.
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
	@TableGenerator(name = "FlowQuotaIdGen", table = "MaxValueEntry", pkColumnName = "NAME", pkColumnValue = "zoneObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@OneToOne(optional = false, fetch = FetchType.LAZY, mappedBy = "quota")
	private ChannelFlowOrigin flow;

	@Enumerated(EnumType.STRING)
	@Column(length = FlowControlStatus.MAX_FLOWCONTROL_STATUS_LEN, nullable = false)
	private FlowControlStatus senderStatus;

	@Column(nullable = false)
	private BigInteger unsentBytes;

	@Enumerated(EnumType.STRING)
	@Column(length = FlowControlStatus.MAX_FLOWCONTROL_STATUS_LEN, nullable = false)
	private FlowControlStatus receiverStatus;

	@Column(nullable = false)
	private BigInteger undeliveredBytes;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	FlowQuota() {
	}

	public FlowQuota(ChannelFlowOrigin flow) {
		setFlow(flow);
		setUndeliveredBytes(BigInteger.ZERO);
		setUnsentBytes(BigInteger.ZERO);
		setSenderStatus(FlowControlStatus.OPEN);
		setReceiverStatus(FlowControlStatus.OPEN);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FlowQuota [id=");
		builder.append(id);
		builder.append(", unsentBytes=").append(unsentBytes);
		builder.append(", undeliveredBytes=").append(undeliveredBytes);
		builder.append(", senderStatus=").append(senderStatus);
		builder.append(", receiverStatus=").append(receiverStatus);
		builder.append("]");
		return builder.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setFlow(ChannelFlowOrigin flow) {
		this.flow = flow;
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

	public BigInteger getUnsentBytes() {
		return unsentBytes;
	}

	public void setUnsentBytes(BigInteger unsentBytes) {
		this.unsentBytes = unsentBytes;
	}

	public BigInteger getUndeliveredBytes() {
		return undeliveredBytes;
	}

	public void setUndeliveredBytes(BigInteger undeliveredBytes) {
		this.undeliveredBytes = undeliveredBytes;
	}

	public FlowControlStatus getSenderStatus() {
		return senderStatus;
	}

	public void setSenderStatus(FlowControlStatus senderStatus) {
		this.senderStatus = senderStatus;
	}

	public FlowControlStatus getReceiverStatus() {
		return receiverStatus;
	}

	public void setReceiverStatus(FlowControlStatus receiverStatus) {
		this.receiverStatus = receiverStatus;
	}

	public ChannelFlowOrigin getFlow() {
		return flow;
	}

}
