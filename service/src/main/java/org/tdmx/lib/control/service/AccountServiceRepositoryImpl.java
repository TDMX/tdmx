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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.control.dao.AccountDao;
import org.tdmx.lib.control.domain.Account;

/**
 * Management of the Account via transactional services.
 * 
 * @author Peter Klauser
 * 
 */
public class AccountServiceRepositoryImpl implements AccountService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(AccountServiceRepositoryImpl.class);

	private AccountDao accountZoneDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(Account account) {
		Account storedAccount = getAccountDao().loadById(account.getAccountId());
		if (storedAccount == null) {
			getAccountDao().persist(account);
		} else {
			getAccountDao().merge(account);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(Account account) {
		Account storedAccount = getAccountDao().loadById(account.getAccountId());
		if (storedAccount != null) {
			getAccountDao().delete(storedAccount);
		} else {
			log.warn("Unable to find Account to delete with root " + account.getAccountId());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public Account findById(String id) {
		return getAccountDao().loadById(id);
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

	public AccountDao getAccountDao() {
		return accountZoneDao;
	}

	public void setAccountDao(AccountDao accountZoneDao) {
		this.accountZoneDao = accountZoneDao;
	}

}
