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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.tx.Transaction;
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
	private static final int LEN_ENTROPY = 32;
	private static final int LEN_CONTINUATION_ID = 8;

	private final Transaction txSpec;
	private final byte[] entropy;

	private final Map<String, ChannelMessage> messages = new HashMap<>(); // map[msgId->ChannelMessage]

	private volatile ScheduledFuture<?> timeoutFuture; // discards TX after tx timeout.

	// TODO #109: for each message we need to know if we've received all chunks and which chunk is the current chunk (
	// which can be repeatedly sent )

	public ScheduledFuture<?> getTimeoutFuture() {
		return timeoutFuture;
	}

	public void setTimeoutFuture(ScheduledFuture<?> timeoutFuture) {
		this.timeoutFuture = timeoutFuture;
	}

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public SenderTransactionContext(Transaction txSpec) {
		this.txSpec = txSpec;
		this.entropy = EntropySource.getRandomBytes(LEN_ENTROPY);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public Transaction getTxSpec() {
		return txSpec;
	}

	/**
	 * Return all the ChannelMessages involved in the transaction.
	 * 
	 * @return
	 */
	public List<ChannelMessage> getMessages() {
		List<ChannelMessage> result = new ArrayList<>();
		result.addAll(messages.values());
		return result;
	}

	/**
	 * Add the message to the transaction. Messages cannot be removed from a transaction, since a transaction is
	 * prepared/committed or rolledback as a whole.
	 * 
	 * @param msg
	 */
	public void addMessage(ChannelMessage msg) {
		messages.put(msg.getMsgId(), msg);
	}

	/**
	 * Lookup the message with the msgId.
	 * 
	 * @param msgId
	 * @return
	 */
	public ChannelMessage getMessage(String msgId) {
		return messages.get(msgId);
	}

	/**
	 * Create the continuationId for the chunkPos. The continuationId is a truncated Hash of the msgId, chunkPos and the
	 * "secret" entropy which the client doesn't know so cannot create the continuationId themselves. This forces the
	 * client to have to receive the continuationId from the server before sending the next chunk.
	 * 
	 * @param chunkPos
	 * @return null if no chunk at the requested pos
	 */
	public String getContinuationId(int chunkPos, ChannelMessage msg) {
		// if the chunk requested starts after the end of the payload then the previous chunk is the last
		if ((msg.getScheme().getChunkSize() * chunkPos) > msg.getPayloadLength()) {
			return null;
		}
		return SignatureUtils.createContinuationId(chunkPos, entropy, msg.getMsgId(), LEN_CONTINUATION_ID);
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

}
