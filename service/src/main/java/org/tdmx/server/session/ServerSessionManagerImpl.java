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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.PartitionControlServer;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.job.NamedThreadFactory;
import org.tdmx.lib.control.service.PartitionControlServerService;
import org.tdmx.server.pcs.CacheInvalidationEventNotifier;
import org.tdmx.server.pcs.CacheInvalidationMessageListener;
import org.tdmx.server.pcs.ServiceHandle;
import org.tdmx.server.pcs.protobuf.Broadcast;
import org.tdmx.server.pcs.protobuf.Broadcast.CacheInvalidationMessage;
import org.tdmx.server.pcs.protobuf.PCSClient.AddCertificateRequest;
import org.tdmx.server.pcs.protobuf.PCSClient.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.PCSClient.CreateSessionRequest;
import org.tdmx.server.pcs.protobuf.PCSClient.GetStatisticsRequest;
import org.tdmx.server.pcs.protobuf.PCSClient.RemoveCertificateRequest;
import org.tdmx.server.pcs.protobuf.PCSClient.SessionManagerProxy;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.ws.ServerRuntimeContextService;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSession;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;
import org.tdmx.server.ws.session.WebServiceSessionManager;

import com.google.protobuf.BlockingService;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import com.googlecode.protobuf.pro.duplex.client.RpcClientConnectionWatchdog;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ServerRpcController;
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
public class ServerSessionManagerImpl implements Manageable, Runnable, CacheInvalidationEventNotifier,
		SessionCertificateInvalidationService, SessionManagerProxy.BlockingInterface {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ServerSessionManagerImpl.class);

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
	private Map<PartitionControlServer, LocalControlServiceListenerClient> serverProxyMap = new HashMap<>();

	/**
	 * Delay in seconds between session timeout checks.
	 */
	private int timeoutCheckIntervalSec = 60;
	/**
	 * Sessions created prior to sessionCreationTimeoutHours are considered too old and need renewing.
	 */
	private int sessionCreationTimeoutHours = 24;
	/**
	 * Sessions not used since sessionIdleTimeoutMinutes are considered orphaned and discarded.
	 */
	private int sessionIdleTimeoutMinutes = 30;

	// - internal
	private List<WebServiceApiName> apiList;
	private Map<WebServiceApiName, WebServiceSessionManagerHolder> apiManagerMap = null;
	private Segment segment;
	private ScheduledExecutorService scheduledThreadPool = null;
	private ExecutorService sessionTimeoutExecutor = null;

	/**
	 * The runtime context providing the server's IP and certificate.
	 */
	private ServerRuntimeContextService runtimeService;

	/**
	 * The WebServiceSessionManagers for MOS, MDS, MRS, ZAS. A subset of these are started at startup time and placed in
	 * the apiManagerMap.
	 */
	private List<WebServiceSessionManager> webServiceSessionManagers;

	/**
	 * The PartitionControlService gives us the information about the PCS servers.
	 */
	private PartitionControlServerService partitionServerService;

	/**
	 * Delegate for handling CacheInvalidationMessage events.
	 */
	private CacheInvalidationMessageListener cacheInvalidationListener;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public org.tdmx.server.pcs.protobuf.PCSClient.ServiceStatistic createSession(RpcController controller,
			CreateSessionRequest request) throws ServiceException {
		WebServiceApiName api = mapApi(request.getApiName());
		WebServiceSessionManagerHolder h = apiManagerMap.get(api);
		if (h != null) {
			String controllerId = getControllerId(ServerRpcController.getRpcChannel(controller));
			PKIXCertificate cert = CertificateIOUtils.safeDecodeX509(request.getClientCert().toByteArray());
			int loadValue = h.getSessionManager().createSession(request.getSessionId(), controllerId, cert,
					mapAttributes(request.getAttributeList()));
			h.setLoadValue(loadValue);

			// formulate the result
			org.tdmx.server.pcs.protobuf.PCSClient.ServiceStatistic.Builder statisticBuilder = org.tdmx.server.pcs.protobuf.PCSClient.ServiceStatistic
					.newBuilder();
			statisticBuilder.setApiName(api.name());
			statisticBuilder.setHttpsUrl(h.getHandle().getHttpsUrl());
			statisticBuilder.setLoadValue(loadValue);
			return statisticBuilder.build();
		}
		return null;
	}

	@Override
	public org.tdmx.server.pcs.protobuf.PCSClient.ServiceStatistic addSessionCertificate(RpcController controller,
			AddCertificateRequest request) throws ServiceException {
		WebServiceApiName api = mapApi(request.getApiName());
		WebServiceSessionManagerHolder h = apiManagerMap.get(api);
		if (h != null) {
			PKIXCertificate cert = CertificateIOUtils.safeDecodeX509(request.getClientCert().toByteArray());
			int loadValue = h.getSessionManager().addCertificate(request.getSessionId(), cert);
			h.setLoadValue(loadValue);

			// formulate the result
			org.tdmx.server.pcs.protobuf.PCSClient.ServiceStatistic.Builder statisticBuilder = org.tdmx.server.pcs.protobuf.PCSClient.ServiceStatistic
					.newBuilder();
			statisticBuilder.setApiName(api.name());
			statisticBuilder.setHttpsUrl(h.getHandle().getHttpsUrl());
			statisticBuilder.setLoadValue(loadValue);
			return statisticBuilder.build();
		}
		return null;
	}

	@Override
	public org.tdmx.server.pcs.protobuf.PCSClient.ServerServiceStatistics removeCertificate(RpcController controller,
			RemoveCertificateRequest request) throws ServiceException {
		for (Entry<WebServiceApiName, WebServiceSessionManagerHolder> apis : apiManagerMap.entrySet()) {
			WebServiceSessionManagerHolder h = apis.getValue();
			PKIXCertificate cert = CertificateIOUtils.safeDecodeX509(request.getClientCert().toByteArray());
			int loadValue = h.getSessionManager().removeCertificate(cert);
			h.setLoadValue(loadValue);

			return getStatistics(controller, null);
		}
		return null;
	}

	@Override
	public org.tdmx.server.pcs.protobuf.PCSClient.ServerServiceStatistics getStatistics(RpcController controller,
			GetStatisticsRequest request) throws ServiceException {
		org.tdmx.server.pcs.protobuf.PCSClient.ServerServiceStatistics.Builder stats = org.tdmx.server.pcs.protobuf.PCSClient.ServerServiceStatistics
				.newBuilder();
		for (Entry<WebServiceApiName, WebServiceSessionManagerHolder> apis : apiManagerMap.entrySet()) {
			WebServiceSessionManagerHolder h = apis.getValue();

			org.tdmx.server.pcs.protobuf.PCSClient.ServiceStatistic.Builder stat = org.tdmx.server.pcs.protobuf.PCSClient.ServiceStatistic
					.newBuilder();
			stat.setApiName(apis.getKey().name());
			stat.setHttpsUrl(h.getHandle().getHttpsUrl());
			stat.setLoadValue(h.getLoadValue());
			stats.addStatistics(stat);
		}
		return stats.build();
	}

	@Override
	public void start(Segment segment, List<WebServiceApiName> apis) {
		if (sessionIdleTimeoutMinutes <= 0) {
			throw new IllegalArgumentException("sessionIdleTimeoutMinutes must be positive");
		}
		if (sessionCreationTimeoutHours <= 0) {
			throw new IllegalArgumentException("sessionCreationTimeoutHours must be positive");
		}
		this.apiList = Collections.unmodifiableList(apis);
		this.segment = segment;
		scheduledThreadPool = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("SessionTimeoutExecutionService"));

		sessionTimeoutExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("SessionTimeoutExecutor"));
		scheduledThreadPool.scheduleWithFixedDelay(this, timeoutCheckIntervalSec, timeoutCheckIntervalSec,
				TimeUnit.SECONDS);

		// construct the service list of this server in the apiManagerMap
		apiManagerMap = new HashMap<>();
		for (WebServiceSessionManager mgr : webServiceSessionManagers) {
			if (apiList.contains(mgr.getApiName())) {
				ServiceHandle service = new ServiceHandle(segment.getSegmentName(), mgr.getApiName(), mgr.getHttpsUrl(),
						runtimeService.getPublicKey());
				WebServiceSessionManagerHolder serviceHolder = new WebServiceSessionManagerHolder(service, mgr);
				apiManagerMap.put(mgr.getApiName(), serviceHolder);
			}
		}

		// create the protobuf interface to ALL the PCS servers which are known for the segment
		try {
			clientFactory = new DuplexTcpClientPipelineFactory();

			clientFactory.setConnectResponseTimeoutMillis(connectResponseTimeoutMillis);
			RpcServerCallExecutor rpcExecutor = new ThreadPoolCallExecutor(coreRpcExecutorThreads,
					maxRpcExecutorThreads);
			clientFactory.setRpcServerCallExecutor(rpcExecutor);

			final RpcCallback<Broadcast.BroadcastMessage> serverBroadcastCallback = new RpcCallback<Broadcast.BroadcastMessage>() {

				@Override
				public void run(Broadcast.BroadcastMessage parameter) {
					if (parameter.getCacheInvalidation() != null) {
						final CacheInvalidationMessageListener delegate = getCacheInvalidationListener();
						if (delegate != null) {
							delegate.handleBroadcast(parameter.getCacheInvalidation());
						}
					} else {
						log.warn("Unhandled broadcast event." + parameter);
					}
				}

			};
			// RPC payloads are uncompressed when logged - so reduce logging
			CategoryPerServiceLogger logger = new CategoryPerServiceLogger();
			logger.setLogRequestProto(false);
			logger.setLogResponseProto(false);
			clientFactory.setRpcLogger(logger);

			// Set up the event pipeline factory.
			// setup a RPC event listener - it just logs what happens
			RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();

			final RpcConnectionEventListener listener = new RpcConnectionEventListener() {

				private void connection(RpcClientChannel clientChannel) {
					log.info("initial connect " + clientChannel);

					PartitionControlServer pcs = getPartitionControlServer(clientChannel);
					LocalControlServiceListenerClient client = new LocalControlServiceListenerClient(clientChannel);
					serverProxyMap.put(pcs, client);

					// register ourselves to receive broadcast messages
					client.registerServer(getManagedServiceList(), null);

					clientChannel.setOobMessageCallback(Broadcast.BroadcastMessage.getDefaultInstance(),
							serverBroadcastCallback);
				}

				private void disconnection(RpcClientChannel clientChannel) {
					PartitionControlServer pcs = getPartitionControlServer(clientChannel);
					LocalControlServiceListenerClient client = serverProxyMap.remove(pcs);
					if (client != null) {
						for (Entry<WebServiceApiName, WebServiceSessionManagerHolder> apis : apiManagerMap.entrySet()) {
							String controllerId = getControllerId(clientChannel);
							log.info("Disconnecting controller " + controllerId + " from " + apis.getKey());
							apis.getValue().getSessionManager().disconnectController(controllerId);
						}
					}
					clientChannel.setOobMessageCallback(null, null);
				}

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
				public void connectionChanged(RpcClientChannel clientChannel) {
					log.info("connectionChanged " + clientChannel);
					connection(clientChannel);
				}

				@Override
				public void connectionLost(RpcClientChannel clientChannel) {
					log.info("connectionLost " + clientChannel);
					disconnection(clientChannel);
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

			BlockingService sessionManagerServiceProxy = SessionManagerProxy.newReflectiveBlockingService(this);
			clientFactory.getRpcServiceRegistry().registerService(sessionManagerServiceProxy);

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
			watchdog.setThreadName("SSM-PCS RPCClient Watchdog");
			rpcEventNotifier.addEventListener(watchdog);
			watchdog.start();

			shutdownHandler = new CleanShutdownHandler();
			shutdownHandler.addResource(workers);
			shutdownHandler.addResource(rpcExecutor);
			shutdownHandler.addResource(watchdog);

			// connect to each PCS server
			serverList = partitionServerService.findBySegment(this.segment.getSegmentName());
			connectToPcsServers();
			// after connection the PCS remote cannot distinguish if we are a SCS or WS client
			// until WS clients call registerServer

		} catch (Exception e) {
			throw new RuntimeException("Unable to do initial connect to PCS", e);
		}
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

		if (sessionTimeoutExecutor != null) {
			sessionTimeoutExecutor.shutdown();
			try {
				sessionTimeoutExecutor.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.warn("Interrupted whilst waiting for termination of jobRunners.", e);
			}
			sessionTimeoutExecutor = null;
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
		bootstrap = null;
		clientFactory = null;
		apiManagerMap = null;
		segment = null;
		apiList = null;
		// although the channelMap should clear when each connection closes, we remove all entries forcibly.
		if (!serverProxyMap.isEmpty()) {
			log.warn("serverProxyMap should have been cleared on shutdown.");
			serverProxyMap.clear();
		}
	}

	@Override
	/**
	 * Perform a check on the existing Sessions if they need discarding.
	 */
	public void run() {
		for (Entry<WebServiceApiName, WebServiceSessionManagerHolder> apis : apiManagerMap.entrySet()) {
			WebServiceSessionManagerHolder h = apis.getValue();

			Calendar idleCutoff = Calendar.getInstance();
			idleCutoff.add(Calendar.MINUTE, 0 - sessionIdleTimeoutMinutes);
			Date idleCutoffDate = idleCutoff.getTime();

			Calendar creationCutoff = Calendar.getInstance();
			creationCutoff.add(Calendar.HOUR, 0 - sessionCreationTimeoutHours);
			Date creationCutoffDate = creationCutoff.getTime();

			log.info("Processing idle sessions for " + apis.getKey() + " in segment " + segment + " created before "
					+ creationCutoffDate + " or not used after " + idleCutoffDate);
			List<WebServiceSession> sessions = h.getSessionManager().getIdleSessions(idleCutoff.getTime(),
					creationCutoff.getTime());

			for (Entry<PartitionControlServer, LocalControlServiceListenerClient> channel : serverProxyMap.entrySet()) {
				Set<String> sessionIds = new HashSet<>();
				String controllerId = getControllerId(channel.getValue().getRpcClient());

				for (WebServiceSession session : sessions) {
					if (controllerId.equals(session.getControllerId())) {
						sessionIds.add(session.getSessionId());
					}
				}
				if (!sessionIds.isEmpty()) {
					channel.getValue().notifySessionsRemoved(apis.getKey(), sessionIds);

				}

			}
		}
	}

	@Override
	public void invalidateCertificate(PKIXCertificate cert) {
		// invalidate the certificate at all PCS instances.
		// this will have the ServerSessionController.invalidateCertificate at the PCS which calls WS server which knows
		// the certificate
		for (Entry<PartitionControlServer, LocalControlServiceListenerClient> pcsServer : serverProxyMap.entrySet()) {
			LocalControlServiceListenerClient client = pcsServer.getValue();

			client.invalidateCertificate(cert);
		}
	}

	@Override
	public boolean broadcastEvent(CacheInvalidationMessage cacheInvalidationMsg) {
		if (serverProxyMap.size() > 0) {
			// if we are connected to any PCS at all, then taking one is enough.
			// we use a hash distribution of the unique ID but we could do round-robin instead.
			LocalControlServiceListenerClient pcsServer = consistentHashToServer(cacheInvalidationMsg.getId());
			if (pcsServer != null) {
				Broadcast.BroadcastMessage.Builder eventBuilder = Broadcast.BroadcastMessage.newBuilder();
				eventBuilder.setCacheInvalidation(cacheInvalidationMsg);

				Broadcast.BroadcastMessage msg = eventBuilder.build();

				pcsServer.getRpcClient().sendOobMessage(msg);
				return true;
			} else {
				log.warn("PCS Server targetted for cache invalidation not connected.");
			}
		}
		return false;
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

	private int consistentHashCode(String key) {
		return key.hashCode() % serverList.size();
	}

	/**
	 * Return the PCS server proxy to which the key maps to with a consistent hash.
	 * 
	 * @param key
	 * @return null if the PCS server proxy is not connected to, otherwise the PCS server to which the key maps
	 *         consistently.
	 */
	private LocalControlServiceListenerClient consistentHashToServer(String key) {
		int serverNo = consistentHashCode(key);
		PartitionControlServer server = serverList.get(serverNo);
		LocalControlServiceListenerClient localProxy = serverProxyMap.get(server);
		return localProxy;
	}

	/**
	 * A helper value type holding the ServerHandle.
	 * 
	 * @author Peter
	 *
	 */
	private static class WebServiceSessionManagerHolder {
		private final ServiceHandle handle;
		private final WebServiceSessionManager sessionManager;

		private int loadValue;

		public WebServiceSessionManagerHolder(ServiceHandle handle, WebServiceSessionManager sessionManager) {
			if (handle == null) {
				throw new IllegalArgumentException();
			}
			if (sessionManager == null) {
				throw new IllegalArgumentException();
			}
			this.handle = handle;
			this.sessionManager = sessionManager;
		}

		public ServiceHandle getHandle() {
			return handle;
		}

		public WebServiceSessionManager getSessionManager() {
			return sessionManager;
		}

		public int getLoadValue() {
			return loadValue;
		}

		public void setLoadValue(int loadValue) {
			this.loadValue = loadValue;
		}
	}

	private String getControllerId(RpcClientChannel channel) {
		return channel.getPeerInfo().toString();
	}

	private Map<SeedAttribute, Long> mapAttributes(List<org.tdmx.server.pcs.protobuf.PCSClient.AttributeValue> attrs) {
		if (attrs == null) {
			return null;
		}
		Map<SeedAttribute, Long> attributes = new HashMap<>();
		for (org.tdmx.server.pcs.protobuf.PCSClient.AttributeValue attr : attrs) {
			attributes.put(map(attr.getName()), attr.getValue());
		}
		return attributes;
	}

	private SeedAttribute map(AttributeId name) {
		if (name == null) {
			return null;
		}
		return SeedAttribute.valueOf(name.name());
	}

	private WebServiceApiName mapApi(String apiName) {
		return apiName != null ? WebServiceApiName.valueOf(apiName) : null;
	}

	private List<ServiceHandle> getManagedServiceList() {
		List<ServiceHandle> serviceList = new ArrayList<>();
		for (Entry<WebServiceApiName, WebServiceSessionManagerHolder> services : apiManagerMap.entrySet()) {
			serviceList.add(services.getValue().getHandle());
		}
		return serviceList;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public ServerRuntimeContextService getRuntimeService() {
		return runtimeService;
	}

	public void setRuntimeService(ServerRuntimeContextService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public List<WebServiceSessionManager> getWebServiceSessionManagers() {
		return webServiceSessionManagers;
	}

	public void setWebServiceSessionManagers(List<WebServiceSessionManager> webServiceSessionManagers) {
		this.webServiceSessionManagers = webServiceSessionManagers;
	}

	public PartitionControlServerService getPartitionServerService() {
		return partitionServerService;
	}

	public void setPartitionServerService(PartitionControlServerService partitionServerService) {
		this.partitionServerService = partitionServerService;
	}

	public int getTimeoutCheckIntervalSec() {
		return timeoutCheckIntervalSec;
	}

	public void setTimeoutCheckIntervalSec(int timeoutCheckIntervalSec) {
		this.timeoutCheckIntervalSec = timeoutCheckIntervalSec;
	}

	public int getSessionCreationTimeoutHours() {
		return sessionCreationTimeoutHours;
	}

	public void setSessionCreationTimeoutHours(int sessionCreationTimeoutHours) {
		this.sessionCreationTimeoutHours = sessionCreationTimeoutHours;
	}

	public int getSessionIdleTimeoutMinutes() {
		return sessionIdleTimeoutMinutes;
	}

	public void setSessionIdleTimeoutMinutes(int sessionIdleTimeoutMinutes) {
		this.sessionIdleTimeoutMinutes = sessionIdleTimeoutMinutes;
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

	public CacheInvalidationMessageListener getCacheInvalidationListener() {
		return cacheInvalidationListener;
	}

	public void setCacheInvalidationListener(CacheInvalidationMessageListener cacheInvalidationListener) {
		this.cacheInvalidationListener = cacheInvalidationListener;
	}

}
