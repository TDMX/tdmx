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

package org.tdmx.lib.zone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.zone.dao.ZoneDao;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Transactional CRUD Services for Zone Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class ZoneServiceRepositoryImpl implements ZoneService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ZoneServiceRepositoryImpl.class);

	private ZoneDao zoneDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ZoneDB")
	public void createOrUpdate(Zone zone) {
		if (zone.getId() != null) {
			Zone storedZone = getZoneDao().loadById(zone.getId());
			if (storedZone != null) {
				getZoneDao().merge(zone);
			} else {
				log.warn("Unable to find Zone with id " + zone.getId());
			}
		} else {
			getZoneDao().persist(zone);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(Zone zone) {
		Zone storedZone = getZoneDao().loadById(zone.getId());
		if (storedZone != null) {
			getZoneDao().delete(storedZone);
		} else {
			log.warn("Unable to find Zone to delete with id " + zone.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public Zone findByZoneApex(Long tenantId, String zoneApex) {
		return getZoneDao().loadByZoneApex(tenantId, zoneApex);
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

	public ZoneDao getZoneDao() {
		return zoneDao;
	}

	public void setZoneDao(ZoneDao zoneDao) {
		this.zoneDao = zoneDao;
	}

}
