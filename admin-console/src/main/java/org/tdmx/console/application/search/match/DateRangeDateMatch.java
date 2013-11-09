package org.tdmx.console.application.search.match;

import org.tdmx.console.application.search.SearchableObjectField;

/**
 * Matching a DateRange value against a Date field.
 * 
 * @author Peter
 *
 */
public class DateRangeDateMatch implements MatchFunction {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private Long from; // date from
	private Long to; // date to
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public DateRangeDateMatch( Long from, Long to ) {
		this.from = from;
		this.to = to;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean match(SearchableObjectField field) {
		long dateValue = (Long)field.searchValue;
		if ( from != null && dateValue < from ) {
			return false;
		}
		if ( to != null && dateValue > to ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String fromT = "";
		String toT = "";
		if ( from != null ) {
			fromT = MatchValueFormatter.getDate(from); 
		}
		if ( to != null ) {
			toT = MatchValueFormatter.getDate(to); 
		}
		return fromT+"..DRD.."+toT;
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
