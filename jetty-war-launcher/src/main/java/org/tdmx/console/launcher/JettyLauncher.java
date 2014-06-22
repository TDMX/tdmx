/*
 * TDMX - Trusted Domain Messaging eXchange enables enterprise B2B messaging between separate corporations via
 * interoperable cloud service providers.
 * 
 * @see http://tdmx.org
 * 
 * Copyright (C) 2014 Peter Klauser
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
package org.tdmx.console.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 
 */
public class JettyLauncher {

	private static String contextPath = "/";
	private static Server server;

	public static void start(String[] args, URL warUrl) throws Exception {

		int httpPort = 8080;
		int httpsPort = -1;

		String keyStorePath = null;
		String keyStorePassword = null;

		String cmd = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("--httpPort=")) {
				String portStr = args[i].substring("--httpPort=".length());
				httpPort = Integer.parseInt(portStr);
			}

			if (args[i].startsWith("--httpsPort=")) {
				String portStr = args[i].substring("--httpsPort=".length());
				httpsPort = Integer.parseInt(portStr);
			}

			if (args[i].startsWith("--httpsKeyStore=")) {
				keyStorePath = args[i].substring("--httpsKeyStore=".length());
			}

			if (args[i].startsWith("--httpsKeyStorePassword=")) {
				keyStorePassword = args[i].substring("--httpsKeyStorePassword=".length());
			}

			if (args[i].startsWith("--prefix=")) {
				String prefix = args[i].substring("--prefix=".length());
				if (prefix.startsWith("/")) {
					contextPath = prefix;
				} else {
					contextPath = "/" + prefix;
				}
			}
			start(httpPort, httpsPort, keyStorePath, keyStorePassword, warUrl);
		}
	}

	public static void start(int httpPort, int httpsPort, String keyStorePath, String keyStorePassword, URL warUrl)
			throws Exception {
		// start a server
		String baseFolder = System.getProperty("org.tdmx.console.home");
		if (baseFolder == null) {
			baseFolder = System.getProperty("user.home");
		}
		File tempDir = new File(new File(baseFolder), "war");
		tempDir.mkdirs();

		// Create a basic jetty server object without declaring the port. Since we are configuring connectors
		// directly we'll be setting ports on those connectors.
		server = new Server();

		// HTTP Configuration
		// HttpConfiguration is a collection of configuration information appropriate for http and https. The default
		// scheme for http is <code>http</code> of course, as the default for secured http is <code>https</code> but
		// we show setting the scheme to show it can be done. The port for secured communication is also set here.
		HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSecureScheme("https");
		http_config.setSecurePort(httpsPort);
		http_config.setOutputBufferSize(32768);

		// HTTP connector
		// The first server connector we create is the one for http, passing in the http configuration we configured
		// above so it can get things like the output buffer size, etc. We also set the port (8080) and configure an
		// idle timeout.
		ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
		http.setPort(httpPort);
		http.setIdleTimeout(30000);

		// SSL Context Factory for HTTPS and SPDY
		// SSL requires a certificate so we configure a factory for ssl contents with information pointing to what
		// keystore the ssl connection needs to know about. Much more configuration is available the ssl context,
		// including things like choosing the particular certificate out of a keystore to be used.
		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(keyStorePath);
		sslContextFactory.setKeyStorePassword(keyStorePassword);
		// sslContextFactory.setKeyManagerPassword("");
		// TODO sslProtocol, ciphers

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
		// TODO remove HTTP
		server.setConnectors(new Connector[] { http, https });

		WebAppContext context = new WebAppContext();

		context.setTempDirectory(tempDir);

		context.setContextPath(contextPath);
		context.setDescriptor(warUrl.toExternalForm() + "/WEB-INF/web.xml");
		context.setServer(server);
		context.setWar(warUrl.toExternalForm());

		server.setHandler(context);
		server.setStopAtShutdown(true);

		Thread monitor = new MonitorThread();
		monitor.start();

		server.start();
		server.join();

	}

	private static class MonitorThread extends Thread {

		private ServerSocket socket;

		public MonitorThread() {
			setDaemon(true);
			setName("StopMonitor");
			try {
				socket = new ServerSocket(8079, 1, InetAddress.getByName("127.0.0.1"));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void run() {
			System.out.println("*** running jetty 'stop' thread");
			Socket accept;
			try {
				accept = socket.accept();
				BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
				reader.readLine();
				System.out.println("*** stopping jetty embedded server");
				server.stop();
				accept.close();
				socket.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
