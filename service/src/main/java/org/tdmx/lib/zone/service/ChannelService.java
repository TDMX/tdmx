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
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelFlowTarget;
import org.tdmx.lib.zone.domain.ChannelFlowTargetDescriptor;
import org.tdmx.lib.zone.domain.ChannelFlowTargetSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.ChannelSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.EndpointPermission;
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
		RECEIVER_AUTHORIZATION_CONFIRMATION_MISMATCH
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
	 * Creates ChannelAuthorization if no Channel exists, otherwise the EndpointPermission relayed in is set as either
	 * the reqSendPermission or reqRecvPermission, which must be later confirmed by the domain administrator using
	 * {@link ChannelService#setAuthorization(Zone, Domain, ChannelOrigin, ChannelDestination, ChannelAuthorization)}.
	 * 
	 * @param zone
	 * @param domain
	 * @param origin
	 * @param dest
	 * @param otherPerm
	 */
	public void relayAuthorization(Zone zone, Domain domain, ChannelOrigin origin, ChannelDestination dest,
			EndpointPermission otherPerm);

	public void createOrUpdate(Channel channel);

	public ChannelAuthorization findByChannel(Zone zone, Domain domain, ChannelOrigin origin, ChannelDestination dest);

	public Channel findById(Long id);

	public List<ChannelAuthorization> search(Zone zone, ChannelAuthorizationSearchCriteria criteria);

	public List<ChannelFlowTarget> search(Zone zone, ChannelFlowTargetSearchCriteria criteria);

	public List<Channel> search(Zone zone, ChannelSearchCriteria criteria);

	/**
	 * Adds or updates the FlowTarget as ChannelFlowTarget within a Channel.
	 * 
	 * This is called on the receiving end of the channel by the target's
	 * {@link MDS#setFlowTargetSession(org.tdmx.core.api.v01.mds.SetFlowTargetSession)} propagation to each channel.
	 * 
	 * @param zone
	 *            the zone
	 * @param channelId
	 *            id of the channel
	 * @param flowTarget
	 */
	public void setChannelFlowTarget(Zone zone, Long channelId, ChannelFlowTargetDescriptor flowTarget);

	/**
	 * Updates the FlowTarget as ChannelFlowTarget within a Channel.
	 * 
	 * This is called on the sending end by the relay in of a remote ChannelFlowTarget via
	 * {@link MRS#relay(org.tdmx.core.api.v01.mrs.Relay)}.
	 * 
	 * @param zone
	 *            the zone
	 * @param channelId
	 *            id of the channel
	 * @param flowTarget
	 */
	public void relayChannelFlowTarget(Zone zone, Long channelId, ChannelFlowTargetDescriptor flowTarget);

	/**
	 * Creates a Flow originating from the originatingUser and terminating in the ChannelFlowTarget referenced by
	 * channelFlowId.
	 * 
	 * @param zone
	 * @param channelFlowTargetId
	 */
	public void createOriginatingUser(Zone zone, Long channelFlowTargetId, AgentCredential originatingUser);

	/**
	 * Delete the Channel, cascades to ChannelFlowTargets and their Flows, and the ChannelAuthorization.
	 * 
	 * @param channel
	 */
	public void delete(Channel channel);

	/**
	 * ChannelFlowTarget's can be deleted without the Channel's knowledge. Cascades to Flows.
	 * 
	 * @param channelFlowTarget
	 */
	public void delete(ChannelFlowTarget channelFlowTarget);
}
