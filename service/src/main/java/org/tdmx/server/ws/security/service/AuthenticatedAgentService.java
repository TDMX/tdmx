/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.server.ws.security.service;

/**
 * Register/Clear/Get the AuthenticatedAgent with the current Thread.
 * 
 * Works together with the {@link AuthenticatedAgentLookupService} to provide the current Threadlocal authenticated
 * agent.
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
	 * Setting the AuthenticatedAgent without it being cleared first for the thread calling will issue a warning.
	 * 
	 * @param agent
	 */
	public void setAuthenticatedAgent(Object agent);

	/**
	 * Clear the AuthenticatedAgent so that non is associated with the current thread.
	 */
	public void clearAuthenticatedAgent();

}
