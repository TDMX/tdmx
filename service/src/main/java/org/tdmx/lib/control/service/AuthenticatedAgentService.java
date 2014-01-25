package org.tdmx.lib.control.service;

/**
 * Register/Clear/Get the AuthenticatedAgent with the current Thread.
 * 
 * Works together with the {@link AuthenticatedAgentLookupService} to provide the current
 * Threadlocal authenticated agent.
 * 
 * @author Peter
 *
 */
public interface AuthenticatedAgentService {

	/**
	 * Get the AuthenticatedAgent associated with the current Thread.
	 * 
	 * @return
	 */
	public Object getAuthenticatedAgent();
	
	/**
	 * Set the AuthenticatedAgent associated with the current Thread.
	 * 
	 * Setting the AuthenticatedAgent without it being cleared first for the thread calling
	 * will issue a warning.
	 * 
	 * @param agent
	 */
	public void setAuthenticatedAgent( Object agent );
	
	/**
	 * Clear the AuthenticatedAgent so that non is associated with the current thread.
	 */
	public void clearAuthenticatedAgent();
	
}
