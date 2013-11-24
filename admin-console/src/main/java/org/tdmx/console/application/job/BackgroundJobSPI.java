package org.tdmx.console.application.job;


public interface BackgroundJobSPI extends BackgroundJob {

	/**
	 * initialize the background job.
	 */
	public void init();
	
	/**
	 * shutdown the background job.
	 */
	public void shutdown();
}
