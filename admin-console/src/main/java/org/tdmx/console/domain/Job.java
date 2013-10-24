package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.Date;

public class Job implements Serializable {
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	/**
	 * The name of the BackgroundJob.
	 */
	private String name;
	/**
	 * The future date at which the Job will run, or null if there is no Job future scheduled.
	 */
	private Date pendingDate;
	
	/**
	 * The time at which the current running Job started.
	 */
	private Date runningDate;
	
	/**
	 * The date at which the Job last completed.
	 */
	private Date lastCompletedDate;
	
	/**
	 * The number of executions of the Job.
	 */
	private int executions;

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public Job( org.tdmx.console.application.job.BackgroundJob j ) {
		this.name = j.getName();
		this.pendingDate = j.getPendingDate();
		this.runningDate = j.getRunningDate();
		this.lastCompletedDate = j.getLastCompletedDate();
		this.executions = j.getExecutions();
	}

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

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
