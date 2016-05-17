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

import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.Destination;
import org.tdmx.lib.zone.domain.DestinationSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;

/**
 * DAO for the Destination Entity.
 * 
 * @author Peter
 * 
 */
public interface DestinationDao {

	public void persist(Destination value);

	public void delete(Destination value);

	public Destination merge(Destination value);

	public Destination loadById(Long id);

	public Destination loadByDestination(Address address, Service service);

	public Destination loadByChannelDestination(Zone zone, ChannelDestination dest);

	public List<Destination> search(Zone zone, DestinationSearchCriteria criteria);
}
