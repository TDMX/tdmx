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
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import org.eclipse.jetty.server.AsyncNCSARequestLog;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.ws.security.RequireClientCertificateFilter;

public class ServerContainer {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static Logger log = LoggerFactory.getLogger(ServerContainer.class);

	private static final String LOCAL_IP_ADDRESS = "127.0.0.1";
	private static final int MILLIS_IN_ONE_SECOND = 1000;

	private Filter userAuthenticationFilter;

	private int httpsPort;
	private String[] httpsCiphers;
	private String[] httpsProtocols;
	private String keyStoreFile;
	private String keyStorePassword;
	private int connectionIdleTimeoutSec;

	private int stopPort;
	private String stopCommand;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void runUntilStopped() {
		// Create a basic jetty server object without declaring the port. Since we are configuring connectors
		// directly we'll be setting ports on those connectors.
		Server server = new Server();

		// HTTP Configuration
		// HttpConfiguration is a collection of configuration information appropriate for http and https. The default
		// scheme for http is <code>http</code> of course, as the default for secured http is <code>https</code> but
		// we show setting the scheme to show it can be done. The port for secured communication is also set here.
		HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSecureScheme("https");
		http_config.setSecurePort(getHttpsPort());
		http_config.setOutputBufferSize(32768);
		// TODO select right key

		// SSL Context Factory for HTTPS and SPDY
		// SSL requires a certificate so we configure a factory for ssl contents with information pointing to what
		// keystore the ssl connection needs to know about. Much more configuration is available the ssl context,
		// including things like choosing the particular certificate out of a keystore to be used.
		SslContextFactory sslContextFactory = new SslContextFactory();
		// we trust all client certs, with security in servlet "filters"
		sslContextFactory.setTrustAll(true);
		// sslContextFactory.setCertAlias("server");
		sslContextFactory.setRenegotiationAllowed(false);
		// TODO change to NEED
		sslContextFactory.setWantClientAuth(true);

		sslContextFactory.setIncludeCipherSuites(getHttpsCiphers());
		sslContextFactory.setIncludeProtocols(getHttpsProtocols());
		sslContextFactory.setKeyStoreType("jks");
		sslContextFactory.setKeyStorePath(getKeyStoreFile());
		sslContextFactory.setKeyStorePassword(getKeyStorePassword());
		// TODO check if needed
		// sslContextFactory.setKeyManagerPassword("changeme");

		// HTTPS Configuration
		// A new HttpConfiguration object is needed for the next connector and you can pass the old one as an
		// argument to effectively clone the contents. On this HttpConfiguration object we add a
		// SecureRequestCustomizer which is how a new connector is able to resolve the https connection before
		// handing control over to the Jetty Server.
		HttpConfiguration https_config = new HttpConfiguration(http_config);
		https_config.addCustomizer(new SecureRequestCustomizer());

		// HTTPS connector
		// We create a second ServerConnector, passing in the http configuration we just made along with the
		// previously created ssl context factory. Next we set the port and a longer idle timeout.
		ServerConnector https = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"),
				new HttpConnectionFactory(https_config));
		https.setPort(getHttpsPort());
		https.setIdleTimeout(getConnectionIdleTimeoutSec() * MILLIS_IN_ONE_SECOND);

		// Here you see the server having multiple connectors registered with it, now requests can flow into the server
		// from both http and https urls to their respective ports and be processed accordingly by jetty. A simple
		// handler is also registered with the server so the example has something to pass requests off to.

		// Set the connectors
		server.setConnectors(new Connector[] { https });

		// The following section adds some handlers, deployers and webapp providers.
		// See: http://www.eclipse.org/jetty/documentation/current/advanced-embedding.html for details.

		// Setup handlers
		HandlerCollection handlers = new HandlerCollection();
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		RequestLogHandler requestLogHandler = new RequestLogHandler();

		handlers.setHandlers(new Handler[] { contexts, requestLogHandler });
		// , new DefaultHandler()

		StatisticsHandler stats = new StatisticsHandler();
		stats.setHandler(handlers);

		server.setHandler(stats);

