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
package org.tdmx.server.pcs;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.ROSClient.CreateSessionRequest;
import org.tdmx.server.pcs.protobuf.ROSClient.GetStatisticsRequest;
import org.tdmx.server.pcs.protobuf.ROSClient.RelaySessionManagerProxy;
import org.tdmx.server.pcs.protobuf.ROSClient.RelayStatistic;

import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

class ReverseRpcRelayOutboundServiceController implements RelayOutboundServiceController {
	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final String ROS = "ROS";

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RemoteControlServiceConnector.class);

	private static final String RPC_ADDRESS = "RPC_ADDRESS";

	private final RpcClientChannel channel;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ReverseRpcRelayOutboundServiceController(RpcClientChannel channel, String rosTcpAddress) {
		this.channel = channel;

		this.channel.setAttribute(ROS, this);
		this.channel.setAttribute(RPC_ADDRESS, rosTcpAddress);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public String getRosTcpAddress() {
		return (String) this.channel.getAttribute(RPC_ADDRESS);
	}

	@Override
	public RelayStatistic createRelaySession(String channelKey, Map<AttributeId, Long> seedAttributes,
			String mrsSessionId) {

		RelaySessionManagerProxy.BlockingInterface blockingService = RelaySessionManagerProxy.newBlockingStub(channel);
		final ClientRpcController controller = channel.newRpcController();
		controller.setTimeoutMs(0);

		CreateSessionRequest.Builder reqBuilder = CreateSessionRequest.newBuilder();
		reqBuilder.setChannelKey(channelKey);
		for (Entry<AttributeId, Long> entry : seedAttributes.entrySet()) {
			AttributeValue.Builder attr = AttributeValue.newBuilder();
			attr.setName(entry.getKey());
			attr.setValue(entry.getValue());
			reqBuilder.addAttribute(attr);
		}
		if (mrsSessionId != null) {
			reqBuilder.setMrsSessionId(mrsSessionId);
		}

		try {
			org.tdmx.server.pcs.protobuf.ROSClient.RelayStatistic response = blockingService
					.createRelaySession(controller, reqBuilder.build());
			return response;
		} catch (ServiceException e) {
			log.warn("createRelaySession call failed.", e);
		}
		return null;
	}

	@Override
	public RelayStatistic getStatistics() {
		RelaySessionManagerProxy.BlockingInterface blockingService = RelaySessionManagerProxy.newBlockingStub(channel);
		final ClientRpcController controller = channel.newRpcController();
		controller.setTimeoutMs(0);

		GetStatisticsRequest.Builder reqBuilder = GetStatisticsRequest.newBuilder();

		try {
			org.tdmx.server.pcs.protobuf.ROSClient.RelayStatistic response = blockingService
					.getRelayStatistics(controller, reqBuilder.build());
			return response;
		} catch (ServiceException e) {
			log.warn("getStatistics call failed.", e);
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
}
