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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.DnsDomainZone;
import org.tdmx.lib.control.domain.DomainZoneApexInfo;
import org.tdmx.lib.control.service.DnsDomainZoneService;

/**
 * The concrete implementation of {@link DomainZoneResolutionService}. This implementation uses a DB backed cache of
 * zone information for each domain successfully resolved.
 * 
 * @author Peter Klauser
 * 
 */
public class DomainZoneResolutionServiceImpl implements DomainZoneResolutionService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DomainZoneResolutionServiceImpl.class);

	private DnsZoneResolutionService dnsZoneResolutionService;
	private DnsDomainZoneService dnsDomainZoneService;
	private int dnsCacheValiditySeconds = 24 * 60 * 60; // 24hrs

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public DomainZoneApexInfo resolveDomain(String domainName) {

		DnsDomainZone storedZoneInfo = dnsDomainZoneService.findCurrentByDomain(domainName);
		if (storedZoneInfo != null) {
			return mapFrom(storedZoneInfo);
		}
		DnsDomainZone dnsInfo = dnsZoneResolutionService.resolveDomain(domainName);
		if (dnsInfo != null) {
			List<DnsDomainZone> allRecords = dnsDomainZoneService.findByDomain(domainName);
			if (allRecords.isEmpty()) {
				extendValidity(dnsInfo);
				dnsDomainZoneService.createOrUpdate(dnsInfo);
				return mapFrom(dnsInfo);
			} else {
				DnsDomainZone lastRecord = allRecords.get(0);

				if (lastRecord.matches(dnsInfo)) {
					// extend validity of existing record
					extendValidity(lastRecord);
					dnsDomainZoneService.createOrUpdate(lastRecord);
					return mapFrom(lastRecord);
				} else {
					extendValidity(dnsInfo);
					dnsDomainZoneService.createOrUpdate(dnsInfo);
					return mapFrom(dnsInfo);
				}
			}
		}

		return null;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void extendValidity(DnsDomainZone dnsInfo) {
		if (dnsInfo.getValidFromTime() == null) {
			dnsInfo.setValidFromTime(new Date());
		}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, dnsCacheValiditySeconds);
		dnsInfo.setValidUntilTime(cal.getTime());
	}

	private DomainZoneApexInfo mapFrom(DnsDomainZone dnsInfo) {
		if (dnsInfo == null) {
			return null;
		}
		DomainZoneApexInfo zi = new DomainZoneApexInfo();
		zi.setDomainName(dnsInfo.getDomainName());
		zi.setZacFingerprint(dnsInfo.getZacFingerprint());
		zi.setScsUrl(dnsInfo.getScsUrl());

		return zi;
	}
	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public DnsDomainZoneService getDnsDomainZoneService() {
		return dnsDomainZoneService;
	}

	public void setDnsDomainZoneService(DnsDomainZoneService dnsDomainZoneService) {
		this.dnsDomainZoneService = dnsDomainZoneService;
	}

	public DnsZoneResolutionService getDnsZoneResolutionService() {
		return dnsZoneResolutionService;
	}

	public void setDnsZoneResolutionService(DnsZoneResolutionService dnsZoneResolutionService) {
		this.dnsZoneResolutionService = dnsZoneResolutionService;
	}

	public int getDnsCacheValiditySeconds() {
		return dnsCacheValiditySeconds;
	}

	public void setDnsCacheValiditySeconds(int dnsCacheValiditySeconds) {
		this.dnsCacheValiditySeconds = dnsCacheValiditySeconds;
	}

}
