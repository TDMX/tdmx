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

	@GET
	@Path("/accounts")
	List<AccountResource> searchAccount(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize);

	@GET
	@Path("/accounts/{id}")
	AccountResource getAccount(@PathParam("id") String id);

	@PUT
	@Path("/accounts/{id}")
	Response updateAccount(AccountResource account);

	@POST
	@Path("/accounts")
	Response createAccount(AccountResource account);

	@DELETE
	@Path("/accounts/{id}")
	Response deleteAccount(@PathParam("id") String id);

}
