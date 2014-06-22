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
package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.DomainObjectType;
import org.tdmx.console.application.search.SearchService;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError;
import org.tdmx.console.domain.validation.OperationError.ERROR;
import org.tdmx.core.system.lang.StringUtils;
import org.xbill.DNS.ResolverConfig;

public class DnsResolverServiceImpl implements DnsResolverService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	public static final String SYSTEM_DNS_RESOLVER_LIST_ID = "system-dns-resolver-list";
	public static final String SYSTEM_DNS_RESOLVER_LIST_NAME = "System";

	private ObjectRegistry objectRegistry;
	private SearchService searchService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public DnsResolverListDO lookup(String id) {
		return objectRegistry.getDnsResolverList(id);
	}

	@Override
	public List<DnsResolverListDO> search(String criteria) {
		if (StringUtils.hasText(criteria)) {
			List<DnsResolverListDO> result = new ArrayList<>();
			Set<DnsResolverListDO> found = searchService.search(DomainObjectType.DnsResolverList, criteria);
			for (DomainObject o : found) {
				result.add((DnsResolverListDO) o);
			}
			return result;
		}
		return objectRegistry.getDnsResolverLists();
	}

	@Override
	public void updateSystemResolverList() {
		DnsResolverListDO systemList = objectRegistry.getDnsResolverList(SYSTEM_DNS_RESOLVER_LIST_ID);
		if (systemList == null) {
			DomainObjectChangesHolder h = new DomainObjectChangesHolder();
			systemList = new DnsResolverListDO();
			systemList.setId(SYSTEM_DNS_RESOLVER_LIST_ID);
			systemList.setActive(Boolean.TRUE);
			systemList.setName(SYSTEM_DNS_RESOLVER_LIST_NAME);
			systemList.setHostnames(getSystemDnsHostnames());
			objectRegistry.notifyAdd(systemList, h);
			searchService.update(h);
			// TODO audit log
		} else {
			DnsResolverListDO systemListCopy = new DnsResolverListDO(systemList);
			systemListCopy.setHostnames(getSystemDnsHostnames());
			createOrUpdate(systemListCopy);
		}
	}

	@Override
	public OperationError createOrUpdate(DnsResolverListDO resolverList) {
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		List<FieldError> validation = resolverList.validate();
		if (!validation.isEmpty()) {
			return new OperationError(validation);
		}
		DnsResolverListDO existing = objectRegistry.getDnsResolverList(resolverList.getId());
		if (existing == null) {
			objectRegistry.notifyAdd(resolverList, holder);
			searchService.update(holder);
		} else {
			DomainObjectFieldChanges changes = existing.merge(resolverList);
			if (!changes.isEmpty()) {
				objectRegistry.notifyModify(changes, holder);
				searchService.update(holder);

				// TODO if system's hostnames change then issue audit warning
			}
		}
		return null;
	}

	@Override
	public OperationError delete(String id) {
		// not allowed to delete the "system" DNS resolver list.
		if (SYSTEM_DNS_RESOLVER_LIST_ID.equals(id)) {
			return new OperationError(ERROR.IMMUTABLE);
		}
		DnsResolverListDO existing = objectRegistry.getDnsResolverList(id);
		if (existing == null) {
			return new OperationError(ERROR.MISSING);
		}
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		objectRegistry.notifyRemove(existing, holder);
		searchService.update(holder);
		return null;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private List<String> getSystemDnsHostnames() {
		List<String> hosts = new ArrayList<>();

		String[] list = ResolverConfig.getCurrentConfig().servers();
		if (list != null) {
			for (String h : list) {
				hosts.add(h);
			}
		}
		return Collections.unmodifiableList(hosts);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

	public void setObjectRegistry(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

}
