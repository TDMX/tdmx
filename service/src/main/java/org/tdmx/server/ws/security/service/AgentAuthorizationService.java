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

import java.security.cert.X509Certificate;

import org.tdmx.lib.control.domain.InterfaceName;

/**
 * AuthorizationService
 * 
 * The AuthorizationService must work in conjunction with the SecurityIncidentService to log any non successful
 * authorization attempts.//TODO
 * 
 * @author Peter
 * 
 */
public interface AgentAuthorizationService {

	public static enum AuthorizationFailureCode {
		UNKNOWN_AGENT, // The certificates provided are not recognized as a valid Agent.
		INVALID_API_USAGE_ATTEMPT, // The Agent is not allowed to access the API requested.
		AGENT_SUSPENDED, // The Agent is currently suspended.
		NON_WHITELISTED_IPADDRESS, // The Agent is connecting from a non whitelisted IP address.
	}

	/**
	 * Whether the Agent identified by the X509Certificate chain is authorized to use the interface API.
	 * 
	 * Agent credentials ( Users / DomainAdministrators / ZoneAdministrators ) may be suspended.
	 * 
	 * @param certChain
	 * @param api
	 * @return
	 */
	public AuthorizationFailureCode isAuthorized(X509Certificate[] certChain, InterfaceName api);

}
