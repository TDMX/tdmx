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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Option;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.annotation.Result;
import org.tdmx.core.system.lang.StringUtils;

/**
 * Immutable value type describing a Command.
 * 
 * @author Peter
 *
 */
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
	private List<OptionDescriptor> options;
	private List<ResultDescriptor> results;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public CommandDescriptor(Class<?> clazz) {
		this.cli = getCli(clazz);
		this.parameters = getParameters(clazz);
		this.options = getOtions(clazz);
		this.results = getResults(clazz);
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

	public List<ParameterDescriptor> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	public ParameterDescriptor getParameter(String parameterName) {
		for (ParameterDescriptor param : parameters) {
			if (param.getName().equals(parameterName)) {
				return param;
			}
		}
		return null;
	}

	public List<OptionDescriptor> getOptions() {
		return Collections.unmodifiableList(options);
	}

	public OptionDescriptor getOption(String optionName) {
		for (OptionDescriptor param : options) {
			if (param.getName().equals(optionName)) {
				return param;
			}
		}
		return null;
	}

	public List<ResultDescriptor> getResults() {
		return Collections.unmodifiableList(results);
	}

	public void printUsage(PrintStream ps) {
		ps.println("cmd=" + cli.name());
		ps.println("\t description=" + cli.description());
		ps.println("\t note=" + cli.note());
		for (OptionDescriptor option : options) {
			ps.print("\t parameter=" + option.getName());
			ps.println("\t\t description=" + option.getDescription());
		}
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
		for (ResultDescriptor result : results) {
			ps.print("\t result=" + result.getName());
			ps.println("\t\t description=" + result.getDescription());
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

	private List<OptionDescriptor> getOtions(Class<?> clazz) {
		List<OptionDescriptor> result = new ArrayList<>();

		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			Option[] parameters = f.getAnnotationsByType(Option.class);
			if (parameters.length == 0) {
				continue;
			}
			if (parameters.length > 1) {
				throw new IllegalStateException(
						"Too many Option annotations on " + clazz.getName() + "#" + f.getName());
			}

			OptionDescriptor optionDescriptor = new OptionDescriptor(parameters[0], f);
			result.add(optionDescriptor);
		}
		return result;
	}

	private List<ResultDescriptor> getResults(Class<?> clazz) {
		List<ResultDescriptor> result = new ArrayList<>();

		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			Result[] parameters = f.getAnnotationsByType(Result.class);
			if (parameters.length == 0) {
				continue;
			}
			if (parameters.length > 1) {
				throw new IllegalStateException(
						"Too many Result annotations on " + clazz.getName() + "#" + f.getName());
			}

			ResultDescriptor resultDescriptor = new ResultDescriptor(parameters[0], f);
			result.add(resultDescriptor);
		}
		return result;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
