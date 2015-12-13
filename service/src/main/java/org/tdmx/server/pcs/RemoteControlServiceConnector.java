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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.PartitionControlServer;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.service.PartitionControlServerService;
import org.tdmx.server.pcs.protobuf.Broadcast;
import org.tdmx.server.pcs.protobuf.Broadcast.BroadcastMessage;
import org.tdmx.server.pcs.protobuf.Broadcast.CacheInvalidationMessage;
import org.tdmx.server.pcs.protobuf.PCSServer.AssignRelaySessionRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.AssignRelaySessionResponse;
import org.tdmx.server.pcs.protobuf.PCSServer.AssociateApiSessionRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.AssociateApiSessionResponse;
import org.tdmx.server.pcs.protobuf.PCSServer.ControlServiceProxy;
import org.tdmx.server.pcs.protobuf.PCSServer.InvalidateCertificateRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.InvalidateCertificateResponse;
import org.tdmx.server.pcs.protobuf.PCSServer.NotifyLoadStatisticRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.NotifyLoadStatisticResponse;
import org.tdmx.server.pcs.protobuf.PCSServer.NotifySessionRemovedRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.NotifySessionRemovedResponse;
import org.tdmx.server.pcs.protobuf.PCSServer.RegisterRelayServerRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.RegisterRelayServerResponse;
import org.tdmx.server.pcs.protobuf.PCSServer.RegisterServerRequest;
import org.tdmx.server.pcs.protobuf.PCSServer.RegisterServerResponse;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.session.WebServiceSessionEndpoint;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;

import com.google.protobuf.BlockingService;
import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ServerRpcController;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.NullLogger;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Handles inbound RPC calls from SCS, ROS and WS clients.
 * 
 * @author Peter
 *
 */
