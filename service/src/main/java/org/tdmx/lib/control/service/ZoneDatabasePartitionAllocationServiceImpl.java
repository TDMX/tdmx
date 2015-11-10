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

import java.security.SecureRandom;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;

/**
 * The implementation of {@link ZoneDatabasePartitionAllocationService} which randomly finds a partition with the
 * likelihood of it's {@link DatabasePartition#getSizeFactor()}.
 * 
 * @author Peter Klauser
 * 
 */
public class ZoneDatabasePartitionAllocationServiceImpl implements ZoneDatabasePartitionAllocationService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ZoneDatabasePartitionAllocationServiceImpl.class);

	private DatabasePartitionService databasePartitionService;

	private final SecureRandom rnd = EntropySource.getSecureRandom();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String getZonePartitionId(String accountId, String zoneApex, String segment) {
		int totalSizeFactor = 0;
		List<DatabasePartition> partitions = getDatabasePartitionService().findByTypeAndSegment(DatabaseType.ZONE,
				segment);
		if (partitions.isEmpty()) {
			log.warn("No ZoneDB partition found for segment " + segment);
			return null;
		}
		for (DatabasePartition p : partitions) {
			if (p.isActive()) {
				totalSizeFactor += p.getSizeFactor();
			}
		}
		if (totalSizeFactor <= 0) {
			log.warn("No active ZoneDB partition found for segment " + segment);
			return null;
		}
		// we get a random number >= 0 and < totalSizeFactor
		int rndValue = rnd.nextInt(totalSizeFactor);
		for (DatabasePartition p : partitions) {
			// we take off each partitions sizeFactor and if we traverse 0 then we've found our partition.
			if (p.isActive()) {
				rndValue -= p.getSizeFactor();
				if (rndValue <= 0) {
					log.info("Chosen ZonePartitionID=" + p.getPartitionId() + " using " + p.getSizeFactor() + "/"
							+ totalSizeFactor + " chance.");
					return p.getPartitionId();
				}
			}
		}
		throw new IllegalStateException("Rnd value smaller than total sizeFactor " + totalSizeFactor);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

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
