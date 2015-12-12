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

/**
 * The PCS functionality regarding the RelayOutboundService.
 * 
 * The PCS manages to load balance channels on the available relay servers. The ROS periodically indicates it's load to
 * the PCS.
 * 
 * @author Peter
 *
 */
public interface RelayControlServiceListener {

	/**
	 * On the attachment of a RelayServer to the PCS.
	 * 
	 * @param rosTcpEndpoint
	 *            the attached relay outbound server's address.
	 * @param sessionCapacity
	 *            the session capacity (relay channels which can be concurrently processed).
	 */
	public void registerRelayServer(String rosTcpEndpoint, int sessionCapacity);

	/**
	 * On the detachment of a RelayServer, we disconnect all relay sessions.
	 * 
	 * @param rosTcpEndpoint
	 *            the attached relay outbound server's address.
	 */
	public void unregisterRelayServer(String rosTcpEndpoint);

	/**
	 * Determine the RelayServer to use for outbound relaying to a channel.
	 * 
	 * @param domain
	 *            the domain owning the channel.
	 * @param channelKey
	 *            the channel key.
	 * @return the RelayServer to use for outbound relaying to the channel.
	 */
	public String assignRelayServer(String domain, String channelKey);

	/**
	 * Set the RelayServer's current load.
	 * 
	 * @param rosTcpEndpoint
	 *            the attached relay outbound server's address.
	 * @param currentLoad
	 *            the current load (relay channels currently in process).
	 */
	public void notifyServerLoad(String rosTcpEndpoint, int currentLoad);
}
