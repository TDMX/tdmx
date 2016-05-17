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
import org.tdmx.lib.zone.dao.ServiceDao;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Transactional CRUD Services for Service Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class ServiceServiceRepositoryImpl implements ServiceService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ServiceServiceRepositoryImpl.class);

	private ServiceDao serviceDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ZoneDB")
	public void createOrUpdate(Service service) {
		if (service.getId() != null) {
			Service storedService = getServiceDao().loadById(service.getId());
			if (storedService != null) {
				getServiceDao().merge(service);
			} else {
				log.warn("Unable to find Service with id " + service.getId());
			}
		} else {
			getServiceDao().persist(service);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(Service service) {
		Service storedService = getServiceDao().loadById(service.getId());
		if (storedService != null) {
			getServiceDao().delete(storedService);
		} else {
			log.warn("Unable to find Service to delete with id " + service.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<Service> search(Zone zone, ServiceSearchCriteria criteria) {
		return getServiceDao().search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public Service findByName(Domain domain, String serviceName) {
		return getServiceDao().loadByName(domain, serviceName);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public Service findById(Long id) {
		return getServiceDao().loadById(id);
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

	public ServiceDao getServiceDao() {
		return serviceDao;
	}

	public void setServiceDao(ServiceDao serviceDao) {
		this.serviceDao = serviceDao;
	}

}
