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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tdmx.core.system.lang.StringUtils;
import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
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

	public static final Pattern TDMX_DNS_TXT_RECORD_PATTERN = Pattern
			.compile("^tdmx version=(\\d) zac=(\\w+) scs=(https://.*)$");

	private static final String TDMX_DNS_TXT_RECORD_STRING = "tdmx version=%d zac=%s scs=%s";

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private DnsUtils() {
	}

	public static class TdmxZoneRecord {
		private final String zoneApex;
		private final int version;
		private final String zacFingerprint;
		private final URL scsUrl;

		public TdmxZoneRecord(String zoneApex, int version, String zacFingerprint, URL scsUrl) {
			this.zoneApex = zoneApex;
			this.version = version;
			this.zacFingerprint = zacFingerprint;
			this.scsUrl = scsUrl;
		}

		public int getVersion() {
			return version;
		}

		public String getZacFingerprint() {
			return zacFingerprint;
		}

		public URL getScsUrl() {
			return scsUrl;
		}

		public String getZoneApex() {
			return zoneApex;
		}

		@Override
		public String toString() {
			return formatDnsTxtRecord(this);
		}
	}

	/**
	 * A container value class for DnsResults.
	 * 
	 * @author Peter
	 *
	 */
	public static class DnsResultHolder {
		private final String apex;
		private final List<String> records;

		public DnsResultHolder(String apex, List<String> records) {
			this.apex = apex;
			this.records = records;
		}

		public String getApex() {
			return apex;
		}

		public List<String> getRecords() {
			return records;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(apex).append(":");
			sb.append(StringUtils.convertStringListToCsv(records));
			return sb.toString();
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Check whether a TXT record value matches the TDMX zone information format.
	 * 
	 * @param textRecord
	 * @return whether a TXT record value matches the TDMX zone information format.
	 */
	public static boolean matchesTdmxZoneRecord(String textRecord) {
		Matcher m = TDMX_DNS_TXT_RECORD_PATTERN.matcher(textRecord);
		return m.matches();
	}

	/**
	 * Formats the DNX TXT record contents.
	 * 
	 * @param zoneRecord
	 * @return the DNX TXT record contents for the zoneRecord.
	 */
	public static String formatDnsTxtRecord(TdmxZoneRecord zoneRecord) {
		return String.format(TDMX_DNS_TXT_RECORD_STRING, zoneRecord.getVersion(), zoneRecord.getZacFingerprint(),
				zoneRecord.getScsUrl());
	}

	/**
	 * Convert a TXT record to a structured TDMX information.
	 * 
	 * @param zoneApex
	 *            the dns domain which has the TXT record.
	 * @param textRecord
	 * @return null if the TXT record does not match the TDMX zone record else the parsed fields.
	 */
	public static TdmxZoneRecord parseTdmxZoneRecord(String zoneApex, String textRecord) {
		Matcher m = TDMX_DNS_TXT_RECORD_PATTERN.matcher(textRecord);
		if (m.matches()) {
			try {
				return new TdmxZoneRecord(zoneApex, Integer.valueOf(m.group(1)), m.group(2), new URL(m.group(3)));
			} catch (NumberFormatException | MalformedURLException e) {
				return null;
			}
		}
		return null;
	}

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
	 * Return the domain names of name servers of the domain as resolved by the resolverAddresses.
	 * 
	 * @param domainName
	 * @param resolverAddresses
	 * @throws TextParseException
	 * @throws UnknownHostException
	 * @throws Exception
	 */
	public static DnsResultHolder getNameServers(String domainName, List<String> resolverAddresses)
			throws TextParseException, UnknownHostException {
		List<String> result = new ArrayList<>();

		Name n = Name.fromString(domainName);

		int numLabels = n.labels();
		// bottom to top lookup of NS records.
		for (int i = 0; i < numLabels - 1; i++) {
			StringBuffer b = new StringBuffer();
			for (int max = i; max < numLabels; max++) {
				String l = n.getLabelString(max);
				b.append(l);
				b.append(".");
			}
			Name searchName = Name.fromString(b.toString());

			Resolver r = createResolver(resolverAddresses);
			Lookup l = new Lookup(searchName, Type.NS);
			l.setResolver(r);
			l.setCache(null);
			l.setSearchPath((Name[]) null);
			Record[] records = l.run();
			if (records != null && records.length > 0) {
				for (Record ns : records) {
					NSRecord sr = (NSRecord) ns;
					result.add(sr.getTarget().toString(true));
				}
				Collections.sort(result);
				return new DnsResultHolder(searchName.toString(true), result);
			}
		}
		return null;
	}

	/**
	 * Return the DNS TXT records as resolved by the resolverAddresses.
	 * 
	 * @param domainName
	 * @param resolverAddresses
	 * @return null if no TDMX ZoneRecord is found in the domain chain upwards from the domainName, or the zoneApex and
	 *         the TXT records.
	 * @throws TextParseException
	 * @throws UnknownHostException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static DnsResultHolder getTdmxZoneRecord(String domainName, List<String> resolverAddresses)
			throws TextParseException, UnknownHostException {
		List<String> result = new ArrayList<>();

		Name dn = Name.fromString(domainName);
		int numLabels = dn.labels();
		// bottom to top lookup of TXT records.
		for (int i = 0; i < numLabels - 1; i++) {
			StringBuffer b = new StringBuffer();
			for (int max = i; max < numLabels; max++) {
				String l = dn.getLabelString(max);
				b.append(l);
				b.append(".");
			}
			Name searchName = Name.fromString(b.toString());

			Resolver r = createResolver(resolverAddresses);
			Lookup l = new Lookup(searchName, Type.TXT);
			l.setResolver(r);
			l.setCache(null);
			l.setSearchPath((Name[]) null);
			Record[] records = l.run();
			if (records != null && records.length > 0) {
				for (Record ns : records) {
					TXTRecord tr = (TXTRecord) ns;
					for (String s : (List<String>) tr.getStrings()) {
						if (matchesTdmxZoneRecord(s)) {
							result.add(s);
						}
					}
				}
			}
			if (!result.isEmpty()) {
				Collections.sort(result);
				return new DnsResultHolder(searchName.toString(true), result);
			}
		}
		return null;
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
		if (answers == null || answers.length == 0) {
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
