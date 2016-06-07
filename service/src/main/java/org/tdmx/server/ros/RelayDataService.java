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
package org.tdmx.server.ros;

import java.util.List;

import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.FlowControlStatus;
import org.tdmx.lib.zone.domain.MessageStatus;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Fetches data from zone services.
 * 
 * @author Peter
 *
 */
public interface RelayDataService {

	public AccountZone getAccountZone(Long accountZoneId);

	public Zone getZone(AccountZone az, Long zoneId);

	public Domain getDomain(AccountZone az, Zone z, Long domainId);

	public Channel getChannel(AccountZone az, Zone z, Domain d, Long channelId);

	/**
	 * Get the ChannelMessage and it's associated MessageState from a given stateId.
	 * 
	 * @param az
	 * @param z
	 * @param d
	 * @param channel
	 * @param stateId
	 * @return
	 */
	public ChannelMessage getMessage(AccountZone az, Zone z, Domain d, Channel channel, Long stateId);

	/**
	 * Outbound ChannelMessages to be relayed on origin side have {@link MessageStatus#SUBMITTED} with a pending
	 * {@see ChannelMessage#getProcessingState()}.
	 * 
	 * @param az
	 * @param z
	 * @param d
	 * @param channel
	 * @param maxMsg
	 *            fetch up to this number of pending messages.
	 * @return the list of stateIds
	 */
	public List<Long> getRelayMessages(AccountZone az, Zone z, Domain d, Channel channel, int maxMsg);

	/**
	 * Update post successful relay out of a message. Message is deleted and quota freed. The relayStatus (closing) of
	 * the destination side is stored locally, to be opened later by a relay in from the other side.
	 * 
	 * @param az
	 * @param z
	 * @param d
	 * @param msg
	 * @param relayStatus
	 */
	public void updatePostRelayChannelMessage(AccountZone az, Zone z, Domain d, ChannelMessage msg,
			FlowControlStatus relayStatus);

	public void updateMessageProcessingState(AccountZone az, Zone z, Domain d, Channel channel, Long stateId,
			ProcessingState newState);

	public void updateChannelAuthorizationProcessingState(AccountZone az, Zone z, Domain d, Long channelId,
			ProcessingState newState);

	public void updateChannelDestinationSessionProcessingState(AccountZone az, Zone z, Domain d, Long channelId,
			ProcessingState newState);

	public void updateChannelFlowControlProcessingState(AccountZone az, Zone z, Domain d, Long quotaId,
			ProcessingState newState);
}
