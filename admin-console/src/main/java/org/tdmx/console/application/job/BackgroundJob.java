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
package org.tdmx.console.application.job;

import java.util.Date;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectType;
import org.tdmx.console.application.domain.ProblemDO;
import org.tdmx.console.application.search.FieldDescriptor;
import org.tdmx.console.application.search.FieldDescriptor.FieldType;

public interface BackgroundJob extends DomainObject {

	public static final class BackgroundJobSO {
		public static final FieldDescriptor NAME = new FieldDescriptor(DomainObjectType.BackgroundJob, "name",
				FieldType.String);
	}

	/**
	 * @return the name of the BackgroundJob.
	 */
	public String getName();

	/**
	 * Whether the Job is pending a future time before executing.
	 * 
	 * @return the future date at which the Job will run, or null if there is no Job future scheduled.
	 */
	public Date getPendingDate();

	/**
	 * The time at which the current running Job started.
	 * 
	 * @return the time at which the current running Job started, or null if no currently running Job.
	 */
	public Date getRunningDate();

	/**
	 * The date at which the Job last completed.
	 * 
	 * @return the date at which the Job last completed, or null if the Job has never run.
	 */
	public Date getLastCompletedDate();

	/**
	 * @return the number of executions of the Job.
	 */
	public int getExecutions();

	/**
	 * @return the last Problem encountered by the Job, or null if there has been no problem in the last run.
	 */
	public ProblemDO getLastProblem();

}
