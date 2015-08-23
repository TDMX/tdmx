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
package org.tdmx.server.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.util.StringUtils;
import org.tdmx.server.ws.session.WebServiceApiName;

/**
 * ServerLauncher
 * 
 * usage:
 * 
 * --server={WS|RX|SCS|ROS|JOB}* defines which servers to start
 * 
 * --api={MOS,MDS,MRS,ZAS} when WS is the server, this defines which APIs are started
 * 
 * --segment=segmentName
 * 
 * --stopPort=8005
 * 
 * --stopCmd=STOP
 * 
 * --stopAddress=X.X.X.X
 * 
 * Instantiates a singleton SpringApplicationContext in classpath*:beanRefContext.xml.
 * 
 * Starts each ServerContainer bean named "<serverName>".Container until stopped.
 * 
 * 
 * @author Peter
 * 
 */
public class ServerLauncher {

	private static final Logger log = LoggerFactory.getLogger(ServerContainer.class);

	private static final String SERVER_ARG_PREFIX = "--server=";
	private static final String API_ARG_PREFIX = "--api=";
	private static final String SEGMENT_ARG_PREFIX = "--segment=";
	private static final String STOP_PORT_ARG_PREFIX = "--stopPort=";
	private static final int DEFAULT_STOP_PORT = 8005;
	private static final String STOP_CMD_ARG_PREFIX = "--stopCmd=";
	private static final String DEFAULT_STOP_CMD = "STOP";
	private static final String STOP_ADDRESS_ARG_PREFIX = "--stopAddress=";

	private static ApplicationContext context;

	private ServerLauncher() {
	}

	public static void main(String[] args) throws UnknownHostException {
		List<ServerName> servers = new ArrayList<>();
		List<WebServiceApiName> apis = new ArrayList<>();
		String segment = null;

		String stopCmd = DEFAULT_STOP_CMD;
		String stopIpAddress = InetAddress.getLocalHost().getHostAddress();
		int stopPort = DEFAULT_STOP_PORT;

		for (String arg : args != null ? args : new String[0]) {
			if (StringUtils.hasText(arg) && arg.startsWith(SERVER_ARG_PREFIX)) {
				for (ServerName srvName : ServerName.values()) {
					if (arg.toUpperCase().indexOf(srvName.toString()) != -1) {
						servers.add(srvName);
					}
				}
			}
			if (StringUtils.hasText(arg) && arg.startsWith(API_ARG_PREFIX)) {
				for (WebServiceApiName apiName : WebServiceApiName.values()) {
					if (arg.toUpperCase().indexOf(apiName.toString()) != -1) {
						apis.add(apiName);
					}
				}
			}
			if (StringUtils.hasText(arg) && arg.startsWith(SEGMENT_ARG_PREFIX)) {
				segment = arg.substring(SEGMENT_ARG_PREFIX.length());
			}
			if (StringUtils.hasText(arg) && arg.startsWith(STOP_PORT_ARG_PREFIX)) {
				stopPort = Integer.parseInt(arg.substring(STOP_PORT_ARG_PREFIX.length()));
			}
			if (StringUtils.hasText(arg) && arg.startsWith(STOP_CMD_ARG_PREFIX)) {
				stopCmd = arg.substring(STOP_CMD_ARG_PREFIX.length());
			}
			if (StringUtils.hasText(arg) && arg.startsWith(STOP_ADDRESS_ARG_PREFIX)) {
				stopIpAddress = arg.substring(STOP_ADDRESS_ARG_PREFIX.length());
			}
		}

		if (servers.size() == 0) {
			log.error("Missing argument " + SERVER_ARG_PREFIX);
			System.exit(-1);
		}

		String javaVersion = System.getProperty("java.version");

		StringTokenizer tokens = new StringTokenizer(javaVersion, ".-_");

		int majorVersion = Integer.parseInt(tokens.nextToken());
		int minorVersion = Integer.parseInt(tokens.nextToken());

		if (majorVersion < 2 && minorVersion < 7) {
			log.error("TDMX-ServerContainer requires Java 7 or later.");
			log.error("Your java version is " + javaVersion);
			log.error("Java Home:  " + System.getProperty("java.home"));
			System.exit(-1);
		}

		if (!StringUtils.hasText(segment)) {
			log.info("Non segmented operation.");
			segment = null;
		} else {
			log.info("Segment: " + segment);
		}

		BeanFactoryLocator beanFactoryLocator = ContextSingletonBeanFactoryLocator.getInstance();
		BeanFactoryReference beanFactoryReference = beanFactoryLocator.useBeanFactory("applicationContext");
		context = (ApplicationContext) beanFactoryReference.getFactory();

		try {
			MonitorThread monitor = new MonitorThread(servers, stopPort, stopCmd, stopIpAddress);
			monitor.start();

			startup(servers, segment, apis);

		} catch (Exception e) {
			log.error("Unable to start " + servers, e);
			shutdown(servers);
		}
	}

