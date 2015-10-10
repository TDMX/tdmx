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

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.server.cli.annotation.Cli;
import org.tdmx.server.cli.annotation.Parameter;

public class CommandDescriptor {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(CommandDescriptor.class);

	private Cli cli;
	private List<ParameterDescriptor> parameters;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public CommandDescriptor(Class<?> clazz) {
		this.cli = getCli(clazz);
		this.parameters = getParameters(clazz);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public String getDescription() {
		return cli.description();
	}

	public String getName() {
		return cli.name();
	}

	public String getNote() {
		return cli.note();
	}

	public List<ParameterDescriptor> getParameters(String parameterName) {
		return Collections.unmodifiableList(parameters);
	}

	public void printUsage(PrintStream ps) {
		ps.println("cmd=" + cli.name());
		ps.println("\t description=" + cli.description());
		ps.println("\t note=" + cli.note());

		for (ParameterDescriptor parameter : parameters) {
			ps.print("\t parameter=" + parameter.getName());
			if (parameter.isRequired()) {
				ps.print(" required=" + parameter.isRequired());
			}
			if (StringUtils.hasText(parameter.getDefaultValue())) {
				ps.print(" defaultValue=" + parameter.getDefaultValue());
			}
			ps.println();
			ps.println("\t\t description=" + parameter.getDescription());
		}
		ps.println();

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

	private List<ParameterDescriptor> getParameters(Class<?> clazz) {
		List<ParameterDescriptor> result = new ArrayList<>();

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

			ParameterDescriptor parameterDescriptor = new ParameterDescriptor(parameters[0], f);
			result.add(parameterDescriptor);
		}
		return result;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
