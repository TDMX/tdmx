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
package org.tdmx.server.scs;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;

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
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.runtime.ServerContainer;
import org.tdmx.server.ws.security.HSTSHandler;
import org.tdmx.server.ws.security.NotFoundHandler;
import org.tdmx.server.ws.security.RequireClientCertificateFilter;
import org.tdmx.server.ws.security.SessionProhibitionFilter;
import org.tdmx.server.ws.session.WebServiceApiName;

public class SCSWebServiceServerContainer implements ServerContainer {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SCSWebServiceServerContainer.class);

	private static final int MILLIS_IN_ONE_SECOND = 1000;
	private static final String HTTP_1_1 = "http/1.1";
	private static final String HTTPS = "https";

	private boolean renegotiationAllowed;
	private String[] cipherSuites;
	private String[] httpsProtocols;
	private int connectionIdleTimeoutSec;
	private String serverAddress;
	private String serverLocalIPAddress;

	private int httpsPort;
	private String contextPath;

	private String keyStoreFile;
	private String keyStoreType;
	private String keyStorePassword;
	private String keyStoreAlias;
	private String trustStoreFile;
	private String trustStoreType;
	private String trustStorePassword;

	private Filter agentAuthorizationFilter;

	private List<Manageable> manageables;

	private Server jetty;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void init() {
		try {
			InetAddress serverInterface = StringUtils.hasText(serverAddress) ? InetAddress.getByName(serverAddress)
					: InetAddress.getLocalHost();
			serverLocalIPAddress = serverInterface.getHostAddress();
			log.debug("SCS ServerContainer IP " + serverLocalIPAddress + ":" + httpsPort);

		} catch (IOException e) {
			String errorMsg = "Unable to determine the IP address of the server [" + serverAddress + "]";
			log.warn(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}

	@Override
	public void start(String segment, List<WebServiceApiName> apis) throws Exception {
		// Create a basic jetty server object without declaring the port. Since we are configuring connectors
		// directly we'll be setting ports on those connectors.
		jetty = new Server();

		ClosedPKIXSslContextFactory sslCF = new ClosedPKIXSslContextFactory();
		sslCF.setRenegotiationAllowed(renegotiationAllowed);
		sslCF.setWantClientAuth(true);

		sslCF.setIncludeCipherSuites(cipherSuites);
		sslCF.setIncludeProtocols(httpsProtocols);
		sslCF.setKeyStoreType(keyStoreType);
		sslCF.setKeyStorePath(keyStoreFile);
		sslCF.setKeyStorePassword(keyStorePassword);
		sslCF.setCertAlias(keyStoreAlias);
		sslCF.setTrustStorePath(trustStoreFile);
		sslCF.setTrustStoreType(trustStoreType);
		sslCF.setTrustStorePassword(trustStorePassword);

		// TODO check if needed
		// sslContextFactory.setKeyManagerPassword("changeme");

		// HTTPS Configuration
		// A new HttpConfiguration object is needed for the next connector and you can pass the old one as an
		// argument to effectively clone the contents. On this HttpConfiguration object we add a
		// SecureRequestCustomizer which is how a new connector is able to resolve the https connection before
		// handing control over to the Jetty ServerContainer.
		HttpConfiguration httpsConf = new HttpConfiguration();
		httpsConf.setSecureScheme(HTTPS);
		httpsConf.setSecurePort(httpsPort);
		httpsConf.setOutputBufferSize(32768);
		httpsConf.addCustomizer(new SecureRequestCustomizer());

		// HTTPS connector
		// We create a second ServerConnector, passing in the http configuration we just made along with the
		// previously created ssl context factory. Next we set the port and a longer idle timeout.
		ServerConnector httpsCon = new ServerConnector(jetty, new SslConnectionFactory(sslCF, HTTP_1_1),
				new HttpConnectionFactory(httpsConf));
		httpsCon.setPort(httpsPort);
		httpsCon.setHost(serverLocalIPAddress);
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
		requestLog.setFilename("scs-yyyy_mm_dd.log");
		requestLog.setExtended(true);
		requestLog.setRetainDays(7);
		requestLogHandler.setRequestLog(requestLog);

		ServletContextHandler wsContext = new ServletContextHandler(
				ServletContextHandler.NO_SECURITY | ServletContextHandler.NO_SESSIONS);
		wsContext.setContextPath(contextPath);
		// Setup Spring context
		wsContext.addEventListener(new org.springframework.web.context.ContextLoaderListener());
		wsContext.setInitParameter("parentContextKey", "applicationContext");
		wsContext.setInitParameter("locatorFactorySelector", "classpath*:beanRefContext.xml");
		wsContext.setInitParameter("contextConfigLocation", "classpath:/scs-context.xml");

		// Add filters
		FilterHolder sf = new FilterHolder();
		sf.setFilter(new SessionProhibitionFilter());
		wsContext.addFilter(sf, "/*", EnumSet.allOf(DispatcherType.class));

		FilterHolder cf = new FilterHolder();
		cf.setFilter(new RequireClientCertificateFilter());
		wsContext.addFilter(cf, "/*", EnumSet.allOf(DispatcherType.class));

		FilterHolder fh = new FilterHolder();
		fh.setFilter(getAgentAuthorizationFilter());
		wsContext.addFilter(fh, "*", EnumSet.of(DispatcherType.REQUEST));

		ServletHolder wsSh = new ServletHolder(CXFServlet.class);
		wsSh.setInitOrder(1);
		wsContext.addServlet(wsSh, "/*");

		contexts.addHandler(wsContext);
		// Start the server
		jetty.start();

		startManageables(segment, apis);
	}

	@Override
	public void stop() {
		if (jetty != null) {
			try {
				jetty.stop();

				stopManageables();
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

	void startManageables(String segment, List<WebServiceApiName> apis) {
		for (Manageable m : getManageables()) {
			m.start(segment, apis);
		}
	}

	void stopManageables() {
		for (Manageable m : getManageables()) {
			m.stop();
		}
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private class ClosedPKIXSslContextFactory extends SslContextFactory {
		@Override
		protected TrustManager[] getTrustManagers(KeyStore trustStore, Collection<? extends CRL> crls)
				throws Exception {
			TrustManager[] managers = super.getTrustManagers(trustStore, crls);
			TrustManager[] wrap = new TrustManager[managers.length];
			for (int i = 0; i < managers.length; i++) {
				final TrustManager tm = managers[i];
				if (tm instanceof X509TrustManager) {
					final X509TrustManager xt = (X509TrustManager) tm;

					wrap[i] = new X509TrustManager() {

						@Override
						public void checkClientTrusted(X509Certificate[] chain, String authType)
								throws CertificateException {
							xt.checkClientTrusted(chain, authType);
						}

						@Override
						public void checkServerTrusted(X509Certificate[] chain, String authType)
								throws CertificateException {
							xt.checkServerTrusted(chain, authType);
						}

						@Override
						public X509Certificate[] getAcceptedIssuers() {
							// we don't publicly state that we accept any CA names
							return new X509Certificate[0];
						}

					};
				} else {
					wrap[i] = tm;
				}
			}
			return wrap;
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

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

	public int getConnectionIdleTimeoutSec() {
		return connectionIdleTimeoutSec;
	}

	public void setConnectionIdleTimeoutSec(int connectionIdleTimeoutSec) {
		this.connectionIdleTimeoutSec = connectionIdleTimeoutSec;
	}

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

	public String getKeyStoreFile() {
		return keyStoreFile;
	}

	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	public String getKeyStoreType() {
		return keyStoreType;
	}

	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
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

	public Filter getAgentAuthorizationFilter() {
		return agentAuthorizationFilter;
	}

	public void setAgentAuthorizationFilter(Filter agentAuthorizationFilter) {
		this.agentAuthorizationFilter = agentAuthorizationFilter;
	}

	public List<Manageable> getManageables() {
		if (manageables == null) {
			manageables = new ArrayList<>();
		}
		return manageables;
	}

	public void setManageables(List<Manageable> manageables) {
		this.manageables = manageables;
	}

}
