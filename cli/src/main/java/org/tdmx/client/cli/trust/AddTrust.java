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
import org.tdmx.client.cli.ClientCliUtils.TrustStoreEntrySearchCriteria;
import org.tdmx.client.cli.ClientCliUtils.ZoneTrustStore;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;

@Cli(name = "trust:add", description = "Add untrusted certificates to the zone's trusted certificate store file - trusted.store")
public class AddTrust implements CommandExecutable {

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

	// TODO #93: friendlyName + comment

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		ZoneTrustStore trusted = ClientCliUtils.loadTrustedCertificates();
		ZoneTrustStore untrusted = ClientCliUtils.loadUntrustedCertificates();

		TrustStoreEntrySearchCriteria criteria = new TrustStoreEntrySearchCriteria(fingerprint, domain, text);
		if (!criteria.hasCriteria()) {
			out.println("No matching criteria provided ( fingerprint, domain, text ).");
			return;
		}
		int numMatches = 0;
		int totalEntries = 0;
		TrustStoreEntry matchingEntry = null;
		for (TrustStoreEntry entry : untrusted.getCertificates()) {
			totalEntries++;
			boolean match = !criteria.hasCriteria(); // if we have no criteria we match all!

			match |= ClientCliUtils.matchesTrustedCertificate(entry, criteria);
			if (match) {
				numMatches++;
				matchingEntry = entry;
			}
		}
		if (numMatches == 1) {
			out.println(ClientCliLoggingUtils.toString(matchingEntry));
			if (!trusted.contains(matchingEntry.getCertificate())) {
				trusted.add(matchingEntry);
				ClientCliUtils.saveTrustedCertificates(trusted);
			} else {
				out.println("Already in trust store.");
			}
		} else {
			out.println("Matched " + numMatches + "/" + totalEntries + " untrusted certificates.");
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
