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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.PartitionControlServer;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.job.NamedThreadFactory;
import org.tdmx.lib.control.service.PartitionControlServerService;
import org.tdmx.server.pcs.CacheInvalidationMessageListener;
import org.tdmx.server.pcs.protobuf.Broadcast;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.scs.LocalControlServiceImpl;
import org.tdmx.server.ws.session.WebServiceApiName;

import com.google.protobuf.RpcCallback;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import com.googlecode.protobuf.pro.duplex.client.RpcClientConnectionWatchdog;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Handles inbound RPC calls from SCS, ROS and WS clients.
 * 
 * @author Peter
 *
 */
public class RelayOutboundServiceConnector implements Manageable, Runnable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static Logger log = LoggerFactory.getLogger(LocalControlServiceImpl.class);

	private static final String PCS_ATTRIBUTE = "PCS";

	private int connectTimeoutMillis = 5000;
	private int connectResponseTimeoutMillis = 10000;
	private int coreRpcExecutorThreads = 2;
	private int maxRpcExecutorThreads = 10;
	private int ioThreads = 16;
	private int ioBufferSize = 1048576;
	private boolean tcpNoDelay = true;
	private long shutdownTimeoutMs = 10000;

	private DuplexTcpClientPipelineFactory clientFactory;
	private Bootstrap bootstrap;
	private CleanShutdownHandler shutdownHandler;

	private List<PartitionControlServer> serverList;
	private Map<PartitionControlServer, RelayControlServiceClient> serverProxyMap = new HashMap<>();

	private Segment segment = null;

	private PartitionControlServerService partitionServerService;
	/**
	 * Delegate for handling Broadcast events.
	 */
	private CacheInvalidationMessageListener cacheInvalidationListener;

	/**
	 * Delay in seconds between notifying PCS about load statistics.
	 */
	private int loadStatisticsNotificationIntervalSec = 60;

	/**
	 * ROS server connector tcpIp address.
	 */
	private String serverAddress;

	/**
	 * ROS server connector port.
	 */
	private int localPort;

	/**
	 * The session handling capacity of this ROS.
	 */
	private int sessionCapacity;

	// - internal
	private ScheduledExecutorService scheduledThreadPool = null;
	private ExecutorService loadNotificationExecutor = null;

	// TODO get load from the "server" ROS part.

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void start(Segment segment, List<WebServiceApiName> apis) {
		String localHostAddress = null;
		try {
			localHostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("Unable to determine localhost IP address.", e);
		}

		String serverHostname = StringUtils.hasText(serverAddress) ? serverAddress : localHostAddress;
		final String rosTcpEndpoint = serverHostname + ":" + localPort;

		this.segment = segment;
		try {
			clientFactory = new DuplexTcpClientPipelineFactory();

			clientFactory.setConnectResponseTimeoutMillis(connectResponseTimeoutMillis);
			RpcServerCallExecutor rpcExecutor = new ThreadPoolCallExecutor(coreRpcExecutorThreads,
					maxRpcExecutorThreads);
			clientFactory.setRpcServerCallExecutor(rpcExecutor);

			// RPC payloads are uncompressed when logged - so reduce logging
			CategoryPerServiceLogger logger = new CategoryPerServiceLogger();
			logger.setLogRequestProto(false);
			logger.setLogResponseProto(false);
			clientFactory.setRpcLogger(logger);

			final RpcCallback<Broadcast.BroadcastMessage> serverBroadcastCallback = new RpcCallback<Broadcast.BroadcastMessage>() {

				@Override
				public void run(Broadcast.BroadcastMessage parameter) {
					if (parameter.getCacheInvalidation() != null) {
						final CacheInvalidationMessageListener delegate = getCacheInvalidationListener();
						if (delegate != null) {
							delegate.handleBroadcast(parameter.getCacheInvalidation());
						}
					}
				}

			};
			// Set up the event pipeline factory.
			// setup a RPC event listener - it just logs what happens
			RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();

			final RpcConnectionEventListener listener = new RpcConnectionEventListener() {

				@Override
				public void connectionReestablished(RpcClientChannel clientChannel) {
					log.info("connectionReestablished " + clientChannel);
					connection(clientChannel);
				}

				@Override
				public void connectionOpened(RpcClientChannel clientChannel) {
					log.info("connectionOpened " + clientChannel);
					connection(clientChannel);
				}

				@Override
				public void connectionLost(RpcClientChannel clientChannel) {
					log.info("connectionLost " + clientChannel);
					disconnection(clientChannel);
				}

				@Override
				public void connectionChanged(RpcClientChannel clientChannel) {
					log.info("connectionChanged " + clientChannel);
					connection(clientChannel);
				}

				private void disconnection(RpcClientChannel clientChannel) {
					PartitionControlServer pcs = getPartitionControlServer(clientChannel);
					serverProxyMap.remove(pcs);
					clientChannel.setOobMessageCallback(null, null);
				}

				private void connection(RpcClientChannel clientChannel) {
					PartitionControlServer pcs = getPartitionControlServer(clientChannel);
					RelayControlServiceClient client = new RelayControlServiceClient(clientChannel, rosTcpEndpoint);

					serverProxyMap.put(pcs, client);
					client.registerRelayServer(sessionCapacity);

					// register ourselves to receive broadcast messages
					clientChannel.setOobMessageCallback(Broadcast.BroadcastMessage.getDefaultInstance(),
							serverBroadcastCallback);
				}

				private PartitionControlServer getPartitionControlServer(RpcClientChannel clientChannel) {
					PartitionControlServer pcs = (PartitionControlServer) clientChannel.getAttribute(PCS_ATTRIBUTE);
					if (pcs == null) {
						throw new IllegalStateException("No PCS attribute on clientChannel " + clientChannel);
					}
					return pcs;
				}
			};
			rpcEventNotifier.addEventListener(listener);
			clientFactory.registerConnectionEventListener(rpcEventNotifier);

			bootstrap = new Bootstrap();
			EventLoopGroup workers = new NioEventLoopGroup(ioThreads,
					new RenamingThreadFactoryProxy("PCS-client-workers", Executors.defaultThreadFactory()));

			bootstrap.group(workers);
			bootstrap.handler(clientFactory);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.TCP_NODELAY, tcpNoDelay);
			bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis);
			bootstrap.option(ChannelOption.SO_SNDBUF, ioBufferSize);
			bootstrap.option(ChannelOption.SO_RCVBUF, ioBufferSize);

			RpcClientConnectionWatchdog watchdog = new RpcClientConnectionWatchdog(clientFactory, bootstrap);
			watchdog.setThreadName("SCS-PCS RpcClient Watchdog");
			rpcEventNotifier.addEventListener(watchdog);
			watchdog.start();

			shutdownHandler = new CleanShutdownHandler();
			shutdownHandler.addResource(workers);
			shutdownHandler.addResource(rpcExecutor);
			shutdownHandler.addResource(watchdog);

			serverList = partitionServerService.findBySegment(this.segment.getSegmentName());
			connectToPcsServers();
		} catch (Exception e) {
			throw new RuntimeException("Unable to do initial connect to PCS", e);
		}

		scheduledThreadPool = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("ROS-LoadNotificationScheduler"));

		loadNotificationExecutor = Executors.newFixedThreadPool(1,
				new NamedThreadFactory("ROS-LoadStatisticNotificationExecutor"));
		scheduledThreadPool.scheduleWithFixedDelay(this, loadStatisticsNotificationIntervalSec,
				loadStatisticsNotificationIntervalSec, TimeUnit.SECONDS);

	}

	@Override
	public void stop() {
		if (scheduledThreadPool != null) {
			scheduledThreadPool.shutdown();
			try {
				scheduledThreadPool.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.warn("Interrupted whilst waiting for termination of scheduledThreadPool.", e);
			}
			scheduledThreadPool = null;
		}

		if (loadNotificationExecutor != null) {
			loadNotificationExecutor.shutdown();
			try {
				loadNotificationExecutor.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.warn("Interrupted whilst waiting for termination of loadNotificationExecutor.", e);
			}
			loadNotificationExecutor = null;
		}

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
		serverList = null;
		bootstrap = null;
		clientFactory = null;
		segment = null;
		// the serverProxyMap should clear automatically when each PCS server connection closes, however we do this just
		// in case.
		if (!serverProxyMap.isEmpty()) {
			log.warn("serverProxyMap should have been cleared on shutdown.");
			serverProxyMap.clear();
		}
	}

	@Override
	public void run() {
		log.info("Notifying PCS about this ROS services load.");

		for (RelayControlServiceClient pcs : serverProxyMap.values()) {
			pcs.notifyLoad(100); // TODO
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	/**
	 * Connect to each of the PCS servers.
	 * 
	 * @throws IOException
	 */
	private void connectToPcsServers() throws IOException {
		for (PartitionControlServer pcs : serverList) {
			if (pcs.getServerModulo() < 0 || pcs.getServerModulo() > serverList.size()) {
				throw new IllegalStateException("pcs modulo " + pcs.getServerModulo() + " inconsistent for " + pcs);
			}

			PeerInfo server = new PeerInfo(pcs.getIpAddress(), pcs.getPort());

			Map<String, Object> attributes = new HashMap<>();
			attributes.put(PCS_ATTRIBUTE, pcs);

			clientFactory.peerWith(server, bootstrap, attributes);
			// the event listener hooks up the localproxy
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public int getLoadStatisticsNotificationIntervalSec() {
		return loadStatisticsNotificationIntervalSec;
	}

	public void setLoadStatisticsNotificationIntervalSec(int loadStatisticsNotificationIntervalSec) {
		this.loadStatisticsNotificationIntervalSec = loadStatisticsNotificationIntervalSec;
	}

	public int getConnectTimeoutMillis() {
		return connectTimeoutMillis;
	}

	public void setConnectTimeoutMillis(int connectTimeoutMillis) {
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

	public int getConnectResponseTimeoutMillis() {
		return connectResponseTimeoutMillis;
	}

	public void setConnectResponseTimeoutMillis(int connectResponseTimeoutMillis) {
		this.connectResponseTimeoutMillis = connectResponseTimeoutMillis;
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

	public PartitionControlServerService getPartitionServerService() {
		return partitionServerService;
	}

	public void setPartitionServerService(PartitionControlServerService partitionServerService) {
		this.partitionServerService = partitionServerService;
	}

	public CacheInvalidationMessageListener getCacheInvalidationListener() {
		return cacheInvalidationListener;
	}

	public void setCacheInvalidationListener(CacheInvalidationMessageListener cacheInvalidationListener) {
		this.cacheInvalidationListener = cacheInvalidationListener;
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

	public int getSessionCapacity() {
		return sessionCapacity;
	}

	public void setSessionCapacity(int sessionCapacity) {
		this.sessionCapacity = sessionCapacity;
	}

}
