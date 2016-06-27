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
import java.util.Date;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;

/**
 * An ChannelMessageStatus stores the control information required to transactionally send, receive and relay the linked
 * {@link ChannelMessage}
 * 
 * The origin and destination UserCertificate's serial numbers are denormalized here to allow more efficient selecting
 * messages or delivery receipts for specific users at either end.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "MessageState")
public class MessageState implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_DESTINATION_NAME_LEN = 255;
	public static final int MAX_XA_TXID_LEN = 256;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(optional = false, fetch = FetchType.LAZY, mappedBy = "state")
	private ChannelMessage msg;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Zone zone;

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

	@Column(nullable = false)
	private int originSerialNr = -1;

	@Column(nullable = false)
	private int destinationSerialNr = -1;

	// -------------------------------------------------------------------------
	// CONTROL FIELDS
	// -------------------------------------------------------------------------

	@Enumerated(EnumType.STRING)
	@Column(length = MessageStatus.MAX_MSGSTATUS_LEN, nullable = false)
	private MessageStatus status;

	@Enumerated(EnumType.STRING)
	@Column(length = ReceiptStatus.MAX_DRSTATUS_LEN)
	private ReceiptStatus report;

	@Column
	private String txId;

	@Column
	private int deliveryCount = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Date redeliverAfter; // the time after delivery exception where we can consider redelivery

	@Column
	private Integer deliveryErrorCode;

	@Column(length = ProcessingState.MAX_ERRORMESSAGE_LEN)
	private String deliveryErrorMessage;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "status", column = @Column(name = "processingStatus", length = ProcessingStatus.MAX_PROCESSINGSTATUS_LEN, nullable = false) ),
			@AttributeOverride(name = "timestamp", column = @Column(name = "processingTimestamp", nullable = false) ),
			@AttributeOverride(name = "errorCode", column = @Column(name = "processingErrorCode", nullable = true) ),
			@AttributeOverride(name = "errorMessage", column = @Column(name = "processingErrorMessage", length = ProcessingState.MAX_ERRORMESSAGE_LEN) ) })
	private ProcessingState processingState = ProcessingState.none();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public MessageState() {
	}

	/**
	 * Construct a MessageStatus from a ChannelMessage. The ChannelMessage's Channel must be known.
	 * 
	 * @param zone
	 * @param msg
	 * @param originSerialNr
	 * @param destionationSerialNr
	 */
	public MessageState(Zone zone, ChannelMessage msg, MessageStatus status, int oSerialNr, int dSerialNr) {
		if (msg.getChannel() == null) {
			throw new IllegalStateException("missing channel");
		}
		setStatus(status);
		setZone(zone);
		setMsg(msg);

		setOriginSerialNr(oSerialNr);
		setOrigin(msg.getChannel().getOrigin());
		setDestinationSerialNr(dSerialNr);
		setDestination(msg.getChannel().getDestination());
	}

	public MessageState(Zone zone, ChannelMessage msg, MessageState other) {
		this(zone, msg, other.getStatus(), other.getOriginSerialNr(), other.getDestinationSerialNr());

		setDeliveryCount(other.getDeliveryCount());
		setDeliveryErrorCode(other.getDeliveryErrorCode());
		setDeliveryErrorMessage(other.getDeliveryErrorMessage());
		setTxId(other.getTxId());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelMessageState [id=");
		builder.append(id);
		builder.append(" status=").append(status);
		builder.append(" origin=").append(origin);
		builder.append(" destination=").append(destination);
		builder.append(" originSerialNr=").append(originSerialNr);
		builder.append(" destinationSerialNr=").append(destinationSerialNr);
		builder.append(" txid=").append(txId);
		builder.append(" deliveryCount=").append(deliveryCount);
		builder.append(" deliveryErrorCode=").append(deliveryErrorCode);
		builder.append(" deliveryErrorMessage=").append(deliveryErrorMessage);
		builder.append(" processingState=").append(processingState);
		builder.append("]");
		return builder.toString();
	}

	public ChannelName getChannelName() {
		return new ChannelName(origin, destination);
	}

	public boolean isSameDomain() {
		return origin.getDomainName().equals(destination.getDomainName());
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

	public ChannelMessage getMsg() {
		return msg;
	}

	public void setMsg(ChannelMessage msg) {
		this.msg = msg;
	}

	public Zone getZone() {
		return zone;
	}

	private void setZone(Zone zone) {
		this.zone = zone;
	}

	public MessageStatus getStatus() {
		return status;
	}

	public void setStatus(MessageStatus status) {
		this.status = status;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public int getDeliveryCount() {
		return deliveryCount;
	}

	public void setDeliveryCount(int deliveryCount) {
		this.deliveryCount = deliveryCount;
	}

	public Date getRedeliverAfter() {
		return redeliverAfter;
	}

	public void setRedeliverAfter(Date redeliverAfter) {
		this.redeliverAfter = redeliverAfter;
	}

	public Integer getDeliveryErrorCode() {
		return deliveryErrorCode;
	}

	public void setDeliveryErrorCode(Integer deliveryErrorCode) {
		this.deliveryErrorCode = deliveryErrorCode;
	}

	public String getDeliveryErrorMessage() {
		return deliveryErrorMessage;
	}

	public void setDeliveryErrorMessage(String deliveryErrorMessage) {
		this.deliveryErrorMessage = deliveryErrorMessage;
	}

	public ProcessingState getProcessingState() {
		return processingState;
	}

	public void setProcessingState(ProcessingState processingState) {
		this.processingState = processingState;
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

	public int getOriginSerialNr() {
		return originSerialNr;
	}

	public void setOriginSerialNr(int originSerialNr) {
		this.originSerialNr = originSerialNr;
	}

	public int getDestinationSerialNr() {
		return destinationSerialNr;
	}

	public void setDestinationSerialNr(int destinationSerialNr) {
		this.destinationSerialNr = destinationSerialNr;
	}

}
