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

import org.tdmx.server.pcs.protobuf.Broadcast.Channel;
import org.tdmx.server.pcs.protobuf.Broadcast.RelayMessage;

import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

/**
 * The PCS functionality regarding the RelayOutboundService.
 * 
 * The WS clients relay messages to the PCS. The RelayOutboundServers register themselves with the PCS and receive the
 * relayed messages. The PCS manages to load balance channels on the available relay servers. If a channel relay has
 * stopped / completed and is idle for some time, the relay server will notify the PCS and stash the used MRS session
 * ID. The ROS periodically indicates it's load to the PCS.
 * 
 * @author Peter
 *
 */
public interface RelayControlServiceListener {

	/**
	 * On the attachment of a RelayServer to the PCS.
	 * 
	 * @param ros
	 *            the attached relay outbound server
	 */
	public void registerRelayServer(RpcClientChannel ros);

	/**
	 * On the detachment of a RelayServer, we disconnect all relay sessions.
	 * 
	 * @param ros
	 *            the relay outbound server which has detached.
	 */
	public void unregisterRelayServer(RpcClientChannel ros);

	/**
	 * Notify a relay session is idle.
	 * 
	 * @param ros
	 *            the relay outbound server which has notified.
	 * @param channel
	 *            the relay channel idle.
	 * @param mrsSessionId
	 *            the MRS session of the channel for later use.
	 */
	public void notifyIdleSession(RpcClientChannel ros, Channel channel, String mrsSessionId);

	/**
	 * Dispatch a relay message received asynchronously from a WS towards the RelayServer which is chose to handle or is
	 * handling the channel.
	 * 
	 * NOTE: the message comes from a web service MRS, MOS, MDS and is pushed to a ROS.
	 * 
	 * @param msg
	 *            the message to dispatch.
	 */
	public void relayMessage(RelayMessage msg);

	/**
	 * Set the RelayServer's current load.
	 * 
	 * @param ros
	 *            the relay server which has communicated their load average.
	 * @param currentLoad
	 *            the current load (relay channels currently in process).
	 */
	public void notifyLoad(RpcClientChannel ros, int currentLoad);
}
