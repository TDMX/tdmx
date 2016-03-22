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

import java.util.HashMap;
import java.util.Map;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.tx.TransactionSpecification;
import org.tdmx.lib.zone.domain.ChannelMessage;

/**
 * A context describing the Messages which are contained in a Transaction.
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

	private final TransactionSpecification txSpec;
	private final long txTimeoutTS;
	private final byte[] entropy;
	private final Map<String, ChannelMessage> messages = new HashMap<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public SenderTransactionContext(TransactionSpecification txSpec) {
		this.txSpec = txSpec;
		this.txTimeoutTS = System.currentTimeMillis() + (txSpec.getTxtimeout() * 1000);
		this.entropy = EntropySource.getRandomBytes(LEN_ENTROPY);
	}

	public TransactionSpecification getTxSpec() {
		return txSpec;
	}

	/**
	 * When the transaction has passed it's timeout timestamp and can only be rolled-back.
	 * 
	 * @return
	 */
	public boolean isTimeout() {
		return System.currentTimeMillis() > txTimeoutTS;
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
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

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
