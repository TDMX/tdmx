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

import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslServerSocketInfo {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SslServerSocketInfo.class);

	private int sslTestPort;

	private String[] supportedCipherSuites;
	private String[] supportedProtocols;
	private String defaultTrustManagerFactoryAlgorithm;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	public void init() {

		try {
			defaultTrustManagerFactoryAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			log.debug("TrustManagerFactory.getDefaultAlgorithm() " + defaultTrustManagerFactoryAlgorithm);

			log.debug("Locating server socket factory for SSL...");
			SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

			log.debug("Creating a server socket on port " + sslTestPort);
			SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(sslTestPort);
			try {
				supportedCipherSuites = serverSocket.getSupportedCipherSuites();
				for (int i = 0; i < supportedCipherSuites.length; i++) {
					log.debug("supported cipher suite: " + supportedCipherSuites[i]);
				}

				supportedProtocols = serverSocket.getSupportedProtocols();
				for (int i = 0; i < supportedProtocols.length; i++) {
					log.debug("supported ssl protocol: " + supportedProtocols[i]);
				}

			} finally {
				serverSocket.close();
			}

		} catch (IOException e) {
			log.warn("Unable to determine SSL capabilities of the JVM.", e);
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
	public int getSslTestPort() {
		return sslTestPort;
	}

	public void setSslTestPort(int sslTestPort) {
		this.sslTestPort = sslTestPort;
	}

	public String[] getSupportedCipherSuites() {
		return supportedCipherSuites;
	}

	public void setSupportedCipherSuites(String[] supportedCipherSuites) {
		this.supportedCipherSuites = supportedCipherSuites;
	}

	public String[] getSupportedProtocols() {
		return supportedProtocols;
	}

	public void setSupportedProtocols(String[] supportedProtocols) {
		this.supportedProtocols = supportedProtocols;
	}

	public String getDefaultTrustManagerFactoryAlgorithm() {
		return defaultTrustManagerFactoryAlgorithm;
	}

}
