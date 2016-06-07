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

import java.util.Date;
import java.util.List;

import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;

/**
 * The DatabasePartitionCache provides a cache of DatabasePartition records.
 * 
 * @author Peter
 * 
 */
public interface DatabasePartitionCache {

	/**
	 * Find the DatabasePartition by partitionId.
	 * 
	 * @param partitionId
	 * @return the DatabasePartition or null if not found.
	 */
	public DatabasePartition findByPartitionId(String partitionId);

	/**
	 * Get the list of DatabasePartitions which are active ( sorted by partitionId ) at a specific time.
	 * 
	 * @param type
	 * @param timestamp
	 * @return
	 */
	public List<DatabasePartition> getActiveAtTimestamp(DatabaseType type, Date timestamp);
}
