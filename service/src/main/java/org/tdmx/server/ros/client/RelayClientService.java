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
package org.tdmx.server.ros.client;

import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.FlowQuota;
import org.tdmx.lib.zone.domain.Zone;

/**
 * The client's interface to the ROS.
 * 
 * @author Peter
 *
 */
public interface RelayClientService {

	/**
	 * Initiate relay of a ChannelAuthorization.
	 * 
	 * @param rosTcpAddress
	 *            the RPC endpoint address of the ROS handling the channel ( null if not known ).
	 * @param accountzone
	 *            the detached accountzone
	 * @param zone
	 *            the detached zone
	 * @param domain
	 *            the detached domain
	 * @param channel
	 *            the detached channel
	 * @param ca
	 *            the detached channel authorization
	 * @return the relay status
	 */
	public RelayStatus relayChannelAuthorization(String rosTcpAddress, AccountZone az, Zone zone, Domain domain,
			Channel channel, ChannelAuthorization ca);

	/**
	 * Initiate relay of a ChannelDestinationSession.
	 * 
	 * @param rosTcpAddress
	 *            the RPC endpoint address of the ROS handling the channel.
	 * @param accountzone
	 *            the detached accountzone
	 * @param zone
	 *            the detached zone
	 * @param domain
	 *            the detached domain
	 * @param channel
	 *            the detached channel
	 * @return the relay status
	 */
	public RelayStatus relayChannelDestinationSession(String rosTcpAddress, AccountZone az, Zone zone, Domain domain,
			Channel channel);

	/**
	 * Initiate relay of a Channel FlowControl state.
	 * 
	 * @param rosTcpAddress
	 *            the RPC endpoint address of the ROS handling the channel.
	 * @param accountzone
	 *            the detached accountzone
	 * @param zone
	 *            the detached zone
	 * @param domain
	 *            the detached domain
	 * @param channel
	 *            the detached channel
	 * @param flowquota
	 *            the detached channel flow quota
	 * @return the relay status
	 */
	public RelayStatus relayChannelFlowControl(String rosTcpAddress, AccountZone az, Zone zone, Domain domain,
			Channel channel, FlowQuota quota);

	/**
	 * Initiate relay of a Channel Message.
	 * 
	 * @param rosTcpAddress
	 *            the RPC endpoint address of the ROS handling the channel.
	 * @param accountzone
	 *            the detached accountzone
	 * @param zone
	 *            the detached zone
	 * @param domain
	 *            the detached domain
	 * @param channel
	 *            the detached channel
	 * @param msg
	 *            the detached message
	 * @return the relay status
	 */
	public RelayStatus relayChannelMessage(String rosTcpAddress, AccountZone az, Zone zone, Domain domain,
			Channel channel, ChannelMessage msg);
}
