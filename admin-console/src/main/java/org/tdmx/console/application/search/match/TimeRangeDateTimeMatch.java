package org.tdmx.console.application.search.match;

import org.tdmx.console.application.search.SearchableObjectField;

/**
 * Matching a TimeRange value against a Time field.
 * 
 * @author Peter
 *
 */
public class TimeRangeDateTimeMatch implements MatchFunction {

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

	public TimeRangeDateTimeMatch( Integer from, Integer to ) {
		this.from = from;
		this.to = to;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean match(SearchableObjectField field) {
		Object[] fieldValue = (Object[])field.searchValue;
		int timeValue = (Integer)fieldValue[1];
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
			fromT = MatchValueFormatter.getTime(from); 
		}
		if ( to != null ) {
			toT = MatchValueFormatter.getTime(to); 
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
