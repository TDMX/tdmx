/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.console.domain.validation;

import java.util.List;

/**
 * An error result of an operation. An operation can fail due to field errors or global errors.
 * 
 * @author Peter
 * 
 */
public class OperationError {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static enum ERROR {
		MISSING, // when an object or object field is missing when it should be present.
		PRESENT, // when an object or object field is present but this is not allowed.
		INVALID, // when an object or object field is not correct syntax
		IMMUTABLE, // when an object is tried to be modified but it should not be
		SYSTEM, // when unexpected system errors occur - must be logged as WARN in log
	}

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private List<FieldError> fieldErrors;
	private ERROR error;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public OperationError(List<FieldError> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

	public OperationError(ERROR error) {
		this.error = error;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		String s = "";
		if (error != null) {
			s += error;
		}
		if (fieldErrors != null) {
			s += fieldErrors;
		}
		return s;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public List<FieldError> getFieldErrors() {
		return fieldErrors;
	}

	public ERROR getError() {
		return error;
	}

}
