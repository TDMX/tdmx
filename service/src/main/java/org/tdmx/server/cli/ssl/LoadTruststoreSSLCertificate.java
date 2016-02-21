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
package org.tdmx.server.cli.ssl;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Option;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.TrustStatus;
import org.tdmx.server.cli.cmd.AbstractCliCommand;
import org.tdmx.server.rs.sas.resource.SSLCertificateResource;

@Cli(name = "ssltruststore:load", description = "loads SSL certificates from a truststore.", note = "The trust status and comment of existing SSL certificates will be overwritten.")
public class LoadTruststoreSSLCertificate extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "truststore", required = true, description = "the truststore file.")
	private String truststore;

	@Parameter(name = "storePassword", required = true, description = "the truststore password.")
	private String storePassword;

	@Parameter(name = "storeType", defaultValue = "jks", description = "the truststore type.")
	private String storeType;

	@Option(name = "distrust", description = "whether to distrust the loaded certificates")
	private boolean distrust;

	@Parameter(name = "alias", description = "to load just one specific certificate referenced by it's alias.")
	private String alias;

	@Parameter(name = "comment", description = "the comment to apply to each loaded certificate.")
	private String comment;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {

		try {

			byte[] tsContents = FileUtils.getFileContents(truststore);
			if (tsContents == null) {
				out.println("No truststore file " + truststore);
				return;
			}
			List<PKIXCertificate> certificates = new ArrayList<>();
			if (StringUtils.hasText(alias)) {
				PKIXCertificate cert = KeyStoreUtils.getTrustedCertificate(tsContents, storeType, storePassword, alias);
				if (cert == null) {
					out.println("No certificate with alias " + alias);
					return;
				}
				certificates.add(cert);
			} else {
				PKIXCertificate[] certs = KeyStoreUtils.getTrustedCertificates(tsContents, storeType, storePassword);
				certificates.addAll(Arrays.asList(certs));
			}

			for (PKIXCertificate cert : certificates) {
				List<SSLCertificateResource> existingCerts = getSas().searchSSLCertificate(0, 1, cert.getFingerprint(),
						null);
				if (existingCerts.isEmpty()) {
					SSLCertificateResource tc = new SSLCertificateResource();
					tc.setTrust(EnumUtils.mapToString(distrust ? TrustStatus.DISTRUSTED : TrustStatus.TRUSTED));
					tc.setComment(comment);
					tc.setPem(CertificateIOUtils.x509certsToPem(new PKIXCertificate[] { cert }));
					SSLCertificateResource newTc = getSas().createSSLCertificate(tc);
					out.println("Added " + newTc.getCliRepresentation());
				} else {
					SSLCertificateResource tc = existingCerts.get(0);
					tc.setTrust(EnumUtils.mapToString(distrust ? TrustStatus.DISTRUSTED : TrustStatus.TRUSTED));
					tc.setComment(comment);
					SSLCertificateResource updatedTc = getSas().updateSSLCertificate(tc.getId(), tc);
					out.println("Modified " + updatedTc.getCliRepresentation());
				}
			}

		} catch (IOException | CryptoCertificateException e) {
			// not a simple file not found - but reading failed somehow.
			throw new IllegalStateException(e);
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

}
