package org.tdmx.server.ws.zas;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;

import org.tdmx.core.api.v01.sp.zas.CreateAddress;
import org.tdmx.core.api.v01.sp.zas.CreateAddressResponse;
import org.tdmx.core.api.v01.sp.zas.CreateAdministrator;
import org.tdmx.core.api.v01.sp.zas.CreateAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.CreateDomain;
import org.tdmx.core.api.v01.sp.zas.CreateDomainResponse;
import org.tdmx.core.api.v01.sp.zas.CreateIpZone;
import org.tdmx.core.api.v01.sp.zas.CreateIpZoneResponse;
import org.tdmx.core.api.v01.sp.zas.CreateService;
import org.tdmx.core.api.v01.sp.zas.CreateServiceResponse;
import org.tdmx.core.api.v01.sp.zas.CreateUser;
import org.tdmx.core.api.v01.sp.zas.CreateUserResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteAddress;
import org.tdmx.core.api.v01.sp.zas.DeleteAddressResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteAdministrator;
import org.tdmx.core.api.v01.sp.zas.DeleteAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteChannelAuthorization;
import org.tdmx.core.api.v01.sp.zas.DeleteChannelAuthorizationResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteDomain;
import org.tdmx.core.api.v01.sp.zas.DeleteDomainResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteIpZone;
import org.tdmx.core.api.v01.sp.zas.DeleteIpZoneResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteService;
import org.tdmx.core.api.v01.sp.zas.DeleteServiceResponse;
import org.tdmx.core.api.v01.sp.zas.DeleteUser;
import org.tdmx.core.api.v01.sp.zas.DeleteUserResponse;
import org.tdmx.core.api.v01.sp.zas.GetChannelAuthorization;
import org.tdmx.core.api.v01.sp.zas.GetChannelAuthorizationResponse;
import org.tdmx.core.api.v01.sp.zas.GetFlowState;
import org.tdmx.core.api.v01.sp.zas.GetFlowStateResponse;
import org.tdmx.core.api.v01.sp.zas.GetFlowTargetState;
import org.tdmx.core.api.v01.sp.zas.GetFlowTargetStateResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyAdministrator;
import org.tdmx.core.api.v01.sp.zas.ModifyAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyFlowTargetState;
import org.tdmx.core.api.v01.sp.zas.ModifyFlowTargetStateResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyIpZone;
import org.tdmx.core.api.v01.sp.zas.ModifyIpZoneResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyService;
import org.tdmx.core.api.v01.sp.zas.ModifyServiceResponse;
import org.tdmx.core.api.v01.sp.zas.ModifyUser;
import org.tdmx.core.api.v01.sp.zas.ModifyUserResponse;
import org.tdmx.core.api.v01.sp.zas.SearchAddress;
import org.tdmx.core.api.v01.sp.zas.SearchAddressResponse;
import org.tdmx.core.api.v01.sp.zas.SearchAdministrator;
import org.tdmx.core.api.v01.sp.zas.SearchAdministratorResponse;
import org.tdmx.core.api.v01.sp.zas.SearchChannelAuthorization;
import org.tdmx.core.api.v01.sp.zas.SearchChannelAuthorizationResponse;
import org.tdmx.core.api.v01.sp.zas.SearchDomain;
import org.tdmx.core.api.v01.sp.zas.SearchDomainResponse;
import org.tdmx.core.api.v01.sp.zas.SearchFlowState;
import org.tdmx.core.api.v01.sp.zas.SearchFlowStateResponse;
import org.tdmx.core.api.v01.sp.zas.SearchFlowTargetState;
import org.tdmx.core.api.v01.sp.zas.SearchFlowTargetStateResponse;
import org.tdmx.core.api.v01.sp.zas.SearchIpZone;
import org.tdmx.core.api.v01.sp.zas.SearchIpZoneResponse;
import org.tdmx.core.api.v01.sp.zas.SearchService;
import org.tdmx.core.api.v01.sp.zas.SearchServiceResponse;
import org.tdmx.core.api.v01.sp.zas.SearchUser;
import org.tdmx.core.api.v01.sp.zas.SearchUserResponse;
import org.tdmx.core.api.v01.sp.zas.SetChannelAuthorization;
import org.tdmx.core.api.v01.sp.zas.SetChannelAuthorizationResponse;
import org.tdmx.core.api.v01.sp.zas.report.Incident;
import org.tdmx.core.api.v01.sp.zas.report.IncidentResponse;
import org.tdmx.core.api.v01.sp.zas.report.Report;
import org.tdmx.core.api.v01.sp.zas.report.ReportResponse;
import org.tdmx.core.api.v01.sp.zas.ws.ZAS;

public class ZASImpl implements ZAS {

