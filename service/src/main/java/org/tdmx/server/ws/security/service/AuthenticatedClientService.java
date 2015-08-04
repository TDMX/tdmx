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
package org.tdmx.server.ws.security.service;

import org.tdmx.client.crypto.certificate.PKIXCertificate;

/**
 * Register/Clear/Get the authenticated PKIXCertificate with the current Thread.
 * 
 * Works together with the {@link AuthenticatedClientLookupService} to provide the current ThreadLocal authenticated
 * agent.
 * 
 * @author Peter
 * 
 */
public interface AuthenticatedClientService extends AuthenticatedClientLookupService {

	/**
	 * Set the PKIXCertificate associated with the current Thread.
	 * 
	 * Setting the PKIXCertificate without it being cleared first for the thread calling will issue a warning.
	 * 
	 * @param agent
	 */
	public void setAuthenticatedClient(PKIXCertificate cert);

	/**
	 * Clear the PKIXCertificate so that none is associated with the current thread.
	 */
	public void clearAuthenticatedClient();

}
