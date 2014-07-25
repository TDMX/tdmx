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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.ws.security.service.AgentCredentialAuthorizationService.AuthorizationResult;

/**
 * AuthenticatedAgentLookupService holds thread bound information about the logged in authorized Agent.
 * 
 * @author Peter Klauser
 * 
 */
public class AuthenticatedAgentServiceImpl implements AuthenticatedAgentService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(AuthenticatedAgentServiceImpl.class);

	private final ThreadLocal<AuthorizationResult> authStore = new ThreadLocal<AuthorizationResult>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public PKIXCertificate getAuthenticatedAgent() {
		AuthorizationResult r = authStore.get();
		return r != null ? r.getPublicCertificate() : null;
	}

	@Override
	public void setAuthenticatedAgent(AuthorizationResult authorization) {
		if (authStore.get() != null) {
			log.warn("SECURITY WARNING: ThreadLocal not cleared when being set." + authorization);
			clearAuthenticatedAgent();
		}
		if (authorization.getFailureCode() != null) {
			log.error("Illegal to setAuthenticatedAgent with AuthorizationResult with failurecode.");
			throw new IllegalStateException();
		}
		authStore.set(authorization);
	}

	@Override
	public String getZoneDbPartitionId() {
		AuthorizationResult r = authStore.get();
		return r != null ? r.getAccountZone().getZonePartitionId() : null;
	}

	@Override
	public void clearAuthenticatedAgent() {
		authStore.remove();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
