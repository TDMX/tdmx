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
package org.tdmx.server.cli.env;

import java.util.List;

import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Option;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.server.cli.cmd.AbstractCliCommand;

@Cli(name = "enum:list", description = "lists all enumerations", note = "not choosing any option will show all enumeration values.")
public class ListEnum extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	@Option(name = "dbType", description = "shows valid database types.")
	private boolean dbType;

	@Option(name = "cache", description = "shows valid cache names.")
	private boolean cache;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		boolean noneSelected = !dbType && !cache;

		if (dbType || noneSelected) {

			List<String> dbTypes = getSas().getDbTypes();
			out.println("Found " + dbTypes.size() + " dbType values.");
			out.println(dbTypes);
		}
		if (cache || noneSelected) {
			List<String> cacheNames = getSas().getCacheNames();
			out.println("Found " + cacheNames.size() + " cache values.");
			out.println(cacheNames);
		}
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
