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
import org.tdmx.lib.control.dao.AccountZoneDao;
import org.tdmx.lib.control.domain.AccountZone;

/**
 * @author Peter Klauser
 * 
 */
public class AccountZoneServiceRepositoryImpl implements AccountZoneService {

	private static Logger log = LoggerFactory.getLogger(AccountZoneServiceRepositoryImpl.class);

	private AccountZoneDao accountZoneDao;

	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(AccountZone accountZone) {
		AccountZone storedZone = getAccountZoneDao().loadById(accountZone.getZoneApex());
		if (storedZone == null) {
			getAccountZoneDao().persist(accountZone);
		} else {
			getAccountZoneDao().merge(accountZone);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(AccountZone accountZone) {
		AccountZone storedZone = getAccountZoneDao().loadById(accountZone.getZoneApex());
		if (storedZone != null) {
			getAccountZoneDao().delete(storedZone);
		} else {
			log.warn("Unable to find AccountZone to delete with root " + accountZone.getZoneApex());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public AccountZone findByZoneApex(String zoneApex) {
		AccountZone storedZone = getAccountZoneDao().loadById(zoneApex);
		return storedZone;
	}

	public AccountZoneDao getAccountZoneDao() {
		return accountZoneDao;
	}

	public void setAccountZoneDao(AccountZoneDao accountZoneDao) {
		this.accountZoneDao = accountZoneDao;
	}

}
