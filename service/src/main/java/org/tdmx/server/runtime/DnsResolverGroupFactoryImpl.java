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
package org.tdmx.server.runtime;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.DnsResolverGroup;
import org.tdmx.lib.control.service.DnsResolverGroupService;
import org.tdmx.server.cache.CacheInvalidationInstruction;
import org.tdmx.server.cache.CacheInvalidationListener;
import org.tdmx.server.pcs.protobuf.Cache.CacheName;

public class DnsResolverGroupFactoryImpl implements DnsResolverGroupFactory, CacheInvalidationListener {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DnsResolverGroupFactoryImpl.class);

	private DnsResolverGroupService dnsResolverGroupService;

	// internal
	private List<DnsResolverGroup> cache;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void invalidateCache(CacheInvalidationInstruction message) {
		if (CacheName.DnsResolverGroup == message.getName()) {
			log.debug("Invalidating cache.");
			cache = null;
		}
	}

	@Override
	public List<DnsResolverGroup> getDnsResolverGroups() {
		if (cache == null) {
			fetchDnsResolverGroups();
		}
		return cache;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private synchronized void fetchDnsResolverGroups() {
		if (cache == null) {
			List<DnsResolverGroup> groups = dnsResolverGroupService.findAll();
			Collections.sort(groups, new Comparator<DnsResolverGroup>() {
				@Override
				public int compare(DnsResolverGroup g1, DnsResolverGroup g2) {
					return g1.getGroupName().compareTo(g2.getGroupName());
				}
			});
			cache = Collections.unmodifiableList(groups);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public DnsResolverGroupService getDnsResolverGroupService() {
		return dnsResolverGroupService;
	}

	public void setDnsResolverGroupService(DnsResolverGroupService dnsResolverGroupService) {
		this.dnsResolverGroupService = dnsResolverGroupService;
	}

}
