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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.server.pcs.SessionHandle;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;

public class SessionHandleFactoryImpl implements SessionHandleFactory {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SessionHandleFactoryImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public SessionHandle createMOSSessionHandle(AccountZone az, AgentCredential agent) {
		Map<SeedAttribute, Long> attributes = new HashMap<>();
		attributes.put(SeedAttribute.AccountZoneId, az.getId());
		attributes.put(SeedAttribute.ZoneId, agent.getZone().getId());
		attributes.put(SeedAttribute.DomainId, agent.getDomain().getId());
		attributes.put(SeedAttribute.AddressId, agent.getAddress().getId());

		String sessionKey = az.getZoneApex() + ":" + agent.getAddress().getLocalName() + "@"
				+ agent.getDomain().getDomainName();
		SessionHandle handle = new SessionHandle(az.getSegment(), WebServiceApiName.MOS, sessionKey, attributes);
		return handle;
	}

	@Override
	public SessionHandle createMDSSessionHandle(AccountZone az, AgentCredential agent, Service service) {
		Map<SeedAttribute, Long> attributes = new HashMap<>();
		attributes.put(SeedAttribute.AccountZoneId, az.getId());
		attributes.put(SeedAttribute.ZoneId, agent.getZone().getId());
		attributes.put(SeedAttribute.DomainId, agent.getDomain().getId());
		attributes.put(SeedAttribute.AddressId, agent.getAddress().getId());
		attributes.put(SeedAttribute.ServiceId, service.getId());

		String sessionKey = az.getZoneApex() + ":" + agent.getAddress().getLocalName() + "@"
				+ agent.getDomain().getDomainName() + ":" + "#" + service.getServiceName();

		SessionHandle handle = new SessionHandle(az.getSegment(), WebServiceApiName.MDS, sessionKey, attributes);
		return handle;
	}

	@Override
	public SessionHandle createZASSessionHandle(AccountZone az, AgentCredential agent) {
		Map<SeedAttribute, Long> attributes = new HashMap<>();
		attributes.put(SeedAttribute.AccountZoneId, az.getId());
		attributes.put(SeedAttribute.ZoneId, agent.getZone().getId());
		if (agent.getDomain() != null) {
			attributes.put(SeedAttribute.DomainId, agent.getDomain().getId());
		}

		String sessionKey = az.getZoneApex();
		if (agent.getDomain() != null) {
			sessionKey = sessionKey + ":" + agent.getDomain().getDomainName();
		}

		SessionHandle handle = new SessionHandle(az.getSegment(), WebServiceApiName.ZAS, sessionKey, attributes);
		return handle;
	}

	@Override
	public SessionHandle createMRSSessionHandle(AccountZone az, PKIXCertificate client, TemporaryChannel channel) {
		Map<SeedAttribute, Long> attributes = new HashMap<>();
		attributes.put(SeedAttribute.AccountZoneId, az.getId());
		attributes.put(SeedAttribute.ZoneId, channel.getDomain().getZone().getId());
		attributes.put(SeedAttribute.DomainId, channel.getDomain().getId());
		attributes.put(SeedAttribute.TemporaryChannelId, channel.getId());

		String sessionKey = az.getZoneApex() + ":" + channel.getOrigin().getLocalName() + "@"
				+ channel.getOrigin().getDomainName() + "->" + channel.getDestination().getLocalName() + "@"
				+ channel.getDestination().getLocalName() + "#" + channel.getDestination().getServiceName();

		SessionHandle handle = new SessionHandle(az.getSegment(), WebServiceApiName.MRS, sessionKey, attributes);
		return handle;
	}

	@Override
	public SessionHandle createMRSSessionHandle(AccountZone az, PKIXCertificate client, Channel channel) {
		Map<SeedAttribute, Long> attributes = new HashMap<>();
		attributes.put(SeedAttribute.AccountZoneId, az.getId());
		attributes.put(SeedAttribute.ZoneId, channel.getDomain().getZone().getId());
		attributes.put(SeedAttribute.DomainId, channel.getDomain().getId());
		attributes.put(SeedAttribute.ChannelId, channel.getId());

		String sessionKey = az.getZoneApex() + ":" + channel.getOrigin().getLocalName() + "@"
				+ channel.getOrigin().getDomainName() + "->" + channel.getDestination().getLocalName() + "@"
				+ channel.getDestination().getLocalName() + "#" + channel.getDestination().getServiceName();

		SessionHandle handle = new SessionHandle(az.getSegment(), WebServiceApiName.MRS, sessionKey, attributes);
		return handle;
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
