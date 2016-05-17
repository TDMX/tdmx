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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.zone.dao.DestinationDao;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.Destination;
import org.tdmx.lib.zone.domain.DestinationSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Transactional CRUD Services for Destination Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class DestinationServiceRepositoryImpl implements DestinationService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DestinationServiceRepositoryImpl.class);

	private DestinationDao destinationDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ZoneDB")
	public void createOrUpdate(Destination target) {
		if (target.getId() != null) {
			Destination storedTarget = destinationDao.loadById(target.getId());
			if (storedTarget != null) {
				destinationDao.merge(target);
			} else {
				log.warn("Unable to find Destination with id " + target.getId());
			}
		} else {
			destinationDao.persist(target);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void setSession(Destination ft) {
		Destination flowTarget = findByDestination(ft.getTarget(), ft.getService());
		if (flowTarget == null) {
			createOrUpdate(ft);
			flowTarget = ft;
		} else {
			flowTarget.setDestinationSession(ft.getDestinationSession());
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(Destination target) {
		Destination storedTarget = destinationDao.loadById(target.getId());
		if (storedTarget != null) {
			destinationDao.delete(storedTarget);
		} else {
			log.warn("Unable to find Destination to delete with id " + target.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<Destination> search(Zone zone, DestinationSearchCriteria criteria) {
		return destinationDao.search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public Destination findByDestination(Address address, Service service) {
		return destinationDao.loadByDestination(address, service);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public Destination findById(Long id) {
		return destinationDao.loadById(id);
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

	public DestinationDao getDestinationDao() {
		return destinationDao;
	}

	public void setDestinationDao(DestinationDao destinationDao) {
		this.destinationDao = destinationDao;
	}

}
