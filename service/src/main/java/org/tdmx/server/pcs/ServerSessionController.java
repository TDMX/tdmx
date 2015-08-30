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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;

public interface ServerSessionController {

	public static class ServerServiceStatistics {
		private final List<ServiceStatistic> stats = new ArrayList<ServiceStatistic>(WebServiceApiName.values().length);

		public void addStatistic(ServiceStatistic s) {
			stats.add(s);
		}

		public List<ServiceStatistic> getStatistics() {
			return stats;
		}
	}

	public static class ServiceStatistic {
		private final WebServiceApiName api;
		private final String httpsUrl;
		private final int loadValue;

		public ServiceStatistic(WebServiceApiName api, String httpsUrl, int loadValue) {
			this.api = api;
			this.httpsUrl = httpsUrl;
			this.loadValue = loadValue;
		}

		public WebServiceApiName getApi() {
			return api;
		}

		public String getHttpsUrl() {
			return httpsUrl;
		}

		public int getLoadValue() {
			return loadValue;
		}

	}

	/**
	 * Creates a new API session for a client with some initial attributes.
	 * 
	 * @param apiName
	 * @param sessionId
	 * @param cert
	 * @param seedAttributes
	 * @return the server statistics
	 */
	public ServerServiceStatistics createSession(WebServiceApiName apiName, String sessionId, PKIXCertificate cert,
			Map<SeedAttribute, Long> seedAttributes);

	/**
	 * Add a new client certificate to an existing API session.
	 * 
	 * @param apiName
	 * @param sessionId
	 * @param cert
	 * @return the server statistics
	 */
	public ServerServiceStatistics addCertificate(WebServiceApiName apiName, String sessionId, PKIXCertificate cert);

	/**
	 * Remove a client certificate from an existing API session.
	 * 
	 * @param apiName
	 * @param sessionId
	 * @param cert
	 * @return the server statistics
	 */
	public ServerServiceStatistics removeCertificate(WebServiceApiName apiName, String sessionId, PKIXCertificate cert);

}
