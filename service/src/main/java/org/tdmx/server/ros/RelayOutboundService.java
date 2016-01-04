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

import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.server.pcs.protobuf.PCSServer.RelayChannelMrsSession;

/**
 * The RelayOutboundService.
 * 
 * @author Peter
 *
 */
public interface RelayOutboundService {

	/**
	 * Start the relay session.
	 * 
	 * @param channelKey
	 * @param zone
	 * @param domain
	 * @param channel
	 * @param mrsSessionId
	 *            the optional mrsSessionId cached at the PCS.
	 */
	public void startRelaySession(String channelKey, Zone zone, Domain domain, Channel channel, String mrsSessionId,
			String pcsServerName);

	/**
	 * Remove any idle relay session associated with the PCS server.
	 * 
	 * @param pcsServerName
	 * @return the channelKeys and their mrsSessionIds.
	 */
	public List<RelayChannelMrsSession> removeIdleRelaySessions(String pcsServerName);

	/**
	 * Gets a value indicating the current active relay session load handled by the server.
	 */
	public int getCurrentLoad();

	/**
	 * When we reconnect to a PCS server, we inform it of the relay sessions we are handling.
	 * 
	 * @param pcsServerName
	 * @return the list of active relay sessions indicated by their channelKey.
	 */
	public List<String> getActiveRelaySessions(String pcsServerName);

	/**
	 * Notify that there is a ChannelAuthorization to relay.
	 * 
	 * @param channelKey
	 */
	public void relayChannelAuthorization(String channelKey);
}
