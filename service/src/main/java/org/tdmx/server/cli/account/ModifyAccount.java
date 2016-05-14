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
package org.tdmx.server.cli.account;

import java.util.List;

import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.server.cli.cmd.AbstractCliCommand;
import org.tdmx.server.rs.sas.resource.AccountResource;

@Cli(name = "account:modify", description = "modifies an account", note = "any parameter not defined will not be changed.")
public class ModifyAccount extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "accountId", required = true, description = "the account's id.")
	private String accountId;

	@Parameter(name = "email", description = "the account owner's email address.")
	private String email;
	@Parameter(name = "firstName", description = "the account owner's firstName.")
	private String firstName;
	@Parameter(name = "lastName", description = "the account owner's lastName.")
	private String lastName;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		List<AccountResource> accounts = getSas().searchAccount(0, 1, null, accountId);
		if (accounts.size() != 1) {
			out.println("Account " + accountId + " not found");
			return;
		}
		AccountResource ar = accounts.get(0);
		if (email != null) {
			ar.setEmail(email);
		}
		if (firstName != null) {
			ar.setFirstname(firstName);
		}
		if (lastName != null) {
			ar.setLastname(lastName);
		}

		AccountResource updatedAr = getSas().updateAccount(ar.getId(), ar);
		out.println(updatedAr);
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
