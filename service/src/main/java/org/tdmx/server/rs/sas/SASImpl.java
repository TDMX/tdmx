package org.tdmx.server.rs.sas;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.domain.AccountSearchCriteria;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.service.AccountService;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.control.service.ObjectIdService;
import org.tdmx.server.rs.exception.ApplicationValidationError;
import org.tdmx.server.rs.exception.FieldValidationError;
import org.tdmx.server.rs.exception.FieldValidationError.FieldValidationErrorType;

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
	private AccountZoneService accountZoneService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public AccountResource createAccount(AccountResource account) {
		validatePresent("account", account);
		Account a = mapTo(account);
		validateNotPresent("id", a.getId());
		validateNotPresent("accountId", a.getAccountId());

		a.setId(getObjectIdService().getNextObjectId());
		a.setAccountId(UUID.randomUUID().toString());

		a.setEmail(account.getEmail());
		a.setFirstName(account.getFirstname());
		a.setLastName(account.getLastname());

		getAccountService().createOrUpdate(a);
		return mapTo(a);
	}

	@Override
	public List<AccountResource> searchAccount(Integer pageNo, Integer pageSize, String email) {
		AccountSearchCriteria sc = new AccountSearchCriteria(getPageSpecifier(pageNo, pageSize));
		sc.setEmail(email);
		List<Account> accounts = getAccountService().search(sc);

		List<AccountResource> result = new ArrayList<>();
		for (Account a : accounts) {
			result.add(mapTo(a));
		}
		return result;
	}

	@Override
	public AccountResource getAccount(Long aId) {
		validateObjectId("aId", aId);
		return mapTo(getAccountService().findById(aId));
	}

	@Override
	public Response updateAccount(AccountResource account) {
		validatePresent("account", account);
		Account a = mapTo(account);
		validateObjectId("id", a.getId());
		validatePresent("accountId", a.getAccountId());
		getAccountService().createOrUpdate(a);
		return Response.ok().build();
	}

	@Override
	public Response deleteAccount(Long aId) {
		validateObjectId("aId", aId);
		Account a = getAccountService().findById(aId);
		getAccountService().delete(a);
		return Response.ok().build();
	}

	@Override
	public AccountZoneResource createAccountZone(Long aId, AccountZoneResource accountZone) {
		validateObjectId("aId", aId);
		AccountZone az = mapTo(accountZone);

		// check that the account exists and accountId same
		Account a = getAccountService().findById(aId);
		validatePresent("account", a);
		validateEquals("accountId", a.getAccountId(), az.getAccountId());

		validatePresent("status", az.getStatus());
		// TODO zonepartitionId exists

		// TODO segment exists.
		getAccountZoneService().createOrUpdate(az);
		return mapTo(az);
	}

	@Override
	public List<AccountZoneResource> searchAccountZone(Long aId, Integer pageNo, Integer pageSize) {
		validateObjectId("aId", aId);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccountZoneResource getAccountZone(Long aId, Long zId) {
		validateObjectId("aId", aId);
		validateObjectId("zId", zId);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response updateAccountZone(Long aId, Long zId, AccountResource account) {
		validateObjectId("aId", aId);
		validateObjectId("zId", zId);
		// TODO Auto-generated method stub

		// change partitionId - store jobId

		return null;
	}

	@Override
	public Response deleteAccountZone(Long aId, Long zId) {
		validateObjectId("aId", aId);
		validateObjectId("zId", zId);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccountZoneAdministrationCredentialResource createAccountZoneAdministrationCredential(Long aId, Long zId,
			AccountZoneAdministrationCredentialResource zac) {
		validateObjectId("aId", aId);
		validateObjectId("zId", zId);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AccountZoneAdministrationCredentialResource> searchAccountZoneAdministrationCredential(Long aId,
			Long zId, Integer pageNo, Integer pageSize) {
		validateObjectId("aId", aId);
		validateObjectId("zId", zId);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccountZoneAdministrationCredentialResource getAccountZoneAdministrationCredential(Long aId, Long zId,
			Long zcId) {
		validateObjectId("aId", aId);
		validateObjectId("zId", zId);
		validateObjectId("zcId", zcId);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response updateAccountZoneAdministrationCredential(Long aId, Long zId, Long zcId,
			AccountZoneAdministrationCredentialResource zac) {
		validateObjectId("aId", aId);
		validateObjectId("zId", zId);
		validateObjectId("zcId", zcId);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteAccountZoneAdministrationCredential(Long aId, Long zId, Long zcId) {
		validateObjectId("aId", aId);
		validateObjectId("zId", zId);
		validateObjectId("zcId", zcId);

		// TODO Auto-generated method stub
		return null;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private PageSpecifier getPageSpecifier(Integer pageNo, Integer pageSize) {
		int pageNumber = pageNo != null ? pageNo : 0;
		int pageSz = pageSize != null ? pageSize : 10;
		return new PageSpecifier(pageNumber, pageSz);

	}

	private AccountZone mapTo(AccountZoneResource az) {
		if (az == null) {
			return null;
		}
		AccountZone a = new AccountZone();
		a.setId(az.getId());
		a.setAccountId(az.getAccountId());
		a.setZoneApex(az.getZoneApex());

		a.setSegment(az.getSegment());
		a.setZonePartitionId(az.getZonePartitionId());
		return a;
	}

	private AccountZoneResource mapTo(AccountZone az) {
		if (az == null) {
			return null;
		}
		AccountZoneResource a = new AccountZoneResource();
		a.setId(az.getId());
		a.setAccountId(az.getAccountId());
		a.setZoneApex(az.getZoneApex());

		a.setSegment(az.getSegment());
		a.setZonePartitionId(az.getZonePartitionId());
		return a;
	}

	private Account mapTo(AccountResource account) {
		if (account == null) {
			return null;
		}
		Account a = new Account();
		a.setId(account.getId());
		a.setAccountId(account.getAccountId());

		a.setEmail(account.getEmail());
		a.setFirstName(account.getFirstname());
		a.setLastName(account.getLastname());
		return a;
	}

	private AccountResource mapTo(Account account) {
		if (account == null) {
			return null;
		}
		AccountResource a = new AccountResource();
		a.setId(account.getId());
		a.setAccountId(account.getAccountId());

		a.setEmail(account.getEmail());
		a.setFirstname(account.getFirstName());
		a.setLastname(account.getLastName());
		return a;
	}

	private ValidationException createVE(FieldValidationErrorType type, String fieldName) {
		List<ApplicationValidationError> errors = new ArrayList<>();
		errors.add(new FieldValidationError(type, fieldName));
		ValidationException ve = new javax.ws.rs.ValidationException(Status.BAD_REQUEST, errors);
		return ve;
	}

	private void validateEquals(String fieldId, String expectedValue, String fieldValue) {
		if (StringUtils.hasText(expectedValue) && StringUtils.hasText(fieldValue)) {
			if (!expectedValue.equals(fieldValue)) {
				throw createVE(FieldValidationErrorType.PRESENT, fieldId);
			}
		} else if (StringUtils.hasText(expectedValue)) {
			throw createVE(FieldValidationErrorType.MISSING, fieldId);
		}
	}

	private void validateNotPresent(String fieldId, String fieldValue) {
		if (StringUtils.hasText(fieldValue)) {
			throw createVE(FieldValidationErrorType.PRESENT, fieldId);
		}
	}

	private void validateNotPresent(String fieldId, Object fieldValue) {
		if (fieldValue != null) {
			throw createVE(FieldValidationErrorType.PRESENT, fieldId);
		}
	}

	private void validatePresent(String fieldId, Object fieldValue) {
		if (fieldValue == null) {
			throw createVE(FieldValidationErrorType.MISSING, fieldId);
		}
	}

	private void validateObjectId(String fieldId, Long oId) {
		if (oId == null) {
			throw createVE(FieldValidationErrorType.MISSING, fieldId);
		}
		if (!getObjectIdService().isValid(oId)) {
			throw createVE(FieldValidationErrorType.INVALID, fieldId);
		}
	}

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

	public AccountZoneService getAccountZoneService() {
		return accountZoneService;
	}

	public void setAccountZoneService(AccountZoneService accountZoneService) {
		this.accountZoneService = accountZoneService;
	}

}
