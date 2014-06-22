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
import java.util.Collections;
import java.util.List;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

public class SystemDnsResolver {

	private final String hostname;

	public SystemDnsResolver(String hostname) {
		this.hostname = hostname;
	}

	public static List<String> getSearchHostnames() {
		List<String> hosts = new ArrayList<>();

		String[] list = ResolverConfig.getCurrentConfig().servers();
		if (list != null) {
			for (String h : list) {
				hosts.add(h);
			}
		}
		return Collections.unmodifiableList(hosts);
	}

	public void getAuthNameServers() throws Exception {
		Name n = Name.fromString("plus.google.com");

		int numLabels = n.labels();
		StringBuffer b = new StringBuffer();
		for (int max = 0; max < numLabels; max++) {
			// TODO bottom to top lookup of SOA record.
			String l = n.getLabelString(max);
			b.append(l);
			b.append(".");
		}
		String dn = b.toString();

		Resolver r = new SimpleResolver("8.8.8.8");
		Lookup l = new Lookup(dn, Type.SOA);
		l.setResolver(r);
		l.setCache(null);
		l.setSearchPath((Name[]) null);
		Record[] records = l.run();

	}

	private Resolver getResolver() {
		Resolver dr = Lookup.getDefaultResolver();
		dr.setTCP(true);
		return dr;
	}
}
