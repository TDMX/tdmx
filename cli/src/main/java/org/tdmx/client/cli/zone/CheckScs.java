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

@Cli(name = "scs:check", description = "Checks the access to the zone's SCS url and shows SCS server's public certificate information.")
public class CheckScs implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "zacPassword", required = true, description = "the zone administrator's keystore password.")
	private String zacPassword;
	@Parameter(name = "verbose", defaultValue = "false", description = "provide more detailed certificate information.")
	private boolean verbose;

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

		ConnectionTestResult ctr = ClientCliUtils.sslTest(zac, zd.getScsUrl());
		out.println("Step: " + ctr.getTestStep());
		out.println("Remote IPAddress: " + ctr.getRemoteIpAddress());
		if (ctr.getServerCertChain() != null) {
			out.println("Certificate chain length: " + ctr.getServerCertChain().length);
			int idx = 0;
			for (PKIXCertificate cert : ctr.getServerCertChain()) {
				if (verbose) {
					out.println("cert[" + idx + "] " + cert);
				} else {
					out.println("cert[" + idx + "] subject=" + cert.getSubject());
					out.println("cert[" + idx + "] fingerprint=" + cert.getFingerprint());
				}
				idx++;
			}
		}
		if (ctr.getException() != null) {
			out.println("Connection exception: " + ctr.getException());
		} else {
			out.println("Connection established successfully.");
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
