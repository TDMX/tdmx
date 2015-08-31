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
import java.util.List;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.server.pcs.protobuf.PCSServer.BlockingPingService;
import org.tdmx.server.pcs.protobuf.PCSServer.Ping;
import org.tdmx.server.pcs.protobuf.PCSServer.Pong;
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
import com.googlecode.protobuf.pro.duplex.execute.ServerRpcController;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.NullLogger;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutExecutor;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutExecutor;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 
 * @author Peter
 *
 */
public class RemoteControlServiceConnector implements Manageable, BlockingPingService.BlockingInterface {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RemoteControlServiceConnector.class);

	/**
	 * The ControlServiceListener delegate.
	 */
	private ControlServiceListener controlListener;

	/**
	 * The ControlService delegate.
	 */
	private ControlService controlService;

	/**
	 * The interface address for multi-homed hosts. Leave empty if not multihomed.
	 */
	private String serverAddress;
	private int localPort;
	private int channelbufferSize = 1048576;
	private boolean tcpNoDelay = true;
	private long shutdownTimeoutMs;

	/**
	 * The segment handled by this PartitionControlService.
	 */
	private String segment;
	private CleanShutdownHandler shutdownHandler;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void start(String segment, List<WebServiceApiName> apis) {
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

		RpcServerCallExecutor executor = new ThreadPoolCallExecutor(3, 200);

		DuplexTcpServerPipelineFactory serverFactory = new DuplexTcpServerPipelineFactory(serverInfo);
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

		RpcTimeoutExecutor timeoutExecutor = new TimeoutExecutor(1, 5);
		RpcTimeoutChecker timeoutChecker = new TimeoutChecker();
		timeoutChecker.setTimeoutExecutor(timeoutExecutor);
		timeoutChecker.startChecking(serverFactory.getRpcClientRegistry());

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

		// we give the server a blocking and non blocking (pong capable) Ping Service
		BlockingService bPingService = BlockingPingService.newReflectiveBlockingService(this);
		serverFactory.getRpcServiceRegistry().registerService(bPingService);

		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap();
		NioEventLoopGroup boss = new NioEventLoopGroup(2,
				new RenamingThreadFactoryProxy("boss", Executors.defaultThreadFactory()));
		NioEventLoopGroup workers = new NioEventLoopGroup(16,
				new RenamingThreadFactoryProxy("worker", Executors.defaultThreadFactory()));
		bootstrap.group(boss, workers);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.option(ChannelOption.SO_SNDBUF, channelbufferSize);
		bootstrap.option(ChannelOption.SO_RCVBUF, channelbufferSize);
		bootstrap.childOption(ChannelOption.SO_RCVBUF, channelbufferSize);
		bootstrap.childOption(ChannelOption.SO_SNDBUF, channelbufferSize);
		bootstrap.option(ChannelOption.TCP_NODELAY, tcpNoDelay);
		bootstrap.childHandler(serverFactory);
		bootstrap.localAddress(serverInfo.getPort());

		// Bind and start to accept incoming connections.
		shutdownHandler = new CleanShutdownHandler();
		shutdownHandler.addResource(boss);
		shutdownHandler.addResource(workers);
		shutdownHandler.addResource(executor);
		shutdownHandler.addResource(timeoutChecker);
		shutdownHandler.addResource(timeoutExecutor);

		bootstrap.bind();

		log.info("Serving " + serverInfo);
	}

	@Override
	public Pong ping(RpcController controller, Ping request) throws ServiceException {
		Pong.Builder responseBuilder = Pong.newBuilder().setSequenceNo(request.getSequenceNo())
				.setPongData(request.getPingPayload());

		RpcClientChannel channel = ServerRpcController.getRpcChannel(controller);

		Pong response = responseBuilder.build();

		return response;
	}

	@Override
	public void stop() {
		if (shutdownHandler != null) {
			shutdownHandler.shutdownAwaiting(shutdownTimeoutMs);
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

	public int getChannelbufferSize() {
		return channelbufferSize;
	}

	public void setChannelbufferSize(int channelbufferSize) {
		this.channelbufferSize = channelbufferSize;
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

	public String getSegment() {
		return segment;
	}

}
