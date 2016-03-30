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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * An TransactionState describes the (persistent) XA transaction status related to a message.
 * 
 * @author Peter Klauser
 * 
 */
@Embeddable
public class TransactionState implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_XA_TXID_LEN = 256;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	@Column
	private String txId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Date txTimeoutTimestamp; // the time where the XA transaction time's out

	@Column
	private int deliveryCount = 0;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private TransactionState() {
		this(null, null, 0);
	}

	private TransactionState(String txId, Date txTimeoutTimestamp, int deliveryCount) {
		this.txId = txId;
		this.txTimeoutTimestamp = txTimeoutTimestamp;
	}

	public static TransactionState none() {
		return new TransactionState(null, null, 0);
	}

	public static TransactionState none(int deliveryCount) {
		return new TransactionState(null, null, deliveryCount);
	}

	public static TransactionState prepared(String txId, Date txTimeoutTimestamp, int deliveryCount) {
		return new TransactionState(txId, txTimeoutTimestamp, deliveryCount);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeliveryState [");
		builder.append(" txId=").append(txId);
		builder.append(" txTimeoutTimestamp=").append(txTimeoutTimestamp);
		builder.append(" deliveryCount=").append(deliveryCount);
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

	public String getTxId() {
		return txId;
	}

	public Date getTxTimeoutTimestamp() {
		return txTimeoutTimestamp;
	}

	public int getDeliveryCount() {
		return deliveryCount;
	}

}
