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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.session.WebServiceSessionEndpoint;
import org.tdmx.server.ws.session.WebServiceApiName;

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

public class LocalControlServiceImpl implements ControlService, Manageable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static Logger log = LoggerFactory.getLogger(LocalControlServiceImpl.class);

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

	private Map<RpcClientChannel, LocalControlServiceClient> channelMap = new HashMap<>();
	private String segment = null;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public WebServiceSessionEndpoint associateApiSession(SessionHandle sessionData, PKIXCertificate clientCertificate) {
		for (LocalControlServiceClient client : channelMap.values()) {
			// TODO figure out which client to use
			return client.associateApiSession(sessionData, clientCertificate);
		}
		return null;
	}

	@Override
	public void start(String segment, List<WebServiceApiName> apis) {
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
					LocalControlServiceClient client = channelMap.get(clientChannel);
					if (client != null) {
						client.setRpcClient(clientChannel);
					}
				}

				@Override
				public void connectionOpened(RpcClientChannel clientChannel) {
					log.info("connectionOpened " + clientChannel);
					LocalControlServiceClient client = channelMap.get(clientChannel);
					if (client != null) {
						client.setRpcClient(clientChannel);
					}
				}

				@Override
				public void connectionLost(RpcClientChannel clientChannel) {
					log.info("connectionLost " + clientChannel);
					LocalControlServiceClient client = channelMap.get(clientChannel);
					if (client != null) {
						client.setRpcClient(null);
					}
				}

				@Override
				public void connectionChanged(RpcClientChannel clientChannel) {
					log.info("connectionChanged " + clientChannel);
					LocalControlServiceClient client = channelMap.get(clientChannel);
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

			LocalControlServiceClient local = new LocalControlServiceClient();
			local.setRpcClient(client);

			channelMap.put(client, local);
		} catch (Exception e) {
			throw new RuntimeException("Unable to do initial connect to PCS", e);
		}
	}

	@Override
	public void stop() {
		shutdownHandler.shutdown();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
	private RpcClient connect(String serverHostname, int serverPort) throws Exception {
		PeerInfo server = new PeerInfo(serverHostname, serverPort);

		return clientFactory.peerWith(server, bootstrap);
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

}
