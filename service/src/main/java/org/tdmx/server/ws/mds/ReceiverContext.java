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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.tx.TransactionSpecification;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;

public class ReceiverContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// TODO #95: cache ros endpoints per channel

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

	// internal
	private final Queue<MessageContext> fetchedMessages = new ConcurrentLinkedDeque<>();

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
			recycleMessagesAfterTransactionTimeout();
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param maxWaitDurationMs
	 * @return
	 */
	public MessageContext getNextPendingMessage(long maxWaitDurationMs, TransactionSpecification txSpec) {
		long waitUntilTimestamp = System.currentTimeMillis() + maxWaitDurationMs;

		MessageContext result = null;
		do {
			result = fetchedMessages.poll();
			if (result == null) {
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
		} while (result == null && System.currentTimeMillis() < waitUntilTimestamp);

		if (result != null) {
			if (unackedMessageMap.containsKey(result.getMsgId())) {
				log.warn("Ignoring message fetched which is unacknowledged " + result.getMsgId());
				result = null;
			} else {
				log.debug("Associating tx " + txSpec.getXid() + " with msg " + result.getMsgId());

				TransactionContext tx = new TransactionContext(txSpec.getXid());

				tx.setCurrentMessage(result);
				tx.setTxTimeoutTimestamp(System.currentTimeMillis() + txSpec.getTxtimeout() * 1000);
				transactionMap.put(tx.getTxId(), tx);
				unackedMessageMap.put(result.getMsgId(), result);
			}
		}
		return result;
	}

	/**
	 * Remove the transaction and associated message.
	 * 
	 * @param txId
	 * @return the message previously associated with the transaction.
	 */
	public MessageContext endTransaction(TransactionSpecification txSpec) {
		TransactionContext tx = transactionMap.remove(txSpec.getXid());
		if (tx != null) {
			MessageContext msg = tx.getCurrentMessage();
			unackedMessageMap.remove(msg.getMsgId());
			return msg;
		}
		return null;
	}

	/**
	 * Adds newly fetched messages to the pending message list and informs any thread's caught in
	 * {@link #getNextPendingMessage(long)} to wake up and take one.
	 * 
	 * @param messages
	 */
	public void addPendingMessages(List<ChannelMessage> messages, boolean moreMsgs) {
		if (!messages.isEmpty()) {

			for (ChannelMessage msg : messages) {
				if (unackedMessageMap.containsKey(msg.getMsgId())) {
					log.debug("Ignoring unacked message " + msg.getMsgId());
					continue;
				}
				MessageContext ctx = new MessageContext(msg);
				fetchedMessages.add(ctx);
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

	/**
	 * Acknowledge the msg referenced by the msgId.
	 * 
	 * @param msgId
	 */
	public void ackMessage(String msgId) {
		MessageContext ctx = unackedMessageMap.remove(msgId);
		if (ctx == null) {
			log.warn("Acked unknown msgId " + msgId);
			return;
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
	private void recycleMessagesAfterTransactionTimeout() {
		// find the open transactions which have reached their timeout time.
		long now = System.currentTimeMillis();
		List<String> timeoutTxIds = new ArrayList<>();
		for (Entry<String, TransactionContext> txEntry : transactionMap.entrySet()) {
			if (txEntry.getValue().getTxTimeoutTimestamp() < now) {
				timeoutTxIds.add(txEntry.getKey());
			}
		}
		// timeout the transaction and recycle the message
		for (String txId : timeoutTxIds) {
			log.info("Transaction timeout " + txId);
			TransactionContext txCtx = transactionMap.remove(txId);
			if (txCtx != null) {

				MessageContext txMsg = txCtx.getCurrentMessage();
				unackedMessageMap.remove(txMsg.getMsgId());
				// increment the number of deliveries for the message being recycled.
				txMsg.setNumDeliveries(txMsg.getNumDeliveries() + 1);
				fetchedMessages.add(txMsg);
			}
		}
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
