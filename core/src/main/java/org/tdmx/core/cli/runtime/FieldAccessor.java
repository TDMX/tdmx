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
package org.tdmx.core.cli.runtime;

import java.lang.reflect.Field;

public class FieldAccessor {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private Field field;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public FieldAccessor(Field field) {
		this.field = field;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Sets the field of the instance with the value provided. Implementations need to perform type conversion.
	 * 
	 * @param instance
	 * @param value
	 */
	public void setValue(Object instance, String value) {
		try {
			field.setAccessible(true);
			if (Integer.TYPE.equals(field.getType())) {
				field.set(instance, Integer.valueOf(value).intValue());
			} else if (Integer.class.equals(field.getType())) {
				field.set(instance, Integer.valueOf(value));
			}
			field.set(instance, value);
		} catch (NumberFormatException | IllegalAccessException e) {
			final String errorMsg = "Unable to set field name " + field.getName() + " with value " + value;
			throw new RuntimeException(errorMsg, e);
		}
	}

	/**
	 * Gets the value of the instance's field in string format.
	 * 
	 * @param instance
	 * @return the value of the instance's field in string format.
	 */
	public String getValue(Object instance) {
		try {
			field.setAccessible(true);
			Object obj = field.get(instance);
			return obj.toString();
		} catch (IllegalAccessException e) {
			final String errorMsg = "Unable to get field name " + field.getName();
			throw new RuntimeException(errorMsg, e);
		}
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