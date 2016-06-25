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
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelMessageSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.DestinationSession;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.EndpointPermission;
import org.tdmx.lib.zone.domain.FlowControlStatus;
import org.tdmx.lib.zone.domain.FlowQuota;
import org.tdmx.lib.zone.domain.MessageState;
import org.tdmx.lib.zone.domain.MessageStatus;
import org.tdmx.lib.zone.domain.MessageStatusSearchCriteria;
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
		RECEIVER_SERVICE_NOT_FOUND, // allowing reception only when service exists.
		RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING,
		RECEIVER_AUTHORIZATION_CONFIRMATION_PROVIDED,
		RECEIVER_AUTHORIZATION_CONFIRMATION_MISMATCH,
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
	 * - if allowing send/recv, the destination service must exist.
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
	 * - if allowing the reception, the destination service must exist.
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
	 * @return channel so that the caller knows the current flowcontrol status
	 */
	public Channel relayAuthorization(Zone zone, Long channelId, EndpointPermission otherPerm);

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
	 * @return channel so that the caller knows the current flowcontrol status
	 */
	public Channel relayInitialAuthorization(Zone zone, Long tempChannelId, EndpointPermission otherPerm);

	/**
	 * Creates (persists) the Channel.
	 * 
	 * NOTE: this is only called by {@link ZoneTransferTask}, since Channel's are normally created by
	 * {@link #setAuthorization(Zone, Domain, ChannelOrigin, ChannelDestination, ChannelAuthorization)}
	 * 
	 * @param channel
	 */
	public void create(Channel channel);

	/**
	 * Creates (persists) the TemporaryChannel.
	 * 
	 * NOTE: called by {@link SCS#getMRSSession(org.tdmx.core.api.v01.scs.GetMRSSession)} when the channel does not
	 * exist and relaying wants to create a session on the Channel.
	 * 
	 * @param channel
	 */
	public void create(TemporaryChannel channel);

	/**
	 * Creates (persists) a ChannelMessage.
	 * 
	 * NOTE: use the {@link ChannelService#preRelayInMessage(Zone, ChannelMessage)} and
	 * {@link ChannelService#preSubmitMessage(Zone, ChannelMessage)} to do pre-persistance updates.
	 * 
	 * @param channel
	 */
	public void create(ChannelMessage message);

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

	/**
	 * Search for Channels.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<Channel> search(Zone zone, ChannelAuthorizationSearchCriteria criteria);

	/**
	 * Search for TemporaryChannels.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<TemporaryChannel> search(Zone zone, TemporaryChannelSearchCriteria criteria);

	/**
	 * Search for Messages.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<ChannelMessage> search(Zone zone, ChannelMessageSearchCriteria criteria);

	/**
	 * Search for Messages with a state criteria - FetchPlan gets the ChannelMessage.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<ChannelMessage> search(Zone zone, MessageStatusSearchCriteria criteria);

	/**
	 * Searches references of MessageStatus with a state criteria.
	 * 
	 * @param zone
	 * @param criteria
	 * @return
	 */
	public List<Long> getStatusReferences(Zone zone, MessageStatusSearchCriteria criteria, int maxResults);

	/**
	 * Fetch the ChannelMessage which has the MessageState with the stateId provided. No fetch plan.
	 * 
	 * @param zone
	 * @param stateId
	 * @param fetchChannel
	 *            whether to fetch the message's channel too.
	 * @return
	 */
	public ChannelMessage findByStateId(Long stateId, boolean fetchChannel);

	/**
	 * Fetch the ChannelMessage which has the messageId provided. FetchPlan includes MessageState.
	 * 
	 * @param zone
	 * @param msgId
	 * @return
	 */
	public ChannelMessage findByMessageId(Long msgId);

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
	 * @return the updated Channel who's processing state defines if relay of the changed DS is necessary.
	 */
	public Channel setChannelDestinationSession(Zone zone, Long channelId, DestinationSession destinationSession);

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

	/**
	 * Delete a ChannelMessage.
	 * 
	 * @param message
	 *            the message
	 */
	public void delete(ChannelMessage message);

	public enum SubmitMessageOperationStatus {
		MESSAGE_TOO_LARGE,
		NOT_ENOUGH_QUOTA_AVAILABLE,
		FLOW_CONTROL_CLOSED,
		CHANNEL_CLOSED,
	}

	/**
	 * A context holder returning the FlowQuota and status of the submit to the caller ( MOS or MRS ).
	 * 
	 * @author Peter
	 *
	 */
	public class SubmitMessageResultHolder {
		public FlowQuota flowQuota;
		public SubmitMessageOperationStatus status;
	}

	/**
	 * Check that the Channel is open and has the required free quota available.
	 * 
	 * @param zone
	 * @param channel
	 *            detached Channel
	 * @param messageSize
	 *            the payload size of the message being checked
	 * @param totalRequiredQuota
	 *            the total payload sizes of messages being sent in the same transaction to the channel.
	 * @return whether the channel is usable and has the free quota available.
	 */
	public SubmitMessageResultHolder checkChannelQuota(Zone zone, Channel channel, long messageSize,
			long totalRequiredQuota);

	/**
	 * Commit messages immediately skipping the prepared step. Reduces the flow quota.
	 * 
	 * @param zone
	 * @param channel
	 * @param messages
	 */
	public void onePhaseCommitSend(Zone zone, Channel channel, List<ChannelMessage> messages);

	/**
	 * Prepare messages for sending. Reduces the flow quota. ChannelMessages are persisted with the xid for eventual
	 * later recovery. The MessageStatus is set to {@link MessageStatus#UPLOADED}.
	 * 
	 * @param zone
	 * @param channel
	 * @param messages
	 *            not yet persisted ChannelMessages.
	 * @param xid
	 */
	public void twoPhasePrepareSend(Zone zone, Channel channel, List<ChannelMessage> messages, String xid);

	/**
	 * Discover the XID of the prepared transactions for the channel origin, by looking over messages in status
	 * {@link MessageStatus#UPLOADED}.
	 * 
	 * @param zone
	 * @param origin
	 * @param originSerialNr
	 * @return
	 */
	public List<String> twoPhaseRecoverSend(Zone zone, ChannelOrigin origin, int originSerialNr);

	/**
	 * Commit messages for sending. Flow quota already reduced by prepare. Clears the xid from the already persisted
	 * messages. The MessageStatus becomes {@link MessageStatus#READY} for same domain submits, or
	 * {@link MessageStatus#SUBMITTED} for the normal case.
	 * 
	 * @param zone
	 * @param origin
	 * @param originSerialNr
	 * @param xid
	 * @return the list of messages committed.
	 */
	public List<MessageState> twoPhaseCommitSend(Zone zone, ChannelOrigin origin, int originSerialNr, String xid);

	/**
	 * Rollback messages for prepared for sending. Flow quota already reduced by prepare, so undo that. Removes the
	 * messages.
	 * 
	 * @param zone
	 * @param origin
	 * @param originSerialNr
	 * @param xid
	 * @param the
	 *            list of messages discarded.
	 */
	public List<MessageState> twoPhaseRollbackSend(Zone zone, ChannelOrigin origin, int originSerialNr, String xid);

	/**
	 * Relay a Message inbound called on the receiver side. Updates the FlowQuota of the channel (increasing
	 * undelivered).
	 * 
	 * @param zone
	 * @param msg
	 *            detached ChannelMessage
	 * @return the modified FlowQuota
	 */
	public FlowQuota relayMessage(Zone zone, ChannelMessage msg);

	/**
	 * Relay a FC-open.
	 * 
	 * @param zone
	 * @param quotaId
	 *            id of the channel's FlowQuota.
	 * @return the modified FlowQuota
	 */
	public FlowQuota relayFlowControlOpen(Zone zone, Long quotaId);

	/**
	 * Post-relay Message updates the FlowQuota of the channel. ChannelMessage is deleted, freeing used channel quota.
	 * 
	 * @param zone
	 * @param msg
	 *            detached ChannelMessage
	 * @param relayStatus
	 *            the other side's relay status known after we send out the message.
	 */
	public void onePhaseCommitRelaySend(Zone zone, ChannelMessage msg, FlowControlStatus relayStatus);

	public class ReceiveMessageResultHolder {
		public boolean flowControlOpened;
		public FlowQuota flowQuota;
		public ChannelMessage msg;
	}

	/**
	 * Fetch the ChannelMessage for receive. Updates the MessageStatus to {@link MessageStatus#RECEIVING}
	 * 
	 * @param zone
	 * @param stateId
	 * @param txTimeoutSec
	 * @return
	 */
	public ReceiveMessageResultHolder receiveMessage(Long stateId, int txTimeoutSec);

	/**
	 * Message acknowledge after successful receipt. Updates the FlowQuota of the channel ( increasing the quota
	 * available on destination side ). This could lead to the channel flow control status changing from closed to open.
	 * The MessageStatus returned indicates {@link MessageStatus#DELETED}.
	 * 
	 * @param zone
	 * @param msg
	 *            detached ChannelMessage
	 * @return
	 */
	public ReceiveMessageResultHolder onePhaseCommitReceive(Zone zone, ChannelMessage msg);

	/**
	 * Message negative-acknowledge. The MessageStatus returned indicates {@link MessageStatus#DELETED} or
	 * {@link MessageStatus#REDELIVER} if the max re-deliveries has not been exceeded. If deleted, the FlowQuota of the
	 * channel is increased on destination side.
	 * 
	 * @param zone
	 * @param msg
	 *            detached ChannelMessage
	 * @return
	 */
	public ReceiveMessageResultHolder onePhaseRollbackReceive(Zone zone, ChannelMessage msg);

	/**
	 * Prepare a message receive. Increases the flow quota. ChannelMessage is persisted as
	 * {@link MessageStatus#DOWNLOADED} with the xid for eventual later recovery.
	 * 
	 * @param zone
	 * @param channel
	 * @param message
	 *            the ChannelMessage to mark as downloaded.
	 * @param xid
	 * @return
	 */
	public ReceiveMessageResultHolder twoPhasePrepareReceive(Zone zone, ChannelMessage message, String xid);

	/**
	 * Discover the XID of the prepared transactions for the channel origin.
	 * 
	 * @param zone
	 * @param destination
	 * @param destinationSerialNr
	 * @return the list of XIDs which are prepared.
	 */
	public List<String> twoPhaseRecoverReceive(Zone zone, ChannelDestination destination, int destinationSerialNr);

	/**
	 * Commit messages for sending, effectively deleting the message. Flow quota already increased by prepare. Clears
	 * the XID from the already persisted messages. The MessageStatus returned indicates {@link MessageStatus#DELETED}.
	 * 
	 * @param zone
	 * @param destination
	 * @param xid
	 * @return the message committed (deleted - should be only 1).
	 */
	public List<MessageState> twoPhaseCommitReceive(Zone zone, ChannelDestination destination, int destinationSerialNr,
			String xid);

	/**
	 * Rollback message prepared for receive. Flow quota already increased by prepare, so undo that. Message status is
	 * changed to {@link MessageStatus#REDELIVER} in case it is to be recycled, otherwise the state is
	 * {@link MessageStatus#DELETED}
	 * 
	 * @param zone
	 * @param destination
	 * @param xid
	 * @return the message recycled or discarded (should be only 1).
	 */
	public List<MessageState> twoPhaseRollbackReceive(Zone zone, ChannelDestination destination,
			int destinationSerialNr, String xid);

	/**
	 * Update the ProcessingState of the Channel's DestinationSession.
	 * 
	 * @param channelId
	 * @param newState
	 */
	public void updateStatusDestinationSession(Long channelId, ProcessingState newState);

	/**
	 * Update the ProcessingState of the Channel's ChannelAuthorization.
	 * 
	 * @param channelId
	 * @param newState
	 */
	public void updateStatusChannelAuthorization(Long channelId, ProcessingState newState);

	/**
	 * Update the ProcessingState of the Channel's FlowControl change.
	 * 
	 * @param quotaId
	 * @param newState
	 * @return the modified FlowQuota
	 */
	public FlowQuota updateStatusFlowQuota(Long quotaId, ProcessingState newState);

	/**
	 * Update the ProcessingState of a MessageState.
	 * 
	 * @param stateId
	 * @param newState
	 */
	public void updateMessageProcessingState(Long stateId, ProcessingState newState);

	/**
	 * Update the ProcessingState of a MessageState.
	 * 
	 * @param stateId
	 * @param status
	 * @param xid
	 * @param newState
	 */
	public void updateMessageProcessingState(Long stateId, MessageStatus status, String xid, ProcessingState newState);
}
