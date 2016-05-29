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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.Common.ObjectType;

public interface WebServiceSessionManager {

	/**
	 * Return the HTTPS url that this WebServiceSessionManager is managing.
	 * 
	 * @return
	 */
	public String getHttpsUrl();

	/**
	 * The API name that this WebServiceSessionManager is managing.
	 * 
	 * @return
	 */
	public WebServiceApiName getApiName();

	/**
	 * Creates a new session for a client with some initial attributes. The sessionId belongs to the controllerId.
	 * 
	 * @param sessionId
	 * @param controllerId
	 * @param cert
	 * @param seedAttributes
	 * @return the number of active sessions.
	 */
	public int createSession(String sessionId, String controllerId, PKIXCertificate cert,
			Map<AttributeId, Long> seedAttributes);

	/**
	 * Removes all sessions controlled by the controller.
	 * 
	 * @param controllerId
	 */
	public void disconnectController(String controllerId);

	/**
	 * Return a list of sessions which have not been used after the lastCutoffDate.
	 * 
	 * @param lastCutoffDate
	 * @param creationCutoffDate
	 * @return
	 */
	public List<WebServiceSession> removeIdleSessions(Date lastCutoffDate);

	/**
	 * Add a new client certificate to an existing session.
	 * 
	 * @param sessionId
	 * @param cert
	 * @return the number of active sessions.
	 */
	public int addCertificate(String sessionId, PKIXCertificate cert);

	/**
	 * Remove a client certificate from all sessions.
	 * 
	 * @param cert
	 * @return the number of active sessions.
	 */
	public int removeCertificate(PKIXCertificate cert);

	/**
	 * Object transferred in from another service.
	 * 
	 * @param sessionId
	 * @param type
	 * @param attributes
	 * @return true if the object was sucessfully processed.
	 */
	public boolean transferObject(String sessionId, ObjectType type, Map<AttributeId, Long> attributes);

}