	private static void shutdown(List<ServerName> servers) {
		for (ServerName srvName : servers) {
			ServerContainer sc = (ServerContainer) context.getBean(srvName + ".Server");
			try {
				sc.stop();
			} catch (Exception e) {
				log.warn("Unable to stop " + srvName, e);
			}
		}
		for (ServerName srvName : servers) {
			ServerContainer sc = (ServerContainer) context.getBean(srvName + ".Server");
			try {
				sc.awaitTermination();
			} catch (Exception e) {
				log.warn("Unable to awaitTermination " + srvName, e);
			}
		}
	}

	private static void startup(List<ServerName> servers, String segment, List<WebServiceApiName> apis)
			throws Exception {
		// Construct the SpringApplication
		// Log config info about the Containers
		for (ServerName srvName : servers) {
			ServerRuntimeContextService si = (ServerRuntimeContextService) context.getBean(srvName + ".Configuration");
			if (si != null) {
				log.info("JVM supportedCipherSuites: "
						+ StringUtils.arrayToCommaDelimitedString(si.getSupportedCipherSuites()));
				log.info("JVM supportedProtocols: "
						+ StringUtils.arrayToCommaDelimitedString(si.getSupportedProtocols()));
				log.info("default TrustManagerFactoryAlgorithm: " + si.getDefaultTrustManagerFactoryAlgorithm());
			}
		}
		// Start the Containers
		for (ServerName srvName : servers) {
			ServerContainer sc = (ServerContainer) context.getBean(srvName + ".Server");
			sc.start(segment, apis);
		}

	}

	private static class MonitorThread extends Thread {
		private final Logger log = LoggerFactory.getLogger(MonitorThread.class);

		private final ServerSocket socket;
		private final List<ServerName> servers;
		private final String stopCommand;

		public MonitorThread(List<ServerName> servers, int port, String stopCommand, String localAddr)
				throws IOException {
			setDaemon(true);
			setName("ServerLauncher#StopMonitor");
			this.stopCommand = stopCommand;
			this.servers = servers;
			this.socket = new ServerSocket(port, 1, InetAddress.getByName(localAddr));
		}

		@Override
		public void run() {
			boolean stopServer = false;
			while (!stopServer) {
				Socket accept = null;
				try {
					log.info("accepting stop connections on " + socket.getLocalPort());
					accept = socket.accept();
					log.info("accepted a stop connection.");
					BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream(),
							Charset.forName("UTF-8")));
					// read just a single line
					String line = reader.readLine();
					// IMPROVEMENT: read only enough bytes to cover the content of the stopCommand + \n
					if (line != null && line.indexOf(stopCommand) != -1) {
						stopServer = true;
						log.info("recieved stop command. Stopping container ...");
					}
				} catch (IOException e) {
					log.warn("Problem reading from stop client.", e);
				} finally {
					if (accept != null) {
						try {
							accept.close();
						} catch (IOException e) {
							log.warn("Unable to close stop client connection.", e);
						}
					}
				}

				// safety that we don't loop too quickly.
				if (!stopServer) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ie) {
						// ignored
					}
				}
			}

			// stop all servers
			shutdown(servers);

			try {
				socket.close();
			} catch (IOException e) {
				log.warn("Unable to close stop server socket.", e);
			}
		}
	}

}
