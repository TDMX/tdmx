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

package org.tdmx.lib.control.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.control.dao.AccountZoneAdministrationCredentialDao;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialSearchCriteria;

/**
 * Transactional CRUD Services for AccountZoneAdministrationCredential Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class AccountZoneAdministrationCredentialServiceRepositoryImpl implements
		AccountZoneAdministrationCredentialService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory
			.getLogger(AccountZoneAdministrationCredentialServiceRepositoryImpl.class);

	private AccountZoneAdministrationCredentialDao accountCredentialDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(AccountZoneAdministrationCredential accountCredential) {
		AccountZoneAdministrationCredential storedAccountCredential = getAccountCredentialDao().loadById(
				accountCredential.getId());
		if (storedAccountCredential == null) {
			getAccountCredentialDao().persist(accountCredential);
		} else {
			getAccountCredentialDao().merge(accountCredential);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(AccountZoneAdministrationCredential accountCredential) {
		AccountZoneAdministrationCredential storedAccountCredential = getAccountCredentialDao().loadById(
				accountCredential.getId());
		if (storedAccountCredential != null) {
			getAccountCredentialDao().delete(storedAccountCredential);
		} else {
			log.warn("Unable to find AccountZoneAdministrationCredential to delete with fingerprint "
					+ accountCredential.getId());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public AccountZoneAdministrationCredential findById(Long id) {
		return getAccountCredentialDao().loadById(id);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<AccountZoneAdministrationCredential> search(AccountZoneAdministrationCredentialSearchCriteria criteria) {
		return getAccountCredentialDao().search(criteria);
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

	public AccountZoneAdministrationCredentialDao getAccountCredentialDao() {
		return accountCredentialDao;
	}

	public void setAccountCredentialDao(AccountZoneAdministrationCredentialDao accountCredentialDao) {
		this.accountCredentialDao = accountCredentialDao;
	}

}
