package org.tdmx.server.rs.sas;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.service.AccountService;
import org.tdmx.lib.control.service.ObjectIdService;

public class SASImpl implements SAS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SASImpl.class);

	private ObjectIdService objectIdService;
	private AccountService accountService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public Response createAccount(AccountResource account) {
		Account a = new Account();
		a.setId(getObjectIdService().getNextObjectId());
		a.setAccountId(UUID.randomUUID().toString());

		a.setEmail(account.getEmail());
		a.setFirstName(account.getFirstname());
		a.setLastName(account.getLastname());
		return Response.ok().build();
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

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public ObjectIdService getObjectIdService() {
		return objectIdService;
	}

	public void setObjectIdService(ObjectIdService objectIdService) {
		this.objectIdService = objectIdService;
	}

	public AccountService getAccountService() {
		return accountService;
	}

	public void setAccountService(AccountService accountService) {
		this.accountService = accountService;
	}

}
