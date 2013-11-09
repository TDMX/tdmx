package org.tdmx.console.application.search;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.search.match.MatchValueNormalizer;

/**
 * The SearchableObjectField contains a DomainObject's field value.
 * 
 * FieldType
 *             [SearchValue-Type]   [OriginalValue-Type]
 *	Text       String[](lower)      String
 *	String     String(lower)        String
 *	Token      String(lower)        String
 *	Number     Long                 Number
 *	Date       Long                 Calendar  [millis since EPOCH of 00:00:00]
 *	DateTime   [Long,Integer,Long]  Calendar  [Date, Time, millis since EPOCH]  
*	Time       Integer              Calendar  [seconds since 00:00:00]
 * 
 * @see MatchValueNormalizer
 * 
 * @author Peter
 *
 */
public final class SearchableObjectField {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	public DomainObject object;
	public FieldDescriptor field; 
	public Object searchValue;
	public Object originalValue; 

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SearchableObjectField( DomainObject object, FieldDescriptor field, Object originalValue ) {
		this.object = object;
		this.field = field;
		this.originalValue = originalValue;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
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
