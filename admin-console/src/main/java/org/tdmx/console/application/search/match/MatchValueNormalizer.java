package org.tdmx.console.application.search.match;

import java.text.DateFormat;
import java.util.Calendar;

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
	
	public static String getString( String string ) {
		return string.toLowerCase();
	}
	
	public static String getString( Number number ) {
		if ( number == null ) {
			return null;
		}
		return number.toString();
	}
	
	public static String[] getStringList( Number n1, Number n2 ) {
		if ( n1 != null && n2 != null ) {
			return new String[] { n1.toString(), n2.toString() };
		} else if ( n1 != null ) {
			return new String[] { n1.toString() };
		} else if ( n2 != null ) {
			return new String[] { n2.toString() };
		}
		return null;
	}
	
	public static Long getNumber( Number number ) {
		if ( number == null ) {
			return null;
		}
		return number.longValue();
	}
	
	public static String getNumberString( Long number ) {
		if ( number == null ) {
			return null;
		}
		return number.toString();
	}
	
	public static Integer getTime( Calendar cal ) {
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		int minutes = cal.get(Calendar.MINUTE);
		int seconds = cal.get(Calendar.SECOND);
		return Integer.valueOf(hours*3600+minutes*60+seconds);
	}
	
	public static String getTimeString( Integer time ) {
		if ( time == null ) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.add(Calendar.SECOND, time);
		return DateFormat.getTimeInstance().format(cal.getTime());
	}
	
	//TODO other field normalizations
	
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
