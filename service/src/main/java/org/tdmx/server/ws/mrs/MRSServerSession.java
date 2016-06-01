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
package org.tdmx.server.ws.mrs;

import java.util.Map;

import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.Common.ObjectType;
import org.tdmx.server.ws.session.WebServiceSession;

public class MRSServerSession extends WebServiceSession {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	protected static final String SHORTCUT = "SHORTCUT";
	private static final String DESTINATION_PREFIX = "Destination:"; // DestinationName->
	private static final String MESSAGE_PREFIX = "Msg:"; // msgId->MessageRelayContext

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public MRSServerSession(String sessionId, AccountZone az, Zone zone, Domain domain) {
		super(sessionId);
		setAccountZone(az);
		setZone(zone);
		setDomain(domain);
	}

	/**
	 * A DestinationContextHolder holds a destination information in a session.
	 * 
	 * @author Peter
	 * 
	 */
	public static class DestinationContextHolder {
		private final String destinationName;
		private final ChannelDestination destination;

		// we cache the transfer MDS address and sessionId for this destination.
		private String tosTcpAddress;
		private String sessionId;

		public DestinationContextHolder(String destinationName, ChannelDestination dest) {
			this.destinationName = destinationName;
			this.destination = dest;
		}

		public ChannelDestination getDestination() {
			return destination;
		}

		public String getTosTcpAddress() {
			return tosTcpAddress;
		}

		public void setTosTcpAddress(String tosTcpAddress) {
			this.tosTcpAddress = tosTcpAddress;
		}

		public String getSessionId() {
			return sessionId;
		}

		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		public String getDestinationName() {
			return destinationName;
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * MRS sessions may not be idled out if any messages are not yet completely transferred and we have not yet time'd
	 * them out.
	 */
	@Override
	public boolean isIdle(java.util.Date lastCutoffDate) {
		if (super.isIdle(lastCutoffDate)) {
			for (Map.Entry<String, Object> attrs : attributeMap.entrySet()) {
				if (isMsgAttr(attrs.getKey())) {
					// if we have any incomplete messages, then we cannot be idle.
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean transferObject(ObjectType type, Map<AttributeId, Long> attributes) {
		// MRS does not handle any inbound objects
		return false;
	}

	public AccountZone getAccountZone() {
		return getAttribute(ACCOUNT_ZONE);
	}

	public Zone getZone() {
		return getAttribute(ZONE);
	}

	public Domain getDomain() {
		return getAttribute(DOMAIN);
	}

	public Channel getChannel() {
		return getAttribute(CHANNEL);
	}

	public TemporaryChannel getTemporaryChannel() {
		return getAttribute(TEMP_CHANNEL);
	}

	// needs to be public since shortcut ROSConnectionProvider set this
	public void setChannel(Channel c) {
		setAttribute(CHANNEL, c);
	}

	// needs to be public since shortcut ROSConnectionProvider set this
	public void setTemporaryChannel(TemporaryChannel c) {
		setAttribute(TEMP_CHANNEL, c);
	}

	public void setSameSegmentShortcutSession() {
		setAttribute(SHORTCUT, Boolean.TRUE);
	}

	/**
	 * Same segment sessions are a shortcut where actual relaying does not take place, but data directly
	 * 
	 * @return
	 */
	public boolean isSameSegmentShortcutSession() {
		return Boolean.TRUE == getAttribute(SHORTCUT);
	}

	public DestinationContextHolder getDestinationContext(String destinationName) {
		return getAttribute(createDestinationKey(destinationName));
	}

	public void setDestinationContext(String destinationName, DestinationContextHolder ddh) {
		setAttribute(createDestinationKey(destinationName), ddh);
	}

	public MessageRelayContext getMessageContext(String msgId) {
		return getAttribute(createMessageKey(msgId));
	}

	public void setMessageContext(String msgId, MessageRelayContext mrc) {
		setAttribute(createMessageKey(msgId), mrc);
	}

	public void removeMessageContext(String msgId) {
		removeAttribute(createMessageKey(msgId));
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	void setAccountZone(AccountZone az) {
		setAttribute(ACCOUNT_ZONE, az);
	}

	void setZone(Zone z) {
		setAttribute(ZONE, z);
	}

	void setDomain(Domain d) {
		setAttribute(DOMAIN, d);
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private String createDestinationKey(String destinationName) {
		return DESTINATION_PREFIX + destinationName;
	}

	private String createMessageKey(String msgId) {
		return MESSAGE_PREFIX + msgId;
	}

	private boolean isMsgAttr(String attributeKey) {
		return attributeKey.startsWith(MESSAGE_PREFIX);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
