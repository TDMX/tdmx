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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.tdmx.core.cli.CommandDescriptorFactory;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.cli.runtime.CommandExecutableFactory;

public class BeanLookupCommandExecutableFactoryImpl implements CommandExecutableFactory, ApplicationContextAware {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(BeanLookupCommandExecutableFactoryImpl.class);

	private ApplicationContext context;

	private CommandDescriptorFactory commandDescriptorFactory;

	// internal
	private Map<String, String> commandNameRefMap;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public BeanLookupCommandExecutableFactoryImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void init() {
		List<String> commandNames = commandDescriptorFactory.getCommandNames();

		for (String commandName : commandNames) {
			if (getCommandExecutable(commandName) == null) {
				throw new IllegalStateException("Executor for " + commandName + " not found.");
			}
		}
	}

	@Override
	public CommandExecutable getCommandExecutable(String commandName) {
		String beanRef = commandNameRefMap.get(commandName);
		if (beanRef == null) {
			throw new IllegalStateException("Executor beanRef undefined for " + commandName);
		}
		CommandExecutable exec = (CommandExecutable) context.getBean(beanRef);
		return exec;
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

	public CommandDescriptorFactory getCommandDescriptorFactory() {
		return commandDescriptorFactory;
	}

	public void setCommandDescriptorFactory(CommandDescriptorFactory commandDescriptorFactory) {
		this.commandDescriptorFactory = commandDescriptorFactory;
	}

	public Map<String, String> getCommandNameRefMap() {
		return commandNameRefMap;
	}

	public void setCommandNameRefMap(Map<String, String> commandNameRefMap) {
		this.commandNameRefMap = commandNameRefMap;
	}

}
