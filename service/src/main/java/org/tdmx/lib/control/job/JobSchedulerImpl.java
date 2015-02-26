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

package org.tdmx.lib.control.job;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.common.domain.Job;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobStatus;
import org.tdmx.lib.control.service.ControlJobService;

/**
 * Non transaction implementation of a JobScheduler.
 * 
 * @author Peter Klauser
 * 
 */
public class JobSchedulerImpl implements JobScheduler {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(JobSchedulerImpl.class);

	private ControlJobService controlJobService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public ControlJob scheduleImmediate(Job task) {
		log.info("Scheduling task " + task);

		ControlJob j = new ControlJob();
		j.setJob(task);
		j.setScheduledTime(new Date());
		j.setStatus(ControlJobStatus.NEW);
		getControlJobService().createOrUpdate(j);

		// lookup after transaction to fetch the ID.
		j = getControlJobService().findByJobId(task.getJobId());
		return j;
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

	public ControlJobService getControlJobService() {
		return controlJobService;
	}

	public void setControlJobService(ControlJobService controlJobService) {
		this.controlJobService = controlJobService;
	}

}
