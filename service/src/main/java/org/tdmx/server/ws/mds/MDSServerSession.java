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
package org.tdmx.server.ws.mds;

import java.util.HashMap;
import java.util.Map;

import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.Common.ObjectType;
import org.tdmx.server.ws.session.WebServiceSession;

public class MDSServerSession extends WebServiceSession {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	// internal
	private final Map<Integer, ReceiverContext> receiverContextMap = new HashMap<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public MDSServerSession(String sessionId, AccountZone az, Zone zone, Domain domain, Address address,
			Service service) {
		super(sessionId);
		setAccountZone(az);
		setZone(zone);
		setDomain(domain);
		setDestinationAddress(address);
		setService(service);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public synchronized ReceiverContext getReceiverContext(Integer serialNr) {
		ReceiverContext r = receiverContextMap.get(serialNr);
		if (r == null) {
			r = new ReceiverContext(serialNr);
			receiverContextMap.put(serialNr, r);
		}
		return r;
	}

	@Override
	public boolean transferObject(ObjectType type, Map<AttributeId, Long> attributes) {
		// MDS receives MSG from MRS
		for (ReceiverContext recv : receiverContextMap.values()) {
			recv.setDirty();
		}
		return true;
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

	public Address getDestinationAddress() {
		return getAttribute(DESTINATION_ADDRESS);
	}

	public Service getService() {
		return getAttribute(SERVICE);
	}

	public ChannelDestination getChannelDestination() {
		ChannelDestination cd = new ChannelDestination();
		cd.setLocalName(getDestinationAddress().getLocalName());
		cd.setDomainName(getDomain().getDomainName());
		cd.setServiceName(getService().getServiceName());
		return cd;
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

	void setDestinationAddress(Address a) {
		setAttribute(DESTINATION_ADDRESS, a);
	}

	void setService(Service s) {
		setAttribute(SERVICE, s);
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
