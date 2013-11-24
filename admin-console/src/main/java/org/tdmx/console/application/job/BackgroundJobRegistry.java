package org.tdmx.console.application.job;

import java.util.List;



public interface BackgroundJobRegistry {

	/**
	 * Get all the Jobs.
	 * @return
	 */
	public List<BackgroundJob> getJobs();
	
}
