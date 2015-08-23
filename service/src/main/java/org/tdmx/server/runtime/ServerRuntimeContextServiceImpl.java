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
import java.net.InetAddress;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;

public class ServerRuntimeContextServiceImpl implements ServerRuntimeContextService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ServerRuntimeContextServiceImpl.class);

	private String[] supportedCipherSuites;
	private String[] supportedProtocols;
	private String defaultTrustManagerFactoryAlgorithm;

	private String serverLocalIPAddress;
	private String serverAddress;
	private int httpsPort;

	private String[] httpsCiphers;
	private String[] httpsProtocols;
	private boolean renegotiationAllowed;
	private int connectionIdleTimeoutSec;

	private String stopLocalIPAddress;
	private String stopAddress;
	private int stopPort;
	private String stopCommand;

	private String keyStoreFile;
	private String keyStorePassword;

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

			InetAddress serverInterface = StringUtils.hasText(serverAddress) ? InetAddress.getByName(serverAddress)
					: InetAddress.getLocalHost();
			serverLocalIPAddress = serverInterface.getHostAddress();
			log.debug("ServerContainer IP " + serverLocalIPAddress + ":" + httpsPort);

			InetAddress stopInterface = StringUtils.hasText(stopAddress) ? InetAddress.getByName(stopAddress)
					: InetAddress.getLocalHost();
			stopLocalIPAddress = stopInterface.getHostAddress();
			log.debug("Stop IP " + stopLocalIPAddress + ":" + stopPort);

			log.debug("Creating a server socket on port " + httpsPort);
			try (SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(httpsPort)) {
				supportedCipherSuites = serverSocket.getSupportedCipherSuites();
				for (int i = 0; i < supportedCipherSuites.length; i++) {
					log.debug("supported cipher suite: " + supportedCipherSuites[i]);
				}

				supportedProtocols = serverSocket.getSupportedProtocols();
				for (int i = 0; i < supportedProtocols.length; i++) {
					log.debug("supported ssl protocol: " + supportedProtocols[i]);
				}
			}

		} catch (IOException e) {
			log.warn("Unable to determine SSL capabilities of the JVM.", e);
		}
	}

	@Override
	public String getServerLocalIPAddress() {
		return serverLocalIPAddress;
	}

	@Override
	public String getStopLocalIPAddress() {
		return stopLocalIPAddress;
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

	@Override
	public String[] getSupportedCipherSuites() {
		return supportedCipherSuites;
	}

	@Override
	public String[] getSupportedProtocols() {
		return supportedProtocols;
	}

	@Override
	public String getDefaultTrustManagerFactoryAlgorithm() {
		return defaultTrustManagerFactoryAlgorithm;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	@Override
	public int getHttpsPort() {
		return httpsPort;
	}

	public void setHttpsPort(int httpsPort) {
		this.httpsPort = httpsPort;
	}

	@Override
	public String[] getHttpsCiphers() {
		return httpsCiphers;
	}

	public void setHttpsCiphers(String[] httpsCiphers) {
		this.httpsCiphers = httpsCiphers;
	}

	@Override
	public String[] getHttpsProtocols() {
		return httpsProtocols;
	}

	public void setHttpsProtocols(String[] httpsProtocols) {
		this.httpsProtocols = httpsProtocols;
	}

	@Override
	public boolean isRenegotiationAllowed() {
		return renegotiationAllowed;
	}

	public void setRenegotiationAllowed(boolean renegotiationAllowed) {
		this.renegotiationAllowed = renegotiationAllowed;
	}

	public String getStopAddress() {
		return stopAddress;
	}

	public void setStopAddress(String stopAddress) {
		this.stopAddress = stopAddress;
	}

	@Override
	public int getStopPort() {
		return stopPort;
	}

	public void setStopPort(int stopPort) {
		this.stopPort = stopPort;
	}

	@Override
	public String getStopCommand() {
		return stopCommand;
	}

	public void setStopCommand(String stopCommand) {
		this.stopCommand = stopCommand;
	}

	@Override
	public String getKeyStoreFile() {
		return keyStoreFile;
	}

	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	@Override
	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	@Override
	public int getConnectionIdleTimeoutSec() {
		return connectionIdleTimeoutSec;
	}

	public void setConnectionIdleTimeoutSec(int connectionIdleTimeoutSec) {
		this.connectionIdleTimeoutSec = connectionIdleTimeoutSec;
	}

}
