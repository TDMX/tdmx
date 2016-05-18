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

import org.tdmx.core.cli.CliPrinterFactory;
import org.tdmx.core.cli.DefaultParameterProvider;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.server.cli.cmd.AbstractCliCommand;

@Cli(name = "default:show", description = "shows the default values.")
public class ShowDefault extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private CliPrinterFactory cliPrinterFactory;
	private DefaultParameterProvider defaultProvider; // currently unused.....

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		out.println("verbose - option " + (cliPrinterFactory.isVerbose() ? "set" : "not set"));
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

	public CliPrinterFactory getCliPrinterFactory() {
		return cliPrinterFactory;
	}

	public void setCliPrinterFactory(CliPrinterFactory cliPrinterFactory) {
		this.cliPrinterFactory = cliPrinterFactory;
	}

	public DefaultParameterProvider getDefaultProvider() {
		return defaultProvider;
	}

	public void setDefaultProvider(DefaultParameterProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}

}
