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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;
import org.tdmx.server.cache.CacheInvalidationInstruction;
import org.tdmx.server.cache.CacheInvalidationListener;
import org.tdmx.server.pcs.protobuf.Cache.CacheName;

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
	private Map<String, DatabasePartition> idMap; // partitionId->PartitionMap
	private Map<DatabaseType, List<DatabaseSetTimeDimension>> typeMap; // dbType->{TimeDimension}

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public DatabasePartitionCacheImpl() {

	}

	public class DatabaseSetTimeDimension {
		private final long fromTimestamp;
		private final long toTimestamp;
		private List<DatabasePartition> activePartitions = new ArrayList<>();

		public DatabaseSetTimeDimension(long fromTS, long toTS) {
			this.fromTimestamp = fromTS;
			this.toTimestamp = toTS;
		}

		/**
		 * fix order and make unmodifiable.
		 */
		public void fix() {
			Collections.sort(activePartitions, new Comparator<DatabasePartition>() {

				@Override
				public int compare(DatabasePartition o1, DatabasePartition o2) {
					return o1.getPartitionId().compareTo(o2.getPartitionId());
				}
			});

			activePartitions = Collections.unmodifiableList(activePartitions);
		}

		public void addActivePartition(DatabasePartition partition) {
			activePartitions.add(partition);
		}

		public List<DatabasePartition> getActivePartitions() {
			return activePartitions;
		}

		public long getFromTimestamp() {
			return fromTimestamp;
		}

		public long getToTimestamp() {
			return toTimestamp;
		}

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void invalidateCache(CacheInvalidationInstruction message) {
		if (CacheName.DatabasePartition == message.getName()) {
			log.debug("Invalidating cache. Forcing new fetch");
			fetchDatabasePartitions(true);
		}
	}

	@Override
	public DatabasePartition findByPartitionId(String partitionId) {
		if (idMap == null) {
			fetchDatabasePartitions(false);
		}
		return idMap.get(partitionId);
	}

	@Override
	public List<DatabasePartition> getActiveAtTimestamp(DatabaseType type, Date timestamp) {
		if (typeMap == null) {
			fetchDatabasePartitions(false);
		}
		List<DatabaseSetTimeDimension> dimensions = typeMap.get(type);
		long ts = timestamp.getTime();
		for (DatabaseSetTimeDimension dimension : dimensions) {
			if (dimension.getFromTimestamp() <= ts && dimension.getToTimestamp() >= ts) {
				return dimension.getActivePartitions();
			}
		}
		log.warn("Did not find active partitions for " + type + " at " + timestamp);
		return Collections.emptyList();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private synchronized void fetchDatabasePartitions(boolean forceFetch) {
		if (idMap == null || typeMap == null || forceFetch) {
			List<DatabasePartition> list = getDatabasePartitionService().findAll();

			// prepare the map of id->partition
			Map<String, DatabasePartition> localIdMap = new HashMap<>();
			for (DatabasePartition partition : list) {
				localIdMap.put(partition.getPartitionId(), partition);
			}

			// prepare the local type map
			Map<DatabaseType, List<DatabaseSetTimeDimension>> localTypeMap = new HashMap<>();
			for (DatabaseType type : DatabaseType.values()) {
				List<DatabasePartition> partitions = getAllActivatedPartitionsOfType(list, type);
				Set<Date> dates = getAllDatesAscendingOrder(partitions);

				List<DatabaseSetTimeDimension> dimensions = convertDatesToDimension(dates);
				mergePartitionsIntoDimensions(partitions, dimensions);
				localTypeMap.put(type, dimensions); // time sorted list of dimensions
			}

			idMap = Collections.unmodifiableMap(localIdMap);
			typeMap = Collections.unmodifiableMap(localTypeMap);
		}
	}

	private void mergePartitionsIntoDimensions(List<DatabasePartition> partitions,
			List<DatabaseSetTimeDimension> periods) {
		for (DatabaseSetTimeDimension period : periods) {
			for (DatabasePartition partition : partitions) {
				// if the partition is active throughout the period the add to the period
				long fromTs = partition.getActivationTimestamp().getTime();
				if (partition.getDeactivationTimestamp() != null) {
					// a deactivated partition
					long toTs = partition.getDeactivationTimestamp().getTime();
					if (fromTs <= period.getFromTimestamp() && toTs >= period.getToTimestamp()) {
						period.addActivePartition(partition);
					}
				} else {
					// an active partition
					if (fromTs <= period.getFromTimestamp()) {
						period.addActivePartition(partition);
					}
				}

			}
		}
	}

	private List<DatabaseSetTimeDimension> convertDatesToDimension(Set<Date> dates) {
		List<Date> allDates = new ArrayList<>();
		allDates.addAll(dates);

		List<DatabaseSetTimeDimension> result = new ArrayList<>();
		for (int idx = 0; idx < allDates.size() - 1; idx++) {
			result.add(new DatabaseSetTimeDimension(allDates.get(idx).getTime(), allDates.get(idx + 1).getTime()));
		}
		return result;
	}

	/**
	 * Get all partition which are activated.
	 * 
	 * @param partitions
	 * @param type
	 * @return
	 */
	private List<DatabasePartition> getAllActivatedPartitionsOfType(List<DatabasePartition> partitions,
			DatabaseType type) {
		List<DatabasePartition> subList = new ArrayList<>();
		for (DatabasePartition partition : partitions) {
			if (type == partition.getDbType() && partition.getActivationTimestamp() != null) {
				subList.add(partition);
			}
		}
		return subList;
	}

	/**
	 * Return the set of all activation and deactivation dates in ascending order.
	 * 
	 * @param partitions
	 * @return
	 */
	private Set<Date> getAllDatesAscendingOrder(List<DatabasePartition> partitions) {
		Set<Date> allDates = new TreeSet<>();
		// default sort increasing...
		for (DatabasePartition partition : partitions) {
			if (partition.getActivationTimestamp() != null) {
				allDates.add(partition.getActivationTimestamp());
				if (partition.getDeactivationTimestamp() != null) {
					allDates.add(partition.getDeactivationTimestamp());
				}
			} else {
				log.debug("Partition " + partition + " is active, ignore.");
			}

		}
		// put an end of time date as last.
		Calendar eot = Calendar.getInstance();
		eot.add(Calendar.YEAR, 100);
		allDates.add(eot.getTime());

		return allDates;
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
