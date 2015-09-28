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
package org.tdmx.core.system.dns;

import java.util.List;

public class DnsLookup {

	private List<String> resolverAddresses;

	private List<String> authoritativeNameServers;

	private List<String> ipAddresses;

	private List<String> textRecords;

	public List<String> getResolverAddresses() {
		return resolverAddresses;
	}

	public void setResolverAddresses(List<String> resolverAddresses) {
		this.resolverAddresses = resolverAddresses;
	}

	public List<String> getAuthoritativeNameServers() {
		return authoritativeNameServers;
	}

	public void setAuthoritativeNameServers(List<String> authoritativeNameServers) {
		this.authoritativeNameServers = authoritativeNameServers;
	}

	public List<String> getIpAddresses() {
		return ipAddresses;
	}

	public void setIpAddresses(List<String> ipAddresses) {
		this.ipAddresses = ipAddresses;
	}

	public List<String> getTextRecords() {
		return textRecords;
	}

	public void setTextRecords(List<String> textRecords) {
		this.textRecords = textRecords;
	}

}
