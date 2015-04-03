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

import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Management Services for a ChannelAuthorization.
 * 
 * @author Peter
 * 
 */
public interface ChannelAuthorizationService {

	public void createOrUpdate(ChannelAuthorization auth);

	public ChannelAuthorization findByChannel(Zone zone, String domainName, ChannelOrigin origin,
			ChannelDestination dest);

	public ChannelAuthorization findById(Long id);

	public List<ChannelAuthorization> search(Zone zone, ChannelAuthorizationSearchCriteria criteria);

	public void delete(ChannelAuthorization auth);

}
