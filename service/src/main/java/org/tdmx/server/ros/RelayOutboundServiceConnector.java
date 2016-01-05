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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.ROSServer.RelayOutboundServiceProxy;
import org.tdmx.server.pcs.protobuf.ROSServer.RelayRequest;
import org.tdmx.server.pcs.protobuf.ROSServer.RelayResponse;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.ws.session.WebServiceApiName;

import com.google.protobuf.BlockingService;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
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
 * Handles inbound RPC calls from WS clients to relay outbound.
 * 
 * @author Peter
 *
 */
public class RelayOutboundServiceConnector implements Manageable, RelayOutboundServiceProxy.BlockingInterface {

	// TODO LATER: use SSL context for protobuf communications

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayOutboundServiceConnector.class);

	private RelayOutboundService relayOutboundService;

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

			@Override
			public void connectionReestablished(RpcClientChannel clientChannel) {
				log.info("connectionReestablished " + clientChannel);
			}

			@Override
			public void connectionOpened(RpcClientChannel clientChannel) {
				log.info("connectionOpened " + clientChannel);
			}

			@Override
			public void connectionLost(RpcClientChannel clientChannel) {
				log.info("connectionLost " + clientChannel);
			}

			@Override
			public void connectionChanged(RpcClientChannel clientChannel) {
				log.info("connectionChanged " + clientChannel);
			}
		};
		rpcEventNotifier.setEventListener(listener);
		serverFactory.registerConnectionEventListener(rpcEventNotifier);

		// we implement a RPC service for clients to call directly.
		BlockingService controlServiceProxy = RelayOutboundServiceProxy.newReflectiveBlockingService(this);
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
	public RelayResponse relay(RpcController controller, RelayRequest request) throws ServiceException {
		String channelKey = request.getChannelKey();

		Map<AttributeId, Long> attrs = mapAttributes(request.getAttributeList());
		switch (request.getRelayType()) {
		case Authorization:
			relayOutboundService.relayChannelAuthorization(channelKey, attrs.get(AttributeId.ChannelId));
			break;
		case DeliveryReply:
			// TODO #95 DR
			break;
		case DestinationSession:
			relayOutboundService.relayChannelDestinationSession(channelKey, attrs.get(AttributeId.ChannelId));
			break;
		case FlowControl:
			relayOutboundService.relayChannelFlowControl(channelKey, attrs.get(AttributeId.ChannelId));
			break;
		case Message:
			relayOutboundService.relayChannelMessage(channelKey, attrs.get(AttributeId.MessageId));
			break;
		default:
			break;
		}

		return null;
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

	private Map<AttributeId, Long> mapAttributes(List<org.tdmx.server.pcs.protobuf.Common.AttributeValue> attrs) {
		if (attrs == null) {
			return null;
		}
		Map<AttributeId, Long> attributes = new HashMap<>();
		for (org.tdmx.server.pcs.protobuf.Common.AttributeValue attr : attrs) {
			attributes.put(attr.getName(), attr.getValue());
		}
		return attributes;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public RelayOutboundService getRelayOutboundService() {
		return relayOutboundService;
	}

	public void setRelayOutboundService(RelayOutboundService relayOutboundService) {
		this.relayOutboundService = relayOutboundService;
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
