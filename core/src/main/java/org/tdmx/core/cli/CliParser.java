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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.runtime.Command;
import org.tdmx.core.cli.runtime.CommandOption;
import org.tdmx.core.cli.runtime.CommandParameter;
import org.tdmx.core.system.lang.StringUtils;

public class CliParser {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(CliParser.class);

	public static final String PI_EXEC = "exec";
	public static final String PI_LIST = "list";
	public static final String PI_USAGE = "usage";
	public static final String PI_HELP = "help";
	public static final String PI_ABORT = "abort";
	public static final String PI_EXIT = "exit";
	public static final String PI_QUIT = "quit";

	private CommandDescriptorFactory commandDescriptorFactory;

	private CliRunner cliRunner;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Parse the input stream into Commands and their Parameters and Options and execute them with the {@link CliRunner}
	 * . Commands are only allowed to throw RuntimeExceptions to exit.
	 * 
	 * @param tokenizer
	 * @param out
	 */
	public void process(InputStreamTokenizer tokenizer, PrintStream out, PrintStream err) {
		ParserState state = ParserState.INITIAL;

		String token = null;
		ParameterDescriptor parameter = null;
		Command cmd = null;
		while ((token = tokenizer.getNextToken()) != null) {
			// we can exit at any time, irrespective of state
			if (!(state == ParserState.PARAMETER || state == ParserState.VALUE)
					&& (token.equalsIgnoreCase(PI_EXIT) || token.equalsIgnoreCase(PI_QUIT))) {
				cmd = null;
				parameter = null;
				break;
			}
			switch (state) {
			case ERROR:
				if (commandDescriptorFactory.getCommand(token) != null) {
					cmd = new Command(commandDescriptorFactory.getCommand(token));
					// TODO #103 add bound variables to the command
					state = ParserState.CMD;
				} else if (token.equalsIgnoreCase(PI_USAGE)) {
					printUsage(out);
				} else if (token.equalsIgnoreCase(PI_HELP) || token.equalsIgnoreCase(PI_LIST)) {
					listCommands(out);
				} else {
					logInfo("Ignore " + token, out);
				}
				break;
			case INITIAL:
				// TODO #103: set "pwd:" and "set:" parameter bindings
				if (commandDescriptorFactory.getCommand(token) != null) {
					cmd = new Command(commandDescriptorFactory.getCommand(token));
					// TODO #103 add bound variables to the command
					state = ParserState.CMD;
				} else if (token.equalsIgnoreCase(PI_USAGE)) {
					printUsage(out);
				} else if (token.equalsIgnoreCase(PI_HELP) || token.equalsIgnoreCase(PI_LIST)) {
					// TODO #104 separate "HELP" for all commands
					listCommands(out);
				} else if (token.equalsIgnoreCase(PI_ABORT)) {
					state = ParserState.INITIAL;
				} else {
					logInfo("Ignore " + token, out);
				}
				break;
			case CMD:
				if (cmd == null) {
					logError("No cmd.", err);
					state = ParserState.ERROR;
				} else if (token.equalsIgnoreCase(PI_HELP) || token.equalsIgnoreCase(PI_USAGE)) {
					printHelp(cmd, out);
				} else if (token.equalsIgnoreCase(PI_LIST)) {
					listParameters(cmd, out);
				} else if (token.equalsIgnoreCase(PI_EXEC)) {
					executeCmd(cmd, out, err);
					cmd = null;
					state = ParserState.INITIAL;
				} else if (token.equalsIgnoreCase(PI_ABORT)) {
					state = ParserState.INITIAL;
				} else if (commandDescriptorFactory.getCommand(token) != null) {
					executeCmd(cmd, out, err);
					// next command started
					cmd = new Command(commandDescriptorFactory.getCommand(token));
					// TODO #103 add bound variables to the command
					state = ParserState.CMD;
				} else if (cmd.supportsOption(token)) {
					cmd.addOption(new CommandOption(cmd.getDescriptor().getOption(token)));
					state = ParserState.CMD;
				} else if (cmd.supportsParameter(token)) {
					parameter = cmd.getDescriptor().getParameter(token);
					state = ParserState.PARAMETER;
				} else {
					logError("Unknown parameter " + token + " for " + cmd.getDescriptor().getName(), err);
				}
				break;
			case PARAMETER:
				if (cmd == null) {
					logError("No cmd.", err);
					state = ParserState.ERROR;
				} else if (parameter == null) {
					logError("No parameter.", err);
					state = ParserState.ERROR;
				} else if (token.equalsIgnoreCase(PI_HELP)) {
					printHelp(cmd, out);
				} else if (token.equalsIgnoreCase(PI_LIST)) {
					listParameters(cmd, out);
				} else if (!"=".equals(token)) {
					cmd.addParameter(new CommandParameter(parameter, token));
					parameter = null;
					state = ParserState.CMD;
				} else {
					state = ParserState.VALUE;
				}
				break;
			case VALUE:
				if (cmd == null) {
					logError("No cmd.", err);
					state = ParserState.ERROR;
				} else if (parameter == null) {
					logError("No parameter.", err);
					state = ParserState.ERROR;
				} else {
					cmd.addParameter(new CommandParameter(parameter, token));
					parameter = null;
					state = ParserState.CMD;
				}
				break;
			default:
				logError("Unknown state " + state, err);
				state = ParserState.ERROR;
			}
		}
		if (cmd != null) {
			if (parameter != null) {
				logError("Incomplete parameter " + parameter, err);
			}
			executeCmd(cmd, out, err);
		}

	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void printUsage(PrintStream out) {
		commandDescriptorFactory.printUsage(out);
	}

	private void printHelp(Command cmd, PrintStream out) {
		cmd.getDescriptor().printUsage(out);
	}

	private void listCommands(PrintStream out) {
		List<String> cmdNames = commandDescriptorFactory.getCommandNames();
		for (String cmd : cmdNames) {
			logInfo(cmd, out);
		}
	}

	private void listParameters(Command cmd, PrintStream out) {
		List<ParameterDescriptor> parameters = cmd.getDescriptor().getParameters();
		for (ParameterDescriptor param : parameters) {
			String paramSet = cmd.getParameter(param.getName()) != null ? cmd.getParameter(param.getName()).getValue()
					: param.getDefaultValue();
			if (!StringUtils.hasText(paramSet) && StringUtils.hasText(param.getDefaultValueText())) {
				paramSet = param.getDefaultValueText();
			}
			if (!StringUtils.hasText(paramSet) && param.isRequired()) {
				paramSet = "MISSING!";
			}
			logInfo(param.getName() + "=" + paramSet, out);
		}
		List<OptionDescriptor> options = cmd.getDescriptor().getOptions();
		for (OptionDescriptor option : options) {
			String optionSet = cmd.getOption(option.getName()) != null ? "set" : "not set";
			logInfo(option.getName() + " -option " + optionSet, out);
		}
	}

	private void logInfo(String msg, PrintStream out) {
		out.println(msg);
	}

	private void logError(String msg, PrintStream err) {
		err.println("error=" + msg);
	}

	private void logError(Throwable t, PrintStream err) {

		// TODO CommandExceptionHandler to log

		String msg = t.getMessage();
		if (!StringUtils.hasText(msg) && t.getCause() != null) {
			msg = t.getCause().getMessage();
		}

		err.println("error=" + msg);
		if (t.getCause() != null) {
			err.println("cause=" + t.getCause().getMessage());
			t.getCause().printStackTrace(err);
		} else {
			t.printStackTrace(err);
		}
	}

	private void executeCmd(Command cmd, PrintStream out, PrintStream err) {
		if (cliRunner != null) {
			try {
				cliRunner.execute(cmd, out);
			} catch (Throwable t) {
				logError(t, err);
			}
		}
	}

	private static enum ParserState {
		INITIAL,
		CMD,
		PARAMETER,
		VALUE,
		ERROR,
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
