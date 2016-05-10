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

import java.io.PrintStream;
import java.util.List;

import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Option;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.lib.control.domain.TrustStatus;
import org.tdmx.server.cli.cmd.AbstractCliCommand;
import org.tdmx.server.rs.sas.resource.SSLCertificateResource;

@Cli(name = "sslcertificate:modify", description = "modifies the trust or comment on a SSL certificate", note = "An SSL certificate can be trusted or distrusted.")
public class ModifySSLCertificate extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "fingerprint", required = true, description = "the fingerprint value of the certificate to delete.")
	private String fingerprint;

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
		List<SSLCertificateResource> existingCerts = getSas().searchSSLCertificate(0, 1, fingerprint, null);
		if (existingCerts.size() != 1) {
			out.println("SSL certificate " + fingerprint + " not found.");
			return;
		}
		SSLCertificateResource cert = existingCerts.get(0);
		cert.setTrust(EnumUtils.mapToString(distrust ? TrustStatus.DISTRUSTED : TrustStatus.TRUSTED));
		cert.setComment(comment);

		SSLCertificateResource updatedCert = getSas().updateSSLCertificate(cert.getId(), cert);
		getPrinter().output(out, updatedCert);
		out.println("Modified");
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
