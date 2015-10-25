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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.runtime.CommandExecutable;

public class CommandDescriptorFactoryImpl implements CommandDescriptorFactory {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(CommandDescriptorFactoryImpl.class);

	// internal
	private final Map<String, CommandDescriptor> cmds = new HashMap<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public CommandDescriptorFactoryImpl(Class<? extends CommandExecutable>[] commandClasses) {
		for (Class<? extends CommandExecutable> clazz : commandClasses) {
			CommandDescriptor desc = new CommandDescriptor(clazz);
			cmds.put(desc.getName(), desc);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public CommandDescriptor getCommand(String cmdName) {
		return cmds.get(cmdName);
	}

	@Override
	public void printUsage(PrintStream ps) {
		List<String> cmdNames = new ArrayList<>();
		for (CommandDescriptor desc : cmds.values()) {
			cmdNames.add(desc.getName());
		}
		Collections.sort(cmdNames);

		for (String cmdName : cmdNames) {
			CommandDescriptor desc = cmds.get(cmdName);
			desc.printUsage(ps);
		}
	}

	@Override
	public List<String> getCommandNames() {
		return Arrays.asList(cmds.keySet().toArray(new String[0]));
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
