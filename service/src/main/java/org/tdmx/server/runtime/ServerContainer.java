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
import org.springframework.util.StringUtils;
import org.tdmx.server.ws.security.RequireClientCertificateFilter;

public class ServerContainer {

	private Server server;

	private Filter userAuthenticationFilter;

	public void startJetty() throws Exception {
		// Create a basic jetty server object without declaring the port. Since we are configuring connectors
		// directly we'll be setting ports on those connectors.
		server = new Server();

		// HTTP Configuration
		// HttpConfiguration is a collection of configuration information appropriate for http and https. The default
		// scheme for http is <code>http</code> of course, as the default for secured http is <code>https</code> but
		// we show setting the scheme to show it can be done. The port for secured communication is also set here.
		HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSecureScheme("https");
		http_config.setSecurePort(8443);
		http_config.setOutputBufferSize(32768);
		// TODO select right key

		// HTTP connector
		// The first server connector we create is the one for http, passing in the http configuration we configured
		// above so it can get things like the output buffer size, etc. We also set the port (8080) and configure an
		// idle timeout.
		ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
		http.setPort(8442);
		http.setIdleTimeout(30000);

		// SSL Context Factory for HTTPS and SPDY
		// SSL requires a certificate so we configure a factory for ssl contents with information pointing to what
		// keystore the ssl connection needs to know about. Much more configuration is available the ssl context,
		// including things like choosing the particular certificate out of a keystore to be used.
		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setTrustAll(true); // we trust all client certs, with security in servlet "filters"
		// sslContextFactory.setCertAlias("server");
		sslContextFactory.setRenegotiationAllowed(false);
		sslContextFactory.setWantClientAuth(true);

		String cipherList = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,TLS_DHE_RSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA,TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA,TLS_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_128_GCM_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_3DES_EDE_CBC_SHA";

		sslContextFactory.setIncludeCipherSuites(StringUtils.tokenizeToStringArray(cipherList, ","));
		sslContextFactory.setIncludeProtocols("SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"); //
		sslContextFactory.setKeyStorePath("server.keystore");
		sslContextFactory.setKeyStorePassword("changeme");
		sslContextFactory.setKeyManagerPassword("changeme");

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
		https.setPort(8443);
		https.setIdleTimeout(500000);

		// Here you see the server having multiple connectors registered with it, now requests can flow into the server
		// from both http and https urls to their respective ports and be processed accordingly by jetty. A simple
		// handler is also registered with the server so the example has something to pass requests off to.

		// Set the connectors
		server.setConnectors(new Connector[] { http, https });

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
		requestLogHandler.setRequestLog(requestLog);

		ServletContextHandler context = new ServletContextHandler();
		contexts.addHandler(context); // link to the overall context
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

		// Start the server
		server.start();

		Thread monitor = new MonitorThread(server, 8079);
		monitor.start();

		server.join();

	}

	private static class MonitorThread extends Thread {

		private ServerSocket socket;
		private Server server;

		public MonitorThread(Server s, int port) {
			setDaemon(true);
			setName("StopMonitor");
			try {
				socket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			this.server = s;
		}

		@Override
		public void run() {
			System.out.println("*** running jetty 'stop' thread"); // TODO
			Socket accept;
			try {
				accept = socket.accept();
				BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
				String line = reader.readLine();

				if (line != null && line.indexOf("STOP") != -1) {
					System.out.println("*** stopping jetty embedded server");
					server.stop();
					accept.close();
					socket.close();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public Filter getUserAuthenticationFilter() {
		return userAuthenticationFilter;
	}

	public void setUserAuthenticationFilter(Filter userAuthenticationFilter) {
		this.userAuthenticationFilter = userAuthenticationFilter;
	}

}
