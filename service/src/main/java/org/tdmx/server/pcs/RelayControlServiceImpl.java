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
package org.tdmx.server.pcs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PCS implementation of the {@link RelayControlServiceListener}
 * 
 * @author Peter
 *
 */
public class RelayControlServiceImpl implements RelayControlServiceListener {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayControlServiceImpl.class);

	// internal

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void registerRelayServer(String rosTcpEndpoint, int sessionCapacity) {
		// TODO Auto-generated method stub
		log.info("registerRelayServer " + rosTcpEndpoint);

	}

	@Override
	public void unregisterRelayServer(String rosTcpEndpoint) {
		// TODO Auto-generated method stub
		log.info("unregisterRelayServer " + rosTcpEndpoint);

	}

	@Override
	public String assignRelayServer(String domain, String channelKey) {
		log.info("assignRelayServer " + domain + " " + channelKey);
		return null;
	}

	@Override
	public void notifyServerLoad(String rosTcpEndpoint, int currentLoad) {
		// TODO Auto-generated method stub
		log.info("notifyLoad " + rosTcpEndpoint + " is " + currentLoad);

	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}