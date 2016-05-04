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
package org.tdmx.server.pcs.client;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.server.cache.CacheInvalidationInstruction;
import org.tdmx.server.cache.CacheInvalidationListener;
import org.tdmx.server.pcs.RelayControlService;
import org.tdmx.server.pcs.SessionControlService;
import org.tdmx.server.pcs.SessionHandle;
import org.tdmx.server.pcs.protobuf.Cache.CacheServiceProxy;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.PCSServer.AssignRelaySessionRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.AssignRelaySessionResponse;
import org.tdmx.server.pcs.protobuf.PCSServer.AssociateApiSessionRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.AssociateApiSessionResponse;
import org.tdmx.server.pcs.protobuf.PCSServer.ControlServiceProxy;
import org.tdmx.server.pcs.protobuf.PCSServer.FindApiSessionRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.FindApiSessionResponse;
import org.tdmx.server.session.WebServiceSessionEndpoint;
import org.tdmx.server.ws.session.WebServiceApiName;

import com.google.protobuf.ByteString;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

public class LocalControlServiceClient
		implements SessionControlService, RelayControlService, CacheInvalidationListener {

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
	private final RpcClientChannel rpcClient;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public LocalControlServiceClient(RpcClientChannel rpcClient) {
		this.rpcClient = rpcClient;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void invalidateCache(CacheInvalidationInstruction instruction) {
		if (!rpcClient.isClosed()) {

			CacheServiceProxy.BlockingInterface blockingService = CacheServiceProxy.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			org.tdmx.server.pcs.protobuf.Cache.InvalidateCacheRequest.Builder req = org.tdmx.server.pcs.protobuf.Cache.InvalidateCacheRequest
					.newBuilder();
			req.setId(instruction.getId());
			req.setCacheName(instruction.getName());
			if (instruction.getKey() != null) {
				req.setKeyValue(instruction.getKey());
			}
			try {
				blockingService.invalidateCache(controller, req.build());
				log.info("Cache invalidated " + instruction);
			} catch (ServiceException e) {
				log.warn("invalidateCache call failed.", e);
			}
		}
	}

	@Override
	public WebServiceSessionEndpoint associateApiSession(SessionHandle sessionData, PKIXCertificate clientCertificate) {
		if (!rpcClient.isClosed()) {
			ControlServiceProxy.BlockingInterface blockingService = ControlServiceProxy.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			org.tdmx.server.pcs.protobuf.PCSServer.SessionHandle.Builder sh = org.tdmx.server.pcs.protobuf.PCSServer.SessionHandle
					.newBuilder();
			sh.setApiName(sessionData.getApi().name());
			sh.setSegment(sessionData.getSegment());
			sh.setSessionKey(sessionData.getSessionKey());
			for (Entry<AttributeId, Long> entry : sessionData.getSeedAttributes().entrySet()) {
				AttributeValue.Builder attr = AttributeValue.newBuilder();
				attr.setName(entry.getKey());
				attr.setValue(entry.getValue());
				sh.addAttribute(attr);
			}
			AssociateApiSessionRequest.Builder reqBuilder = AssociateApiSessionRequest.newBuilder();
			reqBuilder.setHandle(sh);
			reqBuilder.setPkixCertificate(ByteString.copyFrom(clientCertificate.getX509Encoded()));

			AssociateApiSessionRequest request = reqBuilder.build();
			try {
				AssociateApiSessionResponse response = blockingService.associateApiSession(controller, request);
				if (response != null && response.getServerCert() != null) {
					WebServiceSessionEndpoint sep = new WebServiceSessionEndpoint(response.getSessionId(),
							response.getHttpsUrl(),
							CertificateIOUtils.safeDecodeX509(response.getServerCert().toByteArray()));
					return sep;
				} else {
					log.info("No WebServiceSessionEndpoint allocated.");
				}
			} catch (ServiceException e) {
				log.warn("Call failed.", e);
			}
		}
		return null;
	}

	@Override
	public FindApiSessionResponse findApiSession(String segment, WebServiceApiName api, String sessionKey) {
		if (!rpcClient.isClosed()) {
			ControlServiceProxy.BlockingInterface blockingService = ControlServiceProxy.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			FindApiSessionRequest.Builder reqBuilder = FindApiSessionRequest.newBuilder();

			reqBuilder.setApiName(api.name());
			reqBuilder.setSessionKey(sessionKey);
			reqBuilder.setSegment(segment);

			FindApiSessionRequest request = reqBuilder.build();
			try {
				FindApiSessionResponse response = blockingService.findApiSession(controller, request);
				if (response != null && response.getSessionId() != null) {
					return response;
				} else {
					log.debug("No active SessionId found for" + sessionKey);
				}
			} catch (ServiceException e) {
				log.warn("Call failed.", e);
			}
		}
		return null;
	}

	@Override
	public String assignRelayServer(String channelKey, String segment, Map<AttributeId, Long> attributes) {
		if (!rpcClient.isClosed()) {
			ControlServiceProxy.BlockingInterface blockingService = ControlServiceProxy.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			AssignRelaySessionRequest.Builder reqBuilder = AssignRelaySessionRequest.newBuilder();
			reqBuilder.setChannelKey(channelKey);
			reqBuilder.setSegment(segment);
			for (Entry<AttributeId, Long> entry : attributes.entrySet()) {
				AttributeValue.Builder attr = AttributeValue.newBuilder();
				attr.setName(entry.getKey());
				attr.setValue(entry.getValue());
				reqBuilder.addAttribute(attr);
			}

			AssignRelaySessionRequest request = reqBuilder.build();
			try {
				AssignRelaySessionResponse response = blockingService.assignRelaySession(controller, request);
				if (response != null && StringUtils.hasText(response.getRosAddress())) {
					return response.getRosAddress();
				} else {
					log.info("No ROS Address allocated for ." + channelKey);
				}
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

}
