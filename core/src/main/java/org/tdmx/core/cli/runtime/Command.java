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
package org.tdmx.core.cli.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.CommandDescriptor;

public class Command {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(Command.class);

	private final CommandDescriptor descriptor;
	private final List<CommandParameter> parameters = new ArrayList<>();
	private final List<CommandOption> options = new ArrayList<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public Command(CommandDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public CommandDescriptor getDescriptor() {
		return descriptor;
	}

	public List<CommandParameter> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	public void addParameter(CommandParameter param) {
		parameters.add(param);
	}

	public List<CommandOption> getOptions() {
		return Collections.unmodifiableList(options);
	}

	public void addOption(CommandOption option) {
		options.add(option);
	}

	public boolean supportsParameter(String parameterName) {
		return descriptor.getParameter(parameterName) != null;
	}

	public boolean supportsOption(String parameterName) {
		return descriptor.getOption(parameterName) != null;
	}

}
