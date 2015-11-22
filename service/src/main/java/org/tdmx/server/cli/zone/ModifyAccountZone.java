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
package org.tdmx.server.cli.zone;

import java.io.PrintStream;
import java.util.List;

import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.server.cli.cmd.AbstractCliCommand;
import org.tdmx.server.rs.sas.resource.AccountResource;
import org.tdmx.server.rs.sas.resource.AccountZoneResource;
import org.tdmx.server.rs.sas.resource.SegmentResource;

@Cli(name = "zone:modify", description = "modifies an account's zone.", note = ".")
public class ModifyAccountZone extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "accound", required = true, description = "the account identifier.")
	private String accountId;

	@Parameter(name = "zone", required = true, description = "the zone apex.")
	private String zone;

	@Parameter(name = "segment", description = "the zone's segment.")
	private String segment;
	@Parameter(name = "zonePartition", description = "the zone database partition.")
	private String zonePartitionId;
	@Parameter(name = "status", description = "the access status - ACTIVE, MAINTENANCE, BLOCKED.")
	private String status;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		List<AccountResource> accounts = getSas().searchAccount(0, 1, null, accountId);
		if (accounts.isEmpty()) {
			out.println("Account " + accountId + " not found.");
			return;
		}
		AccountResource account = accounts.get(0);

		List<SegmentResource> segments = getSas().searchSegment(0, 1, segment);
		if (segments.isEmpty()) {
			out.println("Segment " + segment + " not found.");
			return;
		}

		List<AccountZoneResource> accountZones = getSas().searchAccountZone(0, 1, account.getAccountId(), zone, null,
				null, null);
		if (accountZones.isEmpty()) {
			out.println("Account zone " + zone + " not found.");
			return;
		}
		AccountZoneResource azr = accountZones.get(0);

		azr.setSegment(segment);
		azr.setZonePartitionId(zonePartitionId);
		azr.setAccessStatus(status);

		AccountZoneResource newAzr = getSas().updateAccountZone(account.getId(), azr.getId(), azr);
		out.println(newAzr.getCliRepresentation());
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
