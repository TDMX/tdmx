package org.tdmx.console.application.search.match;

import org.tdmx.console.application.search.SearchableObjectField;

/**
 * Matching a NumberRange value against a Number field.
 * 
 * @author Peter
 *
 */
public class NumberRangeNumberMatch implements MatchFunction {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private Long from; // number from
	private Long to; // number to
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public NumberRangeNumberMatch( Long from, Long to ) {
		this.from = from;
		this.to = to;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean match(SearchableObjectField field) {
		Long value = ((Long)field.searchValue);
		if ( from != null && value < from ) {
			return false;
		}
		if ( to != null && value > to ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String fromN = "";
		String toN = "";
		if ( from != null ) {
			fromN = MatchValueNormalizer.getNumberString(from); 
		}
		if ( to != null ) {
			toN = MatchValueNormalizer.getNumberString(to); 
		}
		return fromN+"..NRN.."+toN;
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
