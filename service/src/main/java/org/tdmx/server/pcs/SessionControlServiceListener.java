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

import java.util.List;
import java.util.Set;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.ws.session.WebServiceApiName;

public interface SessionControlServiceListener {

	/**
	 * On the attachment of a Server with it's supported Services to the SessionControlService.
	 * 
	 * @param services
	 * @param ssm
	 *            callback
	 */
	public void registerServer(List<ServiceHandle> services, ServerSessionController ssm);

	/**
	 * On the detachment of a Server with it's supported Services from the SessionControlService.
	 * 
	 * @param services
	 */
	public void unregisterServer(List<ServiceHandle> services);

	/**
	 * WebServiceSessionManagers notify the SessionControlService when session's have not been used for some time and are
	 * removed locally.
	 * 
	 * @param api
	 * @param sessionIds
	 */
	public void notifySessionsRemoved(WebServiceApiName api, Set<String> sessionIds);

	/**
	 * Invalidate the certificate in all sessions which it belongs to.
	 * 
	 * @param cert
	 */
	public void invalidateCertificate(PKIXCertificate cert);
}
