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

import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelMessageSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.MessageState;
import org.tdmx.lib.zone.domain.MessageStatusSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

/**
 * DAO for the ChannelMessage Entity.
 * 
 * @author Peter
 * 
 */
public interface MessageDao {

	public void persist(ChannelMessage value);

	public void delete(ChannelMessage value);

	/**
	 * Return the list of prepared XIDs for the sender on ChannelOrigin.
	 * 
	 * @param zone
	 * @param origin
	 * @param originSerialNr
	 * @return the list of prepared XIDs for the sender on ChannelOrigin.
	 */
	List<String> getPreparedSendTransactions(Zone zone, ChannelOrigin origin, int originSerialNr);

	/**
	 * Return the list of prepared XIDs for the receiver on ChannelDestination.
	 * 
	 * @param zone
	 * @param destination
	 * @param destinationSerialNr
	 * @return the list of prepared XIDs for the receiver on ChannelDestination.
	 */
	List<String> getPreparedReceiveTransactions(Zone zone, ChannelDestination destination, int destinationSerialNr);

	/**
	 * Load the ChannelMessage by id.
	 * 
	 * @param id
	 * @return the ChannelMessage or null if not found.
	 */
	public ChannelMessage loadById(Long id, boolean fetchState);

	/**
	 * Load the ChannelMessageState by id.
	 * 
	 * @param stateId
	 * @param whether
	 *            to fetch the associated ChannelMessage
	 * @param whether
	 *            to fetch the associated Channel and it's FlowQuota
	 * @return the ChannelMessageState or null if not found.
	 */
	public MessageState loadStateById(Long id, boolean fetchMsg, boolean fetchChannel);

	/**
	 * Search for ChannelMessages. FetchPlan includes Domain, Channel, ChannelMessageStatus.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<ChannelMessage> search(Zone zone, ChannelMessageSearchCriteria criteria);

	/**
	 * Search for ChannelMessageStates.
	 * 
	 * @param zone
	 * @param criteria
	 * @param fetchMsg
	 *            whether to pre-fetch the associated ChannelMessage.
	 * @return
	 */
	public List<MessageState> search(Zone zone, MessageStatusSearchCriteria criteria, boolean fetchMsg);

	/**
	 * Search for MessageState ids.
	 * 
	 * @param zone
	 * @param criteria
	 * @param maxResults
	 * @return
	 */
	public List<Long> getReferences(Zone zone, MessageStatusSearchCriteria criteria, int maxResults);
}
