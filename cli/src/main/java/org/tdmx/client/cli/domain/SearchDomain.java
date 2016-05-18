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
import org.tdmx.core.api.v01.msg.DomainFilter;
import org.tdmx.core.api.v01.scs.GetZASSession;
import org.tdmx.core.api.v01.scs.GetZASSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.runtime.CommandExecutable;

@Cli(name = "domain:search", description = "searches for domains at the service provider.")
public class SearchDomain implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "zacPassword", required = true, description = "the zone administrator's keystore password.")
	private String zacPassword;

	@Parameter(name = "scsTrustedCertFile", defaultValue = ClientCliUtils.TRUSTED_SCS_CERT, description = "the SCS server's trusted root certificate filename. Use scs:download to fetch it.")
	private String scsTrustedCertFile;

	@Parameter(name = "domain", description = "the domain name to search for. Leave blank to list all domains of a zone.")
	private String domain;

	@Parameter(name = "pageNumber", defaultValue = "0", description = "the result page number.")
	private int pageNumber;

	@Parameter(name = "pageSize", defaultValue = "10", description = "the result page size.")
	private int pageSize;
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

		PKIXCredential zac = ClientCliUtils.getZAC(zacPassword);

		// -------------------------------------------------------------------------
		// GET ZAS SESSION
		// -------------------------------------------------------------------------

		PKIXCertificate scsPublicCertificate = ClientCliUtils.loadSCSTrustedCertificate(scsTrustedCertFile);
		SCS scs = ClientCliUtils.createSCSClient(zac, zd.getScsUrl(), scsPublicCertificate);

		GetZASSession sessionRequest = new GetZASSession();
		GetZASSessionResponse sessionResponse = scs.getZASSession(sessionRequest);
		if (!sessionResponse.isSuccess()) {
			out.println("Unable to get ZAS session. ", sessionResponse.getError());
			return;
		}
		out.println("ZAS sessionId: " + sessionResponse.getSession().getSessionId());

		ZAS zas = ClientCliUtils.createZASClient(zac, sessionResponse.getEndpoint());

		// -------------------------------------------------------------------------
		// CLI FUNCTION
		// -------------------------------------------------------------------------

		org.tdmx.core.api.v01.zas.SearchDomain searchDomainRequest = new org.tdmx.core.api.v01.zas.SearchDomain();
		Page p = new Page();
		p.setNumber(pageNumber);
		p.setSize(pageSize);
		searchDomainRequest.setPage(p);
		DomainFilter df = new DomainFilter();
		df.setDomain(domain);
		searchDomainRequest.setFilter(df);
		searchDomainRequest.setSessionId(sessionResponse.getSession().getSessionId());

		org.tdmx.core.api.v01.zas.SearchDomainResponse searchDomainResponse = zas.searchDomain(searchDomainRequest);
		if (searchDomainResponse.isSuccess()) {
			out.println("Found " + searchDomainResponse.getDomains().size() + " domains.");
			for (String domain : searchDomainResponse.getDomains()) {
				out.println(domain);
			}
			if (searchDomainResponse.getDomains().size() == pageSize) {
				out.println(ClientCliLoggingUtils.truncatedMessage());
			}

		} else {
			out.println("Unable to search domains. ", searchDomainResponse.getError());
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