public class RemoteControlServiceConnector
		implements Manageable, CacheInvalidationMessageListener, ControlServiceProxy.BlockingInterface {

	// TODO LATER: use SSL context for protobuf communications

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RemoteControlServiceConnector.class);

	/**
	 * The name of the attribute of a RPC client to store the session address.
	 */
	private static final String ROS_ADDRESS_ATTRIBUTE = "ROS";

	/**
	 * The ControlServiceListener delegate.
	 */
	private ControlServiceListener controlListener;

	/**
	 * The ControlService delegate.
	 */
	private ControlService controlService;

	/**
	 * The RelayControlService delegate.
	 */
	private RelayControlServiceListener relayService;

	/**
	 * The interface address for multi-homed hosts. Leave empty if not multi-homed.
	 */
	private String serverAddress;
	private int localPort;
	private long shutdownTimeoutMs = 20000;
	private int coreRpcExecutorThreads = 2;
	private int maxRpcExecutorThreads = 10;
	private int acceptorThreads = 2;
	private int ioThreads = 16;
	private int ioBufferSize = 1048576;
	private boolean tcpNoDelay = true;

	/**
	 * The segment handled by this PartitionControlService.
	 */
	private Segment segment;
	private CleanShutdownHandler shutdownHandler;
	private DuplexTcpServerPipelineFactory serverFactory;

	/**
	 * The PartitionControlService gives us the information about the PCS servers.
	 */
	private PartitionControlServerService partitionServerService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void start(Segment segment, List<WebServiceApiName> apis) {
		this.segment = segment;
		// the apis are ignored - since this is not a WS.

		String localHostAddress = null;
		try {
			localHostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("Unable to determine localhost IP address.", e);
		}

		String serverHostname = StringUtils.hasText(serverAddress) ? serverAddress : localHostAddress;

		assertPcsServerRegistered(segment, serverHostname, localPort);

		PeerInfo serverInfo = new PeerInfo(serverHostname, localPort);

		RpcServerCallExecutor executor = new ThreadPoolCallExecutor(coreRpcExecutorThreads, maxRpcExecutorThreads);

		serverFactory = new DuplexTcpServerPipelineFactory(serverInfo);
		serverFactory.setRpcServerCallExecutor(executor);
		// if ( secure ) {
		// RpcSSLContext sslCtx = new RpcSSLContext();
		// sslCtx.setKeystorePassword("changeme");
		// sslCtx.setKeystorePath("./lib/server.keystore");
		// sslCtx.setTruststorePassword("changeme");
		// sslCtx.setTruststorePath("./lib/truststore");
		// sslCtx.init();
		//
		// serverFactory.setSslContext(sslCtx);
		// }

		NullLogger logger = new NullLogger();
		serverFactory.setLogger(logger);

		// setup a RPC event listener - it just logs what happens
		RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();
		RpcConnectionEventListener listener = new RpcConnectionEventListener() {

			final RpcCallback<Broadcast.BroadcastMessage> serverBroadcastCallback = new RpcCallback<Broadcast.BroadcastMessage>() {

				@Override
				public void run(Broadcast.BroadcastMessage parameter) {
					handleBroadcast(parameter);
				}

			};

			private void registerOobCallback(RpcClientChannel clientChannel) {
				clientChannel.setOobMessageCallback(Broadcast.BroadcastMessage.getDefaultInstance(),
						serverBroadcastCallback);
			}

			private void clearOobCallback(RpcClientChannel clientChannel) {
				clientChannel.setOobMessageCallback(null, null);
			}

			@Override
			public void connectionReestablished(RpcClientChannel clientChannel) {
				log.info("connectionReestablished " + clientChannel);
				registerOobCallback(clientChannel);
			}

			@Override
			public void connectionOpened(RpcClientChannel clientChannel) {
				log.info("connectionOpened " + clientChannel);
				registerOobCallback(clientChannel);
			}

			@Override
			public void connectionLost(RpcClientChannel clientChannel) {
				log.info("connectionLost " + clientChannel);
				clearOobCallback(clientChannel);

				// we unregister the WS server's services from the control service
				ReverseRpcServerSessionController ssm = (ReverseRpcServerSessionController) clientChannel
						.getAttribute(ReverseRpcServerSessionController.SSM);
				if (ssm != null) {
					log.info("Disconnect of WS client.");
					controlListener.unregisterServer(ssm.getServices());
					// there should be no more references to the RpcClient which should be garbage collected.
				}

				// we disconnect the ROS server's endpoint from the relay service
				String rosAddress = (String) clientChannel.getAttribute(ROS_ADDRESS_ATTRIBUTE);
				if (rosAddress != null) {
					log.info("Disconnect of ROS client " + rosAddress);
					relayService.unregisterRelayServer(rosAddress);
				}
			}

			@Override
			public void connectionChanged(RpcClientChannel clientChannel) {
				log.info("connectionChanged " + clientChannel);
				registerOobCallback(clientChannel);
			}
		};
		rpcEventNotifier.setEventListener(listener);
		serverFactory.registerConnectionEventListener(rpcEventNotifier);

		// we give the server our Service
		BlockingService controlServiceProxy = ControlServiceProxy.newReflectiveBlockingService(this);
		serverFactory.getRpcServiceRegistry().registerService(controlServiceProxy);

		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap();
		NioEventLoopGroup boss = new NioEventLoopGroup(acceptorThreads,
				new RenamingThreadFactoryProxy("acceptor", Executors.defaultThreadFactory()));
		NioEventLoopGroup workers = new NioEventLoopGroup(ioThreads,
				new RenamingThreadFactoryProxy("worker", Executors.defaultThreadFactory()));
		bootstrap.group(boss, workers);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.option(ChannelOption.SO_SNDBUF, ioBufferSize);
		bootstrap.option(ChannelOption.SO_RCVBUF, ioBufferSize);
		bootstrap.childOption(ChannelOption.SO_RCVBUF, ioBufferSize);
		bootstrap.childOption(ChannelOption.SO_SNDBUF, ioBufferSize);
		bootstrap.option(ChannelOption.TCP_NODELAY, tcpNoDelay);
		bootstrap.childHandler(serverFactory);
		bootstrap.localAddress(serverInfo.getPort());

		// Bind and start to accept incoming connections.
		shutdownHandler = new CleanShutdownHandler();
		shutdownHandler.addResource(boss);
		shutdownHandler.addResource(workers);
		shutdownHandler.addResource(executor);

		bootstrap.bind();

		log.info("Serving " + serverInfo);
	}

	@Override
	public void handleBroadcast(CacheInvalidationMessage message) {
		if (message == null) {
			return;
		}

		log.info("Received cache invalidation[" + message.getId() + ":" + message.getCacheKey() + "]");
		if (serverFactory != null) {
			for (RpcClientChannel channel : serverFactory.getRpcClientRegistry().getAllClients()) {
				if (log.isDebugEnabled()) {
					log.debug("Relaying message " + message.getId() + " to " + channel.getPeerInfo());
				}
				channel.sendOobMessage(message);
			}
		}
	}

	@Override
	public AssociateApiSessionResponse associateApiSession(RpcController controller, AssociateApiSessionRequest request)
			throws ServiceException {

		RpcClientChannel channel = ServerRpcController.getRpcChannel(controller);
		log.info("associateApiSession call from " + channel.getPeerInfo());

		SessionHandle sh = mapSession(request.getHandle());
		PKIXCertificate clientCert = CertificateIOUtils.safeDecodeX509(request.getPkixCertificate().toByteArray());
		WebServiceSessionEndpoint wsse = controlService.associateApiSession(sh, clientCert);

		AssociateApiSessionResponse.Builder responseBuilder = AssociateApiSessionResponse.newBuilder();
		if (wsse != null) {
			responseBuilder.setHttpsUrl(wsse.getHttpsUrl());
			responseBuilder.setSessionId(wsse.getSessionId());
			responseBuilder.setServerCert(ByteString.copyFrom(wsse.getPublicCertificate().getX509Encoded()));
		} else {
			log.info("No endpoint associated for " + sh);
		}
		return responseBuilder.build();
	}

	@Override
	public RegisterServerResponse registerServer(RpcController controller, RegisterServerRequest request)
			throws ServiceException {
		RpcClientChannel channel = ServerRpcController.getRpcChannel(controller);
		log.info("registerServer call from " + channel.getPeerInfo());

		List<ServiceHandle> services = mapServices(request.getServiceList());
		for (ServiceHandle service : services) {
			if (!segment.getSegmentName().equals(service.getSegment())) {
				String warningText = "Incorrect segment " + service.getSegment() + " we only handle "
						+ segment.getSegmentName();
				log.warn(warningText);
				throw new ServiceException(warningText);
			}
		}
		// links the ReverseRpcServerSessionController with the RpcClient
		ReverseRpcServerSessionController ssm = new ReverseRpcServerSessionController(channel, services);

		controlListener.registerServer(services, ssm);
		RegisterServerResponse.Builder responseBuilder = RegisterServerResponse.newBuilder();
		return responseBuilder.build();
	}

	@Override
	public NotifySessionRemovedResponse notifySessionsRemoved(RpcController controller,
			NotifySessionRemovedRequest request) throws ServiceException {
		RpcClientChannel channel = ServerRpcController.getRpcChannel(controller);
		log.info("notifySessionsRemoved call from " + channel.getPeerInfo() + " for removing "
				+ request.getSessionIdCount());

		Set<String> sessionIds = new HashSet<>();
		sessionIds.addAll(request.getSessionIdList());

		controlListener.notifySessionsRemoved(mapApi(request.getApiName()), sessionIds);
		NotifySessionRemovedResponse.Builder responseBuilder = NotifySessionRemovedResponse.newBuilder();
		return responseBuilder.build();
	}

	@Override
	public InvalidateCertificateResponse invalidateCertificate(RpcController controller,
			InvalidateCertificateRequest request) throws ServiceException {
		RpcClientChannel channel = ServerRpcController.getRpcChannel(controller);
		log.info("invalidateCertificate call from " + channel.getPeerInfo());

		PKIXCertificate clientCert = CertificateIOUtils.safeDecodeX509(request.getClientCert().toByteArray());
		controlListener.invalidateCertificate(clientCert);

		InvalidateCertificateResponse.Builder responseBuilder = InvalidateCertificateResponse.newBuilder();
		return responseBuilder.build();
	}

	@Override
	public RegisterRelayServerResponse registerRelayServer(RpcController controller, RegisterRelayServerRequest request)
			throws ServiceException {
		RpcClientChannel channel = ServerRpcController.getRpcChannel(controller);
		log.info("registerRelayServer call from " + channel.getPeerInfo());

		relayService.registerRelayServer(request.getRosAddress(), request.getSessionCapacity());
		// keep track of which ROS server is associated with the RPC client, so we can cleanly disconnect it later.
		channel.setAttribute(ROS_ADDRESS_ATTRIBUTE, request.getRosAddress());

		RegisterRelayServerResponse.Builder responseBuilder = RegisterRelayServerResponse.newBuilder();
		return responseBuilder.build();
	}

	public AssignRelaySessionResponse assignRelaySession(RpcController controller, AssignRelaySessionRequest request)
			throws ServiceException {
		RpcClientChannel channel = ServerRpcController.getRpcChannel(controller);
		log.info("notifyIdleRelaySession call from " + channel.getPeerInfo());

		String rosTcpEndpoint = relayService.assignRelayServer(request.getDomainName(), request.getChannelKey());

		AssignRelaySessionResponse.Builder responseBuilder = AssignRelaySessionResponse.newBuilder();
		responseBuilder.setRosAddress(rosTcpEndpoint);
		return responseBuilder.build();
	}

	@Override
	public NotifyLoadStatisticResponse notifyRelayLoadStatistic(RpcController controller,
			NotifyLoadStatisticRequest request) throws ServiceException {
		RpcClientChannel channel = ServerRpcController.getRpcChannel(controller);
		log.info("notifyRelayLoadStatistic call from " + channel.getPeerInfo());

		relayService.notifyServerLoad(request.getRosAddress(), request.getSessionLoad());

		NotifyLoadStatisticResponse.Builder responseBuilder = NotifyLoadStatisticResponse.newBuilder();
		return responseBuilder.build();
	}

	@Override
	public void stop() {
		if (shutdownHandler != null) {
			Future<Boolean> shutdownResult = shutdownHandler.shutdownAwaiting(shutdownTimeoutMs);
			try {
				if (!shutdownResult.get()) {
					log.warn("Unable to shut down within " + shutdownTimeoutMs + "ms");
				} else {
					log.info("Shutdown RPC client.");
				}
			} catch (InterruptedException e) {
				log.warn("Interupted shutting down.", e);
			} catch (ExecutionException e) {
				log.warn("Error shutting down.", e);
			}
			shutdownHandler = null;
		}
		serverFactory = null;
		segment = null;
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void handleBroadcast(BroadcastMessage message) {
		if (message.getCacheInvalidation() != null) {
			handleBroadcast(message.getCacheInvalidation());
		} else {
			log.warn("Unknown broadcast message " + message);
			// relay messages are sent OOB from WS to ROS directly.
		}
	}

	private void assertPcsServerRegistered(Segment segment, String ipAddress, int port) {
		PartitionControlServer pcs = partitionServerService.findByIpEndpoint(ipAddress, port);
		if (pcs == null) {
			throw new IllegalStateException("PCS server not registered in DB for " + ipAddress + ":" + port);
		}
		if (!segment.getSegmentName().equals(pcs.getSegment())) {
			throw new IllegalStateException("PCS server segment mismatch. [" + segment.getSegmentName()
					+ "] registered is " + pcs.getSegment());
		}
	}

	private SessionHandle mapSession(org.tdmx.server.pcs.protobuf.PCSServer.SessionHandle sh) {
		if (sh == null) {
			return null;
		}
		SessionHandle handle = new SessionHandle(sh.getSegment(), mapApi(sh.getApiName()), sh.getSessionKey(),
				mapAttributes(sh.getAttributeList()));
		return handle;
	}

	private Map<SeedAttribute, Long> mapAttributes(List<org.tdmx.server.pcs.protobuf.Common.AttributeValue> attrs) {
		if (attrs == null) {
			return null;
		}
		Map<SeedAttribute, Long> attributes = new HashMap<>();
		for (org.tdmx.server.pcs.protobuf.Common.AttributeValue attr : attrs) {
			attributes.put(map(attr.getName()), attr.getValue());
		}
		return attributes;
	}

	private List<ServiceHandle> mapServices(List<org.tdmx.server.pcs.protobuf.PCSServer.ServiceHandle> services) {
		if (services == null) {
			return null;
		}
		List<ServiceHandle> result = new ArrayList<>();
		for (org.tdmx.server.pcs.protobuf.PCSServer.ServiceHandle service : services) {
			PKIXCertificate serverCert = CertificateIOUtils.safeDecodeX509(service.getServerCert().toByteArray());
			ServiceHandle handle = new ServiceHandle(service.getSegment(), mapApi(service.getApiName()),
					service.getHttpsUrl(), serverCert);
			result.add(handle);
		}
		return result;
	}

	private SeedAttribute map(org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId name) {
		if (name == null) {
			return null;
		}
		return SeedAttribute.valueOf(name.name());
	}

	private WebServiceApiName mapApi(String apiName) {
		return apiName != null ? WebServiceApiName.valueOf(apiName) : null;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public RelayControlServiceListener getRelayService() {
		return relayService;
	}

	public void setRelayService(RelayControlServiceListener relayService) {
		this.relayService = relayService;
	}

	public PartitionControlServerService getPartitionServerService() {
		return partitionServerService;
	}

	public void setPartitionServerService(PartitionControlServerService partitionServerService) {
		this.partitionServerService = partitionServerService;
	}

	public ControlServiceListener getControlListener() {
		return controlListener;
	}

	public void setControlListener(ControlServiceListener controlListener) {
		this.controlListener = controlListener;
	}

	public ControlService getControlService() {
		return controlService;
	}

	public void setControlService(ControlService controlService) {
		this.controlService = controlService;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public int getCoreRpcExecutorThreads() {
		return coreRpcExecutorThreads;
	}

	public void setCoreRpcExecutorThreads(int coreRpcExecutorThreads) {
		this.coreRpcExecutorThreads = coreRpcExecutorThreads;
	}

	public int getMaxRpcExecutorThreads() {
		return maxRpcExecutorThreads;
	}

	public void setMaxRpcExecutorThreads(int maxRpcExecutorThreads) {
		this.maxRpcExecutorThreads = maxRpcExecutorThreads;
	}

	public int getAcceptorThreads() {
		return acceptorThreads;
	}

	public void setAcceptorThreads(int acceptorThreads) {
		this.acceptorThreads = acceptorThreads;
	}

	public int getIoThreads() {
		return ioThreads;
	}

	public void setIoThreads(int ioThreads) {
		this.ioThreads = ioThreads;
	}

	public int getIoBufferSize() {
		return ioBufferSize;
	}

	public void setIoBufferSize(int ioBufferSize) {
		this.ioBufferSize = ioBufferSize;
	}

	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	public void setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}

	public long getShutdownTimeoutMs() {
		return shutdownTimeoutMs;
	}

	public void setShutdownTimeoutMs(long shutdownTimeoutMs) {
		this.shutdownTimeoutMs = shutdownTimeoutMs;
	}

	public Segment getSegment() {
		return segment;
	}

}
