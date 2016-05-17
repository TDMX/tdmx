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
import org.springframework.util.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.dao.ControlJobDao;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobSearchCriteria;
import org.tdmx.lib.control.domain.ControlJobStatus;

/**
 * Transactional CRUD Services for ControlJobEntry Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class ControlJobServiceRepositoryImpl implements ControlJobService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ControlJobServiceRepositoryImpl.class);

	private ControlJobDao controlJobDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(ControlJob job) {
		if (job == null) {
			throw new IllegalArgumentException("missing job entry");
		}
		if (job.getJob() == null) {
			throw new IllegalArgumentException("missing job");
		}
		if (!StringUtils.hasText(job.getJob().getJobId())) {
			throw new IllegalArgumentException("missing jobId");
		}

		if (job.getId() != null) {
			ControlJob storedAccount = getControlJobDao().loadById(job.getId());
			if (storedAccount != null) {
				getControlJobDao().merge(job);
			} else {
				log.warn("Unable to find ControlJob with id " + job.getId());
			}
		} else {
			getControlJobDao().persist(job);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(ControlJob job) {
		ControlJob storedJob = getControlJobDao().loadById(job.getId());
		if (storedJob != null) {
			getControlJobDao().delete(storedJob);
		} else {
			log.warn("Unable to find ControlJob to delete with id " + job.getId());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<ControlJob> search(ControlJobSearchCriteria criteria) {
		return getControlJobDao().fetch(criteria, LockModeType.NONE);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public ControlJob findByJobId(String jobId) {
		if (!StringUtils.hasText(jobId)) {
			throw new IllegalArgumentException("missing jobId");
		}
		ControlJobSearchCriteria sc = new ControlJobSearchCriteria(new PageSpecifier(0, 1));
		sc.setJobId(jobId);
		List<ControlJob> jobs = getControlJobDao().fetch(sc, LockModeType.NONE);
		return jobs.isEmpty() ? null : jobs.get(0);
	}

	@Override
	@Transactional(value = "ControlDB")
	public List<ControlJob> reserve(int maxJobs) {
		ControlJobSearchCriteria sc = new ControlJobSearchCriteria(new PageSpecifier(0, maxJobs));
		sc.setStatus(ControlJobStatus.NEW);
		sc.setScheduledTimeBefore(new Date());
		List<ControlJob> result = getControlJobDao().fetch(sc, LockModeType.PESSIMISTIC_WRITE);
		for (ControlJob e : result) {
			e.setStatus(ControlJobStatus.RUN);
		}
		// we rely on the transaction finishing on exit and persisting the data without us calling dao explicitly
		// later the caller shall call createOrUpdate to persist further job changes after running the job.
		return result;
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public ControlJob findById(Long id) {
		return getControlJobDao().loadById(id);
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

	public ControlJobDao getControlJobDao() {
		return controlJobDao;
	}

	public void setControlJobDao(ControlJobDao controlJobDao) {
		this.controlJobDao = controlJobDao;
	}

}
