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
package org.tdmx.server.pcs;

import java.util.Map;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.ws.session.WebServiceApiName;

public interface ServerSessionController {

	/**
	 * Creates a new API session for a client with some initial attributes.
	 * 
	 * @param apiName
	 * @param sessionId
	 * @param cert
	 * @param seedAttributes
	 * @return the server statistics
	 */
	public ServiceStatistic createSession(WebServiceApiName apiName, String sessionId, PKIXCertificate cert,
			Map<AttributeId, Long> seedAttributes);

	/**
	 * Add a new client certificate to an existing API session.
	 * 
	 * @param apiName
	 * @param sessionId
	 * @param cert
	 * @return the server statistics
	 */
	public ServiceStatistic addCertificate(WebServiceApiName apiName, String sessionId, PKIXCertificate cert);

	/**
	 * Remove a client certificate from all sessions.
	 * 
	 * @param cert
	 * @return the server statistics
	 */
	public ServerServiceStatistics removeCertificate(PKIXCertificate cert);

	/**
	 * Return the server's load statistics over all services.
	 * 
	 * @return the server's load statistics over all services.
	 */
	public ServerServiceStatistics getStatistics();
}
