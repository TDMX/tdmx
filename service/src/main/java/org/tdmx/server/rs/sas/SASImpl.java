package org.tdmx.server.rs.sas;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

public class SASImpl implements SAS {

	@Override
	public Response createAccount(AccountResource account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AccountResource> searchAccount(Integer pageNo, Integer pageSize) {
		// TODO
		List<AccountResource> response = new ArrayList<>();
		AccountResource a1 = new AccountResource();
		a1.setEmail("pjklauser@gmail.com");
		a1.setId(123l);
		response.add(a1);

		AccountResource a2 = new AccountResource();
		a2.setEmail("ha@gmail.com");
		a2.setId(345l);
		response.add(a2);

		return response;
	}

	@Override
	public AccountResource getAccount(Long aId) {
		AccountResource a = new AccountResource();
		a.setEmail("pjklauser@gmail.com");
		a.setId(123l);
		// TODO
		return a;
	}

	@Override
	public Response updateAccount(AccountResource account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteAccount(Long aId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response createAccountZone(Long aId, AccountZoneResource accountZone) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AccountZoneResource> searchAccountZone(Long aId, Integer pageNo, Integer pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccountZoneResource getAccountZone(Long aId, Long zId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response updateAccountZone(Long aid, Long zId, AccountResource account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteAccountZone(Long aId, Long zId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response createAccountZone(Long aId, Long zId, AccountZoneAdministrationCredentialResource zac) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AccountZoneResource> searchAccountZone(Long aId, Long zId, Integer pageNo, Integer pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccountZoneResource getAccountZone(Long aId, Long zId, Long zcId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response updateAccountZone(Long aId, Long zId, Long zcId, AccountZoneAdministrationCredentialResource zac) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteAccountZone(Long aId, Long zId, Long zcId) {
		// TODO Auto-generated method stub
		return null;
	}

}
