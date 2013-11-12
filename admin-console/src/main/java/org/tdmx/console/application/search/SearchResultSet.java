package org.tdmx.console.application.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tdmx.console.application.domain.DomainObject;

/**
 * A SearchResultSet is a helper class to collect the result of a Search
 * using a SearchCriteria which is an AND of one or more SearchExpressions.
 * 
 * @author Peter
 *
 */
public final class SearchResultSet {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private List<SearchExpression> expressions;
	private Set<DomainObject> result = new HashSet<>();
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SearchResultSet( SearchCriteria criteria ) {
		this.expressions = criteria.getExpressions();
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	/**
	 * Determine if ALL of the SearchCriteria's SearchExpressions match ANY of the
	 * DomainObject fields.
	 * 
	 * @param object
	 * @param fieldList
	 * @return true if ALL of the SearchExpressios match ANY of the DomainObject's fields.
	 */
	public boolean match( DomainObject object, List<SearchableObjectField> fieldList ) {
		for( SearchExpression exp : expressions ) {
			// if a search expression does'nt match for any of the object's fields - we don't
			// match since this is a logical AND.
			boolean matched = false;
			for( SearchableObjectField field : fieldList ) {
				if ( field.match(exp) ) {
					matched = true;
					break;
				}
			}
			if ( !matched ) {
				// if an object hasn't matched one expression stop and dont continue
				// evaluating the other expressions
				return false;
			}
		}
		return true;
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
	
	public Set<DomainObject> getResult() {
		return result;
	}

}
