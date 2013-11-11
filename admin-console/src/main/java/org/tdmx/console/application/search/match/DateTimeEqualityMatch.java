package org.tdmx.console.application.search.match;

import org.tdmx.console.application.search.SearchableObjectField;

/**
 * Matching a DateTime value against a DateTime field.
 * 
 * @author Peter
 *
 */
public class DateTimeEqualityMatch implements MatchFunction {

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

	public DateTimeEqualityMatch( Long date ) {
		this.date = date;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean match(SearchableObjectField field) {
		return date.equals(field.searchValue);
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
