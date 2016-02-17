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
package org.tdmx.server.ws.mds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(TransactionContext.class);

	private final String txId;

	private long txTimeoutTimestamp;

	// the message received in the context of the transaction.
	private MessageContext currentMessage;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public TransactionContext(String txId) {
		this.txId = txId;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public int hashCode() {
		return txId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransactionContext other = (TransactionContext) obj;

		return txId.equals(other.txId);
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

	public long getTxTimeoutTimestamp() {
		return txTimeoutTimestamp;
	}

	public void setTxTimeoutTimestamp(long txTimeoutTimestamp) {
		this.txTimeoutTimestamp = txTimeoutTimestamp;
	}

	public MessageContext getCurrentMessage() {
		return currentMessage;
	}

	public void setCurrentMessage(MessageContext currentMessage) {
		this.currentMessage = currentMessage;
	}

	public String getTxId() {
		return txId;
	}

}
