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
 * Matching a DateTimeRange value against a DateTime field.
 * 
 * @author Peter
 * 
 */
public class DateTimeRangeDateTimeMatch implements MatchFunction {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private final Long from; // dateTimeTS from
	private final Long to; // dateTimeTS to

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public DateTimeRangeDateTimeMatch(Long from, Long to) {
		this.from = from;
		this.to = to;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public boolean match(SearchableObjectField field) {
		Object[] fieldValue = (Object[]) field.searchValue;
		long tsValue = (Long) fieldValue[2];
		if (from != null && tsValue < from) {
			return false;
		}
		if (to != null && tsValue > to) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String fromT = "";
		String toT = "";
		if (from != null) {
			fromT = MatchValueFormatter.getDateTimeTS(from);
		}
		if (to != null) {
			toT = MatchValueFormatter.getDateTimeTS(to);
		}
		return fromT + "..DTRDT.." + toT;
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
