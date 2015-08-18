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
package org.tdmx.server.ws.session;

import java.util.Map;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;

public interface WebServiceSessionManager {

	public ApiName getApiName();

	/**
	 * Creates a new session for a client with some initial attributes.
	 * 
	 * @param sessionId
	 * @param cert
	 * @param seedAttributes
	 * @return the number of active sessions.
	 */
	public int createSession(String sessionId, PKIXCertificate cert, Map<SeedAttribute, Long> seedAttributes);

	/**
	 * Add a new client certificate to an existing session.
	 * 
	 * @param sessionId
	 * @param cert
	 * @return the number of active sessions.
	 */
	public int addCertificate(String sessionId, PKIXCertificate cert);

	/**
	 * Remove a client certificate from an existing session.
	 * 
	 * @param sessionId
	 * @param cert
	 * @return the number of active sessions.
	 */
	public int removeCertificate(String sessionId, PKIXCertificate cert);

}
