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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.tdmx.client.adapter.SslProbeService.ConnectionTestResult.TestStep;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.TrustStoreCertificateIOUtils;

public class SslProbeService {

	private ClientKeyManagerFactory keyManagerFactory;

	private String sslProtocol;
	private String[] enabledCiphers;

	private int connectionTimeoutMillis;
	private int readTimeoutMillis;

	public static class ConnectionTestResult {

		public enum TestStep {
			PRE_CONNECT,
			SOCKET_CONNECT,
			SSL_HANDSHAKE,
			TRUST_CHECK,
			COMPLETE;
		}

		private TestStep testStep = TestStep.PRE_CONNECT;
		private String negotiatedCipherSuite;
		private String remoteIpAddress;
		private PKIXCertificate[] serverCertChain;
		private Exception exception;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ConnectionTestResult [testStep=");
			builder.append(testStep);
			builder.append(", negotiatedCipherSuite=");
			builder.append(negotiatedCipherSuite);
			builder.append(", remoteIpAddress=");
			builder.append(remoteIpAddress);
			builder.append(", serverCertChain=");
			builder.append(Arrays.toString(serverCertChain));
			builder.append(", exception=");
			builder.append(exception);
			builder.append("]");
			return builder.toString();
		}

		public Exception getException() {
			return exception;
		}

		public void setException(Exception exception) {
			this.exception = exception;
		}

		public TestStep getTestStep() {
			return testStep;
		}

		public void setTestStep(TestStep testStep) {
			this.testStep = testStep;
		}

		public String getNegotiatedCipherSuite() {
			return negotiatedCipherSuite;
		}

		public void setNegotiatedCipherSuite(String negotiatedCipherSuite) {
			this.negotiatedCipherSuite = negotiatedCipherSuite;
		}

		public String getRemoteIpAddress() {
			return remoteIpAddress;
		}

		public void setRemoteIpAddress(String remoteIpAddress) {
			this.remoteIpAddress = remoteIpAddress;
		}

		public PKIXCertificate[] getServerCertChain() {
			return serverCertChain;
		}

		public void setServerCertChain(PKIXCertificate[] serverCertChain) {
			this.serverCertChain = serverCertChain;
		}

	}

	public ConnectionTestResult testConnection(String hostname, int port) {
		ConnectionTestResult result = new ConnectionTestResult();

		// the idea is to fall through leaving the last result state we got to
		// as an indicator of where we got to in the test
		try {
			result.setTestStep(TestStep.PRE_CONNECT);

			KeyManager km = getKeyManagerFactory().getKeyManager();// TODO;
			X509TrustManager platformTm = TrustStoreCertificateIOUtils.getDefaultPKIXTrustManager();
			TestingTrustManager ttm = new TestingTrustManager(platformTm);

			SSLContext sc = SSLContext.getInstance(getSslProtocol());
			sc.init(new KeyManager[] { km }, new TrustManager[] { ttm }, null);

			SSLSocketFactory f = sc.getSocketFactory();

			result.setTestStep(TestStep.SOCKET_CONNECT);
			InetSocketAddress addr = new InetSocketAddress(hostname, port);

			Socket sock = new Socket();
			sock.connect(addr, getConnectionTimeoutMillis());
			sock.setSoTimeout(getReadTimeoutMillis());

			SSLSocket c = (SSLSocket) f.createSocket(sock, hostname, port, true);
			result.setRemoteIpAddress(c.getInetAddress().toString());

			result.setTestStep(TestStep.SSL_HANDSHAKE);
			c.setEnabledCipherSuites(getEnabledCiphers());
			c.setEnabledProtocols(new String[] { getSslProtocol() });
			SSLSession ss = c.getSession();
			result.setNegotiatedCipherSuite(ss.getCipherSuite());

			result.setTestStep(TestStep.TRUST_CHECK);
			result.setServerCertChain(CertificateIOUtils.convert(ttm.getServerCertificate()));
			if (ttm.getTrustException() != null) {
				result.setException(ttm.getTrustException());
			} else {
				result.setTestStep(TestStep.COMPLETE);
			}

		} catch (GeneralSecurityException e) {
			result.setException(e);
		} catch (IOException e) {
			result.setException(e);
		}
		return result;
	}

	public String getSslProtocol() {
		return sslProtocol;
	}

	public void setSslProtocol(String sslProtocol) {
		this.sslProtocol = sslProtocol;
	}

	public String[] getEnabledCiphers() {
		return enabledCiphers;
	}

	public void setEnabledCiphers(String[] enabledCiphers) {
		this.enabledCiphers = enabledCiphers;
	}

	public int getConnectionTimeoutMillis() {
		return connectionTimeoutMillis;
	}

	public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
		this.connectionTimeoutMillis = connectionTimeoutMillis;
	}

	public int getReadTimeoutMillis() {
		return readTimeoutMillis;
	}

	public void setReadTimeoutMillis(int readTimeoutMillis) {
		this.readTimeoutMillis = readTimeoutMillis;
	}

	public ClientKeyManagerFactory getKeyManagerFactory() {
		return keyManagerFactory;
	}

	public void setKeyManagerFactory(ClientKeyManagerFactory keyManagerFactory) {
		this.keyManagerFactory = keyManagerFactory;
	}
}
