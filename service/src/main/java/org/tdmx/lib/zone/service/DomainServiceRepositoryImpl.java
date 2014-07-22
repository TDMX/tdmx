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
import org.tdmx.lib.zone.dao.DomainDao;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainID;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;

/**
 * Transactional CRUD Services for Domain Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class DomainServiceRepositoryImpl implements DomainService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DomainServiceRepositoryImpl.class);

	private DomainDao domainDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ZoneDB")
	public void createOrUpdate(Domain domain) {
		Domain storedDomain = getDomainDao().loadById(domain.getId());
		if (storedDomain == null) {
			getDomainDao().persist(domain);
		} else {
			getDomainDao().merge(domain);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(Domain domain) {
		Domain storedDomain = getDomainDao().loadById(domain.getId());
		if (storedDomain != null) {
			getDomainDao().delete(storedDomain);
		} else {
			log.warn("Unable to find Domain to delete with domainName " + domain.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<Domain> search(String zoneApex, DomainSearchCriteria criteria) {
		return getDomainDao().search(zoneApex, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public Domain findById(DomainID domainId) {
		return getDomainDao().loadById(domainId);
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

	public DomainDao getDomainDao() {
		return domainDao;
	}

	public void setDomainDao(DomainDao domainDao) {
		this.domainDao = domainDao;
	}

}
