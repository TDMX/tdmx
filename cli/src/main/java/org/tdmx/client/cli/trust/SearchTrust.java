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
package org.tdmx.client.cli.trust;

import java.io.PrintStream;

import org.tdmx.client.cli.ClientCliLoggingUtils;
import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.cli.ClientCliUtils.ZoneTrustStore;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.lang.StringUtils;

@Cli(name = "trust:search", description = "Search for certificates in the zone's truststore file - trusted.store")
public class SearchTrust implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "fingerprint", description = "the SHA2 fingerprint of a certificate.")
	private String fingerprint;
	@Parameter(name = "domain", description = "find certificates which can be a parent to the domain.")
	private String domain;
	@Parameter(name = "text", description = "find any certificate which matches this text.")
	private String text;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		ZoneTrustStore trusted = ClientCliUtils.loadTrustedCertificates();

		boolean noCriteria = !StringUtils.hasText(fingerprint) && !StringUtils.hasText(domain)
				&& !StringUtils.hasText(text);

		int numMatches = 0;
		int totalEntries = 0;
		for (TrustStoreEntry entry : trusted.getCertificates()) {
			totalEntries++;
			boolean match = noCriteria; // if we have no criteria we match all!

			if (StringUtils.hasText(fingerprint)) {
				match = fingerprint.equalsIgnoreCase(entry.getCertificate().getFingerprint());
			}
			if (StringUtils.hasText(domain)) {
				match = domain.equalsIgnoreCase(entry.getCertificate().getTdmxDomainName());
			}
			if (StringUtils.hasText(text)) {
				String cert = entry.getCertificate().toString().toLowerCase();
				match = cert.contains(text.toLowerCase());
			}
			if (match) {
				out.println(ClientCliLoggingUtils.toString(entry));
			}
		}
		out.println("Found " + numMatches + "/" + totalEntries + " trusted certificates.");

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
