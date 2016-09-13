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
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.system.lang.FileUtils;

/**
 * ClientUIKeystore service.
 * 
 * @author Peter Klauser
 * 
 */
public class ClientUITruststoreServiceImpl implements ClientUITruststoreService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ClientUITruststoreServiceImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public List<PKIXCertificate> getTrustedCertificates(String truststoreFilename, String truststoreType,
			String truststorePassword) {
		byte[] truststoreContents;
		try {
			truststoreContents = FileUtils.getFileContents(truststoreFilename);
		} catch (IOException e) {
			log.warn("Unable to read HTTPS truststore file " + truststoreFilename + ". " + e.getMessage(), e);
			return Collections.emptyList();
		}
		PKIXCertificate[] trustedRootCerts = null;
		try {
			trustedRootCerts = KeyStoreUtils.getTrustedCertificates(truststoreContents, truststoreType,
					truststorePassword);
		} catch (CryptoCertificateException e) {
			log.warn("Unable to access HTTPS keystore. " + e.getMessage(), e);
			return Collections.emptyList();
		}

		return Arrays.asList(trustedRootCerts);
	}

	@Override
	public boolean existsTruststore(String truststoreFilename) {
		File keystoreFile = new File(truststoreFilename);

		return keystoreFile.exists();
	}

	@Override
	public boolean addTrustedCertificate(PKIXCertificate rootCertificate, String truststoreFilename,
			String truststoreType, String truststorePassword) {

		if (contansTrustedCertificate(rootCertificate, truststoreFilename, truststoreType, truststorePassword)) {
			log.warn("Truststore already contains trusted root certificate with fingerprint "
					+ rootCertificate.getFingerprint());
			return true;
		}

		List<PKIXCertificate> trustedCerts = getTrustedCertificates(truststoreFilename, truststoreType,
				truststorePassword);
		// add our new cert.
		trustedCerts.add(rootCertificate);

		try {
			KeyStore trustStore = KeyStoreUtils.createTrustStore(trustedCerts.toArray(new PKIXCertificate[0]),
					truststoreType);
			byte[] contents = KeyStoreUtils.saveTrustStore(trustStore, truststorePassword);

			FileUtils.storeFileContents(truststoreFilename, contents, ".tmp");
			return true;
		} catch (CryptoCertificateException e) {
			log.warn("Unable to create HTTPS truststore contents.", e);
		} catch (IOException e) {
			log.warn("Unable to save HTTPS truststore.", e);
		}
		return false;
	}

	@Override
	public boolean removeTrustedCertificate(PKIXCertificate rootCertificate, String truststoreFilename,
			String truststoreType, String truststorePassword) {
		if (!contansTrustedCertificate(rootCertificate, truststoreFilename, truststoreType, truststorePassword)) {
			log.warn("Truststore does not contain trusted root certificate with fingerprint "
					+ rootCertificate.getFingerprint());
			return true;
		}

		// strip the certificate to remove out.
		List<PKIXCertificate> trustedCerts = getTrustedCertificates(truststoreFilename, truststoreType,
				truststorePassword);
		List<PKIXCertificate> remainingCerts = new ArrayList<>();
		for (PKIXCertificate root : trustedCerts) {
			if (!root.isIdentical(rootCertificate)) {
				remainingCerts.add(root);
			}
		}

		try {
			KeyStore trustStore = KeyStoreUtils.createTrustStore(remainingCerts.toArray(new PKIXCertificate[0]),
					truststoreType);
			byte[] contents = KeyStoreUtils.saveTrustStore(trustStore, truststorePassword);

			FileUtils.storeFileContents(truststoreFilename, contents, ".tmp");
			return true;
		} catch (CryptoCertificateException e) {
			log.warn("Unable to create HTTPS truststore contents.", e);
		} catch (IOException e) {
			log.warn("Unable to save HTTPS truststore.", e);
		}
		return false;
	}

	@Override
	public boolean contansTrustedCertificate(PKIXCertificate rootCertificate, String truststoreFilename,
			String truststoreType, String truststorePassword) {
		List<PKIXCertificate> trustedCerts = getTrustedCertificates(truststoreFilename, truststoreType,
				truststorePassword);
		for (PKIXCertificate root : trustedCerts) {
			if (root.isIdentical(rootCertificate)) {
				return true;
			}
		}
		return false;
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
