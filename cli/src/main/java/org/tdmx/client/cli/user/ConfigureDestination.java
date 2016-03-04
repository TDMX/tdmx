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
package org.tdmx.client.cli.user;

import java.io.PrintStream;

import org.tdmx.client.cli.ClientCliLoggingUtils;
import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.cli.ClientCliUtils.DestinationDescriptor;
import org.tdmx.client.crypto.scheme.IntegratedCryptoScheme;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.lang.StringUtils;

@Cli(name = "destination:configure", description = "configures how a destination user receives from a service.", note = "Unset parameters will remain unchanged.")
public class ConfigureDestination implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	private static final String DEFAULT_SCHEME = "pf_ecdh384-aes256/aes256";
	private static final String DEFAULT_SESSION_DURATION_HOURS = "24";
	private static final String DEFAULT_SESSION_RETENTION_DAYS = "2";

	// TODO <user>-<service>.sks file is a session keystore with the sessionStorePassword protection
	// where the alias is the encryptedContextId , the public X509 certificate contains the public agreement key, and
	// the private key is the private part of the agreement key.

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "destination", required = true, description = "the destination address. Format: <localname>@<domain>#<service>")
	private String destination;

	@Parameter(name = "encryptionScheme", defaultValue = DEFAULT_SCHEME, description = "the encryption scheme name. Use encryption:list to list out the known encryption schemes.")
	private String encryptionScheme;

	@Parameter(name = "dataDir", defaultValueText = "<current working directory>", description = "the directory in which received files are stored.")
	private String dataDirectory;

	@Parameter(name = "sessionDurationInHours", defaultValueText = DEFAULT_SESSION_DURATION_HOURS, description = "the duration of the destination sessions validity in hours.")
	private Integer durationSessionInHours;

	@Parameter(name = "sessionRetentionInDays", defaultValueText = DEFAULT_SESSION_RETENTION_DAYS, description = "the duration of the session retention in days.")
	private Integer durationRetentionDays;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		ClientCliUtils.checkValidDestination(destination);

		boolean created = false;
		DestinationDescriptor rd = null;
		if (ClientCliUtils.destinationDescriptorExists(destination)) {
			rd = ClientCliUtils.loadDestinationDescriptor(destination);
		} else {
			rd = new DestinationDescriptor();

			if (!StringUtils.hasText(encryptionScheme)) {
				encryptionScheme = DEFAULT_SCHEME;
			}
			if (!StringUtils.hasText(dataDirectory)) {
				dataDirectory = ".";
			}
			if (durationSessionInHours == null) {
				durationSessionInHours = Integer.valueOf(DEFAULT_SESSION_DURATION_HOURS);
			}
			if (durationRetentionDays == null) {
				durationRetentionDays = Integer.valueOf(DEFAULT_SESSION_RETENTION_DAYS);
			}
			created = true;
		}

		// -------------------------------------------------------------------------
		// CLI FUNCTION
		// -------------------------------------------------------------------------

		if (StringUtils.hasText(encryptionScheme)) {
			IntegratedCryptoScheme es = IntegratedCryptoScheme.fromName(encryptionScheme);
			if (es == null) {
				out.println("Invalid encryptionScheme. Use encryption:search to determine a valid scheme name.");
				return;
			}
			rd.setEncryptionScheme(es);
		}

		if (StringUtils.hasText(dataDirectory)) {
			rd.setDataDirectory(dataDirectory);
		}

		if (durationSessionInHours != null) {
			rd.setSessionDurationInHours(durationSessionInHours);
		}
		if (durationRetentionDays != null) {
			rd.setSessionRetentionInDays(durationSessionInHours);
		}

		ClientCliUtils.storeDestinationDescriptor(rd, destination);

		out.println("destination descriptor file " + ClientCliUtils.getDestinationDescriptorFilename(destination)
				+ " was " + (created ? "created." : "modified."));
		out.println(ClientCliLoggingUtils.toString(rd));

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
