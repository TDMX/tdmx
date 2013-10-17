package org.tdmx.console.application;

import org.tdmx.console.application.service.ProblemRegistry;

public interface Administration {

	public ProblemRegistry getProblemRegistry();
	public ObjectRegistry getObjectRegistry();
	
}
