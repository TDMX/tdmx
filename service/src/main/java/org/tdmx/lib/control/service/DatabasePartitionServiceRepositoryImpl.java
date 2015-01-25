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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.control.dao.DatabasePartitionDao;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;

/**
 * A transactional service managing the DatabasePartition information.
 * 
 * @author Peter Klauser
 * 
 */
public class DatabasePartitionServiceRepositoryImpl implements DatabasePartitionService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DatabasePartitionServiceRepositoryImpl.class);

	private DatabasePartitionDao databasePartitionDao;

	private long cacheTimeoutMillis = 900000;

	private Map<DatabaseType, Map<String, List<DatabasePartition>>> dptsMap = null;
	private Map<String, DatabasePartition> dpidMap = null;
	private long cacheLoadTimestamp = 0;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(DatabasePartition partition) {
		DatabasePartition storedPartition = getDatabasePartitionDao().loadById(partition.getPartitionId());
		if (storedPartition == null) {
			getDatabasePartitionDao().persist(partition);
		} else {
			assertSame("dbType", storedPartition.getDbType(), partition.getDbType());
			assertSame("segment", storedPartition.getSegment(), partition.getSegment());
			if (storedPartition.getActivationTimestamp() != null) {
				// active partitions cannot change some fields.
				assertSame("sizeFactor", storedPartition.getSizeFactor(), partition.getSizeFactor());
				assertSame("activationTimestamp", storedPartition.getActivationTimestamp(),
						partition.getActivationTimestamp());
				if (storedPartition.getDeactivationTimestamp() != null) {
					assertSame("deactivationTimestamp", storedPartition.getDeactivationTimestamp(),
							partition.getDeactivationTimestamp());
				}
			}

			getDatabasePartitionDao().merge(partition);
		}
		clearCache();
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(DatabasePartition partition) {
		DatabasePartition storedPartition = getDatabasePartitionDao().loadById(partition.getPartitionId());
		if (storedPartition != null) {
			getDatabasePartitionDao().delete(storedPartition);
		} else {
			log.warn("Unable to find DatabasePartition to delete with id " + partition.getPartitionId());
		}
		clearCache();
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public DatabasePartition findById(String partitionId) {
		conditionalRefreshCache();

		return dpidMap != null ? dpidMap.get(partitionId) : null;
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<DatabasePartition> findByTypeAndSegment(DatabaseType type, String segment) {
		conditionalRefreshCache();

		if (dptsMap != null) {
			Map<String, List<DatabasePartition>> segmentMap = dptsMap.get(type);
			if (segmentMap != null) {
				List<DatabasePartition> segmentList = segmentMap.get(segment);
				if (segmentList != null) {
					return segmentList;
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<DatabasePartition> findByType(DatabaseType type) {
		conditionalRefreshCache();
		List<DatabasePartition> result = new ArrayList<>();
		if (dptsMap != null) {
			Map<String, List<DatabasePartition>> segmentMap = dptsMap.get(type);
			if (segmentMap != null) {
				Collection<List<DatabasePartition>> segmentList = segmentMap.values();
				for (List<DatabasePartition> list : segmentList) {
					result.addAll(list);
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private synchronized void clearCache() {
		cacheLoadTimestamp = 0;
	}

	private void conditionalRefreshCache() {
		if (System.currentTimeMillis() - cacheLoadTimestamp > getCacheTimeoutMillis()) {
			loadCache();
		}
	}

	private synchronized void loadCache() {
		cacheLoadTimestamp = System.currentTimeMillis();

		List<DatabasePartition> list = getDatabasePartitionDao().loadAll();

		Map<String, DatabasePartition> localIdMap = new HashMap<>();
		Map<DatabaseType, Map<String, List<DatabasePartition>>> localTypeSegmentMap = new HashMap<>();

		for (DatabasePartition partition : list) {
			localIdMap.put(partition.getPartitionId(), partition);

			Map<String, List<DatabasePartition>> typeMap = localTypeSegmentMap.get(partition.getDbType());
			if (typeMap == null) {
				typeMap = new HashMap<>();
				localTypeSegmentMap.put(partition.getDbType(), typeMap);
			}
			List<DatabasePartition> segmentPartitions = typeMap.get(partition.getSegment());
			if (segmentPartitions == null) {
				segmentPartitions = new ArrayList<>();
				typeMap.put(partition.getSegment(), segmentPartitions);
			}
			segmentPartitions.add(partition);
		}

		dpidMap = Collections.unmodifiableMap(localIdMap);
		dptsMap = Collections.unmodifiableMap(localTypeSegmentMap);
	}

	/**
	 * @throws IllegalStateException
	 *             if try to change immutable fields.
	 */
	private void assertSame(String field, Object target, Object actual) {
		if (target != null) {
			if (!target.equals(actual)) {
				throw new IllegalStateException("Field is immutable, and cannot be changed or removed." + field);
			}
		} else if (actual != null) {
			throw new IllegalStateException("Field is immutable, and cannot be set." + field);
		}
	}

	/**
	 * @throws IllegalStateException
	 *             if try to change immutable fields.
	 */
	private void assertSame(String field, int target, int actual) {
		if (target != actual) {
			throw new IllegalStateException("Field is immutable, and cannot be changed. " + field);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public DatabasePartitionDao getDatabasePartitionDao() {
		return databasePartitionDao;
	}

	public void setDatabasePartitionDao(DatabasePartitionDao databasePartitionDao) {
		this.databasePartitionDao = databasePartitionDao;
	}

	public long getCacheTimeoutMillis() {
		return cacheTimeoutMillis;
	}

	public void setCacheTimeoutMillis(long cacheTimeoutMillis) {
		this.cacheTimeoutMillis = cacheTimeoutMillis;
	}

}
