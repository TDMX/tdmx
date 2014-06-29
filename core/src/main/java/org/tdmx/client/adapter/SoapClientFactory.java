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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
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

public class SoapClientFactory<E> {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SoapClientFactory.class);

	@SuppressWarnings("rawtypes")
	private Class clazz;

	private String url;
	private boolean keepAlive;
	private int receiveTimeout; // TODO millis
	private int connectionTimeout; // TODO millis

	// TODO logging interceptors as properties

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public E createClient() {

		E serviceClient = null;

		Client proxy = null;
		HTTPConduit conduit = null;
		TLSClientParameters params = null;

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

		serviceClient = (E) factory.create();

		proxy = ClientProxy.getClient(serviceClient);
		proxy.getInInterceptors().add(loggingInInterceptor);
		proxy.getOutInterceptors().add(loggingOutInterceptor);

		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy
				.setConnection(isKeepAlive() ? org.apache.cxf.transports.http.configuration.ConnectionType.KEEP_ALIVE
						: org.apache.cxf.transports.http.configuration.ConnectionType.CLOSE);
		httpClientPolicy.setConnectionTimeout(getConnectionTimeout());
		httpClientPolicy.setReceiveTimeout(getReceiveTimeout());
		httpClientPolicy.setCacheControl("no-cache");
		httpClientPolicy.setAutoRedirect(false);

		conduit = (HTTPConduit) proxy.getConduit();
		params = new TLSClientParameters(); // TODO configure keystore/truststore/protocols/ciphersuites
		params.setDisableCNCheck(true);
		params.setSecureSocketProtocol("TLS");

		conduit.setClient(httpClientPolicy);

		SavingTrustManager stm = new SavingTrustManager(null);
		params.setTrustManagers(new TrustManager[] { stm });
		conduit.setTlsClientParameters(params);

		return serviceClient;
		/*
		 * GetDeviceRequest parameters = new GetDeviceRequest(); RequestContext rc = new RequestContext();
		 * rc.setOrderId("O123"); rc.setSourceSystem(Application.PP); parameters.setRequestContext(rc); DeviceReference
		 * ref = new DeviceReference(); ref.setDeviceId("123"); parameters.setDeviceReference(ref); GetDeviceResponse
		 * resp; try { resp = serviceClient.getDevice(parameters); System.out.println(resp.getDevice()); } catch
		 * (ProcessingException e) { // TODO Auto-generated catch block e.printStackTrace(); } catch
		 * (ValidationException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 * 
		 * DeviceV1 serviceClient2 = (DeviceV1) factory.create(); System.out.println("1) " + serviceClient + ", 2) " +
		 * serviceClient2);
		 * 
		 * System.out.println("ServerCert=" + stm.chain);
		 */
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

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

	private static final class DumbX509TrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString)
				throws CertificateException {
			System.out.println("DumbX509TrustManager#checkClientTrusted: " + paramString);
		}

		@Override
		public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString)
				throws CertificateException {
			System.out.println("DumbX509TrustManager#checkServerTrusted: " + paramString);
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			System.out.println("DumbX509TrustManager#getAcceptedIssuers");
			return null;
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

	public int getReceiveTimeout() {
		return receiveTimeout;
	}

	public void setReceiveTimeout(int receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

}
