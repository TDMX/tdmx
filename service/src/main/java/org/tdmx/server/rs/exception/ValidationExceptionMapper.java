package org.tdmx.server.rs.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

	@Override
	public Response toResponse(ValidationException exception) {
		ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
		rb.entity(exception.getMessage());
		return rb.build();
	}

}
