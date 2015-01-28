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

import java.util.Date;
import java.util.List;

import javax.persistence.LockModeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.dao.ControlJobEntryDao;
import org.tdmx.lib.control.domain.ControlJobEntry;
import org.tdmx.lib.control.domain.ControlJobEntrySearchCriteria;
import org.tdmx.lib.control.domain.ControlJobEntryStatus;

/**
 * Transactional CRUD Services for ControlJobEntry Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class ControlJobEntryServiceRepositoryImpl implements ControlJobEntryService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ControlJobEntryServiceRepositoryImpl.class);

	private ControlJobEntryDao controlJobDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(ControlJobEntry job) {
		if (job == null) {
			throw new IllegalArgumentException("missing job entry");
		}
		if (job.getJob() == null) {
			throw new IllegalArgumentException("missing job");
		}
		ControlJobEntry storedAddress = getControlJobDao().loadById(job.getJobId());
		if (storedAddress == null) {
			getControlJobDao().persist(job);
		} else {
			getControlJobDao().merge(job);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(ControlJobEntry job) {
		ControlJobEntry storedJob = getControlJobDao().loadById(job.getJobId());
		if (storedJob != null) {
			getControlJobDao().delete(storedJob);
		} else {
			log.warn("Unable to find ControlJobEntry to delete with jobId " + job.getJobId());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<ControlJobEntry> search(ControlJobEntrySearchCriteria criteria) {
		return getControlJobDao().fetch(criteria, LockModeType.NONE);
	}

	@Override
	@Transactional(value = "ControlDB")
	public List<ControlJobEntry> reserve(int maxJobs) {
		ControlJobEntrySearchCriteria sc = new ControlJobEntrySearchCriteria(new PageSpecifier(0, maxJobs));
		sc.setStatus(ControlJobEntryStatus.NEW);
		sc.setScheduledTimeBefore(new Date());
		List<ControlJobEntry> result = getControlJobDao().fetch(sc, LockModeType.PESSIMISTIC_WRITE);
		for (ControlJobEntry e : result) {
			e.setStatus(ControlJobEntryStatus.RUN);
		}
		// we rely on the transaction finishing on exit and persisting the data without us calling dao explicitly
		// later the caller shall call createOrUpdate to persist further job changes after running the job.
		return result;
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public ControlJobEntry findById(String jobId) {
		return getControlJobDao().loadById(jobId);
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

	public ControlJobEntryDao getControlJobDao() {
		return controlJobDao;
	}

	public void setControlJobDao(ControlJobEntryDao controlJobDao) {
		this.controlJobDao = controlJobDao;
	}

}
