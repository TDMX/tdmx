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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.FlowQuota;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.ROSServer.RelayOutboundServiceProxy;
import org.tdmx.server.pcs.protobuf.ROSServer.RelayRequest;
import org.tdmx.server.pcs.protobuf.ROSServer.RelayRequest.RelayType;
import org.tdmx.server.pcs.protobuf.ROSServer.RelayResponse;

import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

public class RelayOutboundServiceClient implements RelayClientService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayOutboundServiceClient.class);

	private static final String RPC_CHANNEL_CLOSED = "RPC channel to ROS is closed.";

	/**
	 * The RPC channel to the server.
	 */
	private final RpcClientChannel rpcClient;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public RelayOutboundServiceClient(RpcClientChannel rpcClient) {
		this.rpcClient = rpcClient;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public RelayStatus relayChannelAuthorization(String rosTcpAddress, Zone zone, Domain domain, Channel channel,
			ChannelAuthorization ca) {
		String channelKey = channel.getChannelKey(domain.getDomainName());
		if (!rpcClient.isClosed()) {

			RelayRequest request = createRelayRequest(channelKey, RelayType.Authorization, AttributeId.AuthorizationId,
					ca.getId());
			return relay(request);
		}
		return RelayStatus.failure(channelKey, RPC_CHANNEL_CLOSED);
	}

	@Override
	public RelayStatus relayChannelDestinationSession(String rosTcpAddress, Zone zone, Domain domain, Channel channel) {
		String channelKey = channel.getChannelKey(domain.getDomainName());
		if (!rpcClient.isClosed()) {

			RelayRequest request = createRelayRequest(channelKey, RelayType.DestinationSession, AttributeId.ChannelId,
					channel.getId());
			return relay(request);
		}
		return RelayStatus.failure(channelKey, RPC_CHANNEL_CLOSED);
	}

	@Override
	public RelayStatus relayChannelFlowControl(String rosTcpAddress, Zone zone, Domain domain, Channel channel,
			FlowQuota quota) {
		String channelKey = channel.getChannelKey(domain.getDomainName());
		if (!rpcClient.isClosed()) {

			RelayRequest request = createRelayRequest(channelKey, RelayType.FlowControl, AttributeId.FlowQuotaId,
					quota.getId());
			return relay(request);
		}
		return RelayStatus.failure(channelKey, RPC_CHANNEL_CLOSED);
	}

	@Override
	public RelayStatus relayChannelMessage(String rosTcpAddress, Zone zone, Domain domain, Channel channel,
			ChannelMessage msg) {
		String channelKey = channel.getChannelKey(domain.getDomainName());
		if (!rpcClient.isClosed()) {

			RelayRequest request = createRelayRequest(channelKey, RelayType.Message, AttributeId.MessageId,
					msg.getId());
			return relay(request);
		}
		return RelayStatus.failure(channelKey, RPC_CHANNEL_CLOSED);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private RelayRequest createRelayRequest(String channelKey, RelayType type, AttributeId attrId,
			Long attributeValue) {
		RelayRequest.Builder reqBuilder = RelayRequest.newBuilder();
		reqBuilder.setChannelKey(channelKey);
		reqBuilder.setRelayType(type);

		AttributeValue.Builder attr = AttributeValue.newBuilder();
		attr.setName(attrId);
		attr.setValue(attributeValue);
		reqBuilder.addAttribute(attr);

		return reqBuilder.build();
	}

	private RelayStatus relay(RelayRequest request) {
		String errorMsg = null;
		try {
			RelayOutboundServiceProxy.BlockingInterface blockingService = RelayOutboundServiceProxy
					.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			RelayResponse response = blockingService.relay(controller, request);
			if (response != null) {
				return RelayStatus.success(request.getChannelKey());
			} else {
				errorMsg = "No ROS Address allocated for ." + request.getChannelKey();
			}
		} catch (ServiceException e) {
			errorMsg = "relay call failed. " + e.getMessage();
			log.warn(errorMsg, e);
		}
		return RelayStatus.failure(request.getChannelKey(), errorMsg);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public RpcClientChannel getRpcClient() {
		return rpcClient;
	}

}
