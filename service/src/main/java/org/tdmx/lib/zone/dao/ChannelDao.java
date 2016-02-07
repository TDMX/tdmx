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

import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelMessageSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelSearchCriteria;
import org.tdmx.lib.zone.domain.FlowQuota;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.TemporaryChannelSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

/**
 * DAO for the Channel Entity.
 * 
 * @author Peter
 * 
 */
public interface ChannelDao {

	public void persist(Channel value);

	public void persist(TemporaryChannel value);

	public Channel merge(Channel value);

	public void persist(ChannelMessage value);

	public void updateChannelMessageProcessingState(Long msgId, ProcessingState ps);

	public void delete(Channel value);

	public void delete(TemporaryChannel value);

	public void delete(ChannelMessage value);

	public FlowQuota lock(Long quotaId);

	public Channel loadById(Long id, boolean includeFlowQuota, boolean includeAuth);

	public TemporaryChannel loadByTempId(Long tempChannelId);

	/**
	 * Fetch the ChannelMessage ( no fetch plan ) by messageId.
	 * 
	 * @param zone
	 * @param messageId
	 * @return the ChannelMessage or null if not found.
	 */
	public ChannelMessage loadChannelMessageByMessageId(Long msgId);

	/**
	 * Search for Channels. FetchPlan includes ChannelAuthorizations, FlowQuota and Domain.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<Channel> search(Zone zone, ChannelAuthorizationSearchCriteria criteria);

	/**
	 * Search for Channels. No FetchPlan.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<Channel> search(Zone zone, ChannelSearchCriteria criteria);

	/**
	 * Search for TemporaryChannels. No FetchPlan.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<TemporaryChannel> search(Zone zone, TemporaryChannelSearchCriteria criteria);

	/**
	 * Search for ChannelFlowMessages. FetchPlan includes Channel, Domain, unless the channel is provided explicitly, in
	 * which case we only fetch the ChannelMessage.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<ChannelMessage> search(Zone zone, ChannelMessageSearchCriteria criteria);

}
