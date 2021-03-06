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
package org.tdmx.lib.zone.dao;

import java.util.List;

import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

/**
 * DAO for the Domain Entity.
 * 
 * @author Peter
 * 
 */
public interface DomainDao {

	public void persist(Domain value);

	public void delete(Domain value);

	public Domain merge(Domain value);

	/**
	 * Return the Domain with the id provided. No fetch plan.
	 * 
	 * @param id
	 * @return
	 */
	public Domain loadById(Long id);

	/**
	 * Search for the Domains matching the search criteria. No fetch plan.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<Domain> search(Zone zone, DomainSearchCriteria criteria);
}
