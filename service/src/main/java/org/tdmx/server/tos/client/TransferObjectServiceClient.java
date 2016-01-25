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
package org.tdmx.server.tos.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.Common.ObjectType;
import org.tdmx.server.pcs.protobuf.TOSServer.TransferObjectServiceProxy;
import org.tdmx.server.pcs.protobuf.TOSServer.TransferRequest;
import org.tdmx.server.pcs.protobuf.TOSServer.TransferResponse;
import org.tdmx.server.tos.client.TransferStatus.ErrorCode;
import org.tdmx.server.ws.session.WebServiceApiName;

import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

/**
 * RPC helper to call the TOS server.
 * 
 * @author Peter
 *
 */
public class TransferObjectServiceClient {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(TransferObjectServiceClient.class);

	/**
	 * The RPC channel to the server.
	 */
	private final RpcClientChannel rpcClient;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public TransferObjectServiceClient(RpcClientChannel rpcClient) {
		this.rpcClient = rpcClient;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public TransferStatus transferMDS(String segment, String tosTcpAddress, String sessionId, ChannelMessage msg) {

		if (!rpcClient.isClosed()) {
			TransferRequest.Builder reqBuilder = TransferRequest.newBuilder();
			reqBuilder.setApiName(WebServiceApiName.MDS.name());
			reqBuilder.setSegment(segment);
			reqBuilder.setSessionId(sessionId);
			reqBuilder.setTransferType(ObjectType.Message);

			AttributeValue.Builder attr = AttributeValue.newBuilder();
			attr.setName(AttributeId.MessageId);
			attr.setValue(msg.getId());
			reqBuilder.addAttribute(attr);

			return transfer(reqBuilder.build(), tosTcpAddress);
		}
		return TransferStatus.failure(ErrorCode.TOS_RPC_CHANNEL_CLOSED);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private TransferStatus transfer(TransferRequest request, String tosTcpAddress) {
		try {
			TransferObjectServiceProxy.BlockingInterface blockingService = TransferObjectServiceProxy
					.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			TransferResponse response = blockingService.transfer(controller, request);
			if (response != null) {
				if (response.getSuccess()) {
					return TransferStatus.success(request.getSessionId(), tosTcpAddress);
				} else {
					return TransferStatus.failure(ErrorCode.TOS_RELAY_DECLINED);
				}
			} else {
				return TransferStatus.failure(ErrorCode.TOS_RPC_CALL_FAILURE);
			}
		} catch (ServiceException e) {
			log.warn("TOS call failed.", e);
		}
		return TransferStatus.failure(ErrorCode.TOS_RPC_CALL_FAILURE);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public RpcClientChannel getRpcClient() {
		return rpcClient;
	}

}
