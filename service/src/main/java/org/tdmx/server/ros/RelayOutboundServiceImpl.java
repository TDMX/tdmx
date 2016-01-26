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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;
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

	/**
	 * Provides all data for the ROS.
	 */
	private RelayDataService relayDataService;

	/**
	 * Map of all RelayChannelContext's keyed by channelKey.
	 */
	private final Map<String, RelayChannelContext> contextMap = new HashMap<>();

	/**
	 * The current load value.
	 */
	private final AtomicInteger loadValue = new AtomicInteger(0);

	/**
	 * Idle timeout ( 5min )
	 */
	private long idleTimeoutMillis = 300000;

	/**
	 * The max concurrent relays per channel.
	 * 
	 * TODO LATER: the number of concurrent messages relayed at any one time should be a ChannelAuthorization property.
	 */
	private int maxConcurrentRelaysPerChannel = 5;

	// TODO #93: executor and thread pool started/stopped

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void start() {
		log.info("Starting RelayOutboundService.");
		// we start with an emtpy context map.
		contextMap.clear();
	}

	@Override
	public Map<String, List<RelayChannelMrsSession>> stop() {
		log.info("Stopping RelayOutboundService.");
		Map<String, List<RelayChannelMrsSession>> result = new HashMap<>();

		for (Entry<String, RelayChannelContext> ctxEntry : contextMap.entrySet()) {
			RelayChannelContext rc = ctxEntry.getValue();

			if (!rc.isShutdown()) {
				String mrsSessionId = rc.getMrsSessionId();
				if (mrsSessionId != null) {
					List<RelayChannelMrsSession> serverChannelList = result.get(rc.getPcsServerName());
					if (serverChannelList == null) {
						serverChannelList = new ArrayList<>();
						result.put(rc.getPcsServerName(), serverChannelList);
					}
					RelayChannelMrsSession.Builder rcms = RelayChannelMrsSession.newBuilder();
					rcms.setChannelKey(rc.getChannelKey());
					rcms.setMrsSessionId(rc.getMrsSessionId());
					serverChannelList.add(rcms.build());
				}
			} else {
				rc.shutdown();
			}
		}
		return result;
	}

	@Override
	public int getCurrentLoad() {
		log.debug("Current load ");
		return loadValue.get();
	}

	@Override
	public void startRelaySession(String channelKey, Map<AttributeId, Long> attributes, String mrsSessionId,
			String pcsServerName) {
		log.info("Start relay session " + channelKey);

		// lookup the domain objects.
		AccountZone az = relayDataService.getAccountZone(attributes.get(AttributeId.AccountZoneId));
		Zone z = relayDataService.getZone(az, attributes.get(AttributeId.ZoneId));
		Domain d = relayDataService.getDomain(az, z, attributes.get(AttributeId.DomainId));
		Channel c = relayDataService.getChannel(az, z, d, attributes.get(AttributeId.ChannelId));

		RelayDirection dir = c.isSameDomain() ? RelayDirection.Both
				: c.isSend() ? RelayDirection.Fowards : RelayDirection.Backwards;

		RelayChannelContext rc = new RelayChannelContext(pcsServerName, channelKey, az, z, d, c, dir,
				maxConcurrentRelaysPerChannel);
		// take over existing mrs sessionId if provided by PCS.
		rc.setMrsSessionId(mrsSessionId);
		contextMap.put(channelKey, rc);
	}

	@Override
	public List<RelayChannelMrsSession> removeIdleRelaySessions(String pcsServerName) {
		log.info("Remove idle relay sessions for " + pcsServerName);
		List<String> idleSessions = new ArrayList<>();
		for (Entry<String, RelayChannelContext> ctxEntry : contextMap.entrySet()) {
			RelayChannelContext rc = ctxEntry.getValue();
			if (pcsServerName.equals(rc.getPcsServerName()) && rc.isIdleTimeout(idleTimeoutMillis)) {
				idleSessions.add(ctxEntry.getKey());
			}
		}
		List<RelayChannelMrsSession> result = new ArrayList<>();
		for (String channelKey : idleSessions) {
			RelayChannelContext rc = contextMap.remove(channelKey);
			if (pcsServerName.equals(rc.getPcsServerName()) && rc.isIdleTimeout(idleTimeoutMillis)) {
				RelayChannelMrsSession.Builder rs = RelayChannelMrsSession.newBuilder();
				rs.setChannelKey(channelKey);
				rs.setMrsSessionId(rc.getMrsSessionId());
				result.add(rs.build());
			} else {
				contextMap.put(channelKey, rc);
			}
		}
		log.info("Removed " + result.size() + " idle relay sessions for " + pcsServerName);
		return result;
	}

	@Override
	public List<String> getActiveRelaySessions(String pcsServerName) {
		log.info("Get active relay sessions for " + pcsServerName);
		// we find any non IDLE sessions ( active ) to give to the PCS ( discovery on connect ).
		List<String> activeSessions = new ArrayList<>();
		for (Entry<String, RelayChannelContext> ctxEntry : contextMap.entrySet()) {
			if (!ctxEntry.getValue().isIdle()) {
				activeSessions.add(ctxEntry.getKey());
			}
		}
		return activeSessions;
	}

	@Override
	public boolean relayChannelAuthorization(String channelKey, Long channelId) {
		log.info("relayChannelAuthorization " + channelKey);
		RelayChannelContext rc = contextMap.get(channelKey);
		if (rc == null) {
			// normal if ROS session has idle timed-out, client must get new ROS session
			log.debug("relayChannelAuthorization " + channelKey + " could not find relay context.");
			return false;
		}
		// add the CA to the relay context
		schedule(rc.relayChannelAuthorization(channelId));
		return true;
	}

	@Override
	public boolean relayChannelFlowControl(String channelKey, Long quotaId) {
		log.info("relayChannelFlowControl " + channelKey);
		RelayChannelContext rc = contextMap.get(channelKey);
		if (rc == null) {
			// normal if ROS session has idle timed-out, client must get new ROS session
			log.debug("relayChannelFlowControl " + channelKey + " could not find relay context.");
			return false;
		}
		// add the FC to the relay context
		schedule(rc.relayChannelFlowControl(quotaId));
		return true;
	}

	@Override
	public boolean relayChannelMessage(String channelKey, Long messageId) {
		log.info("relayChannelMessage " + channelKey);
		RelayChannelContext rc = contextMap.get(channelKey);
		if (rc == null) {
			// normal if ROS session has idle timed-out, client must get new ROS session
			log.debug("relayChannelMessage " + channelKey + " could not find relay context.");
			return false;
		}
		// add the MSG to the relay context
		schedule(rc.relayChannelMessage(messageId));
		return true;
	}

	@Override
	public boolean relayChannelDestinationSession(String channelKey, Long channelId) {
		log.info("relayChannelDestinationSession " + channelKey);
		RelayChannelContext rc = contextMap.get(channelKey);
		if (rc == null) {
			// normal if ROS session has idle timed-out, client must get new ROS session
			log.debug("relayChannelDestinationSession " + channelKey + " could not find relay context.");
			return false;
		}
		// add the DS to the relay context
		schedule(rc.relayChannelDestinationSession(channelId));
		return true;
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void schedule(List<RelayJobContext> jobs) {
		// TODO #93 implement relay executor
		for (RelayJobContext job : jobs) {
			log.debug("Scheduling " + job);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public RelayDataService getRelayDataService() {
		return relayDataService;
	}

	public void setRelayDataService(RelayDataService relayDataService) {
		this.relayDataService = relayDataService;
	}

	public long getIdleTimeoutMillis() {
		return idleTimeoutMillis;
	}

	public void setIdleTimeoutMillis(long idleTimeoutMillis) {
		this.idleTimeoutMillis = idleTimeoutMillis;
	}

	public int getMaxConcurrentRelaysPerChannel() {
		return maxConcurrentRelaysPerChannel;
	}

	public void setMaxConcurrentRelaysPerChannel(int maxConcurrentRelaysPerChannel) {
		this.maxConcurrentRelaysPerChannel = maxConcurrentRelaysPerChannel;
	}

}
