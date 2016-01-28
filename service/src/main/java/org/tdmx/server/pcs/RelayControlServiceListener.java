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

import java.util.List;

/**
 * The PCS functionality regarding the RelayOutboundService.
 * 
 * The PCS manages to load balance channels on the available relay servers. The ROS periodically notifies the PCS of
 * idle sessions removed.
 * 
 * @author Peter
 *
 */
public interface RelayControlServiceListener {

	/**
	 * On the attachment of a RelayServer to the PCS. Called by ROS.
	 * 
	 * @param rosTcpEndpoint
	 *            the attached relay outbound server's address.
	 * @param segment
	 *            the segment of the ROS.
	 * @param ros
	 *            the reverse RPC api to the ROS (not available on ROS, only PCS)
	 */
	public void registerRelayServer(String rosTcpEndpoint, String segment, RelayOutboundServiceController ros);

	/**
	 * On the detachment of a RelayServer, we disconnect all relay sessions. Happens on disconnect of ROS.
	 * 
	 * @param rosTcpEndpoint
	 *            the attached relay outbound server's address.
	 */
	public void unregisterRelayServer(String rosTcpEndpoint);

	/**
	 * The RelayServer periodically notifies of sessions which have become idle and are removed from the server. Called
	 * by ROS.
	 * 
	 * @param rosTcpEndpoint
	 *            the attached relay outbound server's address.
	 * @param channelKeys
	 *            the sessions which have expired
	 */
	public void notifySessionsRemoved(String rosTcpEndpoint, List<String> channelKeys);
}
