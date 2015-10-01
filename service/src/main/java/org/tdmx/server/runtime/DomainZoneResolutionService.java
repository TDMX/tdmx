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

import org.tdmx.lib.control.domain.DomainZoneApexInfo;

/**
 * Service for retrieval of TDMX information for all Domains.
 * 
 * @author Peter
 * 
 */
public interface DomainZoneResolutionService {

	/**
	 * Determine the DomainZoneApexInfo about the domain by recursive lookup of the domain until the domain's top-level
	 * domain (root). If the domain has no TDMX information anchored at any level at or above itself in DNS, then return
	 * null.
	 * 
	 * @param domainName
	 * @return null if no TDMX info found, else the domain's TDMX zone root info.
	 */
	public DomainZoneApexInfo resolveDomain(String domainName);

}
