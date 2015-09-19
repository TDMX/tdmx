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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.tdmx.core.system.lang.StringUtils;

/**
 * Utility class for DNS operations.
 * 
 * @author Peter
 *
 */
public class DnsUtils {

	/**
	 * Determine if the domainName is a subdomain of the zoneApex.
	 * 
	 * @param domainName
	 * @param zoneApex
	 * @return true if the domainName is a subdomain of the zoneApex.
	 */
	public static boolean isSubdomain(String domainName, String zoneApex) {
		if (StringUtils.hasText(domainName) && StringUtils.hasText(zoneApex)) {
			return domainName.endsWith("." + zoneApex);
		}
		return false;
	}

	/**
	 * Determine the part of domainName which is the subdomain of zoneApex.
	 * 
	 * For example if domainName is "a.b.com" and the zoneApex is "b.com", the the subdomain part is "a".
	 * 
	 * @param domainName
	 * @param zoneApex
	 * @return
	 */
	public static String getSubdomain(String domainName, String zoneApex) {
		if (isSubdomain(domainName, zoneApex)) {
			return domainName.substring(0, domainName.length() - zoneApex.length() - 1);
		}
		return null;
	}

	/**
	 * Get the domain hierarchy of a given domainName from most qualified to "highest" level domain.
	 * 
	 * For example. "a.b.c.com" returns the list {"a.b.c.com","b.c.com","c.com"}
	 * 
	 * @param domainName
	 * @return
	 */
	public static List<String> getDomainHierarchy(String domainName) {
		List<String> result = new ArrayList<>();

		List<String> domainParts = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(domainName, ".");
		while (st.hasMoreTokens()) {
			String part = st.nextToken();
			domainParts.add(part);
		}
		if (domainParts.size() <= 1) {
			return domainParts;
		}
		for (int i = 0; i < domainParts.size() - 1; i++) {
			StringBuffer b = new StringBuffer();
			for (int j = i; j < domainParts.size() - 1; j++) {
				b.append(domainParts.get(j)).append(".");
			}
			b.append(domainParts.get(domainParts.size() - 1));
			result.add(b.toString());
		}
		return result;
	}
}
