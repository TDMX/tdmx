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
package org.tdmx.console.application.search.match;

import org.tdmx.console.application.search.SearchableObjectField;

/**
 * Matching a TimeRange value against a Time field.
 * 
 * @author Peter
 * 
 */
public class TimeRangeDateTimeMatch implements MatchFunction {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private final Integer from; // time from
	private final Integer to; // time to

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public TimeRangeDateTimeMatch(Integer from, Integer to) {
		this.from = from;
		this.to = to;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public boolean match(SearchableObjectField field) {
		Object[] fieldValue = (Object[]) field.searchValue;
		int timeValue = (Integer) fieldValue[1];
		if (from != null && timeValue < from) {
			return false;
		}
		if (to != null && timeValue > to) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String fromT = "";
		String toT = "";
		if (from != null) {
			fromT = MatchValueFormatter.getTime(from);
		}
		if (to != null) {
			toT = MatchValueFormatter.getTime(to);
		}
		return fromT + "..TRT.." + toT;
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
