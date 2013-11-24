package org.tdmx.console.application.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BackgroundJobRegistryImpl implements BackgroundJobRegistry, BackgroundJobRegistrySPI {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private List<BackgroundJobSPI> backgroundJobs = new ArrayList<>();
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public BackgroundJobRegistryImpl() {
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	@Override
	public List<BackgroundJob> getJobs() {
		List<BackgroundJob> result = new ArrayList<>();
		for( BackgroundJobSPI bj : backgroundJobs) {
			result.add(bj);
		}
		return result;
	}


	@Override
	public List<BackgroundJobSPI> getAllBackgroundJobs()  {
		return Collections.unmodifiableList(backgroundJobs);
	}

	@Override
	public void addBackgroundJob(BackgroundJobSPI job) {
		removeBackgroundJob(job); // just incase job with same name constructed
		backgroundJobs.add(job);
	}

	@Override
	public void removeBackgroundJob(BackgroundJobSPI job) {
		backgroundJobs.remove(job);
	}

	@Override
	public void shutdownAndClear() {
		for( BackgroundJobSPI j : backgroundJobs ) {
			j.shutdown();
		}
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

}
