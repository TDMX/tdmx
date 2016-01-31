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
import java.util.concurrent.ConcurrentHashMap;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;
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

	// Map[msgId]->MessageContextHolder
	private static final String MESSAGE_MAP = "MESSAGE_MAP";

	private static final int LEN_ENTROPY = 32;
	private static final int LEN_CONTINUATION_ID = 8;

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

		// pre-initiated context map for sending messages
		Map<String, MessageContextHolder> mcm = new ConcurrentHashMap<>();
		setAttribute(MESSAGE_MAP, mcm);
	}

	/**
	 * A MessageContextHolder holds a message in a session for the duration of it's relevance to sending subsequent
	 * chunks and committing or rolling back the transaction.
	 * 
	 * @author Peter
	 * 
	 */
	public static class MessageContextHolder {
		private final ChannelMessage msg;
		private final byte[] entropy;

		public MessageContextHolder(ChannelMessage msg) {
			this.msg = msg;
			this.entropy = EntropySource.getRandomBytes(LEN_ENTROPY);
		}

		public String getMsgId() {
			return msg.getMsgId();
		}

		public ChannelMessage getChannelMessage() {
			return msg;
		}

		/**
		 * Create the continuationId for the chunkPos. The continuationId is a truncated Hash of the msgId, chunkPos and
		 * the "secret" entropy which the client doesn't know so cannot create the continuationId themselves. This
		 * forces the client to have to receive the continuationId from the server before sending the next chunk.
		 * 
		 * @param chunkPos
		 * @return null if no chunk at the requested pos
		 */
		public String getContinuationId(int chunkPos) {
			// if the chunk requested starts after the end of the payload then the previous chunk is the last
			if ((msg.getChunkSize() * chunkPos) > msg.getPayloadLength()) {
				return null;
			}
			return SignatureUtils.createContinuationId(chunkPos, entropy, msg.getMsgId(), LEN_CONTINUATION_ID);
		}

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

	public MessageContextHolder addMessage(ChannelMessage msg) {
		MessageContextHolder mch = new MessageContextHolder(msg);

		Map<String, MessageContextHolder> mcm = getMessageMap();
		mcm.put(mch.getMsgId(), mch);
		return mch;
	}

	public void removeMessage(ChannelMessage msg) {
		Map<String, MessageContextHolder> mcm = getMessageMap();
		mcm.remove(msg.getMsgId());
	}

	public MessageContextHolder getMessage(String msgId) {
		return getMessageMap().get(msgId);
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

	private Map<String, MessageContextHolder> getMessageMap() {
		return getAttribute(MESSAGE_MAP);
	}

	private Map<String, ChannelContextHolder> getChannelMap() {
		return getAttribute(CHANNEL_MAP);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
