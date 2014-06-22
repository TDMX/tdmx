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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tdmx.console.application.domain.DomainObject;

/**
 * A SearchResultSet is a helper class to collect the result of a Search using a SearchCriteria which is an AND of one
 * or more SearchExpressions.
 * 
 * @author Peter
 * 
 */
public final class SearchResultSet {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final List<SearchExpression> expressions;
	private final Set<DomainObject> result = new HashSet<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public SearchResultSet(SearchCriteria criteria) {
		this.expressions = criteria.getExpressions();
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Determine if ALL of the SearchCriteria's SearchExpressions match ANY of the DomainObject fields.
	 * 
	 * @param object
	 * @param fieldList
	 * @return true if ALL of the SearchExpressios match ANY of the DomainObject's fields.
	 */
	public boolean match(DomainObject object, List<SearchableObjectField> fieldList) {
		for (SearchExpression exp : expressions) {
			// if a search expression doesn't match for any of the object's fields - we don't
			// match since this is a logical AND.
			boolean matched = false;
			for (SearchableObjectField field : fieldList) {
				if (field.match(exp)) {
					matched = true;
					break;
				}
			}
			if (!matched) {
				// if an object hasn't matched one expression stop and dont continue
				// evaluating the other expressions
				return false;
			}
		}
		result.add(object);
		return true;
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

	public Set<DomainObject> getResult() {
		return result;
	}

}
