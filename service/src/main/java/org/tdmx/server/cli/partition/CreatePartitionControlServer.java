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

import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.server.cli.cmd.AbstractCliCommand;
import org.tdmx.server.rs.sas.resource.PartitionControlServerResource;

@Cli(name = "pcs:create", description = "creates a partition control server", note = "PCS servers are configured before the segment servers are started.")
public class CreatePartitionControlServer extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "segment", required = true, description = "the segment name.")
	private String segment;
	@Parameter(name = "modulo", required = true, description = "the server's load distribution modulo.")
	private int modulo;

	@Parameter(name = "ipaddress", required = true, description = "the TCP/IP address of the PCS-API.")
	private String ipaddress;
	@Parameter(name = "port", required = true, description = "the TCP/IP port of the PCS-API.")
	private int port;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		PartitionControlServerResource pcs = new PartitionControlServerResource();
		pcs.setSegment(segment);
		pcs.setIpaddress(ipaddress);
		pcs.setPort(port);
		pcs.setModulo(modulo);

		PartitionControlServerResource newPCS = getSas().createPartitionControlServer(pcs);
		getPrinter().output(out, newPCS);
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
