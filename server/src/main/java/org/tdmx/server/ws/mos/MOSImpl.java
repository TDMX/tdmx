package org.tdmx.server.ws.mos;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;

import org.tdmx.server.api.v01.sp.mos.GetAddress;
import org.tdmx.server.api.v01.sp.mos.GetAddressResponse;
import org.tdmx.server.api.v01.sp.mos.GetFlowTargetSession;
import org.tdmx.server.api.v01.sp.mos.GetFlowTargetSessionResponse;
import org.tdmx.server.api.v01.sp.mos.GetMessageDeliveryStatus;
import org.tdmx.server.api.v01.sp.mos.GetMessageDeliveryStatusResponse;
import org.tdmx.server.api.v01.sp.mos.ListAuthorizedChannelDestination;
import org.tdmx.server.api.v01.sp.mos.ListAuthorizedChannelDestinationResponse;
import org.tdmx.server.api.v01.sp.mos.ListFlowState;
import org.tdmx.server.api.v01.sp.mos.ListFlowStateResponse;
import org.tdmx.server.api.v01.sp.mos.ListService;
import org.tdmx.server.api.v01.sp.mos.ListServiceResponse;
import org.tdmx.server.api.v01.sp.mos.Submit;
import org.tdmx.server.api.v01.sp.mos.SubmitResponse;
import org.tdmx.server.api.v01.sp.mos.Upload;
import org.tdmx.server.api.v01.sp.mos.UploadResponse;
import org.tdmx.server.api.v01.sp.mos.tx.Commit;
import org.tdmx.server.api.v01.sp.mos.tx.CommitResponse;
import org.tdmx.server.api.v01.sp.mos.tx.Forget;
import org.tdmx.server.api.v01.sp.mos.tx.ForgetResponse;
import org.tdmx.server.api.v01.sp.mos.tx.Prepare;
import org.tdmx.server.api.v01.sp.mos.tx.PrepareResponse;
import org.tdmx.server.api.v01.sp.mos.tx.Recover;
import org.tdmx.server.api.v01.sp.mos.tx.RecoverResponse;
import org.tdmx.server.api.v01.sp.mos.tx.Rollback;
import org.tdmx.server.api.v01.sp.mos.tx.RollbackResponse;
import org.tdmx.server.api.v01.sp.mos.ws.MOS;

public class MOSImpl implements MOS {

	@Override
	@WebResult(name = "listAuthorizedChannelDestinationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mos", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/listAuthorizedChannelDestination")
	public ListAuthorizedChannelDestinationResponse listAuthorizedChannelDestination(
			@WebParam(partName = "parameters", name = "listAuthorizedChannelDestination", targetNamespace = "urn:tdmx:api:v1.0:sp:mos") ListAuthorizedChannelDestination parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "listFlowStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mos", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/listFlowState")
	public ListFlowStateResponse listFlowState(
			@WebParam(partName = "parameters", name = "listFlowState", targetNamespace = "urn:tdmx:api:v1.0:sp:mos") ListFlowState parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "commitResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:tx", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/commit")
	public CommitResponse commit(
			@WebParam(partName = "parameters", name = "commit", targetNamespace = "urn:tdmx:api:v1.0:sp:tx") Commit parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "rollbackResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:tx", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/rollback")
	public RollbackResponse rollback(
			@WebParam(partName = "parameters", name = "rollback", targetNamespace = "urn:tdmx:api:v1.0:sp:tx") Rollback parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "forgetResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:tx", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/forget")
	public ForgetResponse forget(
			@WebParam(partName = "parameters", name = "forget", targetNamespace = "urn:tdmx:api:v1.0:sp:tx") Forget parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "prepareResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:tx", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/prepare")
	public PrepareResponse prepare(
			@WebParam(partName = "parameters", name = "prepare", targetNamespace = "urn:tdmx:api:v1.0:sp:tx") Prepare parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "recoverResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:tx", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/recover")
	public RecoverResponse recover(
			@WebParam(partName = "parameters", name = "recover", targetNamespace = "urn:tdmx:api:v1.0:sp:tx") Recover parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "listServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mos", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/listService")
	public ListServiceResponse listService(
			@WebParam(partName = "parameters", name = "listService", targetNamespace = "urn:tdmx:api:v1.0:sp:mos") ListService parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "getAddressResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mos", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/getAddress")
	public GetAddressResponse getAddress(
			@WebParam(partName = "parameters", name = "getAddress", targetNamespace = "urn:tdmx:api:v1.0:sp:mos") GetAddress parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "submitResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mos", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/submit")
	public SubmitResponse submit(
			@WebParam(partName = "parameters", name = "submit", targetNamespace = "urn:tdmx:api:v1.0:sp:mos") Submit parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "getMessageDeliveryStatusResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mos", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/getMessageDeliveryStatus")
	public GetMessageDeliveryStatusResponse getMessageDeliveryStatus(
			@WebParam(partName = "parameters", name = "getMessageDeliveryStatus", targetNamespace = "urn:tdmx:api:v1.0:sp:mos") GetMessageDeliveryStatus parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "getFlowTargetSessionResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mos", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/getFlowTargetSession")
	public GetFlowTargetSessionResponse getFlowTargetSession(
			@WebParam(partName = "parameters", name = "getFlowTargetSession", targetNamespace = "urn:tdmx:api:v1.0:sp:mos") GetFlowTargetSession parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "uploadResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:mos", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:mos-definition/upload")
	public UploadResponse upload(
			@WebParam(partName = "parameters", name = "upload", targetNamespace = "urn:tdmx:api:v1.0:sp:mos") Upload parameters) {
		// TODO Auto-generated method stub
		return null;
	}

}
