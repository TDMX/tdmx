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

package org.tdmx.server.runtime;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.dns.DnsUtils;
import org.tdmx.core.system.dns.DnsUtils.DnsResultHolder;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;
import org.tdmx.lib.control.domain.DnsDomainZone;
import org.tdmx.lib.control.domain.DnsResolverGroup;
import org.xbill.DNS.TextParseException;

/**
 * The concrete implementation of {@link DnsZoneResolutionService}
 * 
 * @author Peter Klauser
 * 
 */
public class DnsZoneResolutionServiceImpl implements DnsZoneResolutionService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DnsZoneResolutionServiceImpl.class);

	private DnsResolverGroupFactory dnsResolverGroupFactory;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public DnsDomainZone resolveDomain(String domainName) {

		List<DnsResolverGroup> resolverGroups = dnsResolverGroupFactory.getDnsResolverGroups();
		if (resolverGroups.isEmpty()) {
			log.warn("No DNS resolver groups defined.");
			return null;
		}
		List<DnsDomainZone> foundZoneInfos = new ArrayList<>();

		for (DnsResolverGroup resolver : resolverGroups) {
			DnsDomainZone zoneInfo = lookupTdmxZone(domainName, resolver);
			if (zoneInfo != null) {
				foundZoneInfos.add(zoneInfo);
			}
		}

		if (foundZoneInfos.isEmpty()) {
			log.debug("No TDMX zone information found in DNS for " + domainName);
			return null;
		}

		if (!hasIdenticalZoneInformation(domainName, foundZoneInfos)) {
			log.info("DNS disagreement for " + domainName);
			return null;
		}

		return foundZoneInfos.get(0);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private boolean hasIdenticalZoneInformation(String domainName, List<DnsDomainZone> zoneInfos) {
		DnsDomainZone zi = zoneInfos.get(0);

		for (DnsDomainZone zone : zoneInfos) {
			if (!zi.getZoneApex().equals(zone.getZoneApex())) {
				log.debug("DNS disagreement for zoneApex for " + domainName);
				return false;
			}
			if (!zi.getNameServerAddresses().containsAll(zone.getNameServerAddresses())
					|| !zone.getNameServerAddresses().containsAll(zi.getNameServerAddresses())) {
				log.debug("DNS name server disagreement for " + domainName);
				return false;
			}
			if (!zi.getScsUrl().equals(zone.getScsUrl())) {
				log.debug("DNS disagreement for scsUrl for " + domainName);
				return false;
			}
			if (!zi.getZacFingerprint().equals(zone.getZacFingerprint())) {
				log.debug("DNS disagreement for ZAC fingerprint for " + domainName);
				return false;
			}
			if (zi.getVersion() != zone.getVersion()) {
				log.debug("DNS disagreement for version for " + domainName);
				return false;
			}
		}
		return true;
	}

	private DnsDomainZone lookupTdmxZone(String domainName, DnsResolverGroup resolver) {
		DnsResultHolder result = getTdmxZoneRecord(domainName, resolver);

		if (result != null) {
			DnsDomainZone zoneInfo = new DnsDomainZone();
			zoneInfo.setDomainName(domainName);
			zoneInfo.setZoneApex(result.getApex());

			TdmxZoneRecord zr = DnsUtils.parseTdmxZoneRecord(result.getApex(), result.getRecords().get(0));
			zoneInfo.setScsUrl(zr.getScsUrl());
			zoneInfo.setZacFingerprint(zr.getZacFingerprint());
			zoneInfo.setVersion(zr.getVersion());

			DnsResultHolder nsResult = getNameServerRecords(domainName, resolver);
			if (nsResult != null) {
				zoneInfo.setNameServerAddresses(nsResult.getRecords());

				return zoneInfo;
			}
		}
		return null;
	}

	private DnsResultHolder getTdmxZoneRecord(String domainName, DnsResolverGroup resolver) {
		try {
			DnsResultHolder result = DnsUtils.getTdmxZoneRecord(domainName, resolver.getIpAddresses());
			if (result.getRecords() == null || result.getRecords().isEmpty()) {
				log.warn("No TXT records found for " + domainName);
				return null;
			}
			if (result.getRecords().size() > 1) {
				log.warn("Multiple TXT records found for " + domainName);
				return null;
			}
			return result;
		} catch (TextParseException | UnknownHostException e) {
			log.warn("Unable to getTdmxZoneRecord for " + domainName, e);
		}
		return null;
	}

	private DnsResultHolder getNameServerRecords(String domainName, DnsResolverGroup resolver) {
		try {
			DnsResultHolder result = DnsUtils.getNameServers(domainName, resolver.getIpAddresses());
			if (result.getRecords() == null || result.getRecords().isEmpty()) {
				log.warn("No NS records found for " + domainName);
				return null;
			}
			return result;
		} catch (TextParseException | UnknownHostException e) {
			log.warn("Unable to getNameServerRecords for " + domainName, e);
		}
		return null;
	}
	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public DnsResolverGroupFactory getDnsResolverGroupFactory() {
		return dnsResolverGroupFactory;
	}

	public void setDnsResolverGroupFactory(DnsResolverGroupFactory dnsResolverGroupFactory) {
		this.dnsResolverGroupFactory = dnsResolverGroupFactory;
	}

}
