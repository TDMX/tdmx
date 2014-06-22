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
package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.Date;

public class Job implements Serializable {

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The name of the BackgroundJob.
	 */
	private final String name;
	/**
	 * The future date at which the Job will run, or null if there is no Job future scheduled.
	 */
	private final Date pendingDate;

	/**
	 * The time at which the current running Job started.
	 */
	private final Date runningDate;

	/**
	 * The date at which the Job last completed.
	 */
	private final Date lastCompletedDate;

	/**
	 * The number of executions of the Job.
	 */
	private final int executions;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public Job(org.tdmx.console.application.job.BackgroundJob j) {
		this.name = j.getName();
		this.pendingDate = j.getPendingDate();
		this.runningDate = j.getRunningDate();
		this.lastCompletedDate = j.getLastCompletedDate();
		this.executions = j.getExecutions();
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public Date getPendingDate() {
		return pendingDate;
	}

	public Date getRunningDate() {
		return runningDate;
	}

	public Date getLastCompletedDate() {
		return lastCompletedDate;
	}

	public int getExecutions() {
		return executions;
	}

}
