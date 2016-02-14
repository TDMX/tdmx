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

import org.tdmx.lib.zone.domain.ChannelMessage;

public class MessageContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final ChannelMessage msg;

	private long redeliverAfterTimestamp = 0;
	private int numDeliveries = 0;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public MessageContext(ChannelMessage msg) {
		this.msg = msg;

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public String getMsgId() {
		return this.msg.getMsgId();
	}

	@Override
	public int hashCode() {
		return msg.getMsgId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MessageContext) {
			return false;
		} else {
			MessageContext other = (MessageContext) obj;
			return msg.getMsgId().equals(other.getMsgId());
		}
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

	public long getRedeliverAfterTimestamp() {
		return redeliverAfterTimestamp;
	}

	public void setRedeliverAfterTimestamp(long redeliverAfterTimestamp) {
		this.redeliverAfterTimestamp = redeliverAfterTimestamp;
	}

	public int getNumDeliveries() {
		return numDeliveries;
	}

	public void setNumDeliveries(int numDeliveries) {
		this.numDeliveries = numDeliveries;
	}

	public ChannelMessage getMsg() {
		return msg;
	}

}
