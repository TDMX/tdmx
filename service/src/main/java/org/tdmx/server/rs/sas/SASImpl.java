/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
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
		Account a = AccountResource.mapTo(account);
		validateNotPresent("id", a.getId());
		validateNotPresent("accountId", a.getAccountId());

		a.setId(getObjectIdService().getNextObjectId());
		a.setAccountId(UUID.randomUUID().toString()); // TODO maxvalue "accountId"

		a.setEmail(account.getEmail());
		a.setFirstName(account.getFirstname());
		a.setLastName(account.getLastname());

		getAccountService().createOrUpdate(a);
		return AccountResource.mapTo(a);
	}

	@Override
	public List<AccountResource> searchAccount(Integer pageNo, Integer pageSize, String email) {
		AccountSearchCriteria sc = new AccountSearchCriteria(getPageSpecifier(pageNo, pageSize));
		sc.setEmail(email);
		List<Account> accounts = getAccountService().search(sc);

		List<AccountResource> result = new ArrayList<>();
		for (Account a : accounts) {
			result.add(AccountResource.mapTo(a));
		}
		return result;
	}

	@Override
	public AccountResource getAccount(Long aId) {
		validateObjectId("aId", aId);
		return AccountResource.mapTo(getAccountService().findById(aId));
	}

	@Override
	public AccountResource updateAccount(AccountResource account) {
		validatePresent("account", account);
		Account a = AccountResource.mapTo(account);
		validateObjectId("id", a.getId());
		validatePresent("accountId", a.getAccountId());
		getAccountService().createOrUpdate(a);
		return AccountResource.mapTo(a);
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
		AccountZone az = AccountZoneResource.mapTo(accountZone);

		// check that the account exists and accountId same
		Account a = getAccountService().findById(aId);
		validatePresent("account", a);
		validateEquals("accountId", a.getAccountId(), az.getAccountId());

		validatePresent("status", az.getStatus());
		validateNotPresent("id", az.getId());
		az.setId(getObjectIdService().getNextObjectId());

		// TODO segment value valid.
		validatePresent("segment", az.getSegment());
		validateNotPresent("zonePartitionId", az.getZonePartitionId());
		// TODO select zonepartitionId from service.
		az.setZonePartitionId("TODO");

		getAccountZoneService().createOrUpdate(az);

		return AccountZoneResource.mapTo(az);
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
	public AccountZoneResource updateAccountZone(Long aId, Long zId, AccountZoneResource account) {
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
	public AccountZoneAdministrationCredentialResource updateAccountZoneAdministrationCredential(Long aId, Long zId,
			Long zcId, AccountZoneAdministrationCredentialResource zac) {
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
