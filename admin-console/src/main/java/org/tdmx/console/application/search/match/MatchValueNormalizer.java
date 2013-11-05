package org.tdmx.console.application.search.match;

import java.text.DateFormat;
import java.util.Calendar;

import org.tdmx.console.application.search.SearchableObjectField;

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
