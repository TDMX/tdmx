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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.display.CliPrinter;
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
	private CliPrinterFactory cliPrinterFactory;
	private DefaultParameterProvider defaultProvider;
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
					state = ParserState.CMD;
				} else if (token.equalsIgnoreCase(PI_USAGE)) {
					printUsage(out);
				} else if (token.equalsIgnoreCase(PI_HELP) || token.equalsIgnoreCase(PI_LIST)) {
					listCommands(out);
				} else {
					getLog(out).println("Ignore " + token);
				}
				break;
			case INITIAL:
				if (commandDescriptorFactory.getCommand(token) != null) {
					cmd = new Command(commandDescriptorFactory.getCommand(token));
					state = ParserState.CMD;
				} else if (token.equalsIgnoreCase(PI_USAGE)) {
					printUsage(out);
				} else if (token.equalsIgnoreCase(PI_HELP) || token.equalsIgnoreCase(PI_LIST)) {
					// TODO #104 separate "HELP" for all commands
					listCommands(out);
				} else if (token.equalsIgnoreCase(PI_ABORT)) {
					state = ParserState.INITIAL;
				} else {
					getLog(out).println("Ignore " + token);
				}
				break;
			case CMD:
				if (cmd == null) {
					getLog(err).println("No cmd.");
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
					state = ParserState.CMD;
				} else if (cmd.supportsOption(token)) {
					cmd.addOption(new CommandOption(cmd.getDescriptor().getOption(token)));
					state = ParserState.CMD;
				} else if (cmd.supportsParameter(token)) {
					parameter = cmd.getDescriptor().getParameter(token);
					state = ParserState.PARAMETER;
				} else {
					getLog(err).println("Unknown parameter " + token + " for " + cmd.getDescriptor().getName());
				}
				break;
			case PARAMETER:
				if (cmd == null) {
					getLog(err).println("No cmd.");
					state = ParserState.ERROR;
				} else if (parameter == null) {
					getLog(err).println("No parameter.");
					state = ParserState.ERROR;
				} else if (token.equalsIgnoreCase(PI_HELP)) {
					printHelp(cmd, out);
				} else if (token.equalsIgnoreCase(PI_LIST)) {
					listParameters(cmd, out);
				} else if (!"=".equals(token)) {
					// options have no = separating the parameter name from value, just a parameter name
					cmd.addParameter(new CommandParameter(parameter, token));
					parameter = null;
					state = ParserState.CMD;
				} else {
					state = ParserState.VALUE;
				}
				break;
			case VALUE:
				if (cmd == null) {
					getLog(err).println("No cmd.");
					state = ParserState.ERROR;
				} else if (parameter == null) {
					getLog(err).println("No parameter.");
					state = ParserState.ERROR;
				} else if ("-".equals(token)) {
					// parse hidden parameter value from console
					String pwd = readHiddenInput(out, parameter);
					cmd.addParameter(new CommandParameter(parameter, new String(pwd)));
					parameter = null;
					state = ParserState.CMD;

				} else {
					cmd.addParameter(new CommandParameter(parameter, token));
					parameter = null;
					state = ParserState.CMD;
				}
				break;
			default:
				getLog(err).println("Unknown state " + state);
				state = ParserState.ERROR;
			}
		}
		if (cmd != null) {
			if (parameter != null) {
				getLog(err).println("Incomplete parameter " + parameter);
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

	private CliPrinter getLog(PrintStream ps) {
		return cliPrinterFactory.getPrinter(ps);
	}

	private String readHiddenInput(PrintStream out, ParameterDescriptor parameter) {
		String prompt = String.format("Enter value for %s: ", parameter.getName());
		// convert the pwd into the parameter value instead of the -
		if (System.console() != null) {
			char[] pwd = System.console().readPassword(prompt);
			return new String(pwd);
		}
		// no console so use Stdin instead
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		out.print(prompt);
		try {
			String pwd = reader.readLine();
			return pwd;
		} catch (IOException e) {
			log.warn("Unable to read parameter value from non-console inputstream.", e);
		}
		return "-";

	}

	private void printUsage(PrintStream out) {
		commandDescriptorFactory.printUsage(out);
	}

	private void printHelp(Command cmd, PrintStream out) {
		cmd.getDescriptor().printUsage(out);
	}

	private void listCommands(PrintStream out) {
		List<String> cmdNames = commandDescriptorFactory.getCommandNames();
		for (String cmd : cmdNames) {
			getLog(out).println(cmd);
		}
	}

	private void listParameters(Command cmd, PrintStream out) {
		List<ParameterDescriptor> parameters = cmd.getDescriptor().getParameters();
		for (ParameterDescriptor param : parameters) {
			// explicit param value ELSE defaultParameter ELSE defaultParamValue
			String paramSet = cmd.getParameter(param.getName()) != null ? cmd.getParameter(param.getName()).getValue()
					: (!param.isNoDefault() && defaultProvider.getDefault(param.getName()) != null)
							? defaultProvider.getDefault(param.getName()) + " (default)" : param.getDefaultValue();
			if (!StringUtils.hasText(paramSet) && StringUtils.hasText(param.getDefaultValueText())) {
				paramSet = param.getDefaultValueText() + " (default)";
			}
			if (!StringUtils.hasText(paramSet) && param.isRequired()) {
				paramSet = "MISSING!";
			}
			// TODO #105: mask pwds
			getLog(out).println(param.getName() + "=" + paramSet);
		}
		List<OptionDescriptor> options = cmd.getDescriptor().getOptions();
		for (OptionDescriptor option : options) {
			String optionSet = cmd.getOption(option.getName()) != null ? "set" : "not set";
			getLog(out).println(option.getName() + " -option " + optionSet);
		}
	}

	private void executeCmd(Command cmd, PrintStream out, PrintStream err) {
		if (cliRunner != null) {
			try {
				cliRunner.execute(cmd, out);
			} catch (Throwable t) {
				getLog(err).println(t);
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

	public CliPrinterFactory getCliPrinterFactory() {
		return cliPrinterFactory;
	}

	public void setCliPrinterFactory(CliPrinterFactory printerFactory) {
		this.cliPrinterFactory = printerFactory;
	}

	public DefaultParameterProvider getDefaultProvider() {
		return defaultProvider;
	}

	public void setDefaultProvider(DefaultParameterProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}

	public CliRunner getCliRunner() {
		return cliRunner;
	}

	public void setCliRunner(CliRunner cliRunner) {
		this.cliRunner = cliRunner;
	}

}
