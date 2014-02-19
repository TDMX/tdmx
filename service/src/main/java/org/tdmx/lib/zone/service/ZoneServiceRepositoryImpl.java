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
package org.tdmx.lib.zone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.zone.dao.ZoneDao;
import org.tdmx.lib.zone.domain.Zone;

/**
 * @author Peter Klauser
 *
 */
public class ZoneServiceRepositoryImpl implements ZoneService {

	private static Logger log = LoggerFactory.getLogger(ZoneServiceRepositoryImpl.class);
	 
	private ZoneDao zoneDao;
	
	@Override
	@Transactional(value="ZoneDB")
	public void createOrUpdate(Zone zone) {
		Zone storedZone = getZoneDao().loadById(zone.getZoneApex());
		if ( storedZone == null ) {
			getZoneDao().persist(zone);
		} else {
			getZoneDao().merge(zone);
		}
	}


	@Override
	@Transactional(value="ZoneDB")
	public void delete(Zone zone) {
		Zone storedZone = getZoneDao().loadById(zone.getZoneApex());
		if ( storedZone != null ) {
			getZoneDao().delete(storedZone);
		} else {
			log.warn("Unable to find Zone to delete with root " + zone.getZoneApex());
		}
	}

	@Override
	@Transactional(value="ZoneDB",readOnly=true)
	public Zone findByZoneApex(String zoneApex) {
		Zone storedZone = getZoneDao().loadById(zoneApex);
		return storedZone;
	}



	
	public ZoneDao getZoneDao() {
		return zoneDao;
	}

	public void setZoneDao(ZoneDao zoneDao) {
		this.zoneDao = zoneDao;
	}


}
