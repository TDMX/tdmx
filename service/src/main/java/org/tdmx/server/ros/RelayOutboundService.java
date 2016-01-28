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
package org.tdmx.server.ros;

import java.util.List;
import java.util.Map;

import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;

/**
 * The RelayOutboundService.
 * 
 * @author Peter
 *
 */
public interface RelayOutboundService {

	/**
	 * Start the relay service.
	 */
	public void start();

	/**
	 * Stop relaying and release any resources held by the service.
	 */
	public void stop();

	/**
	 * Start the relay session.
	 * 
	 * @param channelKey
	 * @param attributes
	 *            the objectIds of the zone, domain, channel
	 * @param pcsServerName
	 */
	public void startRelaySession(String channelKey, Map<AttributeId, Long> attributes, String pcsServerName);

	/**
	 * Remove any idle relay session associated with the PCS server.
	 * 
	 * @param pcsServerName
	 * @return the channelKeys.
	 */
	public List<String> removeIdleRelaySessions(String pcsServerName);

	/**
	 * Shutdown any idle session associated with the PCS server which has disconnected.
	 * 
	 * @param pcsServerName
	 * @return the channelKeys.
	 */
	public List<String> shutdownRelaySessions(String pcsServerName);

	/**
	 * Gets a value indicating the current active relay session load handled by the server.
	 */
	public int getCurrentLoad();

	/**
	 * Notify that there is a ChannelAuthorization to relay.
	 * 
	 * @param channelKey
	 * @param caId
	 *            the identifier of the channel authorization.
	 * @return true if relay data successfully received.
	 */
	public boolean relayChannelAuthorization(String channelKey, Long caId);

	/**
	 * Notify that there is a Channel DestinationSession to relay.
	 * 
	 * @param channelKey
	 * @param channelId
	 *            the identifier of the channel.
	 * @return true if relay data successfully received.
	 */
	public boolean relayChannelDestinationSession(String channelKey, Long channelId);

	/**
	 * Notify that there is an opening of the channel flow control to relay.
	 * 
	 * @param channelKey
	 * @param flowQuotaId
	 *            the identifier of the channel flow quota.
	 * @return true if relay data successfully received.
	 */
	public boolean relayChannelFlowControl(String channelKey, Long quotalId);

	/**
	 * Notify that there is a ChannelMessage to relay.
	 * 
	 * @param channelKey
	 * @param messageId
	 *            the identifier of the channel message.
	 * @return true if relay data successfully received.
	 */
	public boolean relayChannelMessage(String channelKey, Long messageId);

	// TODO relay of DR #95
}
