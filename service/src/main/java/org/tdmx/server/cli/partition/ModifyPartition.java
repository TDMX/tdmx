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
package org.tdmx.server.cli.partition;

import java.io.PrintStream;
import java.util.List;

import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.server.cli.cmd.AbstractCliCommand;
import org.tdmx.server.rs.sas.resource.DatabasePartitionResource;

@Cli(name = "partition:modify", description = "modifies a database partition")
public class ModifyPartition extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "partitionId", required = true, description = "the partitionId.")
	private String partitionId;

	@Parameter(name = "sizeFactor", description = "the partition's size factor - used to load-balance. The value relates this partition's capacity to other partition's capacity for databases of the same type and segment.")
	private Integer sizeFactor;

	@Parameter(name = "url", description = "the RDBMS connection URL.")
	private String url;
	@Parameter(name = "username", description = "the RDBMS connection username.")
	private String username;
	@Parameter(name = "password", description = "the RDBMS connection password.")
	private String password;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		List<DatabasePartitionResource> dbPartitions = getSas().searchDatabasePartition(0, 1, partitionId, null, null);
		if (dbPartitions.isEmpty()) {
			out.println("No DatabasePartition found with partitionId " + partitionId);
			return;
		}

		DatabasePartitionResource dbPartition = dbPartitions.get(0);
		if (sizeFactor != null) {
			dbPartition.setSizeFactor(sizeFactor);
		}
		if (url != null) {
			dbPartition.setUrl(url);
		}
		if (username != null) {
			dbPartition.setUsername(username);
		}
		if (password != null) {
			dbPartition.setPassword(password);
		}

		DatabasePartitionResource newDbPartition = getSas().updateDatabasePartition(dbPartition.getId(), dbPartition);
		out.println(newDbPartition.getCliRepresentation());
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

}
