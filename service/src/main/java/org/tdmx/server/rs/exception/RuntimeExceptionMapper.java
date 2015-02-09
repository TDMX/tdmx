package org.tdmx.server.rs.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

	private static final Logger log = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

	@Override
	public Response toResponse(RuntimeException exception) {
		log.error("RS exception.", exception);
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}

}
