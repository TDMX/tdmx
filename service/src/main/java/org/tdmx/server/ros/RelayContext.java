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

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;

/**
 * The control state of a channel's relaying activity.
 * 
 * @author Peter
 *
 */
public class RelayContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayContext.class);

	// internal
	private RelayContextState state = RelayContextState.META;

	private final LinkedList<RelayObject> objects = new LinkedList<>();
	private String mrsSessionId;

	// reference
	private final String pcsServerName;
	private final String channelKey;
	private final Zone zone;
	private final Domain domain;
	private final Channel channel;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public RelayContext(String pcsServerName, String channelKey, Zone zone, Domain domain, Channel channel) {
		this.pcsServerName = pcsServerName;
		this.channelKey = channelKey;
		this.zone = zone;
		this.domain = domain;
		this.channel = channel;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getPcsServerName() {
		return pcsServerName;
	}

	public String getChannelKey() {
		return channelKey;
	}

	public Zone getZone() {
		return zone;
	}

	public Domain getDomain() {
		return domain;
	}

	public Channel getChannel() {
		return channel;
	}

	public String getMrsSessionId() {
		return mrsSessionId;
	}

	public void setMrsSessionId(String mrsSessionId) {
		this.mrsSessionId = mrsSessionId;
	}

	public RelayContextState getState() {
		return state;
	}
}
