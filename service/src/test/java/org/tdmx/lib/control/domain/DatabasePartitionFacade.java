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
package org.tdmx.lib.control.domain;

import java.util.Date;

public class DatabasePartitionFacade {

	public static DatabasePartition createHQLDatabasePartition(String partitionId, DatabaseType dbType, String segment,
			Date activationDate) throws Exception {
		/*
		 * jdbc.zonedb.url=jdbc:hsqldb:mem:zone
		 * 
		 * jdbc.zonedb.driverClassName=org.hsqldb.jdbcDriver
		 * 
		 * jdbc.zonedb.username=sa
		 * 
		 * jdbc.zonedb.password=
		 * 
		 * jdbc.zonedb.hibernate.dialect=org.hibernate.dialect.HSQLDialect
		 * 
		 * jdbc.zonedb.hibernate.showSql=false
		 * 
		 * jdbc.zonedb.hibernate.generateDdl=true
		 */

		DatabasePartition p = new DatabasePartition();
		p.setPartitionId(partitionId);

		p.setDbType(dbType);
		p.setSegment(segment);

		p.setSizeFactor(100);
		p.setUrl("jdbc:hsqldb:mem:" + partitionId);
		p.setUsername("sa");
		p.setPassword("");

		p.setActivationTimestamp(activationDate); // currently active
		p.setDeactivationTimestamp(null);
		return p;
	}

}
