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
import org.tdmx.core.cli.runtime.Command;
import org.tdmx.core.cli.runtime.CommandOption;
import org.tdmx.core.cli.runtime.CommandParameter;

public class CliParser {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(CliParser.class);

	private CommandDescriptorFactory commandDescriptorFactory;

	private CliRunner cliRunner;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void process(InputStreamTokenizer tokenizer, PrintStream out) {
		ParserState state = ParserState.INITIAL;

		String token = null;
		ParameterDescriptor parameter = null;
		Command cmd = null;
		while ((token = tokenizer.getNextToken()) != null) {
			switch (state) {
			case INITIAL:
				if (commandDescriptorFactory.getCommand(token) != null) {
					cmd = new Command(commandDescriptorFactory.getCommand(token));
					state = ParserState.CMD;
				}
				break;
			case CMD:
				if (cmd == null) {
					throw new IllegalStateException("No cmd.");
				}
				if (commandDescriptorFactory.getCommand(token) != null) {
					executeCmd(cmd, out);
					// next command started
					cmd = new Command(commandDescriptorFactory.getCommand(token));
					state = ParserState.CMD;
				} else if (cmd.supportsOption(token)) {
					cmd.addOption(new CommandOption(cmd.getDescriptor().getOption(token)));
					state = ParserState.CMD;
				} else if (cmd.supportsParameter(token)) {
					parameter = cmd.getDescriptor().getParameter(token);
					state = ParserState.PARAMETER;
				} else {
					throw new IllegalStateException(
							"Unknown parameter " + token + " for " + cmd.getDescriptor().getName());
				}
				break;
			case PARAMETER:
				if (cmd == null) {
					throw new IllegalStateException("No cmd.");
				}
				if (parameter == null) {
					throw new IllegalStateException("No parameter.");
				}
				if (!"=".equals(token)) {
					throw new IllegalStateException("Expecting =");
				} else {
					state = ParserState.VALUE;
				}
				break;
			case VALUE:
				if (cmd == null) {
					throw new IllegalStateException("No cmd.");
				}
				if (parameter == null) {
					throw new IllegalStateException("No parameter.");
				}
				cmd.addParameter(new CommandParameter(parameter, token));
				parameter = null;
				state = ParserState.CMD;
				break;
			default:
				throw new IllegalStateException("Unknown state " + state);
			}

		}
		if (cmd != null) {
			if (parameter != null) {
				throw new IllegalStateException("Incomplete parameter " + parameter);
			}
			executeCmd(cmd, out);
		}

	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void executeCmd(Command cmd, PrintStream out) {
		if (cliRunner != null) {
			cliRunner.execute(cmd, out);
		}
	}

	private static enum ParserState {
		INITIAL,
		CMD,
		PARAMETER,
		VALUE
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public CommandDescriptorFactory getCommandDescriptorFactory() {
		return commandDescriptorFactory;
	}

	public void setCommandDescriptorFactory(CommandDescriptorFactory commandDescriptorFactory) {
		this.commandDescriptorFactory = commandDescriptorFactory;
	}

	public CliRunner getCliRunner() {
		return cliRunner;
	}

	public void setCliRunner(CliRunner cliRunner) {
		this.cliRunner = cliRunner;
	}

}
