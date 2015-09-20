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
package org.tdmx.lib.zone.service;

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabasePartitionFacade;
import org.tdmx.lib.control.domain.DatabaseType;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.domain.SegmentFacade;
import org.tdmx.lib.control.service.DatabasePartitionService;
import org.tdmx.lib.control.service.SegmentService;

public class MockZonePartitionIdInstaller {

	public static final String S1 = "segment1";
	public static final String S2 = "segment2";

	public static String SCS_S1 = "segment1.scs.tdmx.org";
	public static String SCS_S2 = "segment2.scs.tdmx.org";

	public static String ZP1_S1 = "z-unittest-segment1-id1";
	public static String ZP2_S1 = "z-unittest-segment1-id2";
	public static String ZP3_S1 = "z-unittest-segment1-id3";

	public static String ZP1_S2 = "z-unittest-segment2-id1";
	public static String ZP2_S2 = "z-unittest-segment2-id2";
	public static String ZP3_S2 = "z-unittest-segment2-id3";

	private SegmentService segmentService;

	private DatabasePartitionService databasePartitionService;

	public void init() throws Exception {
		{
			Segment s1 = SegmentFacade.createSegment(S1, SCS_S1);
			segmentService.createOrUpdate(s1);
		}
		{
			Segment s2 = SegmentFacade.createSegment(S2, SCS_S2);
			segmentService.createOrUpdate(s2);
		}
		{

			DatabasePartition zp1 = DatabasePartitionFacade.createDatabasePartition(ZP1_S1, DatabaseType.ZONE, S1);
			if (databasePartitionService.findByPartitionId(zp1.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp1);

				installZoneDB(ZP1_S1);
			}

			DatabasePartition zp2 = DatabasePartitionFacade.createDatabasePartition(ZP2_S1, DatabaseType.ZONE, S1);
			if (databasePartitionService.findByPartitionId(zp2.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp2);

				installZoneDB(ZP2_S1);
			}

			DatabasePartition zp3 = DatabasePartitionFacade.createDatabasePartition(ZP3_S1, DatabaseType.ZONE, S1);
			if (databasePartitionService.findByPartitionId(zp3.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp3);

				installZoneDB(ZP3_S1);
			}
		}
		{
			DatabasePartition zp1 = DatabasePartitionFacade.createDatabasePartition(ZP1_S2, DatabaseType.ZONE, S2);
			if (databasePartitionService.findByPartitionId(zp1.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp1);

				installZoneDB(ZP1_S2);
			}

			DatabasePartition zp2 = DatabasePartitionFacade.createDatabasePartition(ZP2_S2, DatabaseType.ZONE, S2);
			if (databasePartitionService.findByPartitionId(zp2.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp2);

				installZoneDB(ZP2_S2);
			}

			DatabasePartition zp3 = DatabasePartitionFacade.createDatabasePartition(ZP3_S2, DatabaseType.ZONE, S2);
			if (databasePartitionService.findByPartitionId(zp3.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp3);

				installZoneDB(ZP3_S2);
			}
		}

	}

	private void installZoneDB(String partitionId) {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		builder = builder.setType(EmbeddedDatabaseType.HSQL).addScript("zoneDBPartition-schema.sql");
		builder.setName(partitionId);
		builder.continueOnError(false);
		builder.ignoreFailedDrops(true);
		builder.build();
	}

	public SegmentService getSegmentService() {
		return segmentService;
	}

	public void setSegmentService(SegmentService segmentService) {
		this.segmentService = segmentService;
	}

	public DatabasePartitionService getDatabasePartitionService() {
		return databasePartitionService;
	}

	public void setDatabasePartitionService(DatabasePartitionService databasePartitionService) {
		this.databasePartitionService = databasePartitionService;
	}

}
