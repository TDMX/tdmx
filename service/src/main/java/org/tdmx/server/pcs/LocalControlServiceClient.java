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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.pcs.protobuf.PCSServer.BlockingPingService;
import org.tdmx.server.pcs.protobuf.PCSServer.Ping;
import org.tdmx.server.pcs.protobuf.PCSServer.Pong;
import org.tdmx.server.session.WebServiceSessionEndpoint;

import com.google.protobuf.ByteString;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

public class LocalControlServiceClient implements ControlService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(LocalControlServiceClient.class);

	/**
	 * The RPC channel to the server.
	 */
	private RpcClientChannel rpcClient;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public WebServiceSessionEndpoint associateApiSession(SessionHandle sessionData, PKIXCertificate clientCertificate) {
		if (!rpcClient.isClosed()) {
			// TODO real api

			BlockingPingService.BlockingInterface blockingService = BlockingPingService.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			Ping.Builder pingBuilder = Ping.newBuilder();
			pingBuilder.setSequenceNo(1);
			pingBuilder.setPingDurationMs(1000);
			pingBuilder.setPingPayload(ByteString.copyFromUtf8("Hello World!"));
			pingBuilder.setPingPercentComplete(false);
			pingBuilder.setPongRequired(false);
			pingBuilder.setPongBlocking(true);
			pingBuilder.setPongDurationMs(1000);
			pingBuilder.setPongTimeoutMs(0);
			pingBuilder.setPongPercentComplete(false);

			Ping ping = pingBuilder.build();
			try {
				Pong pong = blockingService.ping(controller, ping);

			} catch (ServiceException e) {
				log.warn("Call failed.", e);
			}
		}
		return null;
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

	public void setRpcClient(RpcClientChannel rpcClient) {
		this.rpcClient = rpcClient;
	}

}
