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

import java.io.PrintStream;

import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.scs.GetZASSession;
import org.tdmx.core.api.v01.scs.GetZASSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;

@Cli(name = "domain:create", description = "creates a domain at the service provider.")
public class CreateDomain implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "domain", required = true, description = "the domain name.")
	private String domain;

	@Parameter(name = "zacPassword", required = true, description = "the zone administrator's keystore password.")
	private String zacPassword;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		TdmxZoneRecord domainInfo = ClientCliUtils.getSystemDnsInfo(domain);
		if (domainInfo == null) {
			out.println("No TDMX DNS TXT record found for " + domain);
			return;
		}
		out.println("Domain info: " + domainInfo);

		PKIXCredential zac = ClientCliUtils.getZAC(zacPassword);

		PKIXCertificate scsPublicCertificate = null; // TODO
		SCS scs = ClientCliUtils.createSCSClient(zac, domainInfo.getScsUrl(), scsPublicCertificate);

		GetZASSession sessionRequest = new GetZASSession();
		GetZASSessionResponse sessionResponse = scs.getZASSession(sessionRequest);

		out.println("session: " + sessionResponse);
		// out.println("zone=" + zone);
		// out.println("version=" + version);
		// out.println("scsUrl=" + scsUrl);

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
