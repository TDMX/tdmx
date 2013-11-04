package org.tdmx.console.application.search;

import java.util.List;

/**
 * A SearchCriteria is a logically ANDed set of SearchExpression.
 * 
 * SearchCriteria := SearchExpression {" " SearchExpression }*
 * where 
 * SearchExpression := (":"<Type>("."<fieldName>)?" ")?<value>
 * 
 * 
 * ValueType Parsing
 *  QuotedText
 *  RangedText
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
 * Match Operation
 *      [FieldType]    Time, DateTime, Date, Number, Token, String, Text
 *  [ValueType]          
 *  TimeRange          TR1   TR2       n/a   n/a     n/a    n/a     T-either
 *  DateTimeRange      n/a   DTR1      DTR2  n/a     n/a    n/a     T-either
 *  DateRange          n/a   DR1       DR2   n/a     n/a    n/a     T-either
 *  NumberRange        n/a   n/a       n/a   NR      n/a    n/a     T-either
 *  Time               T-eq  DT-eq     n/a   n/a     n/a    n/a     TM-any
 *  DateTime           T-eq  DT-eq     D-eq  n/a     n/a    n/a     TM-any
 *  Date               n/a   D-eq      D-eq  n/a     n/a    n/a     TM-any
 *  Number             n/a   n/a       n/a   N-eq    n/a    SM      TM-any
 *  QuotedText         n/a   n/a       n/a   n/a     n/a    n/a     TM-orig
 *  Text               n/a   n/a       n/a   n/a     T-eq   SM      TM-any
 *  
 *  
 * @author Peter
 *
 */
public final class SearchCriteria {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private List<SearchExpression> expression;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SearchCriteria( List<SearchExpression> expression ) {
		this.expression = expression;
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
