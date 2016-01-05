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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.WSClient.AddCertificateRequest;
import org.tdmx.server.pcs.protobuf.WSClient.CreateSessionRequest;
import org.tdmx.server.pcs.protobuf.WSClient.GetStatisticsRequest;
import org.tdmx.server.pcs.protobuf.WSClient.RemoveCertificateRequest;
import org.tdmx.server.pcs.protobuf.WSClient.SessionManagerProxy;
import org.tdmx.server.ws.session.WebServiceApiName;

import com.google.protobuf.ByteString;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

class ReverseRpcServerSessionController implements ServerSessionController {
	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final String SSM = "SSM";
	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RemoteControlServiceConnector.class);

	private static final String SERVICES = "SERVICES";

	private final RpcClientChannel channel;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ReverseRpcServerSessionController(RpcClientChannel channel, List<ServiceHandle> services) {
		this.channel = channel;

		this.channel.setAttribute(SSM, this);
		this.channel.setAttribute(SERVICES, services);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public List<ServiceHandle> getServices() {
		return (List<ServiceHandle>) this.channel.getAttribute(SERVICES);
	}

	@Override
	public ServiceStatistic createSession(WebServiceApiName apiName, String sessionId, PKIXCertificate cert,
			Map<AttributeId, Long> seedAttributes) {

		SessionManagerProxy.BlockingInterface blockingService = SessionManagerProxy.newBlockingStub(channel);
		final ClientRpcController controller = channel.newRpcController();
		controller.setTimeoutMs(0);

		CreateSessionRequest.Builder reqBuilder = CreateSessionRequest.newBuilder();
		reqBuilder.setApiName(apiName.name());
		reqBuilder.setSessionId(sessionId);
		reqBuilder.setClientCert(ByteString.copyFrom(cert.getX509Encoded()));
		for (Entry<AttributeId, Long> entry : seedAttributes.entrySet()) {
			AttributeValue.Builder attr = AttributeValue.newBuilder();
			attr.setName(entry.getKey());
			attr.setValue(entry.getValue());
			reqBuilder.addAttribute(attr);
		}

		try {
			org.tdmx.server.pcs.protobuf.WSClient.ServiceStatistic response = blockingService.createSession(controller,
					reqBuilder.build());
			return map(response);
		} catch (ServiceException e) {
			log.warn("createSession call failed.", e);
		}
		return null;
	}

	@Override
	public ServiceStatistic addCertificate(WebServiceApiName apiName, String sessionId, PKIXCertificate cert) {
		SessionManagerProxy.BlockingInterface blockingService = SessionManagerProxy.newBlockingStub(channel);
		final ClientRpcController controller = channel.newRpcController();
		controller.setTimeoutMs(0);

		AddCertificateRequest.Builder reqBuilder = AddCertificateRequest.newBuilder();
		reqBuilder.setApiName(apiName.name());
		reqBuilder.setSessionId(sessionId);
		reqBuilder.setClientCert(ByteString.copyFrom(cert.getX509Encoded()));

		try {
			org.tdmx.server.pcs.protobuf.WSClient.ServiceStatistic response = blockingService
					.addSessionCertificate(controller, reqBuilder.build());
			return map(response);
		} catch (ServiceException e) {
			log.warn("addSessionCertificate call failed.", e);
		}
		return null;
	}

	@Override
	public ServerServiceStatistics removeCertificate(PKIXCertificate cert) {
		SessionManagerProxy.BlockingInterface blockingService = SessionManagerProxy.newBlockingStub(channel);
		final ClientRpcController controller = channel.newRpcController();
		controller.setTimeoutMs(0);

		RemoveCertificateRequest.Builder reqBuilder = RemoveCertificateRequest.newBuilder();
		reqBuilder.setClientCert(ByteString.copyFrom(cert.getX509Encoded()));

		try {
			org.tdmx.server.pcs.protobuf.WSClient.ServerServiceStatistics response = blockingService
					.removeCertificate(controller, reqBuilder.build());
			return map(response);
		} catch (ServiceException e) {
			log.warn("removeCertificate call failed.", e);
		}
		return null;
	}

	@Override
	public ServerServiceStatistics getStatistics() {
		SessionManagerProxy.BlockingInterface blockingService = SessionManagerProxy.newBlockingStub(channel);
		final ClientRpcController controller = channel.newRpcController();
		controller.setTimeoutMs(0);

		GetStatisticsRequest.Builder reqBuilder = GetStatisticsRequest.newBuilder();

		try {
			org.tdmx.server.pcs.protobuf.WSClient.ServerServiceStatistics response = blockingService
					.getStatistics(controller, reqBuilder.build());
			return map(response);
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
	private ServerServiceStatistics map(org.tdmx.server.pcs.protobuf.WSClient.ServerServiceStatistics statistics) {
		if (statistics == null) {
			return null;
		}
		ServerServiceStatistics stats = new ServerServiceStatistics();
		for (org.tdmx.server.pcs.protobuf.WSClient.ServiceStatistic stat : statistics.getStatisticsList()) {
			stats.addStatistic(map(stat));
		}
		return stats;
	}

	private ServiceStatistic map(org.tdmx.server.pcs.protobuf.WSClient.ServiceStatistic statistic) {
		if (statistic == null) {
			return null;
		}
		ServiceStatistic stat = new ServiceStatistic(mapApi(statistic.getApiName()), statistic.getHttpsUrl(),
				statistic.getLoadValue());
		return stat;
	}

	private WebServiceApiName mapApi(String apiName) {
		return apiName != null ? WebServiceApiName.valueOf(apiName) : null;
	}

}
