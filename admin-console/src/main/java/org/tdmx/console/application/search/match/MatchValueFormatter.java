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

import java.text.DateFormat;
import java.util.Calendar;

public class MatchValueFormatter {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public static String getNumber(Long number) {
		if (number == null) {
			return null;
		}
		return number.toString();
	}

	public static String[] getStringNumberList(Long n1, Long n2) {
		if (n1 != null && n2 != null) {
			return new String[] { getNumber(n1), getNumber(n2) };
		} else if (n1 != null) {
			return new String[] { getNumber(n1) };
		} else if (n2 != null) {
			return new String[] { getNumber(n2) };
		}
		return null;
	}

	public static String getTime(Integer time) {
		if (time == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.add(Calendar.SECOND, time);
		return DateFormat.getTimeInstance().format(cal.getTime());
	}

	public static String getDate(Long date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
		return DateFormat.getDateInstance().format(cal.getTime());
	}

	public static String getDateTime(Object[] dateTime) {
		if (dateTime == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis((Long) dateTime[0]);
		cal.add(Calendar.SECOND, (Integer) dateTime[1]);
		return DateFormat.getDateTimeInstance().format(cal.getTime());
	}

	public static String getDateTimeTS(Long dateTimeTs) {
		if (dateTimeTs == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dateTimeTs);
		return DateFormat.getDateTimeInstance().format(cal.getTime());
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
