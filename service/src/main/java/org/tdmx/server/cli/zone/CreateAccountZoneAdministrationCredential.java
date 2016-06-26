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
package org.tdmx.server.cli.zone;

import java.io.IOException;
import java.util.List;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.server.cli.cmd.AbstractCliCommand;
import org.tdmx.server.rs.sas.resource.AccountResource;
import org.tdmx.server.rs.sas.resource.AccountZoneAdministrationCredentialCheckResult;
import org.tdmx.server.rs.sas.resource.AccountZoneAdministrationCredentialResource;
import org.tdmx.server.rs.sas.resource.AccountZoneResource;

@Cli(name = "zoneadmin:create", description = "creates a zone administration credential for an account.", note = "The ZAC is installed asynchronously - check the status.")
public class CreateAccountZoneAdministrationCredential extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "account", required = true, description = "the account identifier.")
	private String accountId;

	@Parameter(name = "zone", required = true, description = "the zone apex.")
	private String zone;

	@Parameter(name = "pemText", description = "the zone's administration credential in PEM format.")
	private String pemText;

	@Parameter(name = "pemFile", description = "the ZAC file in PEM format, alternative to pemText.")
	private String pemFile;

	@Parameter(name = "x509File", description = "the ZAC file in X509(DER) format, alternative to pemText/File.")
	private String x509File;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		String certificatePem = pemText;
		try {
			if (StringUtils.hasText(x509File)) {
				byte[] x509Contents;
				x509Contents = FileUtils.getFileContents(x509File);
				if (x509Contents == null) {
					out.println("No x509File found " + x509File);
					return;
				}
				certificatePem = CertificateIOUtils.safeX509certsToPem(x509Contents);
			} else if (StringUtils.hasText(pemFile)) {
				byte[] pemContents = FileUtils.getFileContents(pemFile);
				if (pemContents == null) {
					out.println("No pemFile found " + pemFile);
					return;
				}
				certificatePem = new String(pemContents);
			}
		} catch (IOException e) {
			// not a simple file not found - but reading failed somehow.
			throw new IllegalStateException(e);
		}
		if (!StringUtils.hasText(certificatePem)) {
			out.println("No certificate provided - use one of pemText, pemFile, or x509File parameters.");
			return;
		}

		List<AccountResource> accounts = getSas().searchAccount(0, 1, null, accountId);
		if (accounts.isEmpty()) {
			out.println("Account " + accountId + " not found.");
			return;
		}
		AccountResource account = accounts.get(0);

		List<AccountZoneResource> accountZones = getSas().searchAccountZone(0, 1, account.getAccountId(), zone, null,
				null, null);
		if (accountZones.isEmpty()) {
			out.println("Account zone " + zone + " not found.");
			return;
		}
		AccountZoneResource azr = accountZones.get(0);

		// check the ZAC before creation
		AccountZoneAdministrationCredentialCheckResult checkResult = getSas()
				.checkAccountZoneAdministrationCredential(certificatePem);
		if (StringUtils.hasText(checkResult.getStatus())) {
			out.println("ZAC check failed.", checkResult);
			return;
		}
		AccountZoneAdministrationCredentialResource newZAC = getSas()
				.createAccountZoneAdministrationCredential(account.getId(), azr.getId(), certificatePem);
		out.println(newZAC);
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
