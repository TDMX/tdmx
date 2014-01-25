package org.tdmx.core.system.lang;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarUtils {

	private static Logger log = LoggerFactory.getLogger(CalendarUtils.class);
	
	/**
	 * Convert Calendar to Date 
	 */
	public static Date getDate( Calendar date ) {
		if ( date == null) {
			return null;
		}
		return date.getTime();
	}

	/**
	 * Convert Date to DateCalendar
	 */
	public static Calendar getDate( Date date ) {
		if ( date == null) {
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
	public static Calendar getDateTime( Date date ) {
		if ( date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
	
	public static Date getDateWithOffset( Date base, int units, int offsetInUnits ) {
		Calendar cal = getDate(base);
		if ( cal != null ) {
			cal.add(units, offsetInUnits);
			return cal.getTime();
		}
		return null;
	}
}
