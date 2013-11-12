package org.tdmx.console.application.search.match;

import java.util.Calendar;
import java.util.StringTokenizer;

public class MatchValueNormalizer {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	/**
	 * Get the normalized String from a String field.
	 * @param string
	 * @return
	 */
	public static String getStringFromString( String string ) {
		return string.toLowerCase();
	}
	
	/**
	 * Get the normalized String[] tokenized from a Text field.
	 * @param string
	 * @return
	 */
	public static String[] getStringListFromString( String string ) {
		StringTokenizer tokenizer = new StringTokenizer(string);
		String[] result = new String[tokenizer.countTokens()];
		for( int i = 0; i < result.length; i++) {
			result[i] = tokenizer.nextToken();
		}
		return result;
	}
	
	/**
	 * Get the normalized String from a Number field.
	 * @param number
	 * @return
	 */
	public static String getStringFromNumber( Number number ) {
		return MatchValueFormatter.getNumber(getNumber(number));
	}
	
	/**
	 * Get the normalized String from a Time field.
	 * @param time
	 * @return
	 */
	public static String getStringFromTime( Calendar time ) {
		return MatchValueFormatter.getTime(getTime(time));
	}
	
	/**
	 * Get the normalized String from a DateTime field.
	 * @param time
	 * @return
	 */
	public static String getStringFromDateTime( Calendar time ) {
		return MatchValueFormatter.getDateTime(getDateTime(time));
	}
	
	/**
	 * Get the normalized String from a Date field.
	 * @param time
	 * @return
	 */
	public static String getStringFromDate( Calendar time ) {
		return MatchValueFormatter.getDate(getDate(time));
	}
	
	public static String[] getStringNumberList( Number n1, Number n2 ) {
		if ( n1 != null && n2 != null ) {
			return new String[] { getStringFromNumber(n1), getStringFromNumber(n2) };
		} else if ( n1 != null ) {
			return new String[] { getStringFromNumber(n1) };
		} else if ( n2 != null ) {
			return new String[] { getStringFromNumber(n2) };
		}
		return null;
	}
	
	public static String[] getStringTimeList( Calendar c1, Calendar c2 ) {
		if ( c1 != null && c2 != null ) {
			return new String[] { getStringFromTime(c1), getStringFromTime(c2) };
		} else if ( c1 != null ) {
			return new String[] { getStringFromTime(c1) };
		} else if ( c2 != null ) {
			return new String[] { getStringFromTime(c2) };
		}
		return null;
	}
	
	public static String[] getStringDateTimeList( Calendar c1, Calendar c2 ) {
		if ( c1 != null && c2 != null ) {
			return new String[] { getStringFromDateTime(c1), getStringFromDateTime(c2) };
		} else if ( c1 != null ) {
			return new String[] { getStringFromDateTime(c1) };
		} else if ( c2 != null ) {
			return new String[] { getStringFromDateTime(c2) };
		}
		return null;
	}
	
	public static String[] getStringDateList( Calendar c1, Calendar c2 ) {
		if ( c1 != null && c2 != null ) {
			return new String[] { getStringFromDate(c1), getStringFromDate(c2) };
		} else if ( c1 != null ) {
			return new String[] { getStringFromDate(c1) };
		} else if ( c2 != null ) {
			return new String[] { getStringFromDate(c2) };
		}
		return null;
	}
	
	public static Long getNumber( Number number ) {
		if ( number == null ) {
			return null;
		}
		return number.longValue();
	}
	
	public static Integer getTime( Calendar cal ) {
		if ( cal == null ) {
			return null;
		}
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		int minutes = cal.get(Calendar.MINUTE);
		int seconds = cal.get(Calendar.SECOND);
		return Integer.valueOf(hours*3600+minutes*60+seconds);
	}
	
	public static Long getDate( Calendar cal ) {
		if ( cal == null ) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(cal.getTime());
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		return Long.valueOf(c.getTimeInMillis());
	}
	
	public static Long getDateTimeTS( Calendar cal ) {
		if ( cal == null ) {
			return null;
		}
		return Long.valueOf(cal.getTimeInMillis());
	}
	
	public static Object[] getDateTime( Calendar cal ) {
		Integer time = getTime(cal);
		Long date = getDate(cal);
		return new Object[] { date, time, getDateTimeTS(cal) };
	}
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

}
