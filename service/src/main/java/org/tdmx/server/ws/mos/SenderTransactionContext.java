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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.tx.Transaction;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;

/**
 * A context describing the Messages which are contained in a Transaction.
 * 
 * LocalTransactions will only contain max 1 ChannelMessage, which is the current being submitted. A new
 * LocalTransaction is started for each submitted message of a client. The timeout of a local transaction is therefore
 * the time between the submit of a message and the upload of the final chunk.
 * 
 * XATransactions can contain several ChannelMessages. The XA transaction must be completed (all messages completely
 * uploaded) before the timeout expires. The timer starts with the first message is submitted.
 * 
 * 
 * @author Peter
 * 
 */
public class SenderTransactionContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final int LEN_CONTINUATION_ID = 8;

	private final Transaction txSpec;

	private final Map<String, MessageContextHolder> messages = new HashMap<>(); // map[msgId->MessageContextHolder]

	private volatile ScheduledFuture<?> timeoutFuture; // discards TX after tx timeout.

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public SenderTransactionContext(Transaction txSpec) {
		this.txSpec = txSpec;
	}

	/**
	 * A MessageContextHolder holds a message being sent in the context of a transaction.
	 * 
	 * @author Peter
	 * 
	 */
	public static class MessageContextHolder {
		private static final int LEN_ENTROPY = 8;
		private final byte[] entropy;

		private final ChannelMessage msg;

		private int lastChunkReceived = 0;

		public MessageContextHolder(ChannelMessage msg) {
			this.msg = msg;
			this.entropy = EntropySource.getRandomBytes(LEN_ENTROPY);
		}

		public String getMsgId() {
			return msg.getMsgId();
		}

		public int getLastChunkReceived() {
			return lastChunkReceived;
		}

		public void setLastChunkReceived(int lastChunkReceived) {
			this.lastChunkReceived = lastChunkReceived;
		}

		public ChannelMessage getMsg() {
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
			if ((msg.getScheme().getChunkSize() * chunkPos) > msg.getPayloadLength()) {
				return null;
			}
			return SignatureUtils.createContinuationId(chunkPos, entropy, msg.getMsgId(), LEN_CONTINUATION_ID);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public Transaction getTxSpec() {
		return txSpec;
	}

	public String getXid() {
		return txSpec.getXid();
	}

	/**
	 * Return all the ChannelMessages involved in the transaction.
	 * 
	 * @return
	 */
	public List<ChannelMessage> getMessages() {
		List<ChannelMessage> result = new ArrayList<>();
		for (Entry<String, MessageContextHolder> entry : messages.entrySet()) {
			// always check against persisted instances
			result.add(entry.getValue().getMsg());
		}
		return result;
	}

	/**
	 * Return the cumulative size of message payloads for a given channel.
	 * 
	 * @param channel
	 * @return
	 */
	public long getTotalPayloadSizeForChannel(Channel channel) {
		int totalQuota = 0;
		for (Entry<String, MessageContextHolder> entry : messages.entrySet()) {
			// always check against persisted instances
			if (entry.getValue().getMsg().getChannel().getId().equals(channel.getId())) {
				totalQuota += entry.getValue().getMsg().getPayloadLength();
			}
		}
		return totalQuota;
	}

	/**
	 * Get the ChannelMessages being sent to the Channel in the context of this transaction.
	 * 
	 * @param channel
	 * @return emtpyList if no message for the channel, else the ChannelMessages being sent to the Channel.
	 */
	public List<ChannelMessage> getChannelMessages(Channel channel) {
		List<ChannelMessage> channelMsgs = new ArrayList<>();
		for (Entry<String, MessageContextHolder> entry : messages.entrySet()) {
			// always check against persisted instances
			if (entry.getValue().getMsg().getChannel().getId().equals(channel.getId())) {
				channelMsgs.add(entry.getValue().getMsg());
			}
		}
		return channelMsgs;
	}

	/**
	 * List the unique channels to which messages are being sent in this transaction.
	 * 
	 * @return emptyList if no channels, else the unique channels to which messages are being sent.
	 */
	public List<Channel> getChannels() {
		List<Channel> result = new ArrayList<>();
		Set<Long> uniqueChannelIds = new HashSet<>();
		for (Entry<String, MessageContextHolder> entry : messages.entrySet()) {
			// always check against persisted instances
			Long channelId = entry.getValue().getMsg().getChannel().getId();
			if (!uniqueChannelIds.contains(channelId)) {
				uniqueChannelIds.add(channelId);
				result.add(entry.getValue().getMsg().getChannel());
			}
		}
		return result;
	}

	/**
	 * Add the message to the transaction. Messages cannot be removed from a transaction, since a transaction is
	 * prepared/committed or rolledback as a whole.
	 * 
	 * @param msg
	 */
	public void addMessage(MessageContextHolder msg) {
		messages.put(msg.getMsgId(), msg);
	}

	/**
	 * Lookup the message with the msgId.
	 * 
	 * @param msgId
	 * @return
	 */
	public MessageContextHolder getMessage(String msgId) {
		return messages.get(msgId);
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

	public ScheduledFuture<?> getTimeoutFuture() {
		return timeoutFuture;
	}

	public void setTimeoutFuture(ScheduledFuture<?> timeoutFuture) {
		this.timeoutFuture = timeoutFuture;
	}

}
