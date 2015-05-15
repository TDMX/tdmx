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

	public enum SetAuthorizationOperationStatus {
		SENDER_AUTHORIZATION_CONFIRMATION_MISSING,
		SENDER_AUTHORIZATION_CONFIRMATION_MISMATCH,
		SENDER_AUTHORIZATION_CONFIRMATION_PROVIDED,
		RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING,
		RECEIVER_AUTHORIZATION_CONFIRMATION_PROVIDED,
		RECEIVER_AUTHORIZATION_CONFIRMATION_MISMATCH,
		SUCCESS
	}

	/**
	 * Process the ChannelAuthorization set by a client DAC. The logic is that the local DAC can always set the domain
	 * agent's endpoint permissions, but it must always confirm any remote domain's requested authorization (which is
	 * pending authorization). Confirmation does not imply "ALLOW".
	 * 
	 * lookup any existing ChannelAuthorization in the domain given the provided channel(origin+destination). If no
	 * existing ca - then create one with empty data. decide if
	 * 
	 * 1) setting send&recvAuth on same domain channel
	 * 
	 * - no requested send/recv allowed in existing ca.
	 * 
	 * or 2) sendAuth(+confirm requested recvAuth)
	 * 
	 * - no reqSendAuth allowed in existing ca.
	 * 
	 * - change of sendAuth vs existing sendAuth forces transfer
	 * 
	 * or 3) recvAuth(+confirming requested sendAuth)
	 * 
	 * - no reqRecvAuth allowed in existing ca.
	 * 
	 * - change of recvAuth vs existing recvAuth forces transfer(relay if different SP)
	 * 
	 * persist the new or updated ca.
	 * 
	 * 
	 * @param auth
	 * @return
	 */
	public SetAuthorizationOperationStatus setAuthorization(Zone zone, ChannelAuthorization auth);

	public void createOrUpdate(ChannelAuthorization auth);

	public ChannelAuthorization findByChannel(Zone zone, String domainName, ChannelOrigin origin,
			ChannelDestination dest);

	public ChannelAuthorization findById(Long id);

	public List<ChannelAuthorization> search(Zone zone, ChannelAuthorizationSearchCriteria criteria);

	public void delete(ChannelAuthorization auth);

}
