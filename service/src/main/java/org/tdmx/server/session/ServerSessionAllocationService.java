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

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.Zone;

public interface ServerSessionAllocationService {

	/**
	 * Allocate a MRSSession on the least loaded MRS server serving the channel's account's segment. Use for first time
	 * authorizations.
	 * 
	 * @param az
	 * @param zone
	 * @param client
	 * @param tempChannel
	 * @return null if no capacity for new sessions.
	 */
	public WebServiceSessionEndpoint associateMRSSession(AccountZone az, Zone zone, PKIXCertificate client,
			TemporaryChannel tempChannel);

	/**
	 * Allocate a MRSSession on the least loaded MRS server serving the channel's account's segment.
	 * 
	 * @param az
	 * @param zone
	 * @param client
	 * @param channel
	 * @return null if no capacity for new sessions.
	 */
	public WebServiceSessionEndpoint associateMRSSession(AccountZone az, Zone zone, PKIXCertificate client,
			Channel channel);

	/**
	 * Allocate a MDSSession on the least loaded MDS server serving the account's segment.
	 * 
	 * @param az
	 * @param zone
	 * @param agent
	 * @param service
	 * @return null if no capacity for new sessions.
	 */
	public WebServiceSessionEndpoint associateMDSSession(AccountZone az, Zone zone, AgentCredential agent,
			Service service);

	/**
	 * Allocate a MOSSession on the least loaded MOS server serving the account's segment.
	 * 
	 * @param az
	 * @param zone
	 * @param agent
	 * @return null if no capacity for new sessions.
	 */
	public WebServiceSessionEndpoint associateMOSSession(AccountZone az, Zone zone, AgentCredential agent);

	/**
	 * Allocate a ZASSession on the least loaded ZAS server serving the account's segment.
	 * 
	 * @param az
	 * @param zone
	 * @param agent
	 * @return null if no capacity for new sessions.
	 */
	public WebServiceSessionEndpoint associateZASSession(AccountZone az, Zone zone, AgentCredential agent);
}