		NCSARequestLog requestLog = new AsyncNCSARequestLog();
		requestLog.setFilename("jetty-yyyy_mm_dd.log");
		requestLog.setExtended(true);
		requestLog.setRetainDays(7);
		requestLogHandler.setRequestLog(requestLog);

		ServletContextHandler context = new ServletContextHandler();
		contexts.addHandler(context);
		context.setContextPath("/api");
		// Setup Spring context
		context.addEventListener(new org.springframework.web.context.ContextLoaderListener());
		context.setInitParameter("parentContextKey", "applicationContext");
		context.setInitParameter("locatorFactorySelector", "classpath*:beanRefContext.xml");
		context.setInitParameter("contextConfigLocation", "classpath:/empty-context.xml");

		// Add filters

		FilterHolder cf = new FilterHolder();
		cf.setFilter(new RequireClientCertificateFilter());
		context.addFilter(cf, "/*", EnumSet.allOf(DispatcherType.class));

		FilterHolder fh = new FilterHolder();
		fh.setFilter(getUserAuthenticationFilter());
		context.addFilter(fh, "/v1.0/sp/mds/*", EnumSet.of(DispatcherType.REQUEST));

		// Add servlets
		ServletHolder sh = new ServletHolder(org.apache.cxf.transport.servlet.CXFServlet.class);
		sh.setInitOrder(1);
		context.addServlet(sh, "/*");

		try {
			// Start the server
			server.start();

			Thread monitor = new MonitorThread(server, getStopPort(), getStopCommand());
			monitor.start();

			try {
				// Wait for the server to be stopped by the MonitorThread.
				server.join();
			} catch (InterruptedException ie) {
				log.warn("Container running thread interrupted.", ie);
			}

		} catch (Exception e) {
			log.error("Starting failed.", e);
		}
		// Exiting here will terminate the application.
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private static class MonitorThread extends Thread {
		private static Logger log = LoggerFactory.getLogger(MonitorThread.class);

		private final ServerSocket socket;
		private final Server server;
		private final String stopCommand;

		public MonitorThread(Server s, int port, String stopCommand) throws IOException {
			setDaemon(true);
			setName("ServerContainer#StopMonitor");
			this.stopCommand = stopCommand;
			this.server = s;
			this.socket = new ServerSocket(port, 1, InetAddress.getByName(LOCAL_IP_ADDRESS));
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
					BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
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

			try {
				server.stop();
			} catch (Exception e) {
				log.error("Unable to stop container.", e);
			}

			try {
				socket.close();
			} catch (IOException e) {
				log.warn("Unable to close stop server socket.", e);
			}
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public int getHttpsPort() {
		return httpsPort;
	}

	public void setHttpsPort(int httpsPort) {
		this.httpsPort = httpsPort;
	}

	public String[] getHttpsCiphers() {
		return httpsCiphers;
	}

	public void setHttpsCiphers(String[] httpsCiphers) {
		this.httpsCiphers = httpsCiphers;
	}

	public String[] getHttpsProtocols() {
		return httpsProtocols;
	}

	public void setHttpsProtocols(String[] httpsProtocols) {
		this.httpsProtocols = httpsProtocols;
	}

	public String getKeyStoreFile() {
		return keyStoreFile;
	}

	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public int getConnectionIdleTimeoutSec() {
		return connectionIdleTimeoutSec;
	}

	public void setConnectionIdleTimeoutSec(int connectionIdleTimeoutSec) {
		this.connectionIdleTimeoutSec = connectionIdleTimeoutSec;
	}

	public int getStopPort() {
		return stopPort;
	}

	public void setStopPort(int stopPort) {
		this.stopPort = stopPort;
	}

	public String getStopCommand() {
		return stopCommand;
	}

	public void setStopCommand(String stopCommand) {
		this.stopCommand = stopCommand;
	}

	public Filter getUserAuthenticationFilter() {
		return userAuthenticationFilter;
	}

	public void setUserAuthenticationFilter(Filter userAuthenticationFilter) {
		this.userAuthenticationFilter = userAuthenticationFilter;
	}

}
