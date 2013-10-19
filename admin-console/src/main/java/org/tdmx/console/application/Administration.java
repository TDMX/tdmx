package org.tdmx.console.application;

import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ProblemRegistry;

public interface Administration {

	public ProblemRegistry getProblemRegistry();
	public ObjectRegistry getObjectRegistry();
	
	/**
	 * If 0 - then the application is Idle.
	 * If != 0 - then the application is processing / busy.
	 * 
	 * The idea is that we can tell the difference with the ID that processing is
	 * not just hanging - the ID stays the same.
	 * @return the current processingId if busy, else 0.
	 */
	public int getBusyId();
}
