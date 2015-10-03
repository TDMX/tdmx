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

import org.tdmx.lib.control.domain.DnsDomainZone;

/**
 * Service for retrieval of TDMX information from DNS using {@link DnsResolverGroupFactory}.
 * 
 * TrustPolicy:
 * 
 * The DNS resolvers specified by {@link DnsResolverGroupFactory} are each used to lookup the DNS information.
 * 
 * If all DNS resolver groups identify the SAME authoritative name servers for the domainName, and all of the resolvers
 * groups corroborate the same TDMX zone information then we trust it.
 * 
 * @author Peter
 * 
 */
public interface DnsZoneResolutionService {

	/**
	 * Determine the DomainZoneApexInfo for the domain from DNS using multiple resolver groups supplied by
	 * {@link DnsResolverGroupFactory} and a TODO trust policy.
	 * 
	 * @param domainName
	 * @return null if no TDMX info found and trusted, else the domain's TDMX zone root info.
	 */
	public DnsDomainZone resolveDomain(String domainName);

}
