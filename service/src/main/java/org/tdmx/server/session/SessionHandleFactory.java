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

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.server.pcs.SessionHandle;

/**
 * The SessionHandleFactory creates SessionHandles which the SessionControlService uses. It is an intermediary between the
 * ServerSessionAllocationService and the SessionControlService.
 * 
 * @author Peter
 *
 */
public interface SessionHandleFactory {

	public SessionHandle createMOSSessionHandle(AccountZone az, AgentCredential agent);

	public SessionHandle createMDSSessionHandle(AccountZone az, AgentCredential agent, Service service);

	public SessionHandle createZASSessionHandle(AccountZone az, AgentCredential agent);

	public SessionHandle createMRSSessionHandle(AccountZone az, PKIXCertificate client, Channel channel);

	public SessionHandle createMRSSessionHandle(AccountZone az, PKIXCertificate client, TemporaryChannel channel);
}
