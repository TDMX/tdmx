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
package org.tdmx.console.application.search;

import org.tdmx.console.application.domain.DomainObjectType;

/**
 * A field descriptor value object.
 * 
 * @author Peter
 * 
 */
public class FieldDescriptor {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static enum FieldType {
		Text, // Free text with length of up to 2k, multiple strings, " " separated.
		String, // single free string, no spaces
		Token, // single token, limited range of values
		Number,
		Date,
		DateTime,
		Time,
	}

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private final DomainObjectType objectType;
	private final String name;
	private final FieldType fieldType;

	private final String description;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public FieldDescriptor(DomainObjectType objectType, String name, FieldType fieldType) {
		this.objectType = objectType;
		this.name = name.intern();
		this.fieldType = fieldType;

		StringBuilder sb = new StringBuilder();
		sb.append("FD{");
		sb.append(objectType);
		sb.append(".");
		sb.append(name);
		sb.append("/");
		sb.append(fieldType);
		sb.append("}");
		description = sb.toString();
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		return description;
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

	public DomainObjectType getObjectType() {
		return objectType;
	}

	public String getName() {
		return name;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

}
