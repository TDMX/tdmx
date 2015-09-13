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
package org.tdmx.server.session;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.pcs.ControlServiceListener;
import org.tdmx.server.pcs.ServerSessionController;
import org.tdmx.server.pcs.ServiceHandle;
import org.tdmx.server.pcs.protobuf.PCSServer.ControlServiceProxy;
import org.tdmx.server.ws.session.WebServiceApiName;

import com.google.protobuf.ByteString;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

public class LocalControlServiceListenerClient implements ControlServiceListener {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(LocalControlServiceListenerClient.class);

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
	public void registerServer(List<ServiceHandle> services, ServerSessionController ssm) {
		if (!rpcClient.isClosed()) {
			ControlServiceProxy.BlockingInterface blockingService = ControlServiceProxy.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			org.tdmx.server.pcs.protobuf.PCSServer.RegisterServerRequest.Builder rb = org.tdmx.server.pcs.protobuf.PCSServer.RegisterServerRequest
					.newBuilder();
			for (ServiceHandle service : services) {
				org.tdmx.server.pcs.protobuf.PCSServer.ServiceHandle.Builder sh = org.tdmx.server.pcs.protobuf.PCSServer.ServiceHandle
						.newBuilder();
				sh.setApiName(service.getApi().name());
				sh.setHttpsUrl(service.getHttpsUrl());
				sh.setSegment(service.getSegment());
				sh.setServerCert(ByteString.copyFrom(service.getPublicCertificate().getX509Encoded()));

				rb.addService(sh);
			}
			try {
				blockingService.registerServer(controller, rb.build());
			} catch (ServiceException e) {
				log.warn("invalidateCertificate call failed.", e);
			}
		}
	}

	@Override
	public void unregisterServer(List<ServiceHandle> services) {
		throw new UnsupportedOperationException("Unregister server by closing connection only.");
	}

	@Override
	public void notifySessionsRemoved(WebServiceApiName api, Set<String> sessionIds) {
		if (!rpcClient.isClosed()) {
			ControlServiceProxy.BlockingInterface blockingService = ControlServiceProxy.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			org.tdmx.server.pcs.protobuf.PCSServer.NotifySessionRemovedRequest.Builder rb = org.tdmx.server.pcs.protobuf.PCSServer.NotifySessionRemovedRequest
					.newBuilder();
			rb.setApiName(api.name());
			for (String sessionId : sessionIds) {
				rb.addSessionId(sessionId);
			}
			try {
				blockingService.notifySessionsRemoved(controller, rb.build());
			} catch (ServiceException e) {
				log.warn("invalidateCertificate call failed.", e);
			}
		}
	}

	@Override
	public void invalidateCertificate(PKIXCertificate cert) {
		if (!rpcClient.isClosed()) {
			ControlServiceProxy.BlockingInterface blockingService = ControlServiceProxy.newBlockingStub(rpcClient);
			final ClientRpcController controller = rpcClient.newRpcController();
			controller.setTimeoutMs(0);

			org.tdmx.server.pcs.protobuf.PCSServer.InvalidateCertificateRequest.Builder rb = org.tdmx.server.pcs.protobuf.PCSServer.InvalidateCertificateRequest
					.newBuilder();
			rb.setClientCert(ByteString.copyFrom(cert.getX509Encoded()));
			try {
				blockingService.invalidateCertificate(controller, rb.build());
			} catch (ServiceException e) {
				log.warn("invalidateCertificate call failed.", e);
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

	public void setRpcClient(RpcClientChannel rpcClient) {
		this.rpcClient = rpcClient;
	}

}
