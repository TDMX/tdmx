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
package org.tdmx.client.cli.domain;

import org.tdmx.client.cli.ClientCliLoggingUtils;
import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.cli.ClientCliUtils.ZoneDescriptor;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.msg.AdministratorFilter;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.CredentialStatus;
import org.tdmx.core.api.v01.scs.GetZASSession;
import org.tdmx.core.api.v01.scs.GetZASSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;

@Cli(name = "domainadmin:suspend", description = "suspends the domain administrator credential at the service provider. The keystore filename is <domain>-<dacSerialNumber>.dac, with the public certificate in the file <domain>-<dacSerialNumber>.dac.crt.", note = "There may be many DACs for each domain, differentiated by their serialNumbers. The ZAC keystore file needs to be present in the working directory.")
public class SuspendDomainAdministratorCredentials implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "domain", required = true, description = "the domain name.")
	private String domain;

	@Parameter(name = "dacSerial", defaultValueText = "<greatest existing DAC serial>", description = "the domain administrator's certificate dacSerialNumber.")
	private Integer dacSerialNumber;

	@Parameter(name = "zacPassword", required = true, description = "the zone administrator's keystore password.")
	private String zacPassword;

	@Parameter(name = "scsTrustedCertFile", defaultValue = ClientCliUtils.TRUSTED_SCS_CERT, description = "the SCS server's trusted root certificate filename. Use scs:download to fetch it.")
	private String scsTrustedCertFile;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		ZoneDescriptor zd = ClientCliUtils.loadZoneDescriptor();

		if (zd.getScsUrl() == null) {
			out.println("Missing SCS URL. Use modify:zone to set the SessionControlServer's URL.");
			return;
		}

		TdmxZoneRecord domainInfo = ClientCliUtils.getSystemDnsInfo(domain);
		if (domainInfo == null) {
			out.println("No TDMX DNS TXT record found for " + domain);
			return;
		}
		out.println("Domain info: " + domainInfo);
		if (!zd.getScsUrl().equals(domainInfo.getScsUrl())) {
			out.println("SCS url mismatch DNS=" + domainInfo.getScsUrl() + " local zone descriptor " + zd.getScsUrl());
			return;
		}

		PKIXCredential zac = ClientCliUtils.getZAC(zacPassword);

		if (dacSerialNumber == null) {
			dacSerialNumber = ClientCliUtils.getDACMaxSerialNumber(domain);
			if (dacSerialNumber <= 0) {
				out.println("Unable to find max dacSerialNumber for DACs of " + domain);
				return;
			}
		}
		PKIXCertificate dac = ClientCliUtils.getDACPublicKey(domain, dacSerialNumber);
		if (dac == null) {
			out.println("Unable to locate DAC public certificate for domain " + domain + " and dacSerialNumber "
					+ dacSerialNumber);
			return;
		}

		// -------------------------------------------------------------------------
		// GET ZAS SESSION
		// -------------------------------------------------------------------------

		PKIXCertificate scsPublicCertificate = ClientCliUtils.loadSCSTrustedCertificate(scsTrustedCertFile);
		SCS scs = ClientCliUtils.createSCSClient(zac, domainInfo.getScsUrl(), scsPublicCertificate);

		GetZASSession sessionRequest = new GetZASSession();
		GetZASSessionResponse sessionResponse = scs.getZASSession(sessionRequest);
		if (!sessionResponse.isSuccess()) {
			out.println("Unable to get ZAS session.");
			ClientCliLoggingUtils.logError(out, sessionResponse.getError());
			return;
		}
		out.println("ZAS sessionId: " + sessionResponse.getSession().getSessionId());

		ZAS zas = ClientCliUtils.createZASClient(zac, sessionResponse.getEndpoint());

		// -------------------------------------------------------------------------
		// CLI FUNCTION
		// -------------------------------------------------------------------------

		AdministratorIdentity id = new AdministratorIdentity();
		id.setDomaincertificate(dac.getX509Encoded());
		id.setRootcertificate(zac.getZoneRootPublicCert().getX509Encoded());

		org.tdmx.core.api.v01.zas.SearchAdministrator searchAdminRequest = new org.tdmx.core.api.v01.zas.SearchAdministrator();
		Page p = new Page();
		p.setNumber(0);
		p.setSize(1);
		searchAdminRequest.setPage(p);
		AdministratorFilter df = new AdministratorFilter();
		df.setAdministratorIdentity(id);
		searchAdminRequest.setFilter(df);
		searchAdminRequest.setSessionId(sessionResponse.getSession().getSessionId());

		org.tdmx.core.api.v01.zas.SearchAdministratorResponse searchAdminResponse = zas
				.searchAdministrator(searchAdminRequest);
		if (searchAdminResponse.isSuccess() && !searchAdminResponse.getAdministrators().isEmpty()) {

			org.tdmx.core.api.v01.zas.ModifyAdministrator modifyAdminRequest = new org.tdmx.core.api.v01.zas.ModifyAdministrator();
			modifyAdminRequest.setAdministratorIdentity(id);
			modifyAdminRequest.setStatus(CredentialStatus.SUSPENDED);
			modifyAdminRequest.setSessionId(sessionResponse.getSession().getSessionId());

			org.tdmx.core.api.v01.zas.ModifyAdministratorResponse modifyAdminResponse = zas
					.modifyAdministrator(modifyAdminRequest);
			if (modifyAdminResponse.isSuccess()) {
				out.println("Administrator for domain " + domain + " with fingerprint " + dac.getFingerprint()
						+ " suspended.");
			} else {
				ClientCliLoggingUtils.logError(out, modifyAdminResponse.getError());
			}

		} else {
			out.println("Administrator for domain " + domain + " with fingerprint " + dac.getFingerprint()
					+ " was not found.");
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
