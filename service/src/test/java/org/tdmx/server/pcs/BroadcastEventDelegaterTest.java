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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.tdmx.server.pcs.protobuf.Broadcast.BroadcastMessage;
import org.tdmx.server.pcs.protobuf.Broadcast.BroadcastMessage.MessageType;

/**
 * @author peter.klauser
 * 
 */
public class BroadcastEventDelegaterTest {

	private BroadcastMessage createCacheInvalidationMessage(String id, String payload) {
		BroadcastMessage.Builder b = BroadcastMessage.newBuilder();
		b.setId(id);
		b.setType(MessageType.CacheInvalidation);
		b.addValue(payload);
		return b.build();
	}

	@Test
	public void handleSingleEvent() throws Exception {
		CacheInvalidationListener mockListener = Mockito.mock(CacheInvalidationListener.class);
		List<CacheInvalidationListener> listeners = new ArrayList<>();
		listeners.add(mockListener);

		BroadcastEventDelegater sut = new BroadcastEventDelegater();
		sut.setCacheInvalidationListeners(listeners);

		sut.handleBroadcast(createCacheInvalidationMessage("1", "payload-1"));

		// verify
		Mockito.verify(mockListener, Mockito.times(1)).invalidateCache((String) Matchers.any());
	}

	@Test
	public void handleMultiEvent() throws Exception {
		CacheInvalidationListener mockListener = Mockito.mock(CacheInvalidationListener.class);
		List<CacheInvalidationListener> listeners = new ArrayList<>();
		listeners.add(mockListener);

		BroadcastEventDelegater sut = new BroadcastEventDelegater();
		sut.setCacheInvalidationListeners(listeners);
		for (int i = 0; i < 10; i++) {
			sut.handleBroadcast(createCacheInvalidationMessage("1", "payload-1"));
		}

		// verify
		Mockito.verify(mockListener, Mockito.times(1)).invalidateCache((String) Matchers.any());
	}

	@Test
	public void handleMultiEventRollover() throws Exception {
		CacheInvalidationListener mockListener = Mockito.mock(CacheInvalidationListener.class);
		List<CacheInvalidationListener> listeners = new ArrayList<>();
		listeners.add(mockListener);

		BroadcastEventDelegater sut = new BroadcastEventDelegater();
		sut.setCacheInvalidationListeners(listeners);
		for (int i = 0; i <= 10; i++) {
			sut.handleBroadcast(createCacheInvalidationMessage("" + i, "" + i + "-payload"));
		}
		// the 10th brings it to overflow and kickout the first 0
		sut.handleBroadcast(createCacheInvalidationMessage("0", "payload-0"));
		// verify
		Mockito.verify(mockListener, Mockito.times(12)).invalidateCache((String) Matchers.any());
	}

	@Test
	public void handleUniqueEventSequence() throws Exception {
		CacheInvalidationListener mockListener = Mockito.mock(CacheInvalidationListener.class);
		List<CacheInvalidationListener> listeners = new ArrayList<>();
		listeners.add(mockListener);

		BroadcastEventDelegater sut = new BroadcastEventDelegater();
		sut.setCacheInvalidationListeners(listeners);
		for (int i = 0; i < 100; i++) {
			sut.handleBroadcast(createCacheInvalidationMessage("" + i, "payload-" + i));
		}

		// verify
		Mockito.verify(mockListener, Mockito.times(100)).invalidateCache((String) Matchers.any());
	}
}
