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
package org.tdmx.core.system.lang;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarUtils {

	private static Logger log = LoggerFactory.getLogger(CalendarUtils.class);

	/**
	 * If the date is in the future.
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isInFuture(Calendar date) {
		return date.after(Calendar.getInstance());
	}

	/**
	 * If the date is in the past.
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isInPast(Calendar date) {
		return date.before(Calendar.getInstance());
	}

	/**
	 * Convert Calendar to Date
	 */
	public static Date getDate(Calendar date) {
		if (date == null) {
			return null;
		}
		return date.getTime();
	}

	/**
	 * Convert Date to DateCalendar
	 */
	public static Calendar getDate(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	/**
	 * Convert Date to DateTimeCalendar
	 */
	public static Calendar getDateTime(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	public static Date getDateWithOffset(Date base, int units, int offsetInUnits) {
		Calendar cal = getDate(base);
		if (cal != null) {
			cal.add(units, offsetInUnits);
			return cal.getTime();
		}
		return null;
	}
}
