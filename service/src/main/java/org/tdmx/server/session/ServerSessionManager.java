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

import java.util.Map;

import javax.net.ssl.X509TrustManager;

import org.tdmx.client.crypto.certificate.PKIXCertificate;

public interface ServerSessionManager {

	/**
	 * Provide an X509TrustManager which can decide which TLS client certificates are allowed to connect to the server.
	 * The client certificates which are allowed are all those attached to sessions.
	 * 
	 * @return
	 */
	public X509TrustManager getTrustManager();

	/**
	 * Creates a new session for a client with some initial attributes.
	 * 
	 * @param sessionId
	 * @param cert
	 * @param seedAttributes
	 */
	public void createSession(String sessionId, PKIXCertificate cert, Map<String, String> seedAttributes);

	/**
	 * Add a new client certificate to an existing session.
	 * 
	 * @param sessionId
	 * @param cert
	 */
	public void addCertificate(String sessionId, PKIXCertificate cert);

	/**
	 * Remove a client certificate from an existing session.
	 * 
	 * @param sessionId
	 * @param cert
	 */
	public void removeCertificate(String sessionId, PKIXCertificate cert);

	/**
	 * Return the number of active sessions.
	 * 
	 * @return the number of active sessions.
	 */
	public int getSessionCount();

	/**
	 * Return the ServerSession associated with the Certificate and sessionID
	 * 
	 * @param sessionId
	 * @param cert
	 * @return null if there is no association of sessionID and Certificate.
	 */
	public ServerSession getSession(String sessionId, PKIXCertificate cert);
}
