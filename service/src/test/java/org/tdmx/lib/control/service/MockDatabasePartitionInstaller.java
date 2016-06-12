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

import java.net.URL;
import java.util.Date;

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.tdmx.core.system.lang.NetUtils;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabasePartitionFacade;
import org.tdmx.lib.control.domain.DatabaseType;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.domain.SegmentFacade;
import org.tdmx.lib.control.service.DatabasePartitionService;
import org.tdmx.lib.control.service.SegmentService;

public class MockDatabasePartitionInstaller {

	public static final String S1 = "segment1";
	public static final String S2 = "segment2";

	public static String SCS_S1 = "segment1.scs.tdmx.org";
	public static URL SCS_S1_URL = NetUtils.getURL("https://segment1.scs.tdmx.org/sp/v1.0/scs");
	public static String SCS_S2 = "segment2.scs.tdmx.org";
	public static URL SCS_S2_URL = NetUtils.getURL("https://segment2.scs.tdmx.org/sp/v1.0/scs");

	public static String ZP1_S1 = "z-unittest-segment1-id1";
	public static String ZP2_S1 = "z-unittest-segment1-id2";
	public static String ZP3_S1 = "z-unittest-segment1-id3";

	public static String MP1_S1 = "m-unittest-segment1-id1";
	public static String MP2_S1 = "m-unittest-segment1-id2";
	public static String MP3_S1 = "m-unittest-segment1-id3";

	public static String ZP1_S2 = "z-unittest-segment2-id1";
	public static String ZP2_S2 = "z-unittest-segment2-id2";
	public static String ZP3_S2 = "z-unittest-segment2-id3";

	public static String MP1_S2 = "m-unittest-segment2-id1";
	public static String MP2_S2 = "m-unittest-segment2-id2";
	public static String MP3_S2 = "m-unittest-segment2-id3";

	private SegmentService segmentService;

	private DatabasePartitionService databasePartitionService;

	public void init() throws Exception {
		// setup 2 segments.
		{
			Segment s1 = segmentService.findBySegment(S1);
			if (s1 == null) {
				s1 = SegmentFacade.createSegment(S1, SCS_S1_URL.toString());
				segmentService.createOrUpdate(s1);
			}
		}
		{
			Segment s2 = segmentService.findBySegment(S2);
			if (s2 == null) {
				s2 = SegmentFacade.createSegment(S2, SCS_S2_URL.toString());
				segmentService.createOrUpdate(s2);
			}
		}

		Date activationDate = new Date();
		// setup 2 sets of 3 zonedbs.
		{
			DatabasePartition zp1 = DatabasePartitionFacade.createHQLDatabasePartition(ZP1_S1, DatabaseType.ZONE, S1,
					activationDate);
			if (databasePartitionService.findByPartitionId(zp1.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp1);

				installZoneDB(ZP1_S1);
			}

			DatabasePartition zp2 = DatabasePartitionFacade.createHQLDatabasePartition(ZP2_S1, DatabaseType.ZONE, S1,
					activationDate);
			if (databasePartitionService.findByPartitionId(zp2.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp2);

				installZoneDB(ZP2_S1);
			}

			DatabasePartition zp3 = DatabasePartitionFacade.createHQLDatabasePartition(ZP3_S1, DatabaseType.ZONE, S1,
					activationDate);
			if (databasePartitionService.findByPartitionId(zp3.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp3);

				installZoneDB(ZP3_S1);
			}
		}
		{
			DatabasePartition zp1 = DatabasePartitionFacade.createHQLDatabasePartition(ZP1_S2, DatabaseType.ZONE, S2,
					activationDate);
			if (databasePartitionService.findByPartitionId(zp1.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp1);

				installZoneDB(ZP1_S2);
			}

			DatabasePartition zp2 = DatabasePartitionFacade.createHQLDatabasePartition(ZP2_S2, DatabaseType.ZONE, S2,
					activationDate);
			if (databasePartitionService.findByPartitionId(zp2.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp2);

				installZoneDB(ZP2_S2);
			}

			DatabasePartition zp3 = DatabasePartitionFacade.createHQLDatabasePartition(ZP3_S2, DatabaseType.ZONE, S2,
					activationDate);
			if (databasePartitionService.findByPartitionId(zp3.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(zp3);

				installZoneDB(ZP3_S2);
			}
		}

		// setup the message DBs of the 2 segments.
		{
			DatabasePartition mp1 = DatabasePartitionFacade.createHQLDatabasePartition(MP1_S1, DatabaseType.CHUNK, S1,
					activationDate);
			if (databasePartitionService.findByPartitionId(mp1.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(mp1);

				installChunkDB(MP1_S1);
			}
			DatabasePartition mp2 = DatabasePartitionFacade.createHQLDatabasePartition(MP2_S1, DatabaseType.CHUNK, S1,
					activationDate);
			if (databasePartitionService.findByPartitionId(mp2.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(mp2);

				installChunkDB(MP2_S1);
			}
			DatabasePartition mp3 = DatabasePartitionFacade.createHQLDatabasePartition(MP3_S1, DatabaseType.CHUNK, S1,
					activationDate);
			if (databasePartitionService.findByPartitionId(mp3.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(mp3);

				installChunkDB(MP3_S1);
			}
		}
		{
			DatabasePartition mp1 = DatabasePartitionFacade.createHQLDatabasePartition(MP1_S2, DatabaseType.CHUNK, S2,
					activationDate);
			if (databasePartitionService.findByPartitionId(mp1.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(mp1);

				installChunkDB(MP1_S2);
			}
			DatabasePartition mp2 = DatabasePartitionFacade.createHQLDatabasePartition(MP2_S2, DatabaseType.CHUNK, S2,
					activationDate);
			if (databasePartitionService.findByPartitionId(mp2.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(mp2);

				installChunkDB(MP2_S2);
			}
			DatabasePartition mp3 = DatabasePartitionFacade.createHQLDatabasePartition(MP3_S2, DatabaseType.CHUNK, S2,
					activationDate);
			if (databasePartitionService.findByPartitionId(mp3.getPartitionId()) == null) {
				databasePartitionService.createOrUpdate(mp3);

				installChunkDB(MP3_S2);
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

	private void installChunkDB(String partitionId) {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		builder = builder.setType(EmbeddedDatabaseType.HSQL).addScript("chunkDBPartition-schema.sql");
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
