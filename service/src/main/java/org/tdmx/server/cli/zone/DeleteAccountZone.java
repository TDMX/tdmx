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

import javax.ws.rs.core.Response;

import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.server.cli.cmd.AbstractCliCommand;
import org.tdmx.server.rs.sas.resource.AccountResource;
import org.tdmx.server.rs.sas.resource.AccountZoneResource;

@Cli(name = "zone:delete", description = "deletes an account's zone.", note = ".")
public class DeleteAccountZone extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "account", required = true, description = "the account identifier.")
	private String accountId;

	@Parameter(name = "zone", required = true, description = "the zone apex.")
	private String zone;

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

		List<AccountZoneResource> accountZones = getSas().searchAccountZone(0, 1, account.getAccountId(), zone, null,
				null, null);
		if (accountZones.isEmpty()) {
			out.println("Account zone " + zone + " not found.");
			return;
		}
		AccountZoneResource azr = accountZones.get(0);

		Response response = getSas().deleteAccountZone(account.getId(), azr.getId());
		out.print(azr.getCliRepresentation());
		if (response.getStatus() == SUCCESS) {
			out.println(" Deleted.");
		} else {
			out.println(" Not deleted. StatusCode=" + response.getStatus());
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
