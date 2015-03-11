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
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.dao.AccountZoneDao;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneSearchCriteria;

/**
 * Management of the AccountZone via transactional services.
 * 
 * @author Peter Klauser
 * 
 */
public class AccountZoneServiceRepositoryImpl implements AccountZoneService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(AccountZoneServiceRepositoryImpl.class);

	private AccountZoneDao accountZoneDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(AccountZone accountZone) {
		if (accountZone.getId() != null) {
			AccountZone storedAccount = getAccountZoneDao().loadById(accountZone.getId());
			if (storedAccount != null) {
				getAccountZoneDao().merge(accountZone);
			} else {
				log.warn("Unable to find AccountZone with id " + accountZone.getId());
			}
		} else {
			getAccountZoneDao().persist(accountZone);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(AccountZone accountZone) {
		AccountZone storedZone = getAccountZoneDao().loadById(accountZone.getId());
		if (storedZone != null) {
			getAccountZoneDao().delete(storedZone);
		} else {
			log.warn("Unable to find AccountZone to delete with id " + accountZone.getId());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public AccountZone findById(Long id) {
		return getAccountZoneDao().loadById(id);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public AccountZone findByAccountIdZoneApex(String accountId, String zoneApex) {
		if (!StringUtils.hasText(accountId)) {
			throw new IllegalArgumentException("missing accountId");
		}
		if (!StringUtils.hasText(zoneApex)) {
			throw new IllegalArgumentException("missing zoneApex");
		}
		AccountZoneSearchCriteria sc = new AccountZoneSearchCriteria(new PageSpecifier(0, 1));
		sc.setAccountId(accountId);
		sc.setZoneApex(zoneApex);
		List<AccountZone> accounts = getAccountZoneDao().search(sc);
		if (accounts.isEmpty()) {
			return null;
		}
		return accounts.get(0);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<AccountZone> search(AccountZoneSearchCriteria criteria) {
		return getAccountZoneDao().search(criteria);
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

	public AccountZoneDao getAccountZoneDao() {
		return accountZoneDao;
	}

	public void setAccountZoneDao(AccountZoneDao accountZoneDao) {
		this.accountZoneDao = accountZoneDao;
	}

}
