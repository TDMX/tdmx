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
package org.tdmx.server.ros.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.FlowQuota;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.server.pcs.RelayControlService;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.ros.client.RelayStatus.ErrorCode;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.runtime.RpcAddressUtils;
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
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * The ROS client connects to all PCS servers and keeps these RPC connections alive.
 * 
 * 
 * @author Peter
 *
 */
public class RelayClientServiceImpl implements RelayClientService, Manageable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static Logger log = LoggerFactory.getLogger(RelayClientServiceImpl.class);

	private static final String ROS_TCP_ADDRESS = "ROS_TCP_ADDRESS";

	/**
	 * The service used to lookup the rosTcpAddress for any given channel from the PCS.
	 */
	private RelayControlService relayControlService;

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
	 * A Map of ROS client mapped by ROS RPC endpoint address.
	 */
	private Map<String, RelayClientService> serverProxyMap = new ConcurrentHashMap<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public RelayStatus relayChannelAuthorization(String rosTcpAddress, AccountZone az, Zone zone, Domain domain,
			Channel channel, ChannelAuthorization ca) {
		String channelKey = channel.getChannelName().getChannelKey(domain.getDomainName());
		if (rosTcpAddress == null) {
			rosTcpAddress = getRelayAddress(channelKey, az, zone, domain, channel, ca, null, null);
		}
		if (rosTcpAddress == null) {
			return RelayStatus.failure(channelKey, ErrorCode.PCS_FAILURE);
		}
		RelayClientService rosClient = getRelayClient(rosTcpAddress);
		if (rosClient == null) {
			return RelayStatus.failure(channelKey, ErrorCode.ROS_CONNECTION_REFUSED);
		}

		return rosClient.relayChannelAuthorization(rosTcpAddress, az, zone, domain, channel, ca);
	}

	@Override
	public RelayStatus relayChannelDestinationSession(String rosTcpAddress, AccountZone az, Zone zone, Domain domain,
			Channel channel) {
		String channelKey = channel.getChannelName().getChannelKey(domain.getDomainName());

		if (rosTcpAddress == null) {
			rosTcpAddress = getRelayAddress(channelKey, az, zone, domain, channel, null, null, null);
		}
		if (rosTcpAddress == null) {
			return RelayStatus.failure(channelKey, ErrorCode.PCS_FAILURE);
		}
		RelayClientService rosClient = getRelayClient(rosTcpAddress);
		if (rosClient == null) {
			return RelayStatus.failure(channelKey, ErrorCode.ROS_CONNECTION_REFUSED);
		}
		return rosClient.relayChannelDestinationSession(rosTcpAddress, az, zone, domain, channel);
	}

	@Override
	public RelayStatus relayChannelFlowControl(String rosTcpAddress, AccountZone az, Zone zone, Domain domain,
			Channel channel, FlowQuota quota) {
		String channelKey = channel.getChannelName().getChannelKey(domain.getDomainName());

		if (rosTcpAddress == null) {
			rosTcpAddress = getRelayAddress(channelKey, az, zone, domain, channel, null, quota, null);
		}
		if (rosTcpAddress == null) {
			return RelayStatus.failure(channelKey, ErrorCode.PCS_FAILURE);
		}
		RelayClientService rosClient = getRelayClient(rosTcpAddress);
		if (rosClient == null) {
			return RelayStatus.failure(channelKey, ErrorCode.ROS_CONNECTION_REFUSED);
		}
		return rosClient.relayChannelFlowControl(rosTcpAddress, az, zone, domain, channel, quota);
	}

	@Override
	public RelayStatus relayChannelMessage(String rosTcpAddress, AccountZone az, Zone zone, Domain domain,
			Channel channel, ChannelMessage msg) {
		String channelKey = channel.getChannelName().getChannelKey(domain.getDomainName());

		if (rosTcpAddress == null) {
			rosTcpAddress = getRelayAddress(channelKey, az, zone, domain, channel, null, null, msg);
		}
		if (rosTcpAddress == null) {
			return RelayStatus.failure(channelKey, ErrorCode.PCS_FAILURE);
		}
		RelayClientService rosClient = getRelayClient(rosTcpAddress);
		if (rosClient == null) {
			return RelayStatus.failure(channelKey, ErrorCode.ROS_CONNECTION_REFUSED);
		}
		return rosClient.relayChannelMessage(rosTcpAddress, az, zone, domain, channel, msg);
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
					String rosAddress = getRosTcpAddress(clientChannel);
					serverProxyMap.remove(rosAddress);
				}

				private void connection(RpcClientChannel clientChannel) {
					String rosAddress = getRosTcpAddress(clientChannel);

					serverProxyMap.put(rosAddress, new RelayOutboundServiceClient(clientChannel));
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

	private String getRosTcpAddress(RpcClientChannel clientChannel) {
		String rosAddress = (String) clientChannel.getAttribute(ROS_TCP_ADDRESS);
		if (rosAddress == null) {
			throw new IllegalStateException("No ROS endpoint address attribute on clientChannel " + clientChannel);
		}
		return rosAddress;
	}

	private String getRelayAddress(String channelKey, AccountZone az, Zone zone, Domain domain, Channel channel,
			ChannelAuthorization ca, FlowQuota flow, ChannelMessage msg) {

		Map<AttributeId, Long> attributes = new HashMap<>();

		attributes.put(AttributeId.AccountZoneId, az.getId());
		attributes.put(AttributeId.ZoneId, zone.getId());
		attributes.put(AttributeId.DomainId, domain.getId());
		attributes.put(AttributeId.ChannelId, channel.getId());
		if (ca != null) {
			attributes.put(AttributeId.AuthorizationId, ca.getId());
		}
		if (flow != null) {
			attributes.put(AttributeId.FlowQuotaId, flow.getId());
		}
		if (msg != null) {
			attributes.put(AttributeId.MessageId, flow.getId());
		}

		return relayControlService.assignRelayServer(channelKey, segment.getSegmentName(), attributes);
	}

	private RelayClientService getRelayClient(String rosTcpAddress) {

		RelayClientService client = serverProxyMap.get(rosTcpAddress);
		if (client == null) {
			synchronized (this) {
				client = serverProxyMap.get(rosTcpAddress);
				if (client == null) {
					Map<String, Object> attrs = new HashMap<>();
					attrs.put(ROS_TCP_ADDRESS, rosTcpAddress);

					PeerInfo rosServer = new PeerInfo(RpcAddressUtils.getRosHost(rosTcpAddress),
							RpcAddressUtils.getRosPort(rosTcpAddress));
					try {
						clientFactory.peerWith(rosServer, bootstrap, attrs);
						// the serverProxyMap must be set now
						return serverProxyMap.get(rosTcpAddress);
					} catch (IOException e) {
						log.warn("Unable to open ROS client connection to " + rosServer);
					}
				}
			}
		}
		return client;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

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

	public RelayControlService getRelayControlService() {
		return relayControlService;
	}

	public void setRelayControlService(RelayControlService relayControlService) {
		this.relayControlService = relayControlService;
	}

}
