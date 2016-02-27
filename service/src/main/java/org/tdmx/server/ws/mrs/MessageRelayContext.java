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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.zone.domain.ChannelMessage;

public class MessageRelayContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MessageRelayContext.class);

	// if we don't get the next chunk within this timeframe the whole message transfer will be aborted.
	private static final long idleTimeoutIntervalMs = 300000;

	private final ChannelMessage msg;

	private int currentChunkPos;
	private long lastChunkTimestamp;
	private String runningMAC;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public MessageRelayContext(ChannelMessage msg) {
		this.msg = msg;
		this.currentChunkPos = -1;
		this.lastChunkTimestamp = System.currentTimeMillis();
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public String getMsgId() {
		return msg.getMsgId();
	}

	/**
	 * {@link #idleTimeoutIntervalMs} has gone by without a new chunk being received.
	 * 
	 * @return timeout before a new chunk is received.
	 */
	public boolean isIdle() {
		return lastChunkTimestamp + idleTimeoutIntervalMs < System.currentTimeMillis();
	}

	/**
	 * Check if all Chunks have been received.
	 * 
	 * @return true if all Chunks have been received.
	 */
	public boolean isComplete() {
		return currentChunkPos == msg.getNumberOfChunks() - 1;
	}

	/**
	 * Check if all Chunks have been received AND their checksums ALL correlated.
	 * 
	 * @return
	 */
	public boolean isCorrect() {
		return msg.getMacOfMacs().equals(runningMAC);
	}

	/**
	 * Mark Chunk as received and keep track of the MAC. If a chunk is received in the wrong order return false. We
	 * allow to receive the same chunk twice as long as it is still the "latest" chunk.
	 * 
	 * @param pos
	 * @param mac
	 * @return true if chunk handled ok otherwise false.
	 */
	public boolean setChunkReceived(int pos, String mac) {
		if (pos < 0 || pos >= msg.getNumberOfChunks()) {
			// invalid chunk position
			return false;
		}
		if (pos == currentChunkPos + 1) {
			currentChunkPos = pos;
			if (pos == 0) {
				// initialize the runningMAC
				runningMAC = mac;
			} else {
				// add the MAC to the runningMAC
				// TODO #93: calculate runningMAC
				runningMAC = runningMAC + mac;
			}
			return true;
		} else if (pos == currentChunkPos) {
			// ignore - retry
			return true;
		}
		// invalid order of chunk received
		return false;
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
