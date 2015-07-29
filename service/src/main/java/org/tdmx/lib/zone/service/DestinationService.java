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
package org.tdmx.lib.zone.service;

import java.util.List;

import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.Destination;
import org.tdmx.lib.zone.domain.DestinationSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Management Services for a Destination.
 * 
 * @author Peter
 * 
 */
public interface DestinationService {

	public void createOrUpdate(Destination flowTarget);

	/**
	 * Modifies the Destination's DestinationSession, creating the Destination if necessary.
	 * 
	 * The Destination provided must identify the Address and Service. If a Destination exists then the Session
	 * information is updated, otherwise the Destination is created. The DestinationSession is propagated as the
	 * ChannelDestinationSession to all Channels which have the Destination as the Destination and are "open", ie. have
	 * ChannelAuthorizations which allow communications.
	 * 
	 * @param ft
	 *            detached Destination
	 */
	public void setSession(Destination ft);

	public Destination findByDestination(Address address, Service service);

	public Destination findById(Long id);

	/**
	 * Find FlowTargets given the criteria. FetchPlan includes Domain, Service, Target and Target's address.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<Destination> search(Zone zone, DestinationSearchCriteria criteria);

	public void delete(Destination auth);

}
