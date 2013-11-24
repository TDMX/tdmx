package org.tdmx.console.application.job;

import java.util.List;



public interface BackgroundJobRegistrySPI {

	
	/**
	 * Get all background jobs.
	 * @return
	 */
	public List<BackgroundJobSPI> getAllBackgroundJobs();
	
	/**
	 * Add a background job to the set of all jobs.
	 * @param job
	 */
	public void addBackgroundJob( BackgroundJobSPI job );

	/**
	 * Remove a background job to the set of all jobs.
	 * 
	 * The caller is responsible for shutting down the job.
	 * @param job
	 */
	public void removeBackgroundJob( BackgroundJobSPI job );
	
	/**
	 * Shutsdown all background jobs cleanly and clears the
	 * registry.
	 */
	public void shutdownAndClear();
}
