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

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.pcs.protobuf.Broadcast;
import org.tdmx.server.pcs.protobuf.Broadcast.BroadcastMessage.MessageType;

/**
 * The DelegatingCacheInvalidationNotifier attempts to use one of the registered BroadcastEventNotifers it knows to
 * handle the cache invalidation event.
 * 
 * This delegation is necessary because there are 2 PCS clients within the application context ( one SCS client, one WS
 * client ), and we don't want to send the same cache invalidation event twice to the PCS which distributes the events
 * to all attached clients.
 * 
 * @author Peter
 *
 */
public class DelegatingCacheInvalidationNotifier implements CacheInvalidationNotifier {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DelegatingCacheInvalidationNotifier.class);

	private List<BroadcastEventNotifier> broadcastEventNotifiers;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public DelegatingCacheInvalidationNotifier() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void cacheInvalidated(String key) {
		if (broadcastEventNotifiers == null || broadcastEventNotifiers.isEmpty()) {
			log.warn("No broadcastEventNotifiers.");
			return;
		}
		Broadcast.BroadcastMessage.Builder eventBuilder = Broadcast.BroadcastMessage.newBuilder();
		eventBuilder.setId(UUID.randomUUID().toString());
		eventBuilder.setType(MessageType.CacheInvalidation);
		eventBuilder.addValue(key);

		Broadcast.BroadcastMessage msg = eventBuilder.build();

		for (BroadcastEventNotifier notifier : broadcastEventNotifiers) {
			if (notifier.broadcastEvent(msg)) {
				break;
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

	public List<BroadcastEventNotifier> getBroadcastEventNotifiers() {
		return broadcastEventNotifiers;
	}

	public void setBroadcastEventNotifiers(List<BroadcastEventNotifier> broadcastEventNotifiers) {
		this.broadcastEventNotifiers = broadcastEventNotifiers;
	}

}
