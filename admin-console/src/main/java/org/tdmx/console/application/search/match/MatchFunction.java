package org.tdmx.console.application.search.match;

import org.tdmx.console.application.search.FieldDescriptor;
import org.tdmx.console.application.search.SearchableObjectField;


/**
 * A MatchFunction incorporates a SearchExpression's value in a convenient
 * way for matching with different {@link FieldDescriptor.FieldType}.
 * 
 * Depending on a SearchableObjectField's {@link FieldDescriptor.FieldType}, the 
 * {@link SearchableObjectField#searchValue} has a different Type.
 * 
 * @author Peter
 *
 */
public interface MatchFunction {

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
	public boolean match( SearchableObjectField field );
	
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
