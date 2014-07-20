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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.system.lang.FileUtils;

public class KeystoreFileTrustedCertificateProvider implements TrustedServerCertificateProvider {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(KeystoreFileTrustedCertificateProvider.class);

	private String keystoreType;
	private String keystoreAlias;
	private String keystorePassphrase;
	private String keystoreFilePath;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public PKIXCertificate[] getTrustedCertificates() {
		try {
			byte[] keystoreContents = FileUtils.getFileContents(getKeystoreFilePath());

			return KeyStoreUtils.getTrustedCertificates(keystoreContents, getKeystoreType(), getKeystorePassphrase());

		} catch (IOException | CryptoCertificateException e) {
			log.warn("Unable to load keystore.", e);
		}
		return new PKIXCertificate[0];
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

	public String getKeystoreType() {
		return keystoreType;
	}

	public void setKeystoreType(String keystoreType) {
		this.keystoreType = keystoreType;
	}

	public String getKeystoreAlias() {
		return keystoreAlias;
	}

	public void setKeystoreAlias(String keystoreAlias) {
		this.keystoreAlias = keystoreAlias;
	}

	public String getKeystorePassphrase() {
		return keystorePassphrase;
	}

	public void setKeystorePassphrase(String keystorePassphrase) {
		this.keystorePassphrase = keystorePassphrase;
	}

	public String getKeystoreFilePath() {
		return keystoreFilePath;
	}

	public void setKeystoreFilePath(String keystoreFilePath) {
		this.keystoreFilePath = keystoreFilePath;
	}

}
