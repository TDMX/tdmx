package org.tdmx.server.rs.exception;

public class FieldValidationError implements ApplicationValidationError {

	public enum FieldValidationErrorType {
		PRESENT,
		MISSING,
		TOO_LONG,
		INVALID,
		CONSTRAINT_VIOLATED, ;
	}

	private final String fieldName;
	private final FieldValidationErrorType type;

	public FieldValidationError(FieldValidationErrorType type, String fieldName) {
		this.type = type;
		this.fieldName = fieldName;
	}

	@Override
	public Object getInvalidValue() {
		return null; // never tell - security!
	}

	@Override
	public String getMessage() {
		return type + "=" + fieldName;
	}

}
