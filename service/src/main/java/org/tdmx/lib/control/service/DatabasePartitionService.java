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

import java.util.List;

import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;

/**
 * The DatabasePartitionService provides a cache of slowly changing DatabasePartition records.
 * 
 * Cache is a simple time based cache, refreshing all entities ( of which a small finite number are expected ).
 * 
 * @author Peter
 * 
 */
public interface DatabasePartitionService {

	public void createOrUpdate(DatabasePartition partition);

	// a partition can only be deleted if it is not yet activated.
	public void delete(DatabasePartition partition);

	public DatabasePartition findById(Long id);

	/**
	 * Find the DatabasePartition by partitionId.
	 * 
	 * @param partitionId
	 * @return the DatabasePartition or null if not found.
	 */
	public DatabasePartition findByPartitionId(String partitionId);

	public List<DatabasePartition> findByTypeAndSegment(DatabaseType type, String segment);

	public List<DatabasePartition> findByType(DatabaseType type);

	public List<DatabasePartition> findAll();
}
