package org.tdmx.server.rs.exception;

public class ValidationException extends RuntimeException {

	private final String message;

	@Override
	public String getMessage() {
		return message;
	}

	public ValidationException(String msg) {
		this.message = msg;
	}
}
