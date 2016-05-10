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
import java.util.List;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
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

@Cli(name = "sslcertificate:load", description = "loads an SSL certificate.", note = "Updates the trust status and comment of an existing certificate.")
public class LoadSSLCertificate extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "pemText", description = "the zone's administration credential in PEM format.")
	private String pemText;

	@Parameter(name = "pemFile", description = "the ZAC file in PEM format, alternative to pemText.")
	private String pemFile;

	@Parameter(name = "x509File", description = "the ZAC file in X509(DER) format, alternative to pemText/File.")
	private String x509File;

	@Option(name = "distrust", description = "whether to distrust the certificate.")
	private boolean distrust;

	@Parameter(name = "comment", description = "the comment to apply to the certificate entry.")
	private String comment;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		PKIXCertificate cert = null;
		try {
			if (StringUtils.hasText(x509File)) {
				byte[] x509Contents;
				x509Contents = FileUtils.getFileContents(x509File);
				if (x509Contents == null) {
					out.println("No x509File found " + x509File);
					return;
				}
				cert = CertificateIOUtils.safeDecodeX509(x509Contents);
			} else if (StringUtils.hasText(pemFile)) {
				byte[] pemContents = FileUtils.getFileContents(pemFile);
				if (pemContents == null) {
					out.println("No pemFile found " + pemFile);
					return;
				}
				String pem = new String(pemContents);
				PKIXCertificate[] certs = CertificateIOUtils.safePemToX509certs(pem);
				if (certs != null && certs.length == 1) {
					cert = certs[0];
				}
			}
		} catch (IOException e) {
			// not a simple file not found - but reading failed somehow.
			throw new IllegalStateException(e);
		}

		if (cert == null) {
			out.println("No certificate provided - use one of pemText, pemFile, or x509File parameters.");
			return;
		}

		SSLCertificateResource tc = new SSLCertificateResource();
		tc.setTrust(EnumUtils.mapToString(distrust ? TrustStatus.DISTRUSTED : TrustStatus.TRUSTED));
		tc.setPem(CertificateIOUtils.safeX509certsToPem(cert.getX509Encoded()));
		tc.setComment(comment);

		List<SSLCertificateResource> existingCerts = getSas().searchSSLCertificate(0, 1, cert.getFingerprint(), null);
		if (existingCerts.isEmpty()) {
			SSLCertificateResource newTc = getSas().createSSLCertificate(tc);
			getPrinter().output(out, newTc);
			out.println("Added");
		} else {
			tc.setId(existingCerts.get(0).getId());
			SSLCertificateResource updatedTc = getSas().updateSSLCertificate(tc.getId(), tc);
			getPrinter().output(out, updatedTc);
			out.println("Updated");
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
