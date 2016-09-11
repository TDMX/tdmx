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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.system.lang.FileUtils;

/**
 * ZAC management
 * 
 * @author Peter Klauser
 * 
 */
public class ZoneAdministrationCredentialServiceImpl implements ZoneAdministrationCredentialService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final String KEYSTORE_TYPE = "jks";

	public static final String ALIAS_ZAC = "zac";

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ZoneAdministrationCredentialServiceImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public PKIXCredential getZAC(String zoneApex, String zacPassword) {
		byte[] zacContents;
		try {
			zacContents = FileUtils.getFileContents(createZACKeystoreFilename(zoneApex));
		} catch (IOException e) {
			throw new IllegalStateException(
					"Unable to read ZAC keystore file " + createZACKeystoreFilename(zoneApex) + ". " + e.getMessage(),
					e);
		}
		PKIXCredential zac;
		try {
			zac = KeyStoreUtils.getPrivateCredential(zacContents, KEYSTORE_TYPE, zacPassword, ALIAS_ZAC);
		} catch (CryptoCertificateException e) {
			throw new IllegalStateException("Unable to access ZAC credential. " + e.getMessage(), e);
		}
		return zac;
	}

	@Override
	public PKIXCertificate getZACPublicCertificate(String zoneApex) {
		byte[] zacPKContents;
		try {
			zacPKContents = FileUtils.getFileContents(createZACPublicCertificateFilename(zoneApex));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read ZAC public certificate file "
					+ createZACPublicCertificateFilename(zoneApex) + ". " + e.getMessage(), e);
		}
		PKIXCertificate zac;
		try {
			zac = CertificateIOUtils.decodeX509(zacPKContents);
		} catch (CryptoCertificateException e) {
			throw new IllegalStateException("Unable to access ZAC public key. " + e.getMessage(), e);
		}
		return zac;
	}

	@Override
	public void storeZAC(PKIXCredential zac, String zoneApex, String zacPassword) {
		try {
			PKIXCertificate publicCertificate = zac.getPublicCert();

			// save the keystore protected with the zacPassword
			byte[] ks = KeyStoreUtils.saveKeyStore(zac, "jks", zacPassword, "zac");
			FileUtils.storeFileContents(createZACKeystoreFilename(zoneApex), ks, ".tmp");

			// save the public key separately alongside the keystore
			byte[] pc = publicCertificate.getX509Encoded();
			FileUtils.storeFileContents(createZACPublicCertificateFilename(zoneApex), pc, ".tmp");

			// output the public key to the console
		} catch (IOException | CryptoCertificateException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public boolean existsZAC(String zone) {
		List<File> zacFiles = FileUtils.getFilesMatchingPattern(".", createZACKeystoreFilename(zone));
		return !zacFiles.isEmpty();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private String createZACKeystoreFilename(String zone) {
		return zone + ".zac";
	}

	private String createZACPublicCertificateFilename(String zone) {
		return zone + ".zac.crt";
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
