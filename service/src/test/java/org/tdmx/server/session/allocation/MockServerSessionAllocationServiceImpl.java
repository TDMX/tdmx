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
package org.tdmx.server.session.allocation;

import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.Service;

public class MockServerSessionAllocationServiceImpl implements ServerSessionAllocationService {

	private ServerSessionEndpoint endpoint;

	@Override
	public ServerSessionEndpoint associateMDSSession(AccountZone az, AgentCredential agent, Service service) {
		return endpoint;
	}

	@Override
	public ServerSessionEndpoint associateMOSSession(AccountZone az, AgentCredential agent) {
		return endpoint;
	}

	@Override
	public ServerSessionEndpoint associateZASSession(AccountZone az, AgentCredential agent) {
		return endpoint;
	}

	public ServerSessionEndpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(ServerSessionEndpoint endpoint) {
		this.endpoint = endpoint;
	}

}
