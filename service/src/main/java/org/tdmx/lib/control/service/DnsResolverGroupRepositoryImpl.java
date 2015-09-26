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
import org.tdmx.lib.control.dao.DnsResolverGroupDao;
import org.tdmx.lib.control.domain.DnsResolverGroup;

/**
 * A transactional service managing the DnsResolverGroup information.
 * 
 * @author Peter Klauser
 * 
 */
public class DnsResolverGroupRepositoryImpl implements DnsResolverGroupService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DnsResolverGroupRepositoryImpl.class);

	private DnsResolverGroupDao dnsResolverGroupDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(DnsResolverGroup dnsResolverGroup) {
		if (dnsResolverGroup.getId() != null) {
			DnsResolverGroup storedSegment = getDnsResolverGroupDao().loadById(dnsResolverGroup.getId());
			if (storedSegment != null) {
				getDnsResolverGroupDao().merge(dnsResolverGroup);
			} else {
				log.warn("Unable to find DnsResolverGroup with id " + dnsResolverGroup.getId());
			}
		} else {
			getDnsResolverGroupDao().persist(dnsResolverGroup);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(DnsResolverGroup dnsResolverGroup) {
		DnsResolverGroup storedSegment = getDnsResolverGroupDao().loadById(dnsResolverGroup.getId());
		if (storedSegment != null) {
			getDnsResolverGroupDao().delete(storedSegment);
		} else {
			log.warn("Unable to find DnsResolverGroup to delete with id " + dnsResolverGroup.getId());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public DnsResolverGroup findByName(String groupName) {
		return getDnsResolverGroupDao().loadByName(groupName);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<DnsResolverGroup> findAll() {
		return getDnsResolverGroupDao().loadAll();
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

	public DnsResolverGroupDao getDnsResolverGroupDao() {
		return dnsResolverGroupDao;
	}

	public void setDnsResolverGroupDao(DnsResolverGroupDao dnsResolverGroupDao) {
		this.dnsResolverGroupDao = dnsResolverGroupDao;
	}

}
