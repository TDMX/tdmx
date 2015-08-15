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

import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.mrs.ws.MRS;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelFlowMessageSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.DestinationSession;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.EndpointPermission;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.TemporaryChannelSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Management Services for a Channels and it's ChannelAuthorization and ChannelFlowTargets.
 * 
 * @author Peter
 * 
 */
public interface ChannelService {

	public enum SetAuthorizationOperationStatus {
		SENDER_AUTHORIZATION_CONFIRMATION_MISSING,
		SENDER_AUTHORIZATION_CONFIRMATION_MISMATCH,
		SENDER_AUTHORIZATION_CONFIRMATION_PROVIDED,
		SENDER_UNSENT_BUFFER_LIMIT_MISSING,
		SENDER_UNSENT_BUFFER_LIMIT_PROVIDED,
		RECEIVER_SERVICE_NOT_FOUND, // allowing reception only when service exists.
		RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING,
		RECEIVER_AUTHORIZATION_CONFIRMATION_PROVIDED,
		RECEIVER_AUTHORIZATION_CONFIRMATION_MISMATCH,
		RECEIVER_UNDELIVERED_BUFFER_LIMIT_MISSING,
		RECEIVER_UNDELIVERED_BUFFER_LIMIT_PROVIDED,
	}

	public class SetAuthorizationResultHolder {
		public SetAuthorizationOperationStatus status;
		public ChannelAuthorization channelAuthorization;
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
	 * - No requested send/recv allowed in existing ca.
	 * 
	 * - if allowing send/recv, the destination service must exist. TODO
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
	 * - if allowing the reception, the destination service must exist. TODO
	 * 
	 * - change of recvAuth vs existing recvAuth forces transfer(relay if different SP)
	 * 
	 * persist the new or updated ca.
	 * 
	 * set the processingstate to PENDING will require the caller (non tx layer) to submit the CA to the relay service.
	 * 
	 * 
	 * @param channel
	 *            detached channel
	 * @return
	 */
	public SetAuthorizationResultHolder setAuthorization(Zone zone, Domain domain, ChannelOrigin origin,
			ChannelDestination dest, ChannelAuthorization auth);

	/**
	 * Relayed in EndpointPermission.
	 * 
	 * The EndpointPermission relayed in is set as either the reqSendPermission or reqRecvPermission, which must be
	 * later confirmed by the domain administrator using
	 * {@link ChannelService#setAuthorization(Zone, Domain, ChannelOrigin, ChannelDestination, ChannelAuthorization)}.
	 * 
	 * @param zone
	 * @param channelId
	 * @param otherPerm
	 */
	public void relayAuthorization(Zone zone, Long channelId, EndpointPermission otherPerm);

	/**
	 * Initial relayed in EndpointPermission.
	 * 
	 * Creates the Channel, deletes the TemporaryChannel, and the EndpointPermission relayed in is set as either the
	 * reqSendPermission or reqRecvPermission, which must be later confirmed by the domain administrator using
	 * {@link ChannelService#setAuthorization(Zone, Domain, ChannelOrigin, ChannelDestination, ChannelAuthorization)}.
	 * 
	 * @param zone
	 * @param tempChannelId
	 * @param otherPerm
	 */
	public Channel relayInitialAuthorization(Zone zone, Long tempChannelId, EndpointPermission otherPerm);

	public void create(Channel channel);

	public void create(TemporaryChannel channel);

	/**
	 * Lookup a TemporaryChannel in the Domain.
	 * 
	 * @param zone
	 * @param domain
	 * @param origin
	 * @param dest
	 * @return null if none exist, otherwise the TemporaryChannel matching the origin and destination.
	 */
	public TemporaryChannel findByTemporaryChannel(Zone zone, Domain domain, ChannelOrigin origin,
			ChannelDestination dest);

	public ChannelAuthorization findByChannel(Zone zone, Domain domain, ChannelOrigin origin, ChannelDestination dest);

	/**
	 * Fetch the Channel incl. FlowQuota
	 * 
	 * @param id
	 * @param includeFlowQuota
	 *            fetchPlan
	 * @param includeAuth
	 *            fetchPlan
	 * @return
	 */
	public Channel findById(Long id, boolean includeFlowQuota, boolean includeAuth);

	public List<Channel> search(Zone zone, ChannelAuthorizationSearchCriteria criteria);

	public List<TemporaryChannel> search(Zone zone, TemporaryChannelSearchCriteria criteria);

	public List<ChannelMessage> search(Zone zone, ChannelFlowMessageSearchCriteria criteria);

	/**
	 * Fetch the ChannelMessage which has the messageId provided.
	 * 
	 * @param zone
	 * @param msgId
	 * @return
	 */
	public ChannelMessage findByMessageId(Zone zone, String msgId);

	/**
	 * Fetch the TemporaryChannel which has the id provided.
	 * 
	 * @param findByTempChannelId
	 * @return
	 */
	public TemporaryChannel findByTempChannelId(Long tempChannelId);

	/**
	 * Adds or updates the DestinationSession within a Channel.
	 * 
	 * This is called on the receiving end of the channel by the target's
	 * {@link MDS#setDestinationSession(org.tdmx.core.api.v01.mds.SetDestinationSession)} propagation to each channel.
	 * 
	 * @param zone
	 *            the zone
	 * @param channelId
	 *            id of the channel
	 * @param destinationSession
	 */
	public void setChannelDestinationSession(Zone zone, Long channelId, DestinationSession destinationSession);

	/**
	 * Updates the DestinationSession within a Channel.
	 * 
	 * This is called on the sending end by the relay in of a remote channel DestinationSession via
	 * {@link MRS#relay(org.tdmx.core.api.v01.mrs.Relay)}.
	 * 
	 * @param zone
	 *            the zone
	 * @param channelId
	 *            id of the channel
	 * @param destinationSession
	 */
	public void relayChannelDestinationSession(Zone zone, Long channelId, DestinationSession destinationSession);

	/**
	 * Delete the Channel, cascades to the ChannelAuthorization and FlowQuota and ChannelMessages.
	 * 
	 * @param channel
	 */
	public void delete(Channel channel);

	/**
	 * Delete a TemporaryChannel.
	 * 
	 * @param tempChannel
	 *            the temporary channel
	 */
	public void delete(TemporaryChannel tempChannel);

	public enum SubmitMessageOperationStatus {
		FLOW_CONTROL_CLOSED,
		CHANNEL_CLOSED,
	}

	public class SubmitMessageResultHolder {
		public SubmitMessageOperationStatus status;
		public ChannelMessage message;
	}

	/**
	 * Submit a Message outbound called on the sender side.
	 * 
	 * @param zone
	 * @param msg
	 *            detached ChannelMessage
	 * @return
	 */
	public SubmitMessageResultHolder submitMessage(Zone zone, ChannelMessage msg);

	/**
	 * Relay a Message inbound called on the receiver side.
	 * 
	 * @param zone
	 * @param msg
	 *            detached ChannelMessage
	 * @return
	 */
	public SubmitMessageResultHolder relayMessage(Zone zone, ChannelMessage msg);

}
