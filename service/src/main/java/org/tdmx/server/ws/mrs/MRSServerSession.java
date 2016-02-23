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
package org.tdmx.server.ws.mrs;

import java.util.Map;

import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.Common.ObjectType;
import org.tdmx.server.ws.session.WebServiceSession;

public class MRSServerSession extends WebServiceSession {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public MRSServerSession(String sessionId, AccountZone az, Zone zone, Domain domain) {
		super(sessionId);
		setAccountZone(az);
		setZone(zone);
		setDomain(domain);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public boolean transferObject(ObjectType type, Map<AttributeId, Long> attributes) {
		// MRS does not handle any inbound objects
		return false;
	}

	public AccountZone getAccountZone() {
		return getAttribute(ACCOUNT_ZONE);
	}

	public Zone getZone() {
		return getAttribute(ZONE);
	}

	public Domain getDomain() {
		return getAttribute(DOMAIN);
	}

	public Channel getChannel() {
		return getAttribute(CHANNEL);
	}

	public TemporaryChannel getTemporaryChannel() {
		return getAttribute(TEMP_CHANNEL);
	}

	// needs to be public since shortcut ROSConnectionProvider set this
	public void setChannel(Channel c) {
		setAttribute(CHANNEL, c);
	}

	// needs to be public since shortcut ROSConnectionProvider set this
	public void setTemporaryChannel(TemporaryChannel c) {
		setAttribute(TEMP_CHANNEL, c);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	void setAccountZone(AccountZone az) {
		setAttribute(ACCOUNT_ZONE, az);
	}

	void setZone(Zone z) {
		setAttribute(ZONE, z);
	}

	void setDomain(Domain d) {
		setAttribute(DOMAIN, d);
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
