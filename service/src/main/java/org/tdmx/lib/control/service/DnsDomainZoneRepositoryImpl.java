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
import org.tdmx.lib.control.dao.DnsDomainZoneDao;
import org.tdmx.lib.control.domain.DnsDomainZone;

/**
 * A transactional service managing the DnsDomainZone information.
 * 
 * @author Peter Klauser
 * 
 */
public class DnsDomainZoneRepositoryImpl implements DnsDomainZoneService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DnsDomainZoneRepositoryImpl.class);

	private DnsDomainZoneDao dnsDomainZoneDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(DnsDomainZone dnsDomainZone) {
		if (dnsDomainZone.getId() != null) {
			DnsDomainZone storedDnsDomainZone = getDnsDomainZoneDao().loadById(dnsDomainZone.getId());
			if (storedDnsDomainZone != null) {
				getDnsDomainZoneDao().merge(dnsDomainZone);
			} else {
				log.warn("Unable to find DnsDomainZone with id " + dnsDomainZone.getId());
			}
		} else {
			getDnsDomainZoneDao().persist(dnsDomainZone);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(DnsDomainZone dnsDomainZone) {
		DnsDomainZone storedDnsDomainZone = getDnsDomainZoneDao().loadById(dnsDomainZone.getId());
		if (storedDnsDomainZone != null) {
			getDnsDomainZoneDao().delete(storedDnsDomainZone);
		} else {
			log.warn("Unable to find DnsDomainZone to delete with id " + dnsDomainZone.getId());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public DnsDomainZone findCurrentByDomain(String domainName) {
		return getDnsDomainZoneDao().loadByCurrentDomain(domainName);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<DnsDomainZone> findByDomain(String domainName) {
		return getDnsDomainZoneDao().loadByDomain(domainName);
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

	public DnsDomainZoneDao getDnsDomainZoneDao() {
		return dnsDomainZoneDao;
	}

	public void setDnsDomainZoneDao(DnsDomainZoneDao dnsDomainZoneDao) {
		this.dnsDomainZoneDao = dnsDomainZoneDao;
	}

}
