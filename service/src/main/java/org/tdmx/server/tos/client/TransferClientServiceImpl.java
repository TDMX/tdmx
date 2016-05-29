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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.job.NamedThreadFactory;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.server.pcs.SessionControlService;
import org.tdmx.server.pcs.protobuf.PCSServer.FindApiSessionResponse;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.runtime.RpcAddressUtils;
import org.tdmx.server.session.SessionKeyUtil;
import org.tdmx.server.tos.client.TransferStatus.ErrorCode;
import org.tdmx.server.ws.session.WebServiceApiName;

import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * The TOS client connects to each used TOS server. The TOS server connection is re-used, not closed, but also not
 * reestablished if the connection breaks.
 * 
 * 
 * 
 * @author Peter
 *
 */
public class TransferClientServiceImpl implements TransferClientService, Manageable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static Logger log = LoggerFactory.getLogger(TransferClientServiceImpl.class);

	private static final String TOS_TCP_ADDRESS = "TOS_TCP_ADDRESS";

	/**
	 * The service used to lookup the tosTcpAddress and sessionId for any given sessionKey from the PCS.
	 */
	private SessionControlService controlService;

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

	private Segment segment = null;

	/**
	 * A Map of TOS client mapped by TOS RPC endpoint address.
	 */
	private Map<String, TransferObjectServiceClient> serverProxyMap = new ConcurrentHashMap<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public TransferStatus transferMDS(String tosTcpAddress, String sessionId, AccountZone az,
			ChannelDestination destination, Long stateId) {
		String sessionKey = SessionKeyUtil.createMDSSessionKey(az.getZoneApex(), destination);

		if (tosTcpAddress == null || sessionId == null) {
			FindApiSessionResponse pcsInfo = controlService.findApiSession(segment.getSegmentName(),
					WebServiceApiName.MDS, sessionKey);
			if (pcsInfo != null) {
				if (pcsInfo.hasSessionId()) {
					sessionId = pcsInfo.getSessionId();
				}
				if (pcsInfo.hasTosAddress()) {
					tosTcpAddress = pcsInfo.getTosAddress();
				}
			} else {
				return TransferStatus.failure(ErrorCode.PCS_FAILURE);
			}
		}
		if (tosTcpAddress == null || sessionId == null) {
			return TransferStatus.failure(ErrorCode.PCS_SESSION_NOT_FOUND);
		}
		TransferObjectServiceClient tosClient = getTransferClient(tosTcpAddress);
		if (tosClient == null) {
			return TransferStatus.failure(ErrorCode.TOS_CONNECTION_REFUSED);
		}
		return tosClient.transferMDS(segment.getSegmentName(), tosTcpAddress, sessionId, stateId);
	}

	@Override
	public void start(Segment segment, List<WebServiceApiName> apis) {
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
					String tosAddress = getTosTcpAddress(clientChannel);
					serverProxyMap.remove(tosAddress);
				}

				private void connection(RpcClientChannel clientChannel) {
					String tosAddress = getTosTcpAddress(clientChannel);

					serverProxyMap.put(tosAddress, new TransferObjectServiceClient(clientChannel));
				}

			};
			rpcEventNotifier.addEventListener(listener);
			clientFactory.registerConnectionEventListener(rpcEventNotifier);

			bootstrap = new Bootstrap();
			EventLoopGroup workers = new NioEventLoopGroup(ioThreads, new NamedThreadFactory("TOS-client-workers"));

			bootstrap.group(workers);
			bootstrap.handler(clientFactory);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.TCP_NODELAY, tcpNoDelay);
			bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis);
			bootstrap.option(ChannelOption.SO_SNDBUF, ioBufferSize);
			bootstrap.option(ChannelOption.SO_RCVBUF, ioBufferSize);

			shutdownHandler = new CleanShutdownHandler();
			shutdownHandler.addResource(workers);
			shutdownHandler.addResource(rpcExecutor);

		} catch (Exception e) {
			throw new RuntimeException("Unable to do setup the ROS client.", e);
		}
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

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private String getTosTcpAddress(RpcClientChannel clientChannel) {
		String tosAddress = (String) clientChannel.getAttribute(TOS_TCP_ADDRESS);
		if (tosAddress == null) {
			throw new IllegalStateException("No TOS endpoint address attribute on clientChannel " + clientChannel);
		}
		return tosAddress;
	}

	private TransferObjectServiceClient getTransferClient(String tosTcpAddress) {

		TransferObjectServiceClient client = serverProxyMap.get(tosTcpAddress);
		if (client == null) {
			synchronized (this) {
				client = serverProxyMap.get(tosTcpAddress);
				if (client == null) {
					Map<String, Object> attrs = new HashMap<>();
					attrs.put(TOS_TCP_ADDRESS, tosTcpAddress);

					PeerInfo tosServer = new PeerInfo(RpcAddressUtils.getRosHost(tosTcpAddress),
							RpcAddressUtils.getRosPort(tosTcpAddress));
					try {
						clientFactory.peerWith(tosServer, bootstrap, attrs);
						// the serverProxyMap must be set now
						return serverProxyMap.get(tosTcpAddress);
					} catch (IOException e) {
						log.warn("Unable to open TOS client connection to " + tosServer);
					}
				}
			}
		}
		return client;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public SessionControlService getControlService() {
		return controlService;
	}

	public void setControlService(SessionControlService controlService) {
		this.controlService = controlService;
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

}
