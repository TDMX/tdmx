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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.tx.Transaction;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;

public class ReceiverContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ReceiverContext.class);

	// if we don't get any messages transferred in "fast" then we still poll the DB every 5min.
	private static final long safetyPollIntervalMs = 300000;

	// map of channelId->rosTcpAddress last known good configuration for each channel
	private Map<Long, String> rosTcpAddressChannelMap = new HashMap<>();

	// map of msgId->MessageContext of messages which have been delivered but not yet acknowledged
	private Map<String, MessageContext> unackedMessageMap = new HashMap<>();

	// map of txId->TransactionContext of current transactions.
	private Map<String, TransactionContext> transactionMap = new HashMap<>();

	// indicates that messages related to this destination have been relayed in since the last fetch.
	private boolean dirty = true;

	// the last time that we fetched pending messages from the DB. We use this to stop looking too often
	// for pending messages on the DB if no new messages has been received.
	private long lastFetchTimestamp = 0;

	// internal, message stateId ready to deliver.
	private final Queue<Long> fetchedMessages = new ConcurrentLinkedDeque<>();

	// the destination user's certificate seqNr. We only fetch messages which are exactly determined for this user,
	// where there could be more than one user (version) receiving at the same time on the session.
	private final Integer seqNr;

	public Integer getSeqNr() {
		return seqNr;
	}

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public ReceiverContext(int seqNr) {
		this.seqNr = seqNr;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Set a known good rosTcpAddress for the channel.
	 * 
	 * @param channel
	 * @param rosTcpAddress
	 */
	public void setRosTcpAddress(Channel channel, String rosTcpAddress) {
		if (rosTcpAddress == null) {
			rosTcpAddressChannelMap.remove(channel.getId());
		} else {
			rosTcpAddressChannelMap.put(channel.getId(), rosTcpAddress);
		}
	}

	/**
	 * Clear any rosTcpAddress for the channel.
	 * 
	 * @param channel
	 */
	public void clearRosTcpAddress(Channel channel) {
		rosTcpAddressChannelMap.remove(channel.getId());
	}

	/**
	 * Get a channel's last known working rosTcpAddress.
	 * 
	 * @param channel
	 * @return a channel's last known working rosTcpAddress.
	 */
	public String getRosTcpAddress(Channel channel) {
		return rosTcpAddressChannelMap.get(channel.getId());
	}

	/**
	 * Determine if a fetch is required. Only call with a thread which is prepared to fetch the messages if the return
	 * is true.
	 * 
	 * @return if messages should be fetched from the DB.
	 */
	public synchronized boolean isFetchRequired() {
		if (fetchedMessages.poll() == null // no pending messages
				&& (dirty == true || lastFetchTimestamp + safetyPollIntervalMs < System.currentTimeMillis())) {
			lastFetchTimestamp = System.currentTimeMillis();
			dirty = false;

			// recycle any unacknowledged messages in transactions which have timed-out
			// TODO abortTx, recycleMessagesAfterTransactionTimeout();
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param maxWaitDurationMs
	 * @return the stateId
	 */
	public Long getNextPendingStateId(long maxWaitDurationMs) {
		long waitUntilTimestamp = System.currentTimeMillis() + maxWaitDurationMs;

		Long stateId = null;
		do {
			stateId = fetchedMessages.poll();
			if (stateId == null) {
				synchronized (fetchedMessages) {
					long waitForMs = waitUntilTimestamp - System.currentTimeMillis();
					if (waitForMs > 0) {
						try {
							fetchedMessages.wait(waitForMs);
						} catch (InterruptedException e) {
							return null;
						}
					}
				}
			}
		} while (stateId == null && System.currentTimeMillis() < waitUntilTimestamp);

		return stateId;
	}

	/**
	 * Start the transaction, adding the message to the unackedMessageMap.
	 * 
	 * @param tx
	 * @param msg
	 * @return
	 */
	public MessageContext startTransaction(Transaction tx, ChannelMessage msg) {
		TransactionContext txCtx = new TransactionContext(tx.getXid());
		log.debug("Associating tx " + txCtx.getTxId() + " with msg " + msg.getMsgId());
		MessageContext msgCtx = new MessageContext(msg);
		txCtx.setCurrentMessage(msgCtx);
		txCtx.setTxTimeoutTimestamp(System.currentTimeMillis() + tx.getTxtimeout() * 1000);
		transactionMap.put(txCtx.getTxId(), txCtx);
		unackedMessageMap.put(msgCtx.getMsgId(), msgCtx);
		return msgCtx;
	}

	/**
	 * Remove the transaction and associated message.
	 * 
	 * @param txId
	 * @return the message previously associated with the transaction.
	 */
	public MessageContext endTransaction(String txId) {
		TransactionContext tx = transactionMap.remove(txId);
		if (tx != null) {
			MessageContext msg = tx.getCurrentMessage();
			unackedMessageMap.remove(msg.getMsgId());
			return msg;
		}
		return null;
	}

	public MessageContext getUnackedMessage(String msgId) {
		MessageContext msgCtx = unackedMessageMap.get(msgId);
		return msgCtx;
	}

	/**
	 * Adds newly fetched messages to the pending message list and informs any thread's caught in
	 * {@link #getNextPendingMessage(long)} to wake up and take one.
	 * 
	 * @param messages
	 */
	public void addPendingMessages(List<Long> stateIds, boolean moreMsgs) {
		if (!stateIds.isEmpty()) {
			Set<Long> unackedIds = getUnackedStateIds();
			for (Long stateId : stateIds) {
				if (unackedIds.contains(stateId)) {
					log.debug("Ignoring unacked message " + stateId);
					continue;
				}
				fetchedMessages.add(stateId);
			}
			// we can fetch more as soon as possible
			if (moreMsgs) {
				setDirty();
			}
			// wake up any waiting receivers
			synchronized (fetchedMessages) {
				fetchedMessages.notifyAll();
			}
		}
	}

	/**
	 * Indicate that there are more messages to fetch from the DB.
	 */
	public void setDirty() {
		this.dirty = true;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private Set<Long> getUnackedStateIds() {
		Set<Long> result = new HashSet<>();
		for (Map.Entry<String, MessageContext> unackedMsg : unackedMessageMap.entrySet()) {
			result.add(unackedMsg.getValue().getStateId());
		}
		return result;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public long getLastFetchTimestamp() {
		return lastFetchTimestamp;
	}

	public void setLastFetchTimestamp(long lastFetchTimestamp) {
		this.lastFetchTimestamp = lastFetchTimestamp;
	}

}
