package org.tdmx.console.application.search.match;

import org.tdmx.console.application.search.SearchableObjectField;

/**
 * Matching a DateTimeRange value against a DateTime field.
 * 
 * @author Peter
 *
 */
public class DateTimeRangeDateTimeMatch implements MatchFunction {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private Long from; // dateTimeTS from
	private Long to; // dateTimeTS to
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public DateTimeRangeDateTimeMatch( Long from, Long to ) {
		this.from = from;
		this.to = to;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean match(SearchableObjectField field) {
		Object[] fieldValue = (Object[])field.searchValue;
		long tsValue = (Long)fieldValue[2];
		if ( from != null && tsValue < from ) {
			return false;
		}
		if ( to != null && tsValue > to ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String fromT = "";
		String toT = "";
		if ( from != null ) {
			fromT = MatchValueFormatter.getDateTimeTS(from); 
		}
		if ( to != null ) {
			toT = MatchValueFormatter.getDateTimeTS(to); 
		}
		return fromT+"..DTRDT.."+toT;
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
