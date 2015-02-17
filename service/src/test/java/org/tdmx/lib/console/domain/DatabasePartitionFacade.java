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
package org.tdmx.lib.console.domain;

import java.util.Date;

import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;

public class DatabasePartitionFacade {

	public static DatabasePartition createDatabasePartition(Long id, String partitionId, DatabaseType dbType,
			String segment) throws Exception {
		DatabasePartition p = new DatabasePartition();
		p.setId(id); // TODO remove
		p.setPartitionId(partitionId);

		p.setDbType(dbType);
		p.setSegment(segment);

		p.setSizeFactor(100);
		p.setUrl("db.url-" + partitionId);
		p.setUsername("username-" + partitionId);
		p.setPassword("pwd-" + partitionId);

		p.setActivationTimestamp(new Date()); // currently active
		p.setDeactivationTimestamp(null);
		return p;
	}

}
