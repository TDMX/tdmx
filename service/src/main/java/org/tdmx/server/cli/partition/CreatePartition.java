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

import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.server.cli.cmd.AbstractCliCommand;
import org.tdmx.server.rs.sas.resource.DatabasePartitionResource;

@Cli(name = "partition:create", description = "creates a database partition", note = "once active, a partition's size factor cannot change.")
public class CreatePartition extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "partition", required = true, description = "the partition identifier.")
	private String partitionId;
	@Parameter(name = "dbType", required = true, description = "the database type ( use 'enum:list dbType' for valid values).")
	private String dbType;
	@Parameter(name = "segment", required = true, description = "the segment name.")
	private String segment;

	@Parameter(name = "sizeFactor", required = true, description = "the partition's size factor - used to load-balance. The value relates this partition's capacity to other partition's capacity for databases of the same type and segment.")
	private int sizeFactor;

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
	public void run(CliPrinter out) {
		DatabasePartitionResource dbPartition = new DatabasePartitionResource();
		dbPartition.setPartitionId(partitionId);
		dbPartition.setDbType(dbType);
		dbPartition.setSegment(segment);
		dbPartition.setSizeFactor(sizeFactor);

		dbPartition.setUrl(url);
		dbPartition.setUsername(username);
		dbPartition.setPassword(password);

		DatabasePartitionResource newDbPartition = getSas().createDatabasePartition(dbPartition);
		out.println(newDbPartition);
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
