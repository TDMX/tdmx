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
package org.tdmx.core.cli.display;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An X-Bean style object which can be printed.
 * 
 * @author Peter
 *
 */
public class PrintableObject {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(PrintableObject.class);

	private final List<PrintableAttributeValue> attributeValues = new ArrayList<>();
	private final String name;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public PrintableObject(String name) {
		this.name = name;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public PrintableObject add(String attributeName, Object attributeValue) {
		PrintableAttributeValue v = new PrintableAttributeValue(attributeName, attributeValue);
		v.setOrder(attributeValues.size());
		attributeValues.add(v);
		return this;
	}

	public PrintableObject addVerbose(String attributeName, Object attributeValue) {
		PrintableAttributeValue v = new PrintableAttributeValue(attributeName, attributeValue);
		v.setOrder(attributeValues.size());
		v.setVerbose(true);
		attributeValues.add(v);
		return this;
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

	public List<PrintableAttributeValue> getAttributeValues() {
		return attributeValues;
	}

	public String getName() {
		return name;
	}

}
