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
package org.tdmx.client.adapter;

import java.util.Arrays;

import javax.net.ssl.KeyManager;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapClientFactory<E> {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SoapClientFactory.class);

	private Class<E> clazz;

	private String url;
	private boolean keepAlive;
	private int receiveTimeoutMillis;
	private int connectionTimeoutMillis;
	private boolean disableCNCheck = false;
	private String tlsProtocolVersion;
	private String[] enabledCipherSuites;

	// TODO hookin ServerTrustManagerFactory

	private ClientKeyManagerFactory keyManagerFactory;

	// TODO logging interceptors as properties

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * creates a SOAP client setting up all key material. The returned client should be used by a single thread for a
	 * long duration. Don't create a new client for each call.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public E createClient() {

		/*
		 * turn off logging java.util.logging.Logger logger =
		 * java.util.logging.LogManager.getLogManager().getLogger(""); logger.setLevel(java.util.logging.Level.OFF);
		 */

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();

		/*
		 * StatsInboundInterceptor statsInInterceptor = new StatsInboundInterceptor(); StatsOutboundInterceptor
		 * statsOutInterceptor = new StatsOutboundInterceptor();
		 */
		factory.setAddress(getUrl());
		factory.setServiceClass(getClazz());

		// username password not needed due to client certificate inclusion.

		E serviceClient = (E) factory.create();

		// configure the SOAP properties
		Client proxy = ClientProxy.getClient(serviceClient);
		proxy.getInInterceptors().add(loggingInInterceptor);
		proxy.getOutInterceptors().add(loggingOutInterceptor);

		// configure the HTTP properties
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy
				.setConnection(isKeepAlive() ? org.apache.cxf.transports.http.configuration.ConnectionType.KEEP_ALIVE
						: org.apache.cxf.transports.http.configuration.ConnectionType.CLOSE);
		httpClientPolicy.setConnectionTimeout(getConnectionTimeoutMillis());
		httpClientPolicy.setReceiveTimeout(getReceiveTimeoutMillis());
		httpClientPolicy.setCacheControl("no-cache");
		httpClientPolicy.setAutoRedirect(false);

		// configure the SSL properties
		TLSClientParameters params = new TLSClientParameters();
		params.setDisableCNCheck(isDisableCNCheck());
		params.setSecureSocketProtocol(getTlsProtocolVersion());
		params.setCipherSuites(Arrays.asList(getEnabledCipherSuites()));

		// setup the client identity certificate
		if (getKeyManagerFactory() != null) {
			KeyManager clientKeyManager = getKeyManagerFactory().getKeyManager();
			if (clientKeyManager != null) {
				params.setKeyManagers(new KeyManager[] { clientKeyManager });
			}
		}

		// link the HTTP and SSL configuration with the client's conduit.
		HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
		conduit.setClient(httpClientPolicy);
		conduit.setTlsClientParameters(params);

		return serviceClient;
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

	public Class<E> getClazz() {
		return clazz;
	}

	public void setClazz(Class<E> clazz) {
		this.clazz = clazz;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public int getReceiveTimeoutMillis() {
		return receiveTimeoutMillis;
	}

	public void setReceiveTimeoutMillis(int receiveTimeoutMillis) {
		this.receiveTimeoutMillis = receiveTimeoutMillis;
	}

	public int getConnectionTimeoutMillis() {
		return connectionTimeoutMillis;
	}

	public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
		this.connectionTimeoutMillis = connectionTimeoutMillis;
	}

	public boolean isDisableCNCheck() {
		return disableCNCheck;
	}

	public void setDisableCNCheck(boolean disableCNCheck) {
		this.disableCNCheck = disableCNCheck;
	}

	public String getTlsProtocolVersion() {
		return tlsProtocolVersion;
	}

	public void setTlsProtocolVersion(String tlsProtocolVersion) {
		this.tlsProtocolVersion = tlsProtocolVersion;
	}

	public String[] getEnabledCipherSuites() {
		return enabledCipherSuites;
	}

	public void setEnabledCipherSuites(String[] enabledCipherSuites) {
		this.enabledCipherSuites = enabledCipherSuites;
	}

	public ClientKeyManagerFactory getKeyManagerFactory() {
		return keyManagerFactory;
	}

	public void setKeyManagerFactory(ClientKeyManagerFactory keyManagerFactory) {
		this.keyManagerFactory = keyManagerFactory;
	}

}
