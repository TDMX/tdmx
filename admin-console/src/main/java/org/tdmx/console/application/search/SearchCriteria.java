package org.tdmx.console.application.search;

import java.util.List;

import org.tdmx.console.application.domain.DomainObjectType;

/**
 * A SearchCriteria is a logically ANDed set of SearchExpression.
 * 
 * SearchCriteria := SearchExpression {" " SearchExpression }*
 * where 
 * SearchExpression := (":"<Type>("."<fieldName>)?" ")?<value>
 * 
 * 
 * ValueType Parsing
 *  RangedText && QuotedText 
 *  - depending on default DateFormatter / Locale, the Time,Date or DateTime formats can have " "
 *    which will required quoting to parse correctly. 
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
 *  TimeRange          TRT   TRDT      n/a   n/a     n/a    n/a     QTO
 *  DateTimeRange      n/a   DTR1      DTR2  n/a     n/a    n/a     QTO
 *  DateRange          n/a   DR1       DR2   n/a     n/a    n/a     QTO
 *  NumberRange        n/a   n/a       n/a   NRN     n/a    n/a     TLO
 *  Time               T-eq  DT-eq     n/a   n/a     n/a    n/a     TL
 *  DateTime           T-eq  DT-eq     D-eq  n/a     n/a    n/a     TL
 *  Date               n/a   D-eq      D-eq  n/a     n/a    n/a     TL
 *  Number             n/a   n/a       n/a   NE      n/a    SL      TL
 *  QuotedText         n/a   n/a       n/a   n/a     QT     n/a     QT
 *  Text               n/a   n/a       n/a   n/a     TE     SL      TL
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
	private List<SearchExpression> expressions;
	private DomainObjectType type;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SearchCriteria( DomainObjectType type, List<SearchExpression> expressions ) {
		this.type = type;
		this.expressions = expressions;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public String toString() {
		if ( expressions != null ) {
			return type + " " + expressions.toString();
		}
		return type + " []";
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
	
	public List<SearchExpression> getExpressions() {
		return expressions;
	}

	public DomainObjectType getType() {
		return type;
	}

}
