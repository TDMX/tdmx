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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.pcs.RelayControlServiceListener;
import org.tdmx.server.pcs.RelayOutboundServiceController;
import org.tdmx.server.pcs.protobuf.PCSServer.ControlServiceProxy;
import org.tdmx.server.pcs.protobuf.PCSServer.NotifyRelaySessionIdleRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.RegisterRelayServerRequest;

import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

public class RelayControlServiceClient implements RelayControlServiceListener {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayControlServiceClient.class);

	/**
	 * The RPC channel to the server.
	 */
	private final RpcClientChannel rpcClient;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public RelayControlServiceClient(RpcClientChannel rpcClient) {
		this.rpcClient = rpcClient;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public String getPcsServerName() {
		return rpcClient.getPeerInfo().toString();
	}

	@Override
	public void registerRelayServer(String rosTcpEndpoint, String segment, RelayOutboundServiceController ros) {
		if (ros != null) {
			throw new IllegalArgumentException("ros should not be set on ROS client.");
		}
		if (!rpcClient.isClosed()) {
			ControlServiceProxy.BlockingInterface blockingService = ControlServiceProxy.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			RegisterRelayServerRequest.Builder reqBuilder = RegisterRelayServerRequest.newBuilder();
			reqBuilder.setRosAddress(rosTcpEndpoint);
			reqBuilder.setSegment(segment);

			RegisterRelayServerRequest request = reqBuilder.build();
			try {
				blockingService.registerRelayServer(controller, request);
			} catch (ServiceException e) {
				log.warn("registerRelayServer call failed." + e.getMessage(), e);
			}
		}
	}

	@Override
	public void unregisterRelayServer(String rosTcpEndpoint) {
		throw new UnsupportedOperationException("Unregister relay server supported on PCS side only.");
	}

	@Override
	public void notifySessionsRemoved(String rosTcpEndpoint, List<String> channelKeys) {
		if (!rpcClient.isClosed()) {
			ControlServiceProxy.BlockingInterface blockingService = ControlServiceProxy.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			NotifyRelaySessionIdleRequest.Builder reqBuilder = NotifyRelaySessionIdleRequest.newBuilder();
			reqBuilder.setRosAddress(rosTcpEndpoint);

			NotifyRelaySessionIdleRequest request = reqBuilder.build();
			try {
				blockingService.notifyRelaySessionsIdle(controller, request);
			} catch (ServiceException e) {
				log.warn("notifySessionsRemoved call failed." + e.getMessage(), e);
			}
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public RpcClientChannel getRpcClient() {
		return rpcClient;
	}

}
