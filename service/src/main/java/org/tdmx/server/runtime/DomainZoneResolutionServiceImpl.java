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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.DomainZoneApexInfo;
import org.tdmx.lib.control.service.DnsDomainZoneService;

/**
 * The concrete implementation of {@link DomainZoneResolutionService}
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

	private DnsResolverGroupFactory dnsResolverGroupFactory;
	private DnsDomainZoneService dnsDomainZoneService;

	@Override
	public DomainZoneApexInfo resolveDomain(String domainName) {

		// TODO #80

		return null;
	}

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public DnsDomainZoneService getDnsDomainZoneService() {
		return dnsDomainZoneService;
	}

	public void setDnsDomainZoneService(DnsDomainZoneService dnsDomainZoneService) {
		this.dnsDomainZoneService = dnsDomainZoneService;
	}

	public DnsResolverGroupFactory getDnsResolverGroupFactory() {
		return dnsResolverGroupFactory;
	}

	public void setDnsResolverGroupFactory(DnsResolverGroupFactory dnsResolverGroupFactory) {
		this.dnsResolverGroupFactory = dnsResolverGroupFactory;
	}

}
