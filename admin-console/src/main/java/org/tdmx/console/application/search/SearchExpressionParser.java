package org.tdmx.console.application.search;


/**
 * SearchExpression := (":"<Type>("."<fieldName>)?" ")?<value>
 * 
 * Parses SearchExpressions one by one from a input string.
 * 
 * Start by identifying if the optional part 
 *  (":"<Type>("."<fieldName>)?" ")? 
 * exists by identifying ":" to a WHITESPACE, and parsing it to identify the
 * Type and optional fieldName. 
 * 
 * The next token is then a <value>. The Value is read as QuotedText or Text.
 *  QuotedText ( starts with ", consumed until another " )
 *  Text ( !starting with ", consumed until next WHITESPACE )
 * 
 * From the Text, the 
 *  RangedText ( contains .. )
 *    TimeRange ( Time..Time, ..Time, Time.. )
 *    DateTimeRange ( DateTime..DateTime, ..DateTime, DateTime.. )
 *    DateRange ( Date..Date, ..Date, Date..  )
 *    NumberRange ( Number..Number, ..Number, Number.. )
 *  Time
 *  DateTime
 *  Date
 *  Number
 *  Text
 *  
 *  
 * @author Peter
 *
 */
public final class SearchExpressionParser {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	
	private String input;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SearchExpressionParser(String input) {
		this.input = input;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	/**
	 * Returns the next SearchExpression or null if no further exist.
	 * @return the next SearchExpression or null if no further exist.
	 */
	public SearchExpression parseNext() {
		String text = input.trim();
		//TODO
		return null;
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
