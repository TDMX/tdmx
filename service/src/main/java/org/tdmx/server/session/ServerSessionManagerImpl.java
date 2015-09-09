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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.job.NamedThreadFactory;
import org.tdmx.server.pcs.ServerServiceStatistics;
import org.tdmx.server.pcs.ServerSessionController;
import org.tdmx.server.pcs.ServiceStatistic;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;
import org.tdmx.server.ws.session.WebServiceSessionManager;

import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClient;
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
 * The ServerSessionManager manages all the WebServiceSessionManagers and is controlled by the PartitionControlService
 * to create sessions. Notifies the PartitionControlService when sessions are idle and are removed.
 * 
 * @author Peter
 * 
 */
public class ServerSessionManagerImpl implements Manageable, Runnable, ServerSessionController {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// TODO configuration properties in file

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ServerSessionManagerImpl.class);

	private int connectTimeoutMillis = 5000;
	private int connectResponseTimeoutMillis = 10000;
	private int coreRpcExecutorThreads = 2;
	private int maxRpcExecutorThreads = 10;
	private int ioThreads = 16;
	private int ioBufferSize = 1048576;
	private boolean tcpNoDelay = true;

	private DuplexTcpClientPipelineFactory clientFactory;
	private Bootstrap bootstrap;
	private CleanShutdownHandler shutdownHandler;

	private Map<RpcClientChannel, LocalControlServiceListenerClient> channelMap = new HashMap<>();
	/**
	 * Delay in seconds between session timeout checks.
	 */
	private int timeoutCheckIntervalSec = 60;

	// - internal
	private List<WebServiceApiName> apiList;
	private String segment;
	private ScheduledExecutorService scheduledThreadPool = null;
	private ExecutorService sessionTimeoutExecutor = null;
	/**
	 * The WebServiceSessionManagers for MOS, MDS, MRS, ZAS arranged in a Map.
	 */
	private Map<WebServiceApiName, WebServiceSessionManager> apiManagerMap = null;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public ServiceStatistic createSession(WebServiceApiName apiName, String sessionId, PKIXCertificate cert,
			Map<SeedAttribute, Long> seedAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceStatistic addCertificate(WebServiceApiName apiName, String sessionId, PKIXCertificate cert) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerServiceStatistics removeCertificate(PKIXCertificate cert) {
		for (Entry<WebServiceApiName, WebServiceSessionManager> apis : apiManagerMap.entrySet()) {
			log.info("Removing cert " + cert.getFingerprint() + " from " + apis.getKey());
			apis.getValue().removeCertificate(cert);
		}
		return null;
	}

	@Override
	public ServerServiceStatistics getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	public void init() {
		scheduledThreadPool = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("SessionTimeoutExecutionService"));

		sessionTimeoutExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("SessionTimeoutExecutor"));
	}

	@Override
	public void start(String segment, List<WebServiceApiName> apis) {
		this.apiList = Collections.unmodifiableList(apis);
		this.segment = segment;
		scheduledThreadPool.scheduleWithFixedDelay(this, getTimeoutCheckIntervalSec(), getTimeoutCheckIntervalSec(),
				TimeUnit.SECONDS);

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

			// Set up the event pipeline factory.
			// setup a RPC event listener - it just logs what happens
			RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();

			final RpcConnectionEventListener listener = new RpcConnectionEventListener() {

				@Override
				public void connectionReestablished(RpcClientChannel clientChannel) {
					log.info("connectionReestablished " + clientChannel);
					LocalControlServiceListenerClient client = channelMap.get(clientChannel);
					if (client != null) {
						client.setRpcClient(clientChannel);
					}
				}

				@Override
				public void connectionOpened(RpcClientChannel clientChannel) {
					log.info("connectionOpened " + clientChannel);
					LocalControlServiceListenerClient client = channelMap.get(clientChannel);
					if (client != null) {
						client.setRpcClient(clientChannel);
					}
				}

				@Override
				public void connectionLost(RpcClientChannel clientChannel) {
					log.info("connectionLost " + clientChannel);
					LocalControlServiceListenerClient client = channelMap.get(clientChannel);
					if (client != null) {
						client.setRpcClient(null);

						for (Entry<WebServiceApiName, WebServiceSessionManager> apis : apiManagerMap.entrySet()) {
							String controllerId = getControllerId(clientChannel);
							log.info("Disconnecting controller " + controllerId + " from " + apis.getKey());
							apis.getValue().disconnectController(controllerId);
						}
					}
				}

				@Override
				public void connectionChanged(RpcClientChannel clientChannel) {
					log.info("connectionChanged " + clientChannel);
					LocalControlServiceListenerClient client = channelMap.get(clientChannel);
					if (client != null) {
						client.setRpcClient(clientChannel);
					}
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
			rpcEventNotifier.addEventListener(watchdog);
			watchdog.start();

			shutdownHandler = new CleanShutdownHandler();
			shutdownHandler.addResource(workers);
			shutdownHandler.addResource(rpcExecutor);
			shutdownHandler.addResource(watchdog);

			// TODO FIXME connect to each PCS server
			RpcClient client = connect("192.168.178.21", 8446);
			// after connection the PCS remote cannot distinguish if we are a SCS or WS client
			// until WS clients call registerServer

			LocalControlServiceListenerClient local = new LocalControlServiceListenerClient();
			local.setRpcClient(client);

			channelMap.put(client, local);

			// register ourself at all remote PCSs
			registerServer();
		} catch (Exception e) {
			throw new RuntimeException("Unable to do initial connect to PCS", e);
		}
	}

	private RpcClient connect(String serverHostname, int serverPort) throws Exception {
		PeerInfo server = new PeerInfo(serverHostname, serverPort);

		return clientFactory.peerWith(server, bootstrap);
	}

	private void registerServer() {
		// TODO server handles
		for (Entry<RpcClientChannel, LocalControlServiceListenerClient> entry : channelMap.entrySet()) {
			entry.getValue().registerServer(null, null);
		}
	}

	private String getControllerId(RpcClientChannel channel) {
		return channel.getPeerInfo().toString();
	}

	@Override
	public void stop() {
		if (scheduledThreadPool == null) {
			return; // never initialized
		}
		scheduledThreadPool.shutdown();
		try {
			scheduledThreadPool.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("Interrupted whilst waiting for termination of scheduledThreadPool.", e);
		}
		sessionTimeoutExecutor.shutdown();
		try {
			sessionTimeoutExecutor.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("Interrupted whilst waiting for termination of jobRunners.", e);
		}

		if (shutdownHandler != null) {
			shutdownHandler.shutdown();
		}
	}

	@Override
	public void run() {
		for (WebServiceApiName api : apiList) {
			processIdleSessions(api);
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void processIdleSessions(WebServiceApiName api) {
		log.info("Processing idle sessions for " + api + " in segment " + segment);
		// TODO
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public void setApiManagerMap(Map<WebServiceApiName, WebServiceSessionManager> apiManagerMap) {
		this.apiManagerMap = apiManagerMap;
	}

	public Map<WebServiceApiName, WebServiceSessionManager> getApiManagerMap() {
		return apiManagerMap;
	}

	public int getTimeoutCheckIntervalSec() {
		return timeoutCheckIntervalSec;
	}

	public void setTimeoutCheckIntervalSec(int timeoutCheckIntervalSec) {
		this.timeoutCheckIntervalSec = timeoutCheckIntervalSec;
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

}
