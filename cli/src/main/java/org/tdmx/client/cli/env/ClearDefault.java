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
package org.tdmx.client.cli.env;

import org.tdmx.client.cli.ClientCliLoggingUtils;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Option;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.runtime.CommandExecutable;

@Cli(name = "default:clear", description = "Clear the CLI's default parameters.")
public class ClearDefault implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Option(name = "verbose", description = "clears the output verbosity.")
	private Boolean verbose;

	@Option(name = "zacPassword", description = "the zone administrator's keystore password.")
	private Boolean zacPassword;

	@Option(name = "dacPassword", description = "the domain administrator's keystore password.")
	private Boolean dacPassword;

	@Option(name = "userPassword", description = "the domain administrator's keystore password.")
	private Boolean userPassword;

	@Option(name = "domain", description = "the domain.")
	private Boolean domain;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		if (verbose != null) {
			ClientCliLoggingUtils.setVerbose(false);
			out.println("verbose - option " + (ClientCliLoggingUtils.isVerbose() ? "set" : "not set"));
		}
		if (zacPassword != null) {
			out.println("Clearing default for zacPassword.");
			// FIXME StaticDefaultParameterProvider.clearDefaultValue("zacPassword");
		}
		if (dacPassword != null) {
			out.println("Clearing default for dacPassword.");
			// FIXME StaticDefaultParameterProvider.clearDefaultValue("dacPassword");
		}
		if (userPassword != null) {
			out.println("Clearing default for userPassword.");
			// FIXME StaticDefaultParameterProvider.clearDefaultValue("userPassword");
		}
		if (domain != null) {
			out.println("Clearing default for domain.");
			// FIXME StaticDefaultParameterProvider.clearDefaultValue("domain");
		}
		out.println(ClientCliLoggingUtils.commandExecuted());
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
