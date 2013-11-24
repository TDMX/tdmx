package org.tdmx.console.application.job;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.tdmx.console.application.service.ProblemRegistry;

public abstract class AbstractBackgroundJob implements BackgroundJobSPI {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	protected String name;
	protected ProblemRegistry problemRegistry;
	protected AtomicInteger processingId = new AtomicInteger(0);
	protected Date lastCompletedDate;
	protected Date startedRunningDate;
	

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	/**
	 * Initialize the BackgroundJob. 
	 */
	@Override
	public abstract void init();
	
	/**
	 * Shutdown the BackgroundJob.
	 */
	@Override
	public abstract void shutdown();
	
	
	@Override
	public int getExecutions() {
		return processingId.get();
	}

	@Override
	public Date getRunningDate() {
		return startedRunningDate;
	}

	@Override
	public Date getLastCompletedDate() {
		return lastCompletedDate;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractBackgroundJob other = (AbstractBackgroundJob) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
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

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProblemRegistry getProblemRegistry() {
		return problemRegistry;
	}

	public void setProblemRegistry(ProblemRegistry problemRegistry) {
		this.problemRegistry = problemRegistry;
	}

}
