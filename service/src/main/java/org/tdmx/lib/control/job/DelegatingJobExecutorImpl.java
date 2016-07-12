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

import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobType;

/**
 * Delegates JobExecution to specific executor per {@link ControlJobType}
 * 
 * @author Peter Klauser
 * 
 */
public class DelegatingJobExecutorImpl implements JobExecutor {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private JobExecutor waitJobExecutor;
	private JobExecutor transferZoneJobExecutor;
	// TODO #89 others job executors

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public Date execute(ControlJob job) {
		switch (job.getType()) {
		case DELETE_ACCOUNT:
			// TODO #89 impl?
			break;
		case INVALIDATE_CACHE:
			// TODO #89 impl?
			break;
		case TRANSFER_ZONE:
			return transferZoneJobExecutor.execute(job);
		case WAIT:
			return waitJobExecutor.execute(job);
		default:
			break;
		}
		throw new IllegalArgumentException("Cannot run jobs with type " + job.getType());
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

	public JobExecutor getWaitJobExecutor() {
		return waitJobExecutor;
	}

	public void setWaitJobExecutor(JobExecutor waitJobExecutor) {
		this.waitJobExecutor = waitJobExecutor;
	}

	public JobExecutor getTransferZoneJobExecutor() {
		return transferZoneJobExecutor;
	}

	public void setTransferZoneJobExecutor(JobExecutor transferZoneJobExecutor) {
		this.transferZoneJobExecutor = transferZoneJobExecutor;
	}

}
