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

import java.util.Calendar;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.search.match.MatchFunction;
import org.tdmx.console.application.search.match.MatchValueNormalizer;

/**
 * The SearchableObjectField contains a DomainObject's field value.
 * 
 * <pre>
 * FieldType
 *             [SearchValue-Type]   [OriginalValue-Type]
 *	Text       String[](lower)      String
 *	String     String(lower)        String
 *	Token      String(lower)        String
 *	Number     Long                 Number
 *	Date       Long                 Calendar  [millis since EPOCH of 00:00:00]
 *	DateTime   [Long,Integer,Long]  Calendar  [Date, Time, millis since EPOCH]  
 *	Time       Integer              Calendar  [seconds since 00:00:00]
 * </pre>
 * 
 * @see MatchValueNormalizer
 * 
 * @author Peter
 * 
 */
public final class SearchableObjectField {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final String TOKEN_ON = "on";
	public static final String TOKEN_OFF = "off";

	public static final String TOKEN_TRUSTED = "trusted";
	public static final String TOKEN_REVOKED = "revoked";

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	public DomainObject object;
	public FieldDescriptor field;
	public Object searchValue;
	public Object originalValue;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public SearchableObjectField(DomainObject object, FieldDescriptor field, Object originalValue) {
		this.object = object;
		this.field = field;
		this.originalValue = originalValue;
	}

	public SearchableObjectField(DomainObject object, FieldDescriptor field, Calendar originalValue) {
		this.object = object;
		this.field = field;
		this.originalValue = originalValue;
		switch (field.getFieldType()) {
		case Time:
			searchValue = MatchValueNormalizer.getTime(originalValue);
			break;
		case DateTime:
			searchValue = MatchValueNormalizer.getDateTime(originalValue);
			break;
		case Date:
			searchValue = MatchValueNormalizer.getDate(originalValue);
			break;
		default:
			throw new IllegalStateException("Calendar field " + field.getName() + " with type " + field.getFieldType());
		}
	}

	public SearchableObjectField(DomainObject object, FieldDescriptor field, Number originalValue) {
		this.object = object;
		this.field = field;
		this.originalValue = originalValue;
		switch (field.getFieldType()) {
		case Number:
			searchValue = MatchValueNormalizer.getNumber(originalValue);
			break;
		default:
			throw new IllegalStateException("Number field " + field.getName() + " with type " + field.getFieldType());
		}
	}

	public SearchableObjectField(DomainObject object, FieldDescriptor field, String originalValue) {
		this.object = object;
		this.field = field;
		this.originalValue = originalValue;
		switch (field.getFieldType()) {
		case Token:
			searchValue = MatchValueNormalizer.getStringFromString(originalValue); // TODO missing in searchcriteria?
			break;
		case String:
			searchValue = MatchValueNormalizer.getStringFromString(originalValue);
			break;
		case Text:
			searchValue = MatchValueNormalizer.getStringListFromString(originalValue);
			break;
		default:
			throw new IllegalStateException("String field " + field.getName() + " with type " + field.getFieldType());
		}
	}

	public SearchableObjectField(DomainObject object, FieldDescriptor field, Boolean originalValue) {
		this.object = object;
		this.field = field;
		this.originalValue = originalValue;
		switch (field.getFieldType()) {
		case Token:
			searchValue = MatchValueNormalizer.getStringFromString(originalValue.toString());
			break;
		default:
			throw new IllegalStateException("Boolean field " + field.getName() + " with type " + field.getFieldType());
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Evaluate the SearchExpression on this field and determine if it matches. If there is no MatchFunction defined for
	 * the type of field, then there cannot be a match.
	 * 
	 * @param exp
	 * @return true if the SearchExpression matches this field, else false.
	 */
	public boolean match(SearchExpression exp) {
		// get the match function of the expression for this field's type
		MatchFunction fn = exp.matchFunctionMap.get(field.getFieldType());
		// if there is a function defined, we evaluate it, else it doesn't match
		return fn != null ? fn.match(this) : false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SOF[");
		sb.append(object.getId());
		sb.append(":");
		sb.append(field);
		sb.append("=");
		sb.append(originalValue);
		sb.append("]");
		return sb.toString();
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
