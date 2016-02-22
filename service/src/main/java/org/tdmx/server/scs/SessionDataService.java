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
package org.tdmx.server.scs;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Fetches data from zone services.
 * 
 * @author Peter
 *
 */
public interface SessionDataService {

	public AccountZone getAccountZone(String zoneApex);

	public AgentCredential getAgentCredential(AccountZone az, PKIXCertificate cert);

	public Service getService(AccountZone az, Domain domain, String serviceName);

	public Zone getZone(AccountZone az);

	public Domain getDomain(AccountZone az, Zone zone, String domainName);

	public ChannelAuthorization findChannelAuthorization(AccountZone az, Zone zone, Domain domain, ChannelOrigin co,
			ChannelDestination cd);

	public TemporaryChannel findTemporaryChannel(AccountZone az, Zone zone, Domain domain, ChannelOrigin co,
			ChannelDestination cd);

	public TemporaryChannel createTemporaryChannel(AccountZone az, Domain domain, ChannelOrigin co,
			ChannelDestination cd);
}
