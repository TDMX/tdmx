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
package org.tdmx.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.runtime.Command;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.cli.runtime.CommandExecutableFactory;
import org.tdmx.core.cli.runtime.CommandOption;
import org.tdmx.core.cli.runtime.CommandParameter;

public class CliRunnerImpl implements CliRunner {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(CliRunnerImpl.class);

	// internal
	private CommandExecutableFactory commandExecutableFactory;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void execute(Command cmd) {
		System.out.println("Executing  " + cmd.getDescriptor().getName());
		for (CommandParameter p : cmd.getParameters()) {
			System.out.println("\tParam " + p.getDescriptor().getName() + "=" + p.getValue());
		}
		for (CommandOption o : cmd.getOptions()) {
			System.out.println("\tOption " + o.getDescriptor().getName());
		}

		CommandExecutable exec = commandExecutableFactory.getCommandExecutable(cmd.getDescriptor().getName());
		if (exec == null) {
			log.warn("Unknown executor for " + cmd.getDescriptor().getName());
		}
		bind(cmd, exec);
		run(exec);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void bind(Command cmd, CommandExecutable exec) {
		// TODO #87 bind the parsed variables , setting them on the executable
	}

	private void run(CommandExecutable exec) {
		exec.execute(System.out, System.err);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public CommandExecutableFactory getCommandExecutableFactory() {
		return commandExecutableFactory;
	}

	public void setCommandExecutableFactory(CommandExecutableFactory commandExecutableFactory) {
		this.commandExecutableFactory = commandExecutableFactory;
	}

}
