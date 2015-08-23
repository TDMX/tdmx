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

import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.TrustManager;
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
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.ws.security.HSTSHandler;
import org.tdmx.server.ws.security.NotFoundHandler;
import org.tdmx.server.ws.session.WebServiceApiName;

public class RxServerContainer implements ServerContainer {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RxServerContainer.class);

	private static final int MILLIS_IN_ONE_SECOND = 1000;
	private static final String HTTP_1_1 = "http/1.1";
	private static final String HTTPS = "https";

	private ServerRuntimeContextService runtimeContext;

	private Filter agentAuthorizationFilter;

	private TrustManagerProvider trustProvider;
	private List<Manageable> manageables;

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

		TrustingSslContextFactory sslExt = new TrustingSslContextFactory();
		// we trust client certs known to our ServerName, with security in servlet "filters"
		// sslContextFactory.setCertAlias("server");
		sslExt.setRenegotiationAllowed(runtimeContext.isRenegotiationAllowed());
		sslExt.setWantClientAuth(false);

		sslExt.setIncludeCipherSuites(runtimeContext.getHttpsCiphers());
		sslExt.setIncludeProtocols(runtimeContext.getHttpsProtocols());
		sslExt.setKeyStoreType("jks");
		sslExt.setKeyStorePath(runtimeContext.getKeyStoreFile());
		sslExt.setKeyStorePassword(runtimeContext.getKeyStorePassword());
		// TODO check if needed
		// sslContextFactory.setKeyManagerPassword("changeme");

		// HTTPS Configuration
		// A new HttpConfiguration object is needed for the next connector and you can pass the old one as an
		// argument to effectively clone the contents. On this HttpConfiguration object we add a
		// SecureRequestCustomizer which is how a new connector is able to resolve the https connection before
		// handing control over to the Jetty ServerContainer.
		HttpConfiguration httpsConfigExt = new HttpConfiguration();
		httpsConfigExt.setSecureScheme(HTTPS);
		httpsConfigExt.setSecurePort(runtimeContext.getHttpsPort());
		httpsConfigExt.setOutputBufferSize(32768);
		httpsConfigExt.addCustomizer(new SecureRequestCustomizer());

		// HTTPS connector
		// We create a second ServerConnector, passing in the http configuration we just made along with the
		// previously created ssl context factory. Next we set the port and a longer idle timeout.
		ServerConnector httpsExt = new ServerConnector(jetty, new SslConnectionFactory(sslExt, HTTP_1_1),
				new HttpConnectionFactory(httpsConfigExt));
		httpsExt.setPort(runtimeContext.getHttpsPort());
		httpsExt.setHost(runtimeContext.getServerLocalIPAddress());
		httpsExt.setIdleTimeout(runtimeContext.getConnectionIdleTimeoutSec() * MILLIS_IN_ONE_SECOND);

		// Set the connectors
		jetty.setConnectors(new Connector[] { httpsExt });

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

		ServletContextHandler rsContext = new ServletContextHandler(ServletContextHandler.NO_SECURITY
				| ServletContextHandler.NO_SESSIONS);
		rsContext.setContextPath("/rs");
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

	private class TrustingSslContextFactory extends SslContextFactory {
		@Override
		protected TrustManager[] getTrustManagers(KeyStore trustStore, Collection<? extends CRL> crls) throws Exception {
			return new TrustManager[] { getTrustProvider().getTrustManager() };
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public Filter getAgentAuthorizationFilter() {
		return agentAuthorizationFilter;
	}

	public void setAgentAuthorizationFilter(Filter agentAuthorizationFilter) {
		this.agentAuthorizationFilter = agentAuthorizationFilter;
	}

	public TrustManagerProvider getTrustProvider() {
		return trustProvider;
	}

	public void setTrustProvider(TrustManagerProvider trustProvider) {
		this.trustProvider = trustProvider;
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
