package org.tdmx.server.rs.exception;

import javax.ws.rs.ValidationError;
import javax.ws.rs.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

	@Override
	public Response toResponse(ValidationException exception) {
		ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
		StringBuilder sb = new StringBuilder();
		sb.append("ValidationErrors [");
		for (ValidationError error : exception.getViolations()) {
			sb.append(error.getMessage());
			sb.append(",");
		}
		sb.append("]");
		rb.entity(exception.getMessage());
		return rb.build();
	}

}
