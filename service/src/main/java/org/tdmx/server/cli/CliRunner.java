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
package org.tdmx.server.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CliRunner implements ApplicationContextAware {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(CliRunner.class);

	private ApplicationContext context;

	private Map<Class<?>, String> commandClassRefMap;

	// internal
	private Map<String, String> commandNameRefMap;
	private Map<String, CommandDescriptor> commandNameClassMap;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public CliRunner() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void init() {
		log.debug("Initializing.");
		commandNameRefMap = new HashMap<>();
		commandNameClassMap = new HashMap<>();

		for (Entry<Class<?>, String> e : commandClassRefMap.entrySet()) {
			String beanRef = e.getValue();
			log.info("Class=" + e.getKey().getName() + " ref=" + beanRef);

			CommandDescriptor cd = new CommandDescriptor(e.getKey());
			String cmdName = cd.getName();

			commandNameClassMap.put(cmdName, cd);
			commandNameRefMap.put(cmdName, e.getValue());

			Runnable cmd = (Runnable) context.getBean(beanRef);
			log.debug("Found bean: " + beanRef);

		}
	}

	public void printUsage(PrintStream ps) {
		for (Entry<String, CommandDescriptor> cmds : commandNameClassMap.entrySet()) {
			cmds.getValue().printUsage(ps);
		}
	}

	public void process(InputStream is) {

		// TODO tokenize the input stream

		String cmdName = "test:function";
		String cmdRef = commandNameRefMap.get(cmdName);
		if (cmdRef == null) {
			// TODO error cmdNotFound
			return;
		}

		Runnable cmd = (Runnable) context.getBean(cmdRef);

	}

	public void process(InputStreamTokenizer tokenizer, List<String> cmdNames) {
		ParserState state = ParserState.INITIAL;

		String token = null;
		String parameterName = null;
		CmdDescriptor cmd = null;
		while ((token = tokenizer.getNextToken()) != null) {
			switch (state) {
			case INITIAL:
				if (cmdNames.contains(token)) {
					cmd = new CmdDescriptor(token);
					state = ParserState.CMD;
				}
				break;
			case CMD:
				if (cmd == null) {
					throw new IllegalStateException("No cmd.");
				}
				if (cmdNames.contains(token)) {
					executeCmd(cmd);
					// next command started
					cmd = new CmdDescriptor(token);
					state = ParserState.CMD;
				} else if (cmd.supportsParameter(token)) {
					parameterName = token;
					state = ParserState.PARAMETER;
				} else {
					throw new IllegalStateException("Unknown parameter " + token + " for " + cmd.cmdToken);
				}
				break;
			case PARAMETER:
				if (cmd == null) {
					throw new IllegalStateException("No cmd.");
				}
				if (parameterName == null) {
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
				if (parameterName == null) {
					throw new IllegalStateException("No parameter.");
				}
				cmd.addParameter(new CmdParameter(parameterName, token));
				parameterName = null;
				state = ParserState.CMD;
				break;
			default:
				throw new IllegalStateException("Unknown state " + state);
			}

		}
		if (cmd != null) {
			if (parameterName != null) {
				throw new IllegalStateException("Incomplete parameter " + parameterName);
			}
			executeCmd(cmd);
		}

	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void executeCmd(CmdDescriptor cmd) {
		System.out.println("Executing  " + cmd.getCmdToken());
		for (CmdParameter p : cmd.getParameters()) {
			System.out.println("\tParam " + p.getName() + "=" + p.getValue());
		}
	}

	private static class CmdParameter {
		private final String name;
		private final String value;

		public CmdParameter(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}

	private static class CmdDescriptor {
		private final String cmdToken;
		private final List<CmdParameter> parameters = new ArrayList<>();

		public CmdDescriptor(String cmdToken) {
			this.cmdToken = cmdToken;
		}

		public String getCmdToken() {
			return cmdToken;
		}

		public List<CmdParameter> getParameters() {
			return Collections.unmodifiableList(parameters);
		}

		public void addParameter(CmdParameter param) {
			parameters.add(param);
		}

		public boolean supportsParameter(String parameterName) {
			// TODO
			return true;
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

	public Map<Class<?>, String> getCommandClassRefMap() {
		return commandClassRefMap;
	}

	public void setCommandClassRefMap(Map<Class<?>, String> commandClassRefMap) {
		this.commandClassRefMap = commandClassRefMap;
	}

}
