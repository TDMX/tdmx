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
package org.tdmx.client.cli.cmd.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.apache.cxf.transport.servlet.CXFServlet;
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
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.tdmx.client.cli.cmd.AbstractCliCommand;
import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.ServerIpCredentialSpecifier;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.server.ws.security.HSTSHandler;
import org.tdmx.server.ws.security.NotFoundHandler;

@Cli(name = "ui:start", description = "starts the client administration UI", note = "A default keystore is created if it doesn't exist, featuring a self signed certificate for 'localhost'.")
public class StartClientAdminUI extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private static final int MILLIS_IN_ONE_SECOND = 1000;
	private static final String HTTP_1_1 = "http/1.1";
	private static final String HTTPS = "https";

	@Parameter(name = "serverAddress", description = "the specific server hostname to bind to for multi-homed hosts.")
	private String serverAddress;
	@Parameter(name = "httpsPort", required = true, description = "the HTTPS port.")
	private Integer httpsPort;
	@Parameter(name = "cipherSuites", required = true, description = "a csv list of HTTPS cipher suites allowed.")
	private String cipherSuites;
	@Parameter(name = "httpsProtocols", required = true, description = "a csv list of HTTPS cipher suites allowed.")
	private String httpsProtocols;
	@Parameter(name = "connectionIdleTimeoutSec", defaultValue = "90", description = "the TCP layer idle timeout in seconds.")
	private Integer connectionIdleTimeoutSec;

	@Parameter(name = "keystoreFile", required = true, description = "the HTTPS keystore file.")
	private String keyStoreFile;
	@Parameter(name = "keystorePassword", required = true, masked = true, description = "the HTTPS keystore password.")
	private String keyStorePassword;
	@Parameter(name = "keystoreType", required = true, description = "the HTTPS keystore type.")
	private String keyStoreType;
	@Parameter(name = "keystoreAlias", required = true, description = "the HTTPS keystore key alias.")
	private String keyStoreAlias;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		Server jetty = AdminUIHolder.getServer();
		if (jetty != null) {
			out.println("Admin UI already started. Stop with ui:stop.");
			return;
		}

		// we create a HTTPS keystore to provide secure access to https://localhost:443

		if (!getUiKeystoreService().existsServerKey(keyStoreFile)) {
			if (!createKeystore(out)) {
				out.println("Unable to create HTTPS keystore " + keyStoreFile);
				return;
			}
		} else {
			PKIXCredential serverKey = getUiKeystoreService().getServerKey(keyStoreFile, keyStoreType, keyStorePassword,
					keyStoreAlias);
			if (serverKey == null) {
				out.println("Abort starting UI due to inability to get server key.");
				return;
			}
			// check expiration.
			if (serverKey.getPublicCert().getNotAfter().before(Calendar.getInstance())) {
				out.println("HTTPS server key has expired. Delete the keystore and restart.");
				return;
			}
		}

		// Create a basic jetty server object without declaring the port. Since we are configuring connectors
		// directly we'll be setting ports on those connectors.
		jetty = new Server();

		SslContextFactory sslCF = new SslContextFactory();
		// we trust client certs known to our ServiceName, with security in servlet "filters"
		// sslContextFactory.setCertAlias("server");
		sslCF.setRenegotiationAllowed(true);
		sslCF.setWantClientAuth(false);

		sslCF.setIncludeCipherSuites(StringUtils.convertCsvToStringArray(cipherSuites));
		sslCF.setIncludeProtocols(StringUtils.convertCsvToStringArray(httpsProtocols));
		sslCF.setKeyStoreType(keyStoreType);
		sslCF.setKeyStorePath(keyStoreFile);
		sslCF.setKeyStorePassword(keyStorePassword);
		sslCF.setCertAlias(keyStoreAlias);

		// HTTPS Configuration
		// A new HttpConfiguration object is needed for the next connector and you can pass the old one as an
		// argument to effectively clone the contents. On this HttpConfiguration object we add a
		// SecureRequestCustomizer which is how a new connector is able to resolve the https connection before
		// handing control over to the Jetty ServerContainer.
		HttpConfiguration httpsConfig = new HttpConfiguration();
		httpsConfig.setSecureScheme(HTTPS);
		httpsConfig.setSecurePort(httpsPort);
		httpsConfig.setOutputBufferSize(32768);
		httpsConfig.addCustomizer(new SecureRequestCustomizer());

		// HTTPS connector
		// We create a second ServerConnector, passing in the http configuration we just made along with the
		// previously created ssl context factory. Next we set the port and a longer idle timeout.
		ServerConnector httpsCon = new ServerConnector(jetty, new SslConnectionFactory(sslCF, HTTP_1_1),
				new HttpConnectionFactory(httpsConfig));
		httpsCon.setPort(httpsPort);
		httpsCon.setHost(serverAddress);
		httpsCon.setIdleTimeout(connectionIdleTimeoutSec * MILLIS_IN_ONE_SECOND);

		// Set the connectors
		jetty.setConnectors(new Connector[] { httpsCon });

		// The following section adds some handlers, deployers and webapp providers.
		// See: http://www.eclipse.org/jetty/documentation/current/advanced-embedding.html for details.

		// Setup handlers
		HandlerCollection handlers = new HandlerCollection();
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		RequestLogHandler requestLogHandler = new RequestLogHandler();
		HSTSHandler hstsHandler = new HSTSHandler();
		NotFoundHandler notfoundHandler = new NotFoundHandler();

		handlers.setHandlers(new Handler[] { hstsHandler, contexts, requestLogHandler, notfoundHandler });

		StatisticsHandler stats = new StatisticsHandler();
		stats.setHandler(handlers);

		jetty.setHandler(stats);

		NCSARequestLog requestLog = new AsyncNCSARequestLog();
		requestLog.setFilename("ca-yyyy_mm_dd.log");
		requestLog.setExtended(true);
		requestLog.setRetainDays(7);
		requestLogHandler.setRequestLog(requestLog);

		ServletContextHandler rsContext = new ServletContextHandler(
				ServletContextHandler.NO_SECURITY | ServletContextHandler.SESSIONS);
		rsContext.setContextPath("ca");
		// Setup Spring context
		rsContext.addEventListener(new org.springframework.web.context.ContextLoaderListener());
		rsContext.setInitParameter("parentContextKey", "clientAdminContext");
		rsContext.setInitParameter("locatorFactorySelector", "classpath:clientAdminBeanRefContext.xml");
		rsContext.setInitParameter("contextConfigLocation", "classpath:/ca-context.xml");

		// Add filters
		/*
		 * FilterHolder rsfh = new FilterHolder(); rsfh.setFilter(getAgentAuthorizationFilter());
		 * rsContext.addFilter(rsfh, "/sas/*", EnumSet.of(DispatcherType.REQUEST));
		 */
		// Add servlets
		ServletHolder rsSh = new ServletHolder(CXFServlet.class);
		rsSh.setInitOrder(1);
		rsContext.addServlet(rsSh, "/*");

		contexts.addHandler(rsContext);
		try {
			jetty.start();
		} catch (Exception e) {
			out.println("Unable to start client admin UI.", e);
			try {
				jetty.stop();
			} catch (Exception e1) {
				out.println("Unable to tear down client admin UI.", e);
			}
			return;
		}
		AdminUIHolder.setServer(jetty);

		out.println("Client admin UI started. https://localhost:" + httpsPort + "/");
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private boolean createKeystore(CliPrinter out) {
		try {
			InetAddress serverInterface = StringUtils.hasText(serverAddress) ? InetAddress.getByName(serverAddress)
					: InetAddress.getLocalHost();
			String serverLocalIPAddress = serverInterface.getHostAddress();
			out.println("creating self signed HTTPS server key for " + serverLocalIPAddress + ":" + httpsPort);

			PKIXCredential serverCred;
			try {
				serverCred = createServerPrivateKey(serverLocalIPAddress);
			} catch (CryptoCertificateException e) {
				out.println("Unable to create HTTPS private key.", e);
				return false;
			}

			getUiKeystoreService().saveServerKey(serverCred, keyStoreFile, keyStoreType, keyStorePassword,
					keyStoreAlias);
			return true;
		} catch (UnknownHostException e) {
			out.println("Unable to resolve host " + e.getMessage(), e);
		}
		return false;

	}

	private PKIXCredential createServerPrivateKey(String serverLocalIPAddress) throws CryptoCertificateException {
		Calendar now = Calendar.getInstance();
		Calendar later = Calendar.getInstance();
		later.add(Calendar.DATE, 720);

		ServerIpCredentialSpecifier sics = new ServerIpCredentialSpecifier(serverLocalIPAddress);
		sics.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		sics.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);

		sics.setNotAfter(later);
		sics.setNotBefore(now);

		return CredentialUtils.createServerIpCredential(sics);
	}
	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
