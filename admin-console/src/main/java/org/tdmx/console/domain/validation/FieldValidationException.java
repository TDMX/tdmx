package org.tdmx.console.domain.validation;

import java.util.List;

import org.tdmx.console.application.domain.DomainObject;

public class FieldValidationException extends Exception {

	private static final long serialVersionUID = -3340667312593427822L;

	private List<FieldError> errors;
	private DomainObject object;
	
	public FieldValidationException(DomainObject object, List<FieldError> errors) {
		this.errors = errors;
		this.object = object;
	}
	
	/**
	 * @return the errors
	 */
	public List<FieldError> getErrors() {
		return errors;
	}

	public DomainObject getObject() {
		return object;
	}

}
