package org.tdmx.console.domain.validation;

import org.tdmx.console.domain.validation.OperationError.ERROR;




/**
 * 
 * @author Peter
 *
 */
public class FieldError {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String field;
	private int position = 0;
	private ERROR error;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public FieldError(String field, ERROR errorCode) {
		this.field = field;
		this.error = errorCode;
	}
	
	public FieldError(String field, int pos, ERROR errorCode) {
		this.field = field;
		this.position = pos;
		this.error = errorCode;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public String toString() {
		if ( position > 0 ) {
			return getError()+"["+getField()+"("+getPosition()+")]";
		} else {
			return getError()+"["+getField()+"]";
		}
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

	public String getField() {
		return field;
	}

	public int getPosition() {
		return position;
	}

	public ERROR getError() {
		return error;
	}

}
