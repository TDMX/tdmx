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
package org.tdmx.client.cli;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.DefaultParameterProvider;

public class StaticDefaultParameterProvider implements DefaultParameterProvider {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(StaticDefaultParameterProvider.class);

	// internal
	private Map<String, String> defaults = new HashMap<>();

	private static StaticDefaultParameterProvider singleton = new StaticDefaultParameterProvider();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private StaticDefaultParameterProvider() {
	}

	public static StaticDefaultParameterProvider getInstance() {
		return singleton;
	}
	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void setDefault(String parameterName, String parameterValue) {
		defaults.put(parameterName, parameterValue);
	}

	@Override
	public String getDefault(String parameterName) {
		return defaults.get(parameterName);
	}

	@Override
	public void clearDefault(String parameterName) {
		defaults.remove(parameterName);
	}

	public static void setDefaultValue(String parameterName, String parameterValue) {
		singleton.setDefault(parameterName, parameterValue);
	}

	public static String getDefaultValue(String parameterName) {
		return singleton.getDefault(parameterName);
	}

	public static void clearDefaultValue(String parameterName) {
		singleton.clearDefault(parameterName);
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
