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
