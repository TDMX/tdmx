package org.tdmx.console.domain.validation;

import java.util.List;




/**
 * An error result of an operation. An operation can fail due to field errors
 * or global errors.
 * 
 * @author Peter
 *
 */
public class OperationError {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static enum ERROR {
		MISSING, // when an object or object field is missing when it should be present.
		PRESENT, // when an object or object field is present but this is not allowed. 
		INVALID, // when an object or object field is not correct syntax 
		IMMUTABLE, // when an object is tried to be modified but it should not be 
		SYSTEM, // when unexpected system errors occur - must be logged as WARN in log
	}
	
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private List<FieldError> fieldErrors;
	private ERROR error;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public OperationError(List<FieldError> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}
	
	public OperationError(ERROR error) {
		this.error = error;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public String toString() {
		String s = "";
		if ( error != null ) {
			s += error;
		}
		if ( fieldErrors != null ) {
			s += fieldErrors;
		}
		return s;
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

	public List<FieldError> getFieldErrors() {
		return fieldErrors;
	}

	public ERROR getError() {
		return error;
	}

}
