package org.tdmx.server.rs.sas;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

public class SASImpl implements SAS {

	@Override
	public List<AccountResource> searchAccount(Integer pageNo, Integer pageSize) {
		// TODO
		List<AccountResource> response = new ArrayList<>();
		AccountResource a1 = new AccountResource();
		a1.setEmail("pjklauser@gmail.com");
		a1.setId("123");
		response.add(a1);

		AccountResource a2 = new AccountResource();
		a2.setEmail("ha@gmail.com");
		a2.setId("345");
		response.add(a2);

		return response;
	}

	@Override
	public AccountResource getAccount(String id) {
		AccountResource a = new AccountResource();
		a.setEmail("pjklauser@gmail.com");
		a.setId("123");
		// TODO
		return a;
	}

	@Override
	public Response updateAccount(AccountResource account) {
		// TODO Auto-generated method stub
		return Response.ok().build();
	}

	@Override
	public Response createAccount(AccountResource account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteAccount(String id) {
		// TODO Auto-generated method stub
		return Response.ok().build();
	}

}
