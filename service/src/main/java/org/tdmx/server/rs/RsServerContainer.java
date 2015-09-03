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
package org.tdmx.server.rs;

import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.runtime.ServerContainer;
import org.tdmx.server.ws.security.HSTSHandler;
import org.tdmx.server.ws.security.NotFoundHandler;
import org.tdmx.server.ws.session.WebServiceApiName;

public class RsServerContainer implements ServerContainer {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RsServerContainer.class);

	private static final int MILLIS_IN_ONE_SECOND = 1000;
	private static final String HTTP_1_1 = "http/1.1";
	private static final String HTTPS = "https";

	private String serverAddress;

	private int httpsPort;
	private String contextPath;
	private boolean renegotiationAllowed;
	private String[] cipherSuites;
	private String[] httpsProtocols;
	private String keystorePath;
	private String keystorePassword;
	private String keystoreType;
	private String keyStoreAlias;
	private String trustStoreFile;
	private String trustStoreType;
	private String trustStorePassword;

	private int connectionIdleTimeoutSec;

	private Server jetty;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void start(String segment, List<WebServiceApiName> apis) throws Exception {
		// Create a basic jetty server object without declaring the port. Since we are configuring connectors
		// directly we'll be setting ports on those connectors.
		jetty = new Server();

		SslContextFactory sslCF = new SslContextFactory();
		// we trust client certs known to our ServiceName, with security in servlet "filters"
		// sslContextFactory.setCertAlias("server");
		sslCF.setRenegotiationAllowed(renegotiationAllowed);
		sslCF.setWantClientAuth(false);

		sslCF.setIncludeCipherSuites(cipherSuites);
		sslCF.setIncludeProtocols(httpsProtocols);
		sslCF.setKeyStoreType(keystoreType);
		sslCF.setKeyStorePath(keystorePath);
		sslCF.setKeyStorePassword(keystorePassword);
		sslCF.setCertAlias(keyStoreAlias);
		sslCF.setTrustStorePath(trustStoreFile);
		sslCF.setTrustStoreType(trustStoreType);
		sslCF.setTrustStorePassword(trustStorePassword);

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
		requestLog.setFilename("rx-yyyy_mm_dd.log");
		requestLog.setExtended(true);
		requestLog.setRetainDays(7);
		requestLogHandler.setRequestLog(requestLog);

		ServletContextHandler rsContext = new ServletContextHandler(
				ServletContextHandler.NO_SECURITY | ServletContextHandler.NO_SESSIONS);
		rsContext.setContextPath(contextPath);
		// Setup Spring context
		rsContext.addEventListener(new org.springframework.web.context.ContextLoaderListener());
		rsContext.setInitParameter("parentContextKey", "applicationContext");
		rsContext.setInitParameter("locatorFactorySelector", "classpath*:beanRefContext.xml");
		rsContext.setInitParameter("contextConfigLocation", "classpath:/rs-context.xml");

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
		jetty.start();
	}

	@Override
	public void stop() {
		if (jetty != null) {
			try {
				jetty.stop();
			} catch (Exception e) {
				log.error("Unable to stop jetty.", e);
			}
		}
	}

	@Override
	public void awaitTermination() {
		// Wait for the server to be stopped by the MonitorThread.
		if (jetty != null) {
			try {
				jetty.join();
			} catch (InterruptedException e) {
				log.warn("Interupted awaitTermination.", e);
			}
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

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getHttpsPort() {
		return httpsPort;
	}

	public void setHttpsPort(int httpsPort) {
		this.httpsPort = httpsPort;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public boolean isRenegotiationAllowed() {
		return renegotiationAllowed;
	}

	public void setRenegotiationAllowed(boolean renegotiationAllowed) {
		this.renegotiationAllowed = renegotiationAllowed;
	}

	public String[] getCipherSuites() {
		return cipherSuites;
	}

	public void setCipherSuites(String[] cipherSuites) {
		this.cipherSuites = cipherSuites;
	}

	public String[] getHttpsProtocols() {
		return httpsProtocols;
	}

	public void setHttpsProtocols(String[] httpsProtocols) {
		this.httpsProtocols = httpsProtocols;
	}

	public String getKeystorePath() {
		return keystorePath;
	}

	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public String getKeystoreType() {
		return keystoreType;
	}

	public void setKeystoreType(String keystoreType) {
		this.keystoreType = keystoreType;
	}

	public String getKeyStoreAlias() {
		return keyStoreAlias;
	}

	public void setKeyStoreAlias(String keyStoreAlias) {
		this.keyStoreAlias = keyStoreAlias;
	}

	public String getTrustStoreFile() {
		return trustStoreFile;
	}

	public void setTrustStoreFile(String trustStoreFile) {
		this.trustStoreFile = trustStoreFile;
	}

	public String getTrustStoreType() {
		return trustStoreType;
	}

	public void setTrustStoreType(String trustStoreType) {
		this.trustStoreType = trustStoreType;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	public int getConnectionIdleTimeoutSec() {
		return connectionIdleTimeoutSec;
	}

	public void setConnectionIdleTimeoutSec(int connectionIdleTimeoutSec) {
		this.connectionIdleTimeoutSec = connectionIdleTimeoutSec;
	}
}
