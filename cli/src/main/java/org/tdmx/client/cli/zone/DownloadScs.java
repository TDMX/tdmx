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
package org.tdmx.client.cli.zone;

import java.io.PrintStream;

import org.tdmx.client.adapter.SslProbeService.ConnectionTestResult;
import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.cli.ClientCliUtils.ZoneDescriptor;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.lang.StringUtils;

@Cli(name = "scs:download", description = "Download and save SCS server's public certificate - in the scs.crt file.")
public class DownloadScs implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "zacPassword", required = true, description = "the zone administrator's keystore password.")
	private String zacPassword;
	@Parameter(name = "fingerprint", defaultValueText = "the root certificate's fingerprint", description = "the fingerprint of the certificate in the servers public certificate chain.")
	private String fingerprint;
	@Parameter(name = "scsTrustedCertFile", defaultValue = ClientCliUtils.TRUSTED_SCS_CERT, description = "the SCS server's trusted root certificate filename. Use scs:download to fetch it.")
	private String scsTrustedCertFile;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		ZoneDescriptor zd = ClientCliUtils.loadZoneDescriptor();

		if (zd.getScsUrl() == null) {
			out.println("Missing SCS URL. Use modify:zone to set the SessionControlServer's URL.");
			return;
		}

		PKIXCredential zac = ClientCliUtils.getZAC(zacPassword);

		ConnectionTestResult ctr = ClientCliUtils.sslTest(zac, zd.getScsUrl(), null);
		out.println("Step: " + ctr.getTestStep());
		out.println("Remote IPAddress: " + ctr.getRemoteIpAddress());
		if (ctr.getServerCertChain() != null) {
			out.println("Certificate chain length: " + ctr.getServerCertChain().length);
			PKIXCertificate trustedCert = null;
			if (StringUtils.hasText(fingerprint)) {
				for (PKIXCertificate cert : ctr.getServerCertChain()) {
					if (fingerprint.equals(cert.getFingerprint())) {
						trustedCert = cert;
					}
				}
			} else {
				trustedCert = ctr.getServerCertChain()[ctr.getServerCertChain().length - 1]; // the root cert
			}
			if (trustedCert != null) {
				ClientCliUtils.storeSCSTrustedCertificate(scsTrustedCertFile, trustedCert);
				out.println("Trusted certificate stored in file " + scsTrustedCertFile);
			} else {
				out.println("Trusted certificate not found.");
			}
		} else {
			out.println("No certificates fetched from the SCS url.");
			if (ctr.getException() != null) {
				out.println("Connection exception: " + ctr.getException());
			} else {
				out.println("Connection established successfully.");
			}
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
