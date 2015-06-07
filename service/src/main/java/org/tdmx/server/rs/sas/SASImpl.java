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

import javax.ws.rs.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.Job;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.domain.AccountSearchCriteria;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneSearchCriteria;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.job.JobFactory;
import org.tdmx.lib.control.job.JobScheduler;
import org.tdmx.lib.control.service.AccountService;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.control.service.UniqueIdService;
import org.tdmx.lib.control.service.ZoneDatabasePartitionAllocationService;
import org.tdmx.server.rs.exception.ApplicationValidationError;
import org.tdmx.server.rs.exception.FieldValidationError;
import org.tdmx.server.rs.exception.FieldValidationError.FieldValidationErrorType;
import org.tdmx.service.control.task.dao.ZoneInstallTask;

public class SASImpl implements SAS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public enum PARAM {
		AID("aId"),
		ACCOUNT("account"),
		ZID("zId"),
		ZCID("zcId"), ;

		private PARAM(String n) {
			this.n = n;
		}

		private final String n;

		@Override
		public String toString() {
			return this.n;
		}

	}

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SASImpl.class);

	private UniqueIdService accountIdService;
	private AccountService accountService;
	private AccountZoneService accountZoneService;
	private ZoneDatabasePartitionAllocationService zonePartitionService;
	private JobFactory jobFactory;
	private JobScheduler jobScheduler;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public AccountResource createAccount(AccountResource account) {
		validatePresent(AccountResource.FIELD.ACCOUNTID, account);
		Account a = AccountResource.mapTo(account);
		validateNotPresent(AccountResource.FIELD.ID, a.getId());
		validateNotPresent(AccountResource.FIELD.ACCOUNTID, a.getAccountId());

		a.setAccountId(getAccountIdService().getNextId());
		a.setEmail(account.getEmail());
		a.setFirstName(account.getFirstname());
		a.setLastName(account.getLastname());

		log.info("Creating accountId " + a.getAccountId() + " for " + a.getEmail());
		getAccountService().createOrUpdate(a);

		// the ID is only created on commit of the createOrUpdate above
		Account storedAccount = getAccountService().findByAccountId(a.getAccountId());
		return AccountResource.mapTo(storedAccount);
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
		validatePresent(PARAM.AID, aId);
		return AccountResource.mapTo(getAccountService().findById(aId));
	}

	@Override
	public AccountResource updateAccount(AccountResource account) {
		validatePresent(PARAM.ACCOUNT, account);
		Account a = AccountResource.mapTo(account);
		validatePresent(AccountResource.FIELD.ID, a.getId());
		validatePresent(AccountResource.FIELD.ACCOUNTID, a.getAccountId());
		getAccountService().createOrUpdate(a);
		return AccountResource.mapTo(a);
	}

	@Override
	public Response deleteAccount(Long aId) {
		validatePresent(PARAM.AID, aId);
		Account a = getAccountService().findById(aId);
		getAccountService().delete(a);
		return Response.ok().build();
	}

	@Override
	public AccountZoneResource createAccountZone(Long aId, AccountZoneResource accountZone) {
		validatePresent(PARAM.AID, aId);
		AccountZone az = AccountZoneResource.mapTo(accountZone);

		// check that the account exists and accountId same
		Account a = getAccountService().findById(aId);
		validatePresent(PARAM.ACCOUNT, a);
		validateEquals(AccountZoneResource.FIELD.ACCOUNTID, a.getAccountId(), az.getAccountId());

		validatePresent(AccountZoneResource.FIELD.ZONEAPEX, az.getZoneApex());
		validatePresent(AccountZoneResource.FIELD.ACCESSSTATUS, az.getStatus());
		validateNotPresent(AccountZoneResource.FIELD.ID, az.getId());

		// TODO segment value valid.
		validatePresent(AccountZoneResource.FIELD.SEGMENT, az.getSegment());
		validateNotPresent(AccountZoneResource.FIELD.ZONEPARTITIONID, az.getZonePartitionId());

		String partitionId = getZonePartitionService().getZonePartitionId(a.getAccountId(), az.getZoneApex(),
				az.getSegment());
		az.setZonePartitionId(partitionId);
		validatePresent(AccountZoneResource.FIELD.ZONEPARTITIONID, az.getZonePartitionId());

		ZoneInstallTask installTask = new ZoneInstallTask();
		installTask.setAccountId(az.getAccountId());
		installTask.setZoneApex(az.getZoneApex());
		Job installJob = getJobFactory().createJob(installTask);

		log.info("Creating AccountZone " + az.getZoneApex() + " in ZoneDB partition " + az.getZonePartitionId());
		getAccountZoneService().createOrUpdate(az);

		// the ID is only created on commit of the createOrUpdate above
		AccountZone storedAccountZone = getAccountZoneService().findByZoneApex(az.getZoneApex());

		// schedule the job to install the Zone in the ZoneDB partition.
		ControlJob j = getJobScheduler().scheduleImmediate(installJob);

		storedAccountZone.setJobId(j.getId());

		getAccountZoneService().createOrUpdate(storedAccountZone);
		return AccountZoneResource.mapTo(storedAccountZone);
	}

	@Override
	public List<AccountZoneResource> searchAccountZone(Long aId, Integer pageNo, Integer pageSize) {
		validatePresent(PARAM.AID, aId);
		AccountZoneSearchCriteria sc = new AccountZoneSearchCriteria(getPageSpecifier(pageNo, pageSize));
		List<AccountZone> accountzones = getAccountZoneService().search(sc);

		List<AccountZoneResource> result = new ArrayList<>();
		for (AccountZone az : accountzones) {
			result.add(AccountZoneResource.mapTo(az));
		}
		return result;
	}

	@Override
	public AccountZoneResource getAccountZone(Long aId, Long zId) {
		validatePresent(PARAM.AID, aId);
		validatePresent(PARAM.ZID, zId);

		Account a = getAccountService().findById(aId);
		if (a != null) {
			AccountZone az = getAccountZoneService().findById(zId);
			if (az != null && a.getAccountId().equals(az.getAccountId())) {
				return AccountZoneResource.mapTo(az);
			}
		}
		return null;
	}

	@Override
	public AccountZoneResource updateAccountZone(Long aId, Long zId, AccountZoneResource account) {
		validatePresent(PARAM.AID, aId);
		validatePresent(PARAM.ZID, zId);
		// TODO Auto-generated method stub

		// change partitionId - store jobId

		return null;
	}

	@Override
	public Response deleteAccountZone(Long aId, Long zId) {
		validatePresent(PARAM.AID, aId);
		validatePresent(PARAM.ZID, zId);
		Account a = getAccountService().findById(aId);
		if (a != null) {
			AccountZone az = getAccountZoneService().findById(zId);
			if (az != null && a.getAccountId().equals(az.getAccountId())) {
				getAccountZoneService().delete(az);
			}
		}
		return Response.ok().build();
	}

	@Override
	public AccountZoneAdministrationCredentialResource createAccountZoneAdministrationCredential(Long aId, Long zId,
			AccountZoneAdministrationCredentialResource zac) {
		validatePresent(PARAM.AID, aId);
		validatePresent(PARAM.ZID, zId);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AccountZoneAdministrationCredentialResource> searchAccountZoneAdministrationCredential(Long aId,
			Long zId, Integer pageNo, Integer pageSize) {
		validatePresent(PARAM.AID, aId);
		validatePresent(PARAM.ZID, zId);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccountZoneAdministrationCredentialResource getAccountZoneAdministrationCredential(Long aId, Long zId,
			Long zcId) {
		validatePresent(PARAM.AID, aId);
		validatePresent(PARAM.ZID, zId);
		validatePresent(PARAM.ZCID, zcId);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccountZoneAdministrationCredentialResource updateAccountZoneAdministrationCredential(Long aId, Long zId,
			Long zcId, AccountZoneAdministrationCredentialResource zac) {
		validatePresent(PARAM.AID, aId);
		validatePresent(PARAM.ZID, zId);
		validatePresent(PARAM.ZCID, zcId);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteAccountZoneAdministrationCredential(Long aId, Long zId, Long zcId) {
		validatePresent(PARAM.AID, aId);
		validatePresent(PARAM.ZID, zId);
		validatePresent(PARAM.ZCID, zcId);

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
		return new javax.ws.rs.ValidationException(Status.BAD_REQUEST, errors);
	}

	private void validateEquals(Enum<?> fieldId, String expectedValue, String fieldValue) {
		if (StringUtils.hasText(expectedValue) && StringUtils.hasText(fieldValue)) {
			if (!expectedValue.equals(fieldValue)) {
				throw createVE(FieldValidationErrorType.PRESENT, fieldId.toString());
			}
		} else if (StringUtils.hasText(expectedValue)) {
			throw createVE(FieldValidationErrorType.MISSING, fieldId.toString());
		}
	}

	private void validateNotPresent(Enum<?> fieldId, String fieldValue) {
		if (StringUtils.hasText(fieldValue)) {
			throw createVE(FieldValidationErrorType.PRESENT, fieldId.toString());
		}
	}

	private void validateNotPresent(Enum<?> fieldId, Object fieldValue) {
		if (fieldValue != null) {
			throw createVE(FieldValidationErrorType.PRESENT, fieldId.toString());
		}
	}

	private void validatePresent(Enum<?> fieldId, Object fieldValue) {
		if (fieldValue == null) {
			throw createVE(FieldValidationErrorType.MISSING, fieldId.toString());
		}
	}

	private void validatePresent(Enum<?> fieldId, String fieldValue) {
		if (!StringUtils.hasText(fieldValue)) {
			throw createVE(FieldValidationErrorType.MISSING, fieldId.toString());
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public UniqueIdService getAccountIdService() {
		return accountIdService;
	}

	public void setAccountIdService(UniqueIdService accountIdService) {
		this.accountIdService = accountIdService;
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

	public ZoneDatabasePartitionAllocationService getZonePartitionService() {
		return zonePartitionService;
	}

	public void setZonePartitionService(ZoneDatabasePartitionAllocationService zonePartitionService) {
		this.zonePartitionService = zonePartitionService;
	}

	public JobFactory getJobFactory() {
		return jobFactory;
	}

	public void setJobFactory(JobFactory jobFactory) {
		this.jobFactory = jobFactory;
	}

	public JobScheduler getJobScheduler() {
		return jobScheduler;
	}

	public void setJobScheduler(JobScheduler jobScheduler) {
		this.jobScheduler = jobScheduler;
	}

}
