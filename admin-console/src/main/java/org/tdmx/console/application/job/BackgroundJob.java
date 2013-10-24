package org.tdmx.console.application.job;

import java.util.Date;

public interface BackgroundJob {

	/**
	 * @return the name of the BackgroundJob.
	 */
	public String getName();
	
	/**
	 * Whether the Job is pending a future time before executing.
	 * @return the future date at which the Job will run, or null if there is no Job future scheduled.
	 */
	public Date getPendingDate();
	
	/**
	 * The time at which the current running Job started.
	 * @return the time at which the current running Job started, or null if no currently running Job.
	 */
	public Date getRunningDate();
	
	/**
	 * The date at which the Job last completed.
	 * @return the date at which the Job last completed, or null if the Job has never run.
	 */
	public Date getLastCompletedDate();
	
	/**
	 * @return the number of executions of the Job.
	 */
	public int getExecutions();
}
