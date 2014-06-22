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
package org.tdmx.console.application.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tdmx.core.system.lang.StringUtils;

/**
 * An object representing the System's properties.
 * 
 * @author Peter
 * 
 */
public class SystemPropertiesVO implements ValueObject {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final Map<String, String> properties = new HashMap<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public SystemPropertiesVO() {
	}

	public SystemPropertiesVO(List<String> propertyNames) {
		if (propertyNames != null) {
			for (String name : propertyNames) {
				try {
					String value = System.getProperty(name);
					if (StringUtils.hasText(value)) {
						add(name, value);
					}
				} catch (SecurityException e) {
					add(name, "<unknown>");
				}
			}
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void add(String key, String value) {
		properties.put(key, value);
	}

	public String get(String key) {
		return properties.get(key);
	}

	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	public Set<String> getDeletedKeys(Map<String, String> other) {
		Set<String> deletes = new HashSet<>();

		// TODO
		return deletes;
	}

	public Set<String> getNewKeys(Map<String, String> other) {
		Set<String> added = new HashSet<>();
		// TODO
		return added;
	}

	public Set<String> getModifiedKeys(Map<String, String> other) {
		Set<String> modified = new HashSet<>();
		// TODO
		return modified;
	}

	@Override
	public <E extends ValueObject> E copy() {
		// TODO Auto-generated method stub
		return null;
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
