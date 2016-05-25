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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.tdmx.core.api.v01.tx.Transaction;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.Channel;
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
	private static final String CHANNEL_MAP = "CHANNEL_MAP";

	// Map[txId]->SenderTransactionContext
	private static final String TX_MAP = "TX_MAP";

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public MOSServerSession(String sessionId, AccountZone az, Zone zone, Domain domain, Address address) {
		super(sessionId);
		setAccountZone(az);
		setZone(zone);
		setDomain(domain);
		setOriginatingAddress(address);

		// pre-initiated context map for Channels.
		Map<String, ChannelContextHolder> ccm = new ConcurrentHashMap<>();
		setAttribute(CHANNEL_MAP, ccm);

		// pre-initiated context map for transactions.
		Map<String, SenderTransactionContext> mcm = new ConcurrentHashMap<>();
		setAttribute(TX_MAP, mcm);
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

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public boolean transferObject(ObjectType type, Map<AttributeId, Long> attributes) {
		// TODO #93: MOS receives DR from MRS
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

	public ChannelContextHolder getChannel(String channelKey) {
		return getChannelMap().get(channelKey);
	}

	public ChannelContextHolder addChannel(String channelKey, Channel channel) {
		ChannelContextHolder cch = new ChannelContextHolder(channelKey, channel);

		Map<String, ChannelContextHolder> ccm = getChannelMap();
		ccm.put(cch.getChannelKey(), cch);
		return cch;
	}

	public SenderTransactionContext getTransaction(Transaction txSpec) {
		return getTransactionMap().get(txSpec.getXid());
	}

	public void setTransaction(Transaction txSpec, SenderTransactionContext tx) {
		getTransactionMap().put(txSpec.getXid(), tx);
	}

	public void removeTransaction(Transaction txSpec) {
		getTransactionMap().remove(txSpec.getXid());
	}

	/**
	 * Return the transaction which contains the message or null if not found.
	 * 
	 * @param msgId
	 * @return
	 */
	public SenderTransactionContext getTransaction(String msgId) {
		for (Entry<String, SenderTransactionContext> entry : getTransactionMap().entrySet()) {
			if (entry.getValue().getMessage(msgId) != null) {
				return entry.getValue();
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

	private Map<String, SenderTransactionContext> getTransactionMap() {
		return getAttribute(TX_MAP);
	}

	private Map<String, ChannelContextHolder> getChannelMap() {
		return getAttribute(CHANNEL_MAP);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
