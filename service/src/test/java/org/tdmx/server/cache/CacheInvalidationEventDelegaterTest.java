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
package org.tdmx.server.cache;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.tdmx.server.cache.CacheInvalidationEventDelegater;
import org.tdmx.server.cache.CacheInvalidationInstruction;
import org.tdmx.server.cache.CacheInvalidationListener;
import org.tdmx.server.pcs.protobuf.Cache.CacheName;

/**
 * @author peter.klauser
 * 
 */

public class CacheInvalidationEventDelegaterTest {

	private CacheInvalidationInstruction createCacheInvalidationMessage(CacheName cacheName) {
		CacheInvalidationInstruction i = CacheInvalidationInstruction.clearCache(cacheName);
		return i;
	}

	@Test
	public void handleSingleEvent() throws Exception {
		CacheInvalidationListener mockListener = Mockito.mock(CacheInvalidationListener.class);
		List<CacheInvalidationListener> listeners = new ArrayList<>();
		listeners.add(mockListener);

		CacheInvalidationEventDelegater sut = new CacheInvalidationEventDelegater();
		sut.setCacheInvalidationListeners(listeners);

		sut.invalidateCache(createCacheInvalidationMessage(CacheName.DatabasePartition));

		// verify
		Mockito.verify(mockListener, Mockito.times(1)).invalidateCache((CacheInvalidationInstruction) Matchers.any());
	}

	@Test
	public void handleMultiEvent() throws Exception {
		CacheInvalidationListener mockListener = Mockito.mock(CacheInvalidationListener.class);
		List<CacheInvalidationListener> listeners = new ArrayList<>();
		listeners.add(mockListener);

		CacheInvalidationEventDelegater sut = new CacheInvalidationEventDelegater();
		CacheInvalidationInstruction same = createCacheInvalidationMessage(CacheName.DatabasePartition);
		sut.setCacheInvalidationListeners(listeners);
		for (int i = 0; i < 10; i++) {
			sut.invalidateCache(same);
		}

		// verify
		Mockito.verify(mockListener, Mockito.times(1)).invalidateCache((CacheInvalidationInstruction) Matchers.any());
	}

	@Test
	public void handleMultiEventRollover() throws Exception {
		CacheInvalidationListener mockListener = Mockito.mock(CacheInvalidationListener.class);
		List<CacheInvalidationListener> listeners = new ArrayList<>();
		listeners.add(mockListener);

		CacheInvalidationEventDelegater sut = new CacheInvalidationEventDelegater();
		sut.setCacheInvalidationListeners(listeners);
		CacheInvalidationInstruction original = createCacheInvalidationMessage(CacheName.DatabasePartition);
		CacheInvalidationInstruction next = original;
		for (int i = 0; i <= 10; i++) {
			sut.invalidateCache(next);
			next = createCacheInvalidationMessage(CacheName.DatabasePartition);
		}
		// the 10th brings it to overflow and kickout the first 0
		sut.invalidateCache(original);
		// verify
		Mockito.verify(mockListener, Mockito.times(12)).invalidateCache((CacheInvalidationInstruction) Matchers.any());
	}

	@Test
	public void handleUniqueEventSequence() throws Exception {
		CacheInvalidationListener mockListener = Mockito.mock(CacheInvalidationListener.class);
		List<CacheInvalidationListener> listeners = new ArrayList<>();
		listeners.add(mockListener);

		CacheInvalidationEventDelegater sut = new CacheInvalidationEventDelegater();
		sut.setCacheInvalidationListeners(listeners);
		for (int i = 0; i < 100; i++) {
			sut.invalidateCache(createCacheInvalidationMessage(CacheName.DatabasePartition));
		}

		// verify
		Mockito.verify(mockListener, Mockito.times(100)).invalidateCache((CacheInvalidationInstruction) Matchers.any());
	}
}
