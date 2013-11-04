package org.tdmx.console.application.search.match;

import org.tdmx.console.application.search.SearchableObjectField;

/**
 * Used to match Ranged hi/low values with Text Field's searchValue.
 * 
 * The compared values are all converted to lowercase prior to use in the 
 * match function.
 * 
 * @author Peter
 *
 */
public class TextLikeOrMatch implements MatchFunction {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private String[] texts;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public TextLikeOrMatch( String[] texts ) {
		this.texts = texts;
	}
	
	@Override
	public String toString() {
		return "like-any "+texts;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean match(SearchableObjectField field) {
		for( String text : texts ) {
			for( String fv : (String[])field.searchValue){
				if (fv.indexOf(text) != -1 ) {
					return true;
				}
			}
		}
		return false;
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
