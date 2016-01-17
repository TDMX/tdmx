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
package org.tdmx.server.ros;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.zone.domain.ChannelMessage;

/**
 * The reference to an object which can be relayed.
 * 
 * @author Peter
 *
 */
public class RelayJobContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayJobContext.class);

	// parent reference
	private final RelayChannelContext channelContext;

	// reference
	private final RelayJobType type;
	private final Long objectId;

	private Object relayObject;
	private long timestamp = 0L;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public RelayJobContext(RelayChannelContext channelContext, RelayJobType type, Long objectId) {
		this.channelContext = channelContext;
		this.type = type;
		this.objectId = objectId;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RelayJobContext [type=").append(type);
		builder.append("objectId=").append(objectId).append("]");
		return builder.toString();
	}

	public void setChannelMessage(ChannelMessage msg) {
		relayObject = msg;
		timestamp = msg.getSignature().getSignatureDate().getTime();
	}

	public ChannelMessage getChannelMessage() {
		if (relayObject instanceof ChannelMessage) {
			return (ChannelMessage) relayObject;
		}
		return null;
	}

	// TODO #95 DR

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public RelayChannelContext getChannelContext() {
		return channelContext;
	}

	public RelayJobType getType() {
		return type;
	}

	public Long getObjectId() {
		return objectId;
	}

	public long getTimestamp() {
		return timestamp;
	}

}
