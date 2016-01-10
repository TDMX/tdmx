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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.PCSServer.RelayChannelMrsSession;

/**
 * Handles the outbound relay.
 * 
 * @author Peter
 *
 */
public class RelayOutboundServiceImpl implements RelayOutboundService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayOutboundServiceImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, List<RelayChannelMrsSession>> stop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCurrentLoad() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startRelaySession(String channelKey, Map<AttributeId, Long> attributes, String mrsSessionId,
			String pcsServerName) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<RelayChannelMrsSession> removeIdleRelaySessions(String pcsServerName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getActiveRelaySessions(String pcsServerName) {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public void relayChannelAuthorization(String channelKey, Long channelId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void relayChannelFlowControl(String channelKey, Long channelId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void relayChannelMessage(String channelKey, Long messageId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void relayChannelDestinationSession(String channelKey, Long channelId) {
		// TODO Auto-generated method stub

	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
