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

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.zone.domain.Zone;

/**
 * The Service to use to lookup the authenticated agent's certificate and zone DB partition information.
 * 
 * @author Peter
 * 
 */
public interface AuthenticatedAgentLookupService {

	/**
	 * Returns the authorized agent.
	 * 
	 * @return the authorized agent or null if there is none.
	 */
	public PKIXCertificate getAuthenticatedAgent();

	/**
	 * Returns the ZoneDB partition used by the authorized agent.
	 * 
	 * @return the ZoneDB partition used by the authorized agent or null if there is none.
	 */
	public String getZoneDbPartitionId();

	/**
	 * Return the Zone of the authorized agent.
	 * 
	 * @return the Zone of the authorized agent.
	 */
	public Zone getZone();

}
