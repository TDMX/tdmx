package org.tdmx.console.application.search.match;

import org.tdmx.console.application.search.SearchableObjectField;

/**
 * Matching a Date value against a DateTime field.
 * 
 * @author Peter
 *
 */
public class DateDateTimeMatch implements MatchFunction {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private Long date;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public DateDateTimeMatch( Long date ) {
		this.date = date;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean match(SearchableObjectField field) {
		Object[] fieldValue = (Object[])field.searchValue;
		return date.equals( (Long)fieldValue[0]);
	}

	@Override
	public String toString() {
		return "=D="+ MatchValueFormatter.getDate(date);
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
