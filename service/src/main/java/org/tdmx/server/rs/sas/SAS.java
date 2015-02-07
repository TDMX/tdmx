package org.tdmx.server.rs.sas;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/sas")
@Produces({ "application/json" })
public interface SAS {

	/*
	 * RESTFUL service for Account
	 */
	@POST
	@Path("/accounts")
	Response createAccount(AccountResource account);

	@GET
	@Path("/accounts")
	List<AccountResource> searchAccount(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize);

	@GET
	@Path("/accounts/{aid}")
	AccountResource getAccount(@PathParam("aid") Long aId);

	@PUT
	@Path("/accounts/{aid}")
	Response updateAccount(AccountResource account);

	@DELETE
	@Path("/accounts/{aid}")
	Response deleteAccount(@PathParam("aid") Long aId);

	/*
	 * RESTFUL service for AccountZone
	 */
	@POST
	@Path("/accounts/{aid}/zones")
	Response createAccountZone(@PathParam("aid") Long aId, AccountZoneResource accountZone);

	@GET
	@Path("/accounts/{aid}/zones")
	List<AccountZoneResource> searchAccountZone(@PathParam("aid") Long aId, @QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize);

	@GET
	@Path("/accounts/{aid}/zones/{zid}")
	AccountZoneResource getAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId);

	@PUT
	@Path("/accounts/{aid}/zones/{zid}")
	Response updateAccountZone(@PathParam("aid") Long aid, @PathParam("zid") Long zId, AccountResource account);

	@DELETE
	@Path("/accounts/{aid}/zones/{zid}")
	Response deleteAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId);

	/*
	 * RESTFUL service for AccountZoneAdministrationCredential
	 */
	@POST
	@Path("/accounts/{aid}/zones/{zid}/zacs")
	Response createAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId,
			AccountZoneAdministrationCredentialResource zac);

	@GET
	@Path("/accounts/{aid}/zones/{zid}/zacs")
	List<AccountZoneResource> searchAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId,
			@QueryParam("pageNumber") Integer pageNo, @QueryParam("pageSize") Integer pageSize);

	@GET
	@Path("/accounts/{aid}/zones/{zid}/zacs/{zcid}")
	AccountZoneResource getAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId,
			@PathParam("zcid") Long zcId);

	@PUT
	@Path("/accounts/{aid}/zones/{zid}/zacs/{zcid}")
	Response updateAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId, @PathParam("zcid") Long zcId,
			AccountZoneAdministrationCredentialResource zac);

	@DELETE
	@Path("/accounts/{aid}/zones/{zid}/zacs/{zcid}")
	Response deleteAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId, @PathParam("zcid") Long zcId);

}
