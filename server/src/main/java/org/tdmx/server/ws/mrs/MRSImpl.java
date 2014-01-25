package org.tdmx.server.ws.mrs;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;

import org.tdmx.server.api.v01.sp.mrs.CreateSession;
import org.tdmx.server.api.v01.sp.mrs.CreateSessionResponse;
import org.tdmx.server.api.v01.sp.mrs.Relay;
import org.tdmx.server.api.v01.sp.mrs.RelayResponse;
import org.tdmx.server.api.v01.sp.mrs.ws.MRS;

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
