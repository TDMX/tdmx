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

import org.tdmx.server.ws.session.WebServiceApiName;

public class ServiceStatistic {
	private final WebServiceApiName api;
	private final String httpsUrl;
	private int loadValue;

	public ServiceStatistic(WebServiceApiName api, String httpsUrl, int loadValue) {
		this.api = api;
		this.httpsUrl = httpsUrl;
		this.loadValue = loadValue;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(api).append(":").append(httpsUrl).append("->").append(loadValue);
		return b.toString();
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

	public void setLoadValue(int loadValue) {
		this.loadValue = loadValue;
	}

}