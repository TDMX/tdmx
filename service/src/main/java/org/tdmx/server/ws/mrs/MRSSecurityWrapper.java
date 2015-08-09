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
package org.tdmx.server.ws.mrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.mrs.Relay;
import org.tdmx.core.api.v01.mrs.RelayResponse;
import org.tdmx.core.api.v01.mrs.ws.MRS;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.server.ws.security.ServerSecurityManager;
import org.tdmx.server.ws.security.service.AuthorizedSessionService;

public class MRSSecurityWrapper implements MRS {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MRSSecurityWrapper.class);

	private ServerSecurityManager<MRSServerSession> securityManager;
	private AuthorizedSessionService<MRSServerSession> authorizationService;
	private ThreadLocalPartitionIdProvider partitionIdService;

	private MRS delegate;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public RelayResponse relay(Relay parameters) {
		MRSServerSession session = securityManager.getSession(parameters.getSessionId());
		authorizationService.setAuthorizedSession(session);
		try {
			AccountZone az = session.getAccountZone();
			partitionIdService.setPartitionId(az.getZonePartitionId());

			return delegate.relay(parameters);

		} finally {
			getAuthorizationService().clearAuthorizedSession();
			getPartitionIdService().clearPartitionId();
		}

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

	public ServerSecurityManager<MRSServerSession> getSecurityManager() {
		return securityManager;
	}

	public void setSecurityManager(ServerSecurityManager<MRSServerSession> securityManager) {
		this.securityManager = securityManager;
	}

	public AuthorizedSessionService<MRSServerSession> getAuthorizationService() {
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizedSessionService<MRSServerSession> authorizationService) {
		this.authorizationService = authorizationService;
	}

	public ThreadLocalPartitionIdProvider getPartitionIdService() {
		return partitionIdService;
	}

	public void setPartitionIdService(ThreadLocalPartitionIdProvider partitionIdService) {
		this.partitionIdService = partitionIdService;
	}

	public MRS getDelegate() {
		return delegate;
	}

	public void setDelegate(MRS delegate) {
		this.delegate = delegate;
	}
}
