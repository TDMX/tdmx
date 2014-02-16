/**
 *   Copyright 2010 Peter Klauser
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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
	@Transactional(value="ControlDB")
	public void createOrUpdate(AccountZone accountZone) {
		AccountZone storedZone = getAccountZoneDao().loadById(accountZone.getZoneApex());
		if ( storedZone == null ) {
			getAccountZoneDao().persist(accountZone);
		} else {
			getAccountZoneDao().merge(accountZone);
		}
	}


	@Override
	@Transactional(value="ControlDB")
	public void delete(AccountZone accountZone) {
		AccountZone storedZone = getAccountZoneDao().loadById(accountZone.getZoneApex());
		if ( storedZone != null ) {
			getAccountZoneDao().delete(storedZone);
		} else {
			log.warn("Unable to find AccountZone to delete with root " + accountZone.getZoneApex());
		}
	}

	@Override
	@Transactional(value="ControlDB",readOnly=true)
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
