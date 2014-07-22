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
package org.tdmx.server.ws.mds;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.sp.mds.Download;
import org.tdmx.core.api.v01.sp.mds.DownloadResponse;
import org.tdmx.core.api.v01.sp.mds.GetAddress;
import org.tdmx.core.api.v01.sp.mds.GetAddressResponse;
import org.tdmx.core.api.v01.sp.mds.GetTargetSession;
import org.tdmx.core.api.v01.sp.mds.GetTargetSessionResponse;
import org.tdmx.core.api.v01.sp.mds.ListAuthorizedChannelOrigin;
import org.tdmx.core.api.v01.sp.mds.ListAuthorizedChannelOriginResponse;
import org.tdmx.core.api.v01.sp.mds.ListFlowState;
import org.tdmx.core.api.v01.sp.mds.ListFlowStateResponse;
import org.tdmx.core.api.v01.sp.mds.ListService;
import org.tdmx.core.api.v01.sp.mds.ListServiceResponse;
import org.tdmx.core.api.v01.sp.mds.Receive;
import org.tdmx.core.api.v01.sp.mds.ReceiveResponse;
import org.tdmx.core.api.v01.sp.mds.SetTargetSession;
import org.tdmx.core.api.v01.sp.mds.SetTargetSessionResponse;
import org.tdmx.core.api.v01.sp.mds.tx.Commit;
import org.tdmx.core.api.v01.sp.mds.tx.CommitResponse;
import org.tdmx.core.api.v01.sp.mds.tx.Forget;
import org.tdmx.core.api.v01.sp.mds.tx.ForgetResponse;
import org.tdmx.core.api.v01.sp.mds.tx.Prepare;
import org.tdmx.core.api.v01.sp.mds.tx.PrepareResponse;
import org.tdmx.core.api.v01.sp.mds.tx.Recover;
import org.tdmx.core.api.v01.sp.mds.tx.RecoverResponse;
import org.tdmx.core.api.v01.sp.mds.tx.Rollback;
import org.tdmx.core.api.v01.sp.mds.tx.RollbackResponse;
import org.tdmx.core.api.v01.sp.mds.ws.MDS;

public class MDSImpl implements MDS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MDSImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@WebResult(name = "forgetResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:tx", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/forget")
	public ForgetResponse forget(
			@WebParam(partName = "parameters", name = "forget", targetNamespace = "urn:tdmx:api:v1.0:sp:tx") Forget parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "listServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mds", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/listService")
	public ListServiceResponse listService(
			@WebParam(partName = "parameters", name = "listService", targetNamespace = "urn:tdmx:api:v1.0:sp:mds") ListService parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "prepareResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:tx", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/prepare")
	public PrepareResponse prepare(
			@WebParam(partName = "parameters", name = "prepare", targetNamespace = "urn:tdmx:api:v1.0:sp:tx") Prepare parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "getAddressResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mds", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/getAddress")
	public GetAddressResponse getAddress(
			@WebParam(partName = "parameters", name = "getAddress", targetNamespace = "urn:tdmx:api:v1.0:sp:mds") GetAddress parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "downloadResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mds", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/download")
	public DownloadResponse download(
			@WebParam(partName = "parameters", name = "download", targetNamespace = "urn:tdmx:api:v1.0:sp:mds") Download parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "receiveResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mds", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/receive")
	public ReceiveResponse receive(
			@WebParam(partName = "parameters", name = "receive", targetNamespace = "urn:tdmx:api:v1.0:sp:mds") Receive parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "rollbackResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:tx", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/rollback")
	public RollbackResponse rollback(
			@WebParam(partName = "parameters", name = "rollback", targetNamespace = "urn:tdmx:api:v1.0:sp:tx") Rollback parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "commitResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:tx", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/commit")
	public CommitResponse commit(
			@WebParam(partName = "parameters", name = "commit", targetNamespace = "urn:tdmx:api:v1.0:sp:tx") Commit parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "listFlowStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mds", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/listFlowState")
	public ListFlowStateResponse listFlowState(
			@WebParam(partName = "parameters", name = "listFlowState", targetNamespace = "urn:tdmx:api:v1.0:sp:mds") ListFlowState parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "listAuthorizedChannelOriginResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mds", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/listAuthorizedChannelOrigin")
	public ListAuthorizedChannelOriginResponse listAuthorizedChannelOrigin(
			@WebParam(partName = "parameters", name = "listAuthorizedChannelOrigin", targetNamespace = "urn:tdmx:api:v1.0:sp:mds") ListAuthorizedChannelOrigin parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "getTargetSessionResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mds", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/getTargetSession")
	public GetTargetSessionResponse getTargetSession(
			@WebParam(partName = "parameters", name = "getTargetSession", targetNamespace = "urn:tdmx:api:v1.0:sp:mds") GetTargetSession parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "recoverResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:tx", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/recover")
	public RecoverResponse recover(
			@WebParam(partName = "parameters", name = "recover", targetNamespace = "urn:tdmx:api:v1.0:sp:tx") Recover parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "setTargetSessionResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mds", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mds-definition/setTargetSession")
	public SetTargetSessionResponse setTargetSession(
			@WebParam(partName = "parameters", name = "setTargetSession", targetNamespace = "urn:tdmx:api:v1.0:sp:mds") SetTargetSession parameters) {
		// TODO Auto-generated method stub
		return null;
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

}
