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
import org.tdmx.server.pcs.protobuf.PCSServer.RelayChannelMrsSession;

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
	 * 
	 * @return the mrsSessionId associated with each channel which has stopped mapped by PCS controllerId.
	 */
	public Map<String, List<RelayChannelMrsSession>> stop();

	/**
	 * Start the relay session.
	 * 
	 * @param channelKey
	 * @param attributes
	 *            the objectIds of the zone, domain, channel
	 * @param mrsSessionId
	 *            the optional mrsSessionId cached at the PCS.
	 */
	public void startRelaySession(String channelKey, Map<AttributeId, Long> attributes, String mrsSessionId,
			String pcsServerName);

	/**
	 * Remove any idle relay session associated with the PCS server.
	 * 
	 * @param controllerId
	 * @return the channelKeys and their mrsSessionIds.
	 */
	public List<RelayChannelMrsSession> removeIdleRelaySessions(String controllerId);

	/**
	 * Gets a value indicating the current active relay session load handled by the server.
	 */
	public int getCurrentLoad();

	/**
	 * When we reconnect to a PCS server, we inform it of the relay sessions we are handling.
	 * 
	 * @param controllerId
	 * @return the list of active relay sessions indicated by their channelKey.
	 */
	public List<String> getActiveRelaySessions(String controllerId);

	/**
	 * Notify that there is a ChannelAuthorization to relay.
	 * 
	 * @param channelKey
	 * @param caId
	 *            the identifier of the channel authorization.
	 */
	public void relayChannelAuthorization(String channelKey, Long caId);

	/**
	 * Notify that there is a Channel DestinationSession to relay.
	 * 
	 * @param channelKey
	 * @param channelId
	 *            the identifier of the channel.
	 */
	public void relayChannelDestinationSession(String channelKey, Long channelId);

	/**
	 * Notify that there is an opening of the channel flow control to relay.
	 * 
	 * @param channelKey
	 * @param flowQuotaId
	 *            the identifier of the channel flow quota.
	 */
	public void relayChannelFlowControl(String channelKey, Long quotalId);

	/**
	 * Notify that there is a ChannelMessage to relay.
	 * 
	 * @param channelKey
	 * @param messageId
	 *            the identifier of the channel message.
	 */
	public void relayChannelMessage(String channelKey, Long messageId);

	// TODO relay of DR #95
}
