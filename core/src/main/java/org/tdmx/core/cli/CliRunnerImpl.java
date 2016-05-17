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

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.runtime.Command;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.cli.runtime.CommandExecutableFactory;
import org.tdmx.core.cli.runtime.CommandOption;
import org.tdmx.core.cli.runtime.CommandParameter;
import org.tdmx.core.system.lang.StringUtils;

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
	private CliPrinterFactory cliPrinterFactory;
	private DefaultParameterProvider defaultProvider;
	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void execute(Command cmd, PrintStream out) {
		debugLog(cmd);

		CommandExecutable exec = commandExecutableFactory.getCommandExecutable(cmd.getDescriptor().getName());
		if (exec == null) {
			throw new IllegalArgumentException("Unknown executor for " + cmd.getDescriptor().getName());
		}
		complete(cmd);
		check(cmd);
		bind(cmd, exec);

		CliPrinter cliPrinter = cliPrinterFactory.getPrinter(out);
		exec.run(cliPrinter);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	/**
	 * Check that all required parameters are set, and set any parameters which were not provided but we have a default
	 * value.
	 * 
	 * @param cmd
	 */
	private void complete(Command cmd) {
		for (ParameterDescriptor param : cmd.getDescriptor().getParameters()) {
			// set the default values of any where the value is not yet set
			if (cmd.getParameter(param.getName()) == null && defaultProvider.getDefault(param.getName()) != null) {
				log.debug("Defaulting " + param.getName() + " from default provider.");
				CommandParameter p = new CommandParameter(param, defaultProvider.getDefault(param.getName()));
				cmd.addParameter(p);
			}

			// provide the default value to optional parameters where the value is not already set.
			if (cmd.getParameter(param.getName()) == null && !param.isRequired()
					&& StringUtils.hasText(param.getDefaultValue())) {
				log.debug("Defaulting " + param.getName() + " from command definition.");
				CommandParameter p = new CommandParameter(param, param.getDefaultValue());
				cmd.addParameter(p);
			}
		}
	}

	/**
	 * Check that all required parameters are set, and set any parameters which were not provided but we have a default
	 * value.
	 * 
	 * @param cmd
	 */
	private void check(Command cmd) {
		for (ParameterDescriptor param : cmd.getDescriptor().getParameters()) {
			if (param.isRequired() && cmd.getParameter(param.getName()) == null) {
				throw new IllegalArgumentException("Missing required parameter " + param.getName());
			}
		}
	}

	/**
	 * Bind the parsed parameters and options to the executable command.
	 * 
	 * @param cmd
	 * @param exec
	 */
	private void bind(Command cmd, CommandExecutable exec) {
		for (CommandOption option : cmd.getOptions()) {
			option.setValue(exec);
		}
		for (CommandParameter param : cmd.getParameters()) {
			param.setValue(exec);
		}
	}

	private void debugLog(Command cmd) {
		log.debug("Executing  " + cmd.getDescriptor().getName());
		for (CommandParameter p : cmd.getParameters()) {
			log.debug("\tParam " + p.getDescriptor().getName() + "=" + p.getValue());
		}
		for (CommandOption o : cmd.getOptions()) {
			log.debug("\tOption " + o.getDescriptor().getName());
		}
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
