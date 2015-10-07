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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.tdmx.server.cli.annotation.Cli;
import org.tdmx.server.cli.annotation.Parameter;

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

	private Map<String, String> commandNameRefMap;
	private Map<String, Class<?>> commandNameClassMap;

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

			Cli cli = getCli(e.getKey());
			String cmdName = cli.name();

			commandNameClassMap.put(cmdName, e.getKey());
			commandNameRefMap.put(cmdName, e.getValue());

			Runnable cmd = (Runnable) context.getBean(beanRef);
			log.debug("Found bean: " + beanRef);

		}
	}

	public void printUsage(PrintStream ps) {
		for (Entry<Class<?>, String> e : commandClassRefMap.entrySet()) {
			Cli cli = getCli(e.getKey());

			ps.println(cli.name());
			ps.println("\t\t description=" + cli.description());
			ps.println("\t\t note=" + cli.note());
			ps.println();
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

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private Cli getCli(Class<?> clazz) {
		Cli[] clis = clazz.getAnnotationsByType(Cli.class);
		if (clis == null) {
			throw new IllegalStateException("No Cli annotation on " + clazz.getName());
		}
		if (clis.length > 1) {
			throw new IllegalStateException("Too many Cli annotations on " + clazz.getName());
		}
		Cli cli = clis[0];

		return cli;
	}

	private void getParameters(Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			Parameter[] parameters = f.getAnnotationsByType(Parameter.class);
			if (parameters.length == 0) {
				continue;
			}
			if (parameters.length > 1) {
				throw new IllegalStateException(
						"Too many Parameter annotations on " + clazz.getName() + "#" + f.getName());
			}
		}
	}

	public interface FieldSetter {
		public void setValue(Field field, Object instance, String value)
				throws IllegalArgumentException, IllegalAccessException;
	}

	public static class StringFieldSetter implements FieldSetter {
		@Override
		public void setValue(Field field, Object instance, String value)
				throws IllegalArgumentException, IllegalAccessException {
			field.set(instance, value);

		}
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
