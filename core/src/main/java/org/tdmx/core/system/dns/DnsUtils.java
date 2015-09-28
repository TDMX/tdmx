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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.core.system.lang.StringUtils;
import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

/**
 * Utility class for DNS operations.
 * 
 * @author Peter
 *
 */
public class DnsUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private DnsUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

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

	/**
	 * Return the IPAddresses of the JVM's default DNS resolvers.
	 * 
	 * @return the IPAddresses of the JVM's default DNS resolvers.
	 */
	public static List<String> getSystemDnsResolverAddresses() {
		List<String> hosts = new ArrayList<>();

		String[] list = ResolverConfig.getCurrentConfig().servers();
		if (list != null) {
			for (String h : list) {
				hosts.add(h);
			}
		}
		return Collections.unmodifiableList(hosts);
	}

	/**
	 * Return the authoritative name servers of the domain as resolved by the resolverAddresses.
	 * 
	 * @param domainName
	 * @param resolverAddresses
	 * @throws Exception
	 */
	public static void getAuthNameServers(String domainName, List<String> resolverAddresses) throws Exception {
		Name n = Name.fromString(domainName);

		int numLabels = n.labels();
		// bottom to top lookup of SOA record.
		for (int i = 0; i < numLabels; i++) {
			StringBuffer b = new StringBuffer();
			for (int max = i; max < numLabels; max++) {
				String l = n.getLabelString(max);
				b.append(l);
				b.append(".");
			}
			String dn = b.toString();

			Resolver r = createResolver(resolverAddresses);
			Lookup l = new Lookup(dn, Type.SOA);
			l.setResolver(r);
			l.setCache(null);
			l.setSearchPath((Name[]) null);
			Record[] records = l.run();
			if (records != null) {
				log.info("Found authoritative server names " + records);
			}
		}

	}

	public static String reverseDns(String hostIp, List<String> resolverAddresses) {
		Name name;
		try {
			name = ReverseMap.fromAddress(hostIp);
		} catch (UnknownHostException e) {
			return hostIp;
		}
		int type = Type.PTR;
		int dclass = DClass.IN;
		Record rec = Record.newRecord(name, type, dclass);
		Message query = Message.newQuery(rec);
		Message response;
		try {
			Resolver res = createResolver(resolverAddresses);

			response = res.send(query);
		} catch (IOException e) {
			return hostIp;
		}

		Record[] answers = response.getSectionArray(Section.ANSWER);
		if (answers.length == 0) {
			return hostIp;
		} else {
			return answers[0].rdataToString();
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private static Resolver createResolver(List<String> resolverAddresses) throws UnknownHostException {
		List<Resolver> simpleResolvers = new ArrayList<>();
		for (String address : resolverAddresses) {
			Resolver r = new SimpleResolver(address);
			r.setTCP(true);
			r.setTimeout(10);
			simpleResolvers.add(r);
		}

		ExtendedResolver er = new ExtendedResolver(simpleResolvers.toArray(new Resolver[0]));
		return er;
	}
}
