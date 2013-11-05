package org.tdmx.console.application.search.match;

import java.text.DateFormat;
import java.util.Calendar;

import org.tdmx.console.application.search.SearchableObjectField;

/**
 * Matching a TimeRange value against a Time field.
 * 
 * @author Peter
 *
 */
public class TimeRangeTimeMatch implements MatchFunction {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private Integer from; // time from
	private Integer to; // time to
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public TimeRangeTimeMatch( Integer from, Integer to ) {
		this.from = from;
		this.to = to;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean match(SearchableObjectField field) {
		int timeValue = ((Integer)field.searchValue);
		if ( from != null && timeValue < from ) {
			return false;
		}
		if ( to != null && timeValue > to ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String fromT = "";
		String toT = "";
		if ( from != null ) {
			fromT = MatchValueNormalizer.getTimeString(from); 
		}
		if ( to != null ) {
			toT = MatchValueNormalizer.getTimeString(to); 
		}
		return fromT+"..TRT.."+toT;
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
