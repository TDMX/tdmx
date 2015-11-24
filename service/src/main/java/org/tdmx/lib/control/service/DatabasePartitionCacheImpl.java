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
package org.tdmx.lib.control.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.server.pcs.CacheInvalidationListener;

public class DatabasePartitionCacheImpl implements DatabasePartitionCache, CacheInvalidationListener {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DatabasePartitionCacheImpl.class);

	private DatabasePartitionService databasePartitionService;

	// internal
	private Map<String,DatabasePartition> idMap;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void invalidateCache(String key) {
		if (DatabasePartitionService.CACHE_KEY.equals(key)) {
			log.debug("Invalidating cache " + key);
			idMap = null;
		}
	}

	@Override
	public DatabasePartition findByPartitionId(String partitionId) {
		if (idMap == null) {
			fetchDatabasePartitions();
		}
		return idMap.get(partitionId);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private synchronized void fetchDatabasePartitions() {
		if (idMap == null) {
			List<DatabasePartition> list = getDatabasePartitionService().findAll();

			Map<String, DatabasePartition> localIdMap = new HashMap<>();

			for (DatabasePartition partition : list) {
				localIdMap.put(partition.getPartitionId(), partition);
			}

			idMap = Collections.unmodifiableMap(localIdMap);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public DatabasePartitionService getDatabasePartitionService() {
		return databasePartitionService;
	}

	public void setDatabasePartitionService(DatabasePartitionService databasePartitionService) {
		this.databasePartitionService = databasePartitionService;
	}

}
