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
package org.tdmx.server.ws.mrs;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;

import org.tdmx.core.api.v01.sp.mrs.CreateSession;
import org.tdmx.core.api.v01.sp.mrs.CreateSessionResponse;
import org.tdmx.core.api.v01.sp.mrs.Relay;
import org.tdmx.core.api.v01.sp.mrs.RelayResponse;
import org.tdmx.core.api.v01.sp.mrs.ws.MRS;

public class MRSImpl implements MRS {

	@Override
	@WebResult(name = "createSessionResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mrs", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mrs-definition/createSession")
	public CreateSessionResponse createSession(
			@WebParam(partName = "parameters", name = "createSession", targetNamespace = "urn:tdmx:api:v1.0:sp:mrs") CreateSession parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "relayResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mrs", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mrs-definition/relay")
	public RelayResponse relay(
			@WebParam(partName = "parameters", name = "relay", targetNamespace = "urn:tdmx:api:v1.0:sp:mrs") Relay parameters) {
		// TODO Auto-generated method stub
		return null;
	}

}
