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

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

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
import org.tdmx.client.crypto.certificate.PKIXCredential;

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

	private CredentialProvider credentialProvider;

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
		TLSClientParameters params = new TLSClientParameters(); // TODO configure
																// keystore/truststore/protocols/ciphersuites
		params.setDisableCNCheck(true); // TODO fix
		params.setSecureSocketProtocol("TLSv1.2");

		SavingTrustManager stm = new SavingTrustManager(null);
		params.setTrustManagers(new TrustManager[] { stm });

		// setup the client identity certificate
		if (getCredentialProvider() != null) {
			PKIXCredential identity = getCredentialProvider().getCredential();
			if (identity != null) {
				params.setKeyManagers(new KeyManager[] { new PKIXCredentialKeyManager(identity) });
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

	private static class PKIXCredentialKeyManager implements X509KeyManager {

		private static final Logger log = LoggerFactory.getLogger(PKIXCredentialKeyManager.class);

		private final PKIXCredential credential;

		public PKIXCredentialKeyManager(PKIXCredential credential) {
			this.credential = credential;
		}

		@Override
		public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
			log.info("chooseClientAlias keyType{" + keyType + "} issuers {" + issuers + "} on "
					+ socket.getInetAddress());
			return "identity";
		}

		@Override
		public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
			log.info("chooseServerAlias keyType{" + keyType + "} issuers {" + issuers + "} on "
					+ socket.getInetAddress());
			// not relevant for client side
			return null;
		}

		@Override
		public X509Certificate[] getCertificateChain(String alias) {
			log.info("getCertificateChain alias{" + alias + "}");
			// we don't provide the "full" chain to the TDMX zone root issuer since
			// the service providers always know our certificates exactly, and the
			// "certification" chain is not relevant for TLS trust - since the SP "knows"
			// each identity.
			return new X509Certificate[] { credential.getPublicCert().getCertificate() };
		}

		@Override
		public String[] getClientAliases(String keyType, Principal[] issuers) {
			log.info("getClientAliases keyType{" + keyType + "} issuers {" + issuers + "}");
			return null;
		}

		@Override
		public PrivateKey getPrivateKey(String alias) {
			log.info("getPrivateKey alias{" + alias + "}");
			return credential.getPrivateKey();
		}

		@Override
		public String[] getServerAliases(String keyType, Principal[] issuers) {
			log.info("getServerAliases keyType{" + keyType + "} issuers {" + issuers + "}");
			// not relevant for client side.
			return null;
		}

	}

	private static class SavingTrustManager implements X509TrustManager {

		// private final X509TrustManager tm;
		private X509Certificate[] chain;

		SavingTrustManager(X509TrustManager tm) {
			// this.tm = tm;
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
			// throw new UnsupportedOperationException();
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			// tm.checkServerTrusted(chain, authType);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public Class getClazz() {
		return clazz;
	}

	public void setClazz(Class clazz) {
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

	public CredentialProvider getCredentialProvider() {
		return credentialProvider;
	}

	public void setCredentialProvider(CredentialProvider credentialProvider) {
		this.credentialProvider = credentialProvider;
	}

}