	@Override
	@WebResult(name = "searchDomainResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchDomain")
	public SearchDomainResponse searchDomain(
			@WebParam(partName = "parameters", name = "searchDomain", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchDomain parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "searchUserResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchUser")
	public SearchUserResponse searchUser(
			@WebParam(partName = "parameters", name = "searchUser", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchUser parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "createDomainResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createDomain")
	public CreateDomainResponse createDomain(
			@WebParam(partName = "parameters", name = "createDomain", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateDomain parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "searchAdministratorResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchAdministrator")
	public SearchAdministratorResponse searchAdministrator(
			@WebParam(partName = "parameters", name = "searchAdministrator", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchAdministrator parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "modifyUserResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyUser")
	public ModifyUserResponse modifyUser(
			@WebParam(partName = "parameters", name = "modifyUser", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") ModifyUser parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "searchIpZoneResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchIpZone")
	public SearchIpZoneResponse searchIpZone(
			@WebParam(partName = "parameters", name = "searchIpZone", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "createAddressResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createAddress")
	public CreateAddressResponse createAddress(
			@WebParam(partName = "parameters", name = "createAddress", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateAddress parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "deleteChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteChannelAuthorization")
	public DeleteChannelAuthorizationResponse deleteChannelAuthorization(
			@WebParam(partName = "parameters", name = "deleteChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteChannelAuthorization parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "incidentResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:report", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/incident")
	public IncidentResponse incident(
			@WebParam(partName = "parameters", name = "incident", targetNamespace = "urn:tdmx:api:v1.0:sp:report") Incident parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "createIpZoneResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createIpZone")
	public CreateIpZoneResponse createIpZone(
			@WebParam(partName = "parameters", name = "createIpZone", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "deleteIpZoneResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteIpZone")
	public DeleteIpZoneResponse deleteIpZone(
			@WebParam(partName = "parameters", name = "deleteIpZone", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "deleteDomainResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteDomain")
	public DeleteDomainResponse deleteDomain(
			@WebParam(partName = "parameters", name = "deleteDomain", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteDomain parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "reportResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:report", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/report")
	public ReportResponse report(
			@WebParam(partName = "parameters", name = "report", targetNamespace = "urn:tdmx:api:v1.0:sp:report") Report parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "getChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/getChannelAuthorization")
	public GetChannelAuthorizationResponse getChannelAuthorization(
			@WebParam(partName = "parameters", name = "getChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") GetChannelAuthorization parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "deleteUserResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteUser")
	public DeleteUserResponse deleteUser(
			@WebParam(partName = "parameters", name = "deleteUser", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteUser parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "modifyFlowTargetStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyFlowTargetState")
	public ModifyFlowTargetStateResponse modifyFlowTargetState(
			@WebParam(partName = "parameters", name = "modifyFlowTargetState", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") ModifyFlowTargetState parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "modifyIpZoneResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyIpZone")
	public ModifyIpZoneResponse modifyIpZone(
			@WebParam(partName = "parameters", name = "modifyIpZone", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") ModifyIpZone parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "modifyServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyService")
	public ModifyServiceResponse modifyService(
			@WebParam(partName = "parameters", name = "modifyService", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") ModifyService parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "getFlowStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/getFlowState")
	public GetFlowStateResponse getFlowState(
			@WebParam(partName = "parameters", name = "getFlowState", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") GetFlowState parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "searchAddressResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchAddress")
	public SearchAddressResponse searchAddress(
			@WebParam(partName = "parameters", name = "searchAddress", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchAddress parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "createAdministratorResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createAdministrator")
	public CreateAdministratorResponse createAdministrator(
			@WebParam(partName = "parameters", name = "createAdministrator", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateAdministrator parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "searchFlowTargetStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchFlowTargetState")
	public SearchFlowTargetStateResponse searchFlowTargetState(
			@WebParam(partName = "parameters", name = "searchFlowTargetState", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchFlowTargetState parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "deleteServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteService")
	public DeleteServiceResponse deleteService(
			@WebParam(partName = "parameters", name = "deleteService", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteService parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "getFlowTargetStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/getFlowTargetState")
	public GetFlowTargetStateResponse getFlowTargetState(
			@WebParam(partName = "parameters", name = "getFlowTargetState", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") GetFlowTargetState parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "searchServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchService")
	public SearchServiceResponse searchService(
			@WebParam(partName = "parameters", name = "searchService", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchService parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "deleteAddressResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteAddress")
	public DeleteAddressResponse deleteAddress(
			@WebParam(partName = "parameters", name = "deleteAddress", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteAddress parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "modifyAdministratorResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/modifyAdministrator")
	public ModifyAdministratorResponse modifyAdministrator(
			@WebParam(partName = "parameters", name = "modifyAdministrator", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") ModifyAdministrator parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "searchFlowStateResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchFlowState")
	public SearchFlowStateResponse searchFlowState(
			@WebParam(partName = "parameters", name = "searchFlowState", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchFlowState parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "setChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/setChannelAuthorization")
	public SetChannelAuthorizationResponse setChannelAuthorization(
			@WebParam(partName = "parameters", name = "setChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SetChannelAuthorization parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "createUserResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createUser")
	public CreateUserResponse createUser(
			@WebParam(partName = "parameters", name = "createUser", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateUser parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "searchChannelAuthorizationResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/searchChannelAuthorization")
	public SearchChannelAuthorizationResponse searchChannelAuthorization(
			@WebParam(partName = "parameters", name = "searchChannelAuthorization", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") SearchChannelAuthorization parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "createServiceResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/createService")
	public CreateServiceResponse createService(
			@WebParam(partName = "parameters", name = "createService", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") CreateService parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "deleteAdministratorResponse", targetNamespace = "urn:tdmx:api:v1.0:sp:zas", partName = "parameters")
	@WebMethod(action = "urn:tdmx:api:v1.0:sp:zas-definition/deleteAdministrator")
	public DeleteAdministratorResponse deleteAdministrator(
			@WebParam(partName = "parameters", name = "deleteAdministrator", targetNamespace = "urn:tdmx:api:v1.0:sp:zas") DeleteAdministrator parameters) {
		// TODO Auto-generated method stub
		return null;
	}

}
