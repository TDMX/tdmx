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

import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Service;

/**
 * The SessionKeyUtil creates SessionKey from domain objects which determine them.
 * 
 * @author Peter
 *
 */
public class SessionKeyUtil {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	private SessionKeyUtil() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public static String createMOSSessionKey(String zoneApex, Domain originDomain, Address originAddress) {
		String sessionKey = zoneApex + ":" + originAddress.getLocalName() + "@" + originDomain.getDomainName();
		return sessionKey;
	}

	public static String createMOSSessionKey(String zoneApex, ChannelOrigin origin) {
		String sessionKey = zoneApex + ":" + origin.getLocalName() + "@" + origin.getDomainName();
		return sessionKey;
	}

	public static String createMDSSessionKey(String zoneApex, Domain destDomain, Address destAddress, Service service) {
		String sessionKey = zoneApex + ":" + destAddress.getLocalName() + "@" + destDomain.getDomainName() + "#"
				+ service.getServiceName();
		return sessionKey;
	}

	public static String createMDSSessionKey(String zoneApex, ChannelDestination dest) {
		String sessionKey = zoneApex + ":" + dest.getLocalName() + "@" + dest.getDomainName() + "#"
				+ dest.getServiceName();
		return sessionKey;
	}

	public static String createZASSessionKey(String zoneApex, String destDomainName) {
		String sessionKey = zoneApex;
		if (destDomainName != null) {
			sessionKey = sessionKey + ":" + destDomainName;
		}
		return sessionKey;
	}

	public static String createMRSSessionKey(String zoneApex, Channel channel) {
		return createMRSSessionKey(zoneApex, channel.getOrigin(), channel.getDestination());
	}

	public static String createMRSSessionKey(String zoneApex, ChannelOrigin origin, ChannelDestination dest) {
		String sessionKey = zoneApex + ":" + origin.getLocalName() + "@" + origin.getDomainName() + "->"
				+ dest.getLocalName() + "@" + dest.getDomainName() + "#" + dest.getServiceName();
		return sessionKey;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
