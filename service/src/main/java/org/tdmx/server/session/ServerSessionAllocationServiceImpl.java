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
package org.tdmx.server.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.server.pcs.ControlService;
import org.tdmx.server.pcs.SessionHandle;

public class ServerSessionAllocationServiceImpl implements ServerSessionAllocationService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ServerSessionAllocationServiceImpl.class);

	private SessionHandleFactory handleFactory;
	private ControlService controlService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public WebServiceSessionEndpoint associateMDSSession(AccountZone az, AgentCredential agent, Service service) {
		SessionHandle handle = handleFactory.createMDSSessionHandle(az, agent, service);

		return controlService.associateApiSession(handle, agent.getPublicCertificate());
	}

	@Override
	public WebServiceSessionEndpoint associateMOSSession(AccountZone az, AgentCredential agent) {
		SessionHandle handle = handleFactory.createMOSSessionHandle(az, agent);

		return controlService.associateApiSession(handle, agent.getPublicCertificate());
	}

	@Override
	public WebServiceSessionEndpoint associateZASSession(AccountZone az, AgentCredential agent) {
		SessionHandle handle = handleFactory.createZASSessionHandle(az, agent);

		return controlService.associateApiSession(handle, agent.getPublicCertificate());
	}

	@Override
	public WebServiceSessionEndpoint associateMRSSession(AccountZone az, PKIXCertificate client,
			TemporaryChannel tempChannel) {
		SessionHandle handle = handleFactory.createMRSSessionHandle(az, client, tempChannel);

		return controlService.associateApiSession(handle, client);
	}

	@Override
	public WebServiceSessionEndpoint associateMRSSession(AccountZone az, PKIXCertificate client, Channel channel) {
		SessionHandle handle = handleFactory.createMRSSessionHandle(az, client, channel);

		return controlService.associateApiSession(handle, client);
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

	public SessionHandleFactory getHandleFactory() {
		return handleFactory;
	}

	public void setHandleFactory(SessionHandleFactory handleFactory) {
		this.handleFactory = handleFactory;
	}

}
