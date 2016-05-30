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
import org.tdmx.client.cli.StaticDefaultParameterProvider;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Option;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.lang.StringUtils;

@Cli(name = "default:set", description = "Sets the CLI's default parameters and options.")
public class SetDefault implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Option(name = "verbose", description = "sets the output verbosity.")
	private Boolean verbose;

	@Parameter(name = "zacPassword", masked = true, description = "the zone administrator's keystore password.", noDefault = true)
	private String zacPassword;

	@Parameter(name = "dacPassword", masked = true, description = "the domain administrator's keystore password.", noDefault = true)
	private String dacPassword;

	@Parameter(name = "userPassword", masked = true, description = "the user's keystore password.", noDefault = true)
	private String userPassword;

	@Parameter(name = "domain", description = "the domain.", noDefault = true)
	private String domain;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		if (verbose != null) {
			ClientCliLoggingUtils.setVerbose(true);
			out.println("verbose - option " + (ClientCliLoggingUtils.isVerbose() ? "set" : "not set"));
		}
		if (StringUtils.hasText(zacPassword)) {
			out.println("Setting zacPassword.");
			StaticDefaultParameterProvider.setDefaultValue("zacPassword", zacPassword);
		}
		if (StringUtils.hasText(dacPassword)) {
			out.println("Setting dacPassword.");
			StaticDefaultParameterProvider.setDefaultValue("dacPassword", dacPassword);
		}
		if (StringUtils.hasText(userPassword)) {
			out.println("Setting userPassword.");
			StaticDefaultParameterProvider.setDefaultValue("userPassword", userPassword);
		}
		if (StringUtils.hasText(domain)) {
			out.println("Setting domain.");
			StaticDefaultParameterProvider.setDefaultValue("domain", domain);
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
