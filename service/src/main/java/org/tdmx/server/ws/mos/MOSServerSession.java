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
package org.tdmx.server.ws.mos;

import java.util.Map;

import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.Common.ObjectType;
import org.tdmx.server.ws.session.WebServiceSession;

/**
 * The MOSServerSession is shared by all concurrently sending AgentCredentials of a particular Address.
 * 
 * @author Peter
 * 
 */
public class MOSServerSession extends WebServiceSession {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final String CHANNEL_PREFIX = "Channel:"; // ChannelKey->
	private static final String DESTINATION_PREFIX = "Destination:"; // DestinationName->
	private static final String TX_PREFIX = "Tx:"; // txId->

	// Map[txId]->SenderTransactionContext

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public MOSServerSession(String sessionId, AccountZone az, Zone zone, Domain domain, Address address) {
		super(sessionId);
		setAccountZone(az);
		setZone(zone);
		setDomain(domain);
		setOriginatingAddress(address);
	}

	/**
	 * A ChannelContextHolder holds a channel in a session.
	 * 
	 * @author Peter
	 * 
	 */
	public static class ChannelContextHolder {
		private final String channelKey;
		private final Channel channel;

		// we cache the relay address for each channel
		private String rosTcpAddress;

		public ChannelContextHolder(String channelKey, Channel channel) {
			this.channelKey = channelKey;
			this.channel = channel;
		}

		public String getChannelKey() {
			return channelKey;
		}

		public Channel getChannel() {
			return channel;
		}

		public String getRosTcpAddress() {
			return rosTcpAddress;
		}

		public void setRosTcpAddress(String rosTcpAddress) {
			this.rosTcpAddress = rosTcpAddress;
		}

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
	 * MOS sessions may not be idled out if transactions are active. Note: transaction timeout or completion removes the
	 * transaction from the session.
	 */
	@Override
	public boolean isIdle(java.util.Date lastCutoffDate) {
		if (super.isIdle(lastCutoffDate)) {
			for (Map.Entry<String, Object> attrs : attributeMap.entrySet()) {
				if (isTxAttr(attrs.getKey())) {
					// if we have any active transactions, then we cannot be idle.
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean transferObject(ObjectType type, Map<AttributeId, Long> attributes) {
		// TODO #113: MAS receives DR from MRS
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

	public Address getOriginatingAddress() {
		return getAttribute(ORIGIN_ADDRESS);
	}

	public ChannelContextHolder getChannelContext(String channelKey) {
		return getAttribute(createChannelKey(channelKey));
	}

	public void setChannelContext(String channelKey, ChannelContextHolder cch) {
		setAttribute(createChannelKey(channelKey), cch);
	}

	public DestinationContextHolder getDestinationContext(String destinationName) {
		return getAttribute(createDestinationKey(destinationName));
	}

	public void setDestinationContext(String destinationName, DestinationContextHolder ddh) {
		setAttribute(createDestinationKey(destinationName), ddh);
	}

	public SenderTransactionContext getTransactionContext(String xid) {
		return getAttribute(createTxKey(xid));
	}

	public void setTransactionContext(String xid, SenderTransactionContext stc) {
		setAttribute(createTxKey(xid), stc);
	}

	public void removeTransactionContext(String xid) {
		removeAttribute(createTxKey(xid));
	}

	public ChannelOrigin getChannelOrigin() {
		ChannelOrigin co = new ChannelOrigin();
		co.setLocalName(getOriginatingAddress().getLocalName());
		co.setDomainName(getDomain().getDomainName());
		return co;
	}

	/**
	 * Return the transaction which contains the message or null if not found.
	 * 
	 * @param msgId
	 * @return
	 */
	public SenderTransactionContext getTransactionByMsgId(String msgId) {
		for (Map.Entry<String, Object> attrs : attributeMap.entrySet()) {
			if (isTxAttr(attrs.getKey())) {
				// if we have any active transactions, then we cannot be idle.
				SenderTransactionContext stc = (SenderTransactionContext) attrs.getValue();
				if (stc.getMessage(msgId) != null) {
					return stc;
				}
			}
		}
		return null;
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

	void setOriginatingAddress(Address a) {
		setAttribute(ORIGIN_ADDRESS, a);
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private String createChannelKey(String channelKey) {
		return CHANNEL_PREFIX + channelKey;
	}

	private String createDestinationKey(String destinationName) {
		return DESTINATION_PREFIX + destinationName;
	}

	private String createTxKey(String xid) {
		return TX_PREFIX + xid;
	}

	private boolean isTxAttr(String attributeKey) {
		return attributeKey.startsWith(TX_PREFIX);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
