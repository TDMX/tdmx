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

import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.cli.ClientCliUtils.ZoneTrustStore;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.lang.StringUtils;

@Cli(name = "distrust:delete", description = "Delete certificates from the zone's distrusted certificate store file - distrusted.store", note = "This doesn't result in trust in the certificate - it just becomes untrusted.")
public class DeleteDistrust implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "fingerprint", required = true, description = "the SHA2 fingerprint of a certificate to remove.")
	private String fingerprint;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		ZoneTrustStore trusted = ClientCliUtils.loadDistrustedCertificates();

		TrustStoreEntry foundEntry = null;
		for (TrustStoreEntry entry : trusted.getCertificates()) {

			if (StringUtils.hasText(fingerprint)) {
				foundEntry = entry;
				break;
			}
		}
		if (foundEntry != null) {
			trusted.remove(foundEntry);
			ClientCliUtils.saveDistrustedCertificates(trusted);
			out.println("Distrusted certificate removed.");
		} else {
			out.println("Certificate not found.");
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
