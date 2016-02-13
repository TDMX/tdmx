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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.Common.ObjectType;
import org.tdmx.server.ws.session.WebServiceSession;

public class MDSServerSession extends WebServiceSession {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long pollIntervalMs = 60000;

	// indicates that messages related to this destination have been relayed in.
	private boolean dirty = true;
	// the last time that we fetched pending messages from the DB. We use this to stop looking too often
	// for pending messages on the DB if no new messages has been received.
	private long lastFetchTimestamp = 0;

	// internal
	private final Queue<ChannelMessage> fetchedMessages = new ConcurrentLinkedDeque<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public MDSServerSession(String sessionId, AccountZone az, Zone zone, Domain domain, Address address,
			Service service) {
		super(sessionId);
		setAccountZone(az);
		setZone(zone);
		setDomain(domain);
		setDestinationAddress(address);
		setService(service);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Determine if a fetch is required. Only call with a thread which is prepared to fetch the messages if the return
	 * is true.
	 * 
	 * @return if messages should be fetched from the DB.
	 */
	public synchronized boolean isFetchRequired() {
		// TODO #95: consider only fetching when there are no outstanding ACKs or tx

		if (fetchedMessages.poll() == null
				&& (dirty == true || lastFetchTimestamp + pollIntervalMs < System.currentTimeMillis())) {
			lastFetchTimestamp = System.currentTimeMillis();
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param maxWaitDurationMs
	 * @return
	 */
	public ChannelMessage getNextPendingMessage(long maxWaitDurationMs) {
		long waitUntilTimestamp = System.currentTimeMillis() + maxWaitDurationMs;

		ChannelMessage result = null;
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
		return result;
	}

	/**
	 * Adds newly fetched messages to the pending message list and informs any thread's caught in
	 * {@link #getNextPendingMessage(long)} to wake up and take one.
	 * 
	 * @param messages
	 */
	public void addPendingMessages(List<ChannelMessage> messages) {
		if (!messages.isEmpty()) {
			fetchedMessages.addAll(messages);
			synchronized (fetchedMessages) {
				fetchedMessages.notifyAll();
			}
		}
	}

	@Override
	public boolean transferObject(ObjectType type, Map<AttributeId, Long> attributes) {
		// MDS receives MSG from MRS
		dirty = true;
		return true;
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

	public Address getDestinationAddress() {
		return getAttribute(DESTINATION_ADDRESS);
	}

	public Service getService() {
		return getAttribute(SERVICE);
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

	void setDestinationAddress(Address a) {
		setAttribute(DESTINATION_ADDRESS, a);
	}

	void setService(Service s) {
		setAttribute(SERVICE, s);
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public long getLastFetchTimestamp() {
		return lastFetchTimestamp;
	}

	public void setLastFetchTimestamp(long lastFetchTimestamp) {
		this.lastFetchTimestamp = lastFetchTimestamp;
	}

}
