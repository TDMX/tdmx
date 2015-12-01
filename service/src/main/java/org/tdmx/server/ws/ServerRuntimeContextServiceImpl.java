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
package org.tdmx.server.ws;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.ServerIpCredentialSpecifier;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.core.system.lang.StringUtils;

public class ServerRuntimeContextServiceImpl implements ServerRuntimeContextService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ServerRuntimeContextServiceImpl.class);

	private String serverAddress;
	private String serverLocalIPAddress;
	private int httpsPort;
	private String contextPath;

	private String keyStoreFile;
	private String keyStoreType;
	private String keyStorePassword;
	private String keyStoreAlias;

	private PublicKeyAlgorithm keyAlgorithm;
	private SignatureAlgorithm signatureAlgorithm;
	private int certificateValidityDays;

	// internal
	private PKIXCertificate publicKey;

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
			log.info("ServerContainer IP " + serverLocalIPAddress + ":" + httpsPort);

		} catch (IOException e) {
			String errorMsg = "Unable to determine the IP address of the server [" + serverAddress + "]";
			log.warn(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}

		try {
			PKIXCredential serverCert = createServerPrivateKey();

			byte[] keystore = KeyStoreUtils.saveKeyStore(serverCert, keyStoreType, keyStorePassword, keyStoreAlias);

			FileUtils.storeFileContents(keyStoreFile, keystore, ".tmp");

			publicKey = serverCert.getPublicCert();

			log.info("Server dynamic public certificate fingerprint " + publicKey.getFingerprint());
			log.info("Public certificate " + publicKey);
		} catch (CryptoCertificateException e) {
			String errorMsg = "Unable to create servers private credential [" + serverLocalIPAddress + "]";
			log.warn(errorMsg, e);
			throw new RuntimeException(errorMsg, e);

		} catch (IOException e) {
			String errorMsg = "Unable to store servers private credential [" + keyStoreFile + "]";
			log.warn(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}

	@Override
	public String getServerLocalIPAddress() {
		return serverLocalIPAddress;
	}

	@Override
	public int getHttpsPort() {
		return httpsPort;
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public String getKeyStoreFile() {
		return keyStoreFile;
	}

	@Override
	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	@Override
	public String getKeyStoreType() {
		return keyStoreType;
	}

	@Override
	public String getKeyStoreAlias() {
		return keyStoreAlias;
	}

	@Override
	public PKIXCertificate getPublicKey() {
		return publicKey;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private PKIXCredential createServerPrivateKey() throws CryptoCertificateException {
		Calendar now = Calendar.getInstance();
		Calendar later = Calendar.getInstance();
		later.add(Calendar.DATE, certificateValidityDays);

		ServerIpCredentialSpecifier sics = new ServerIpCredentialSpecifier(serverLocalIPAddress);
		sics.setKeyAlgorithm(keyAlgorithm);
		sics.setSignatureAlgorithm(signatureAlgorithm);

		sics.setNotAfter(later);
		sics.setNotBefore(now);

		return CredentialUtils.createServerIpCredential(sics);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public void setHttpsPort(int httpsPort) {
		this.httpsPort = httpsPort;
	}

	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	public PublicKeyAlgorithm getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(PublicKeyAlgorithm keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public SignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

	public int getCertificateValidityDays() {
		return certificateValidityDays;
	}

	public void setCertificateValidityDays(int certificateValidityDays) {
		this.certificateValidityDays = certificateValidityDays;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public void setKeyStoreAlias(String keyStoreAlias) {
		this.keyStoreAlias = keyStoreAlias;
	}

	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

}
