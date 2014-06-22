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
package org.tdmx.server.ws.security;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.ws.security.service.AgentAuthorizationService.AuthorizationFailureCode;

public class AgentAuthenticationFilter implements Filter {

	private static Logger log = LoggerFactory.getLogger(AgentAuthenticationFilter.class);

	private static String CLIENT_CERTIFICATE = "javax.servlet.request.X509Certificate";
	private static Map<AuthorizationFailureCode, String> errorMessageMap = new HashMap<>();
	static {
		errorMessageMap.put(AuthorizationFailureCode.UNKNOWN_AGENT, "Unknown Agent.");
		errorMessageMap.put(AuthorizationFailureCode.AGENT_SUSPENDED, "Suspended.");
		errorMessageMap.put(AuthorizationFailureCode.INVALID_API_USAGE_ATTEMPT, "Wrong API usage attempt.");
		errorMessageMap.put(AuthorizationFailureCode.NON_WHITELISTED_IPADDRESS,
				"Connection from non whitelisted IP address.");
	}

	// TODO wire agentAuthorizationService

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("init");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		log.debug("doFilter");

		X509Certificate[] certs = (X509Certificate[]) request.getAttribute(CLIENT_CERTIFICATE);
		if (certs != null && certs.length > 0) {
			log.info("Client Cert: " + certs[0]);
		} else {
			log.info("No client cert.");
		}
		try {
			// TODO setAuthorizedAgent
			chain.doFilter(request, response);

		} finally {
			// TODO clearAUthorizedAgent
		}
	}

	@Override
	public void destroy() {
		log.debug("destroy");
	}

}
