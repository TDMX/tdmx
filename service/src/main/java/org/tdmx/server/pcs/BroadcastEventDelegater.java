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
package org.tdmx.server.pcs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.pcs.protobuf.Broadcast.BroadcastMessage;

/**
 * 
 * @author Peter
 *
 */
public class BroadcastEventDelegater implements BroadcastEventListener {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(BroadcastEventDelegater.class);

	private Map<String, BroadcastMessage> eventMap = new HashMap<>();
	private LinkedList<BroadcastMessage> lruList = new LinkedList<>();

	private int sizeLimit = 10;

	private List<CacheInvalidationListener> cacheInvalidationListeners;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public BroadcastEventDelegater() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void handleBroadcast(BroadcastMessage event) {
		String key = event.getId();

		synchronized (eventMap) {
			if (eventMap.containsKey(key)) {
				if (log.isDebugEnabled()) {
					log.debug("Filtering duplicate event " + key);
				}
				return;
			}
			if (lruList.size() >= getSizeLimit()) {
				// remove the last added entry from the linked list ( front )
				BroadcastMessage oldestEvent = lruList.removeFirst();
				String oldestKey = oldestEvent.getId();
				eventMap.remove(oldestKey);
			}
			eventMap.put(key, event);
			lruList.addLast(event);
		}

		// notify all cache invalidation listeners
		if (cacheInvalidationListeners != null) {
			for (CacheInvalidationListener cil : cacheInvalidationListeners) {
				cil.invalidateCache(key); // TODO type differentiation FIXME payload
			}
		}
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

	public int getSizeLimit() {
		return sizeLimit;
	}

	public void setSizeLimit(int sizeLimit) {
		this.sizeLimit = sizeLimit;
	}

	public List<CacheInvalidationListener> getCacheInvalidationListeners() {
		return cacheInvalidationListeners;
	}

	public void setCacheInvalidationListeners(List<CacheInvalidationListener> cacheInvalidationListeners) {
		this.cacheInvalidationListeners = cacheInvalidationListeners;
	}

}
