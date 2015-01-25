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

package org.tdmx.lib.zone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.datasource.PartitionIdProvider;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.server.ws.security.service.AuthenticatedAgentService;

/**
 * The PartitionIdProvider for the Zone-DB
 * 
 * @author Peter Klauser
 * 
 */
public class ZonePartitionIdProviderImpl implements PartitionIdProvider {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ZonePartitionIdProviderImpl.class);

	private AuthenticatedAgentService authenticatedAgentService;

	// if a thread stipulates the partition explicitly then this is taken overriding the
	// authenticated
	private ThreadLocalPartitionIdProvider threadLocalProvider;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	public String getPartitionId() {
		String partitionId = null;
		if (getThreadLocalProvider() != null) {
			partitionId = getThreadLocalProvider().getPartitionId();
		}
		if (partitionId == null && getAuthenticatedAgentService() != null) {
			partitionId = getAuthenticatedAgentService().getZoneDbPartitionId();
		}
		return partitionId;
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

	public AuthenticatedAgentService getAuthenticatedAgentService() {
		return authenticatedAgentService;
	}

	public void setAuthenticatedAgentService(AuthenticatedAgentService authenticatedAgentService) {
		this.authenticatedAgentService = authenticatedAgentService;
	}

	public ThreadLocalPartitionIdProvider getThreadLocalProvider() {
		return threadLocalProvider;
	}

	public void setThreadLocalProvider(ThreadLocalPartitionIdProvider threadLocalProvider) {
		this.threadLocalProvider = threadLocalProvider;
	}
}
