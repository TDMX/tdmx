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

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.lib.zone.domain.ChannelMessage;

public class MessageContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	private static final int LEN_ENTROPY = 32;
	private static final int LEN_CONTINUATION_ID = 8;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final ChannelMessage msg;

	private final byte[] entropy;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public MessageContext(ChannelMessage msg) {
		this.msg = msg;
		this.entropy = EntropySource.getRandomBytes(LEN_ENTROPY);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public String getMsgId() {
		return this.msg.getMsgId();
	}

	public Long getStateId() {
		return this.msg.getState().getId();
	}

	@Override
	public int hashCode() {
		return msg.getMsgId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MessageContext) {
			return false;
		} else {
			MessageContext other = (MessageContext) obj;
			return msg.getMsgId().equals(other.getMsgId());
		}
	}

	/**
	 * Create the continuationId for the chunkPos. The continuationId is a truncated Hash of the msgId, chunkPos and the
	 * "secret" entropy which the client doesn't know so cannot create the continuationId themselves. This forces the
	 * client to have to receive the continuationId from the server before sending the next chunk.
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

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public ChannelMessage getMsg() {
		return msg;
	}

}
