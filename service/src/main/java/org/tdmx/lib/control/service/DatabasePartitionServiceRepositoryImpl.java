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

import static org.tdmx.core.system.lang.AssertionUtils.assertSame;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.control.dao.DatabasePartitionDao;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabasePartitionSearchCriteria;
import org.tdmx.server.pcs.CacheInvalidationNotifier;

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
	private CacheInvalidationNotifier cacheInvalidationNotifier;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(DatabasePartition partition) {
		if (partition == null) {
			throw new IllegalArgumentException("missing partition");
		}
		if (partition.getId() != null) {
			DatabasePartition storedPartition = getDatabasePartitionDao().loadById(partition.getId());
			if (storedPartition != null) {
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

			} else {
				log.warn("Unable to find DatabasePartition with id " + partition.getId());
			}
		} else {
			getDatabasePartitionDao().persist(partition);
		}
		notifyPartitionsChanged();
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(DatabasePartition partition) {
		DatabasePartition storedPartition = getDatabasePartitionDao().loadById(partition.getId());
		if (storedPartition != null) {
			getDatabasePartitionDao().delete(storedPartition);
		} else {
			log.warn("Unable to find DatabasePartition to delete with id " + partition.getId());
		}
		notifyPartitionsChanged();
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<DatabasePartition> findAll() {
		return getDatabasePartitionDao().loadAll();
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public DatabasePartition findById(Long id) {
		return getDatabasePartitionDao().loadById(id);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public DatabasePartition findByPartitionId(String partitionId) {
		return getDatabasePartitionDao().loadByPartitionId(partitionId);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<DatabasePartition> search(DatabasePartitionSearchCriteria criteria) {
		return getDatabasePartitionDao().search(criteria);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void notifyPartitionsChanged() {
		if (cacheInvalidationNotifier != null) {
			cacheInvalidationNotifier.cacheInvalidated(CACHE_KEY);
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

	public CacheInvalidationNotifier getCacheInvalidationNotifier() {
		return cacheInvalidationNotifier;
	}

	public void setCacheInvalidationNotifier(CacheInvalidationNotifier cacheInvalidationNotifier) {
		this.cacheInvalidationNotifier = cacheInvalidationNotifier;
	}

}
