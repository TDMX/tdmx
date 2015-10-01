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
package org.tdmx.lib.control.dao;

import java.util.List;

import org.tdmx.lib.control.domain.DnsDomainZone;

public interface DnsDomainZoneDao {

	public void persist(DnsDomainZone value);

	public void delete(DnsDomainZone value);

	public DnsDomainZone merge(DnsDomainZone value);

	public DnsDomainZone loadById(Long id);

	/**
	 * Fetch the DnsDomainZone which is currently valid.
	 * 
	 * @param domainName
	 * @return
	 */
	public DnsDomainZone loadByCurrentDomain(String domainName);

	/**
	 * Fetch all DnsDomainZone (all historic) records for the domain.
	 * 
	 * @param domainName
	 * @return
	 */
	public List<DnsDomainZone> loadByDomain(String domainName);

}
