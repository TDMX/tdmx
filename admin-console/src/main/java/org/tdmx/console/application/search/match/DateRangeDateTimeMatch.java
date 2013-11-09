package org.tdmx.console.application.search.match;

import org.tdmx.console.application.search.SearchableObjectField;

/**
 * Matching a DateRange value against a DateTime field.
 * 
 * @author Peter
 *
 */
public class DateRangeDateTimeMatch implements MatchFunction {

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

	public DateRangeDateTimeMatch( Long from, Long to ) {
		this.from = from;
		this.to = to;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean match(SearchableObjectField field) {
		Object[] fieldValue = (Object[])field.searchValue;
		long dateValue = (Long)fieldValue[0];
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
		return fromT+"..DRDT.."+toT;
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
