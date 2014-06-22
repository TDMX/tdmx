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

import java.util.HashMap;
import java.util.Map;

import org.tdmx.console.application.domain.DomainObjectType;
import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.match.MatchFunction;

/**
 * <pre>
 * SearchExpression := (":"<fieldName>" ")?<value>
 * </pre>
 * 
 * @author Peter
 * 
 */
public final class SearchExpression {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static enum ValueType {
		TimeRange,
		DateTimeRange,
		DateRange,
		NumberRange,
		Time,
		DateTime,
		Date,
		Number,
		QuotedText,
		Text
	}

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	public DomainObjectType objectType;
	public String fieldName;
	public ValueType valueType;
	public Map<FieldType, MatchFunction> matchFunctionMap = new HashMap<FieldDescriptor.FieldType, MatchFunction>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public SearchExpression() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void add(FieldType fieldType, MatchFunction fn) {
		matchFunctionMap.put(fieldType, fn);
	}

	@Override
	public String toString() {
		String ot = objectType != null ? objectType.name() : "*";
		String fn = fieldName != null ? fieldName : "*";
		// TODO
		return ":" + ot + "." + fn + valueType + " " + matchFunctionMap;
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
