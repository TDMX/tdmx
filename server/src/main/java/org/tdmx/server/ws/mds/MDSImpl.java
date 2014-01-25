package org.tdmx.server.ws.mds;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;

import org.tdmx.server.api.v01.sp.mds.Download;
import org.tdmx.server.api.v01.sp.mds.DownloadResponse;
import org.tdmx.server.api.v01.sp.mds.GetAddress;
import org.tdmx.server.api.v01.sp.mds.GetAddressResponse;
import org.tdmx.server.api.v01.sp.mds.GetTargetSession;
import org.tdmx.server.api.v01.sp.mds.GetTargetSessionResponse;
import org.tdmx.server.api.v01.sp.mds.ListAuthorizedChannelOrigin;
import org.tdmx.server.api.v01.sp.mds.ListAuthorizedChannelOriginResponse;
import org.tdmx.server.api.v01.sp.mds.ListFlowState;
import org.tdmx.server.api.v01.sp.mds.ListFlowStateResponse;
import org.tdmx.server.api.v01.sp.mds.ListService;
import org.tdmx.server.api.v01.sp.mds.ListServiceResponse;
import org.tdmx.server.api.v01.sp.mds.Receive;
import org.tdmx.server.api.v01.sp.mds.ReceiveResponse;
import org.tdmx.server.api.v01.sp.mds.SetTargetSession;
import org.tdmx.server.api.v01.sp.mds.SetTargetSessionResponse;
import org.tdmx.server.api.v01.sp.mds.tx.Commit;
import org.tdmx.server.api.v01.sp.mds.tx.CommitResponse;
import org.tdmx.server.api.v01.sp.mds.tx.Forget;
import org.tdmx.server.api.v01.sp.mds.tx.ForgetResponse;
import org.tdmx.server.api.v01.sp.mds.tx.Prepare;
import org.tdmx.server.api.v01.sp.mds.tx.PrepareResponse;
import org.tdmx.server.api.v01.sp.mds.tx.Recover;
import org.tdmx.server.api.v01.sp.mds.tx.RecoverResponse;
import org.tdmx.server.api.v01.sp.mds.tx.Rollback;
import org.tdmx.server.api.v01.sp.mds.tx.RollbackResponse;
import org.tdmx.server.api.v01.sp.mds.ws.MDS;

public class MDSImpl implements MDS {

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

}
