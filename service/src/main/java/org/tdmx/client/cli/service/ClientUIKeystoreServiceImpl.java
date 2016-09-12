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

package org.tdmx.client.cli.service;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.system.lang.FileUtils;

/**
 * ClientUIKeystore service.
 * 
 * @author Peter Klauser
 * 
 */
public class ClientUIKeystoreServiceImpl implements ClientUIKeystoreService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ClientUIKeystoreServiceImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public PKIXCredential getServerKey(String keystoreFilename, String keystoreType, String keystorePassword,
			String keystoreAlias) {
		byte[] keystoreContents;
		try {
			keystoreContents = FileUtils.getFileContents(keystoreFilename);
		} catch (IOException e) {
			log.warn("Unable to read HTTPS keystore file " + keystoreFilename + ". " + e.getMessage(), e);
			return null;
		}
		PKIXCredential serverKey = null;
		try {
			serverKey = KeyStoreUtils.getPrivateCredential(keystoreContents, keystoreType, keystorePassword,
					keystoreAlias);
		} catch (CryptoCertificateException e) {
			log.warn("Unable to access HTTPS keystore. " + e.getMessage(), e);
		}

		return serverKey;
	}

	@Override
	public boolean saveServerKey(PKIXCredential serverKey, String keystoreFilename, String keystoreType,
			String keystorePassword, String keystoreAlias) {
		try {
			byte[] contents = KeyStoreUtils.saveKeyStore(serverKey, keystoreType, keystorePassword, keystoreAlias);

			FileUtils.storeFileContents(keystoreFilename, contents, ".tmp");
			return true;
		} catch (CryptoCertificateException e) {
			log.warn("Unable to create HTTPS keystore contents.", e);
		} catch (IOException e) {
			log.warn("Unable to save HTTPS keystore.", e);
		}
		return false;
	}

	@Override
	public boolean existsServerKey(String keystoreFilename) {
		File keystoreFile = new File(keystoreFilename);

		return keystoreFile.exists();
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

}
