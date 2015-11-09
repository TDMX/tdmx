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
import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialStatus;
import org.tdmx.lib.control.domain.AccountZoneStatus;
import org.tdmx.server.cli.cmd.AbstractCliCommand;
import org.tdmx.server.rs.sas.resource.AccountZoneAdministrationCredentialResource;

@Cli(name = "zoneadmin:search", description = "searches for account zone administration credentials.", note = ".")
public class SearchAccountZoneAdministrationCredential extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "accountId", description = "the account identifier.")
	private String accountId;
	@Parameter(name = "zone", description = "the zone apex.")
	private String zone;
	@Parameter(name = "fingerprint", description = "the fingerprint of the ZAC.")
	private String fingerprint;
	@Parameter(name = "status", description = "the installation status - PENDING_INSTALLATION, PENDING_DEINSTALLATION, INVALID_PEM, INVALID_TDMX, INVALID_ZAC, NON_ZAC, NO_DNS_TRUST, INSTALLED, DEINSTALLED.")
	private String status;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		if (StringUtils.hasText(status)
				&& EnumUtils.mapTo(AccountZoneAdministrationCredentialStatus.class, status) == null) {
			out.println("Status invalid " + status + ". Use one of "
					+ StringUtils.arrayToCommaDelimitedString(AccountZoneStatus.values()));
			return;
		}

		int results = 0;
		int page = 0;
		List<AccountZoneAdministrationCredentialResource> accountZACs = null;
		do {
			accountZACs = getSas().searchAccountZoneAdministrationCredential(page++, PAGE_SIZE, zone, accountId,
					fingerprint, status);

			for (AccountZoneAdministrationCredentialResource azac : accountZACs) {
				out.println(azac.getCliRepresentation());
				results++;
			}
		} while (accountZACs.size() == PAGE_SIZE);
		out.println("Found " + results + " zone administration credentials.");
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
