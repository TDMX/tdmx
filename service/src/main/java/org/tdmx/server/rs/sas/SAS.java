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

	// TODO configuration value (admin all, user - some)

	// TODO maxvalue (admin only)

	// TODO control job (admin only)

	// TODO databasepartition (admin only)

	// TODO lock (admin only)

	/*
	 * RESTFUL service for Account
	 */
	@POST
	@Path("/accounts")
	AccountResource createAccount(AccountResource account);

	@GET
	@Path("/accounts")
	List<AccountResource> searchAccount(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize, @QueryParam("email") String email);

	@GET
	@Path("/accounts/{aid}")
	AccountResource getAccount(@PathParam("aid") Long aId);

	@PUT
	@Path("/accounts/{aid}")
	AccountResource updateAccount(AccountResource account);

	@DELETE
	@Path("/accounts/{aid}")
	Response deleteAccount(@PathParam("aid") Long aId);

	/*
	 * RESTFUL service for AccountZone
	 */
	@POST
	@Path("/accounts/{aid}/zones")
	AccountZoneResource createAccountZone(@PathParam("aid") Long aId, AccountZoneResource accountZone);

	@GET
	@Path("/accounts/{aid}/zones")
	List<AccountZoneResource> searchAccountZone(@PathParam("aid") Long aId, @QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize);

	@GET
	@Path("/accounts/{aid}/zones/{zid}")
	AccountZoneResource getAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId);

	@PUT
	@Path("/accounts/{aid}/zones/{zid}")
	AccountZoneResource updateAccountZone(@PathParam("aid") Long aid, @PathParam("zid") Long zId,
			AccountZoneResource account);

	@DELETE
	@Path("/accounts/{aid}/zones/{zid}")
	Response deleteAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId);

	/*
	 * RESTFUL service for AccountZoneAdministrationCredential
	 */
	@POST
	@Path("/accounts/{aid}/zones/{zid}/zacs")
	AccountZoneAdministrationCredentialResource createAccountZoneAdministrationCredential(@PathParam("aid") Long aId,
			@PathParam("zid") Long zId, AccountZoneAdministrationCredentialResource zac);

	@GET
	@Path("/accounts/{aid}/zones/{zid}/zacs")
	List<AccountZoneAdministrationCredentialResource> searchAccountZoneAdministrationCredential(
			@PathParam("aid") Long aId, @PathParam("zid") Long zId, @QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize);

	@GET
	@Path("/accounts/{aid}/zones/{zid}/zacs/{zcid}")
	AccountZoneAdministrationCredentialResource getAccountZoneAdministrationCredential(@PathParam("aid") Long aId,
			@PathParam("zid") Long zId, @PathParam("zcid") Long zcId);

	@PUT
	@Path("/accounts/{aid}/zones/{zid}/zacs/{zcid}")
	AccountZoneAdministrationCredentialResource updateAccountZoneAdministrationCredential(@PathParam("aid") Long aId,
			@PathParam("zid") Long zId, @PathParam("zcid") Long zcId, AccountZoneAdministrationCredentialResource zac);

	@DELETE
	@Path("/accounts/{aid}/zones/{zid}/zacs/{zcid}")
	Response deleteAccountZoneAdministrationCredential(@PathParam("aid") Long aId, @PathParam("zid") Long zId,
			@PathParam("zcid") Long zcId);

}
