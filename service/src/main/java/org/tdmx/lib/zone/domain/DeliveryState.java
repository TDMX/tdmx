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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdmx.core.system.lang.StringUtils;

/**
 * An DeliveryState describes the current (persistent) status of delivery of a message.
 * 
 * @author Peter Klauser
 * 
 */
@Embeddable
public class DeliveryState implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_ERRORMESSAGE_LEN = 2048;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	@Enumerated(EnumType.STRING)
	@Column
	private DeliveryStatus status;

	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Date timestamp; // the time since we've been in this status

	@Column
	private Integer errorCode;

	@Column
	private String errorMessage;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private DeliveryState() {
		this(DeliveryStatus.UNDELIVERED);
	}

	private DeliveryState(DeliveryStatus currentStatus) {
		status = currentStatus;
		timestamp = new Date();
	}

	public static DeliveryState submitted() {
		return new DeliveryState(DeliveryStatus.SUBMITTED);
	}

	public static DeliveryState transferred() {
		return new DeliveryState(DeliveryStatus.TRANSFERRED);
	}

	public static DeliveryState ready() {
		return new DeliveryState(DeliveryStatus.READY);
	}

	public static DeliveryState exception(int errorCode, String errorMsg) {
		DeliveryState e = new DeliveryState(DeliveryStatus.EXCEPTION);
		e.setErrorCode(errorCode);
		e.setErrorMessage(errorMsg);
		return e;
	}

	public static DeliveryState delivered() {
		return new DeliveryState(DeliveryStatus.DELIVERED);
	}

	public static DeliveryState undelivered(int errorCode, String errorMsg) {
		DeliveryState e = new DeliveryState(DeliveryStatus.UNDELIVERED);
		e.setErrorCode(errorCode);
		e.setErrorMessage(errorMsg);
		return e;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeliveryState [");
		builder.append(" status=").append(status);
		builder.append(" timestamp=").append(timestamp);
		builder.append(" errorCode=").append(errorCode);
		builder.append(" errorMessage=").append(errorMessage);
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

	public DeliveryStatus getStatus() {
		return status;
	}

	public void setStatus(DeliveryStatus status) {
		this.status = status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = StringUtils.truncateToMaxLen(errorMessage, MAX_ERRORMESSAGE_LEN);
	}

}
