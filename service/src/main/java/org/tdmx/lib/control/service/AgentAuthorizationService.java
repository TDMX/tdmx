package org.tdmx.lib.control.service;

import java.security.cert.X509Certificate;

import org.tdmx.lib.control.domain.InterfaceName;

/**
 * AuthorizationService
 * 
 * The AuthorizationService must work in conjunction with the SecurityIncidentService to
 * log any non successful authorization attempts.//TODO
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
	 * Whether the Agent identified by the X509Certificate chain is authorized to use
	 * the interface API.
	 * 
	 * Agent credentials ( Users / DomainAdministrators / ZoneAdministrators ) may be suspended.
	 * 
	 * @param certChain
	 * @param api
	 * @return
	 */
	public AuthorizationFailureCode isAuthorized( X509Certificate[] certChain, InterfaceName api );
	
}
