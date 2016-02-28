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

import org.tdmx.client.cli.ClientCliLoggingUtils;
import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.msg.ChannelAuthorizationFilter;
import org.tdmx.core.api.v01.msg.ChannelDestinationFilter;
import org.tdmx.core.api.v01.msg.ChannelEndpointFilter;
import org.tdmx.core.api.v01.msg.Channelinfo;
import org.tdmx.core.api.v01.scs.GetZASSession;
import org.tdmx.core.api.v01.scs.GetZASSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Option;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;

@Cli(name = "channel:search", description = "searches for channel in a domain at the service provider.")
public class SearchChannel implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "from", description = "the address at the source endpoint of the channel.")
	private String from;

	@Parameter(name = "to", description = "the address at the source endpoint of the channel.")
	private String to;

	@Option(name = "unconfirmed", description = "fetch only channels with pending authorizations.")
	private Boolean unconfirmed;

	@Parameter(name = "domain", required = true, description = "the domain name.")
	private String domain;

	@Parameter(name = "dacSerial", defaultValueText = "<greatest existing DAC serial>", description = "the domain administrator's certificate dacSerialNumber.")
	private Integer dacSerialNumber;

	@Parameter(name = "dacPassword", required = true, description = "the domain administrator's keystore password.")
	private String dacPassword;

	@Parameter(name = "scsTrustedCertFile", defaultValue = ClientCliUtils.TRUSTED_SCS_CERT, description = "the SCS server's trusted root certificate filename. Use scs:download to fetch it.")
	private String scsTrustedCertFile;

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
	public void run(PrintStream out) {
		TdmxZoneRecord domainInfo = ClientCliUtils.getSystemDnsInfo(domain);
		if (domainInfo == null) {
			out.println("No TDMX DNS TXT record found for " + domain);
			return;
		}
		out.println("Domain info: " + domainInfo);

		// get the DAC keystore
		if (dacSerialNumber == null) {
			dacSerialNumber = ClientCliUtils.getDACMaxSerialNumber(domain);
		}
		PKIXCredential dac = ClientCliUtils.getDAC(domain, dacSerialNumber, dacPassword);

		// -------------------------------------------------------------------------
		// GET ZAS SESSION
		// -------------------------------------------------------------------------

		PKIXCertificate scsPublicCertificate = ClientCliUtils.loadSCSTrustedCertificate(scsTrustedCertFile);
		SCS scs = ClientCliUtils.createSCSClient(dac, domainInfo.getScsUrl(), scsPublicCertificate);

		GetZASSession sessionRequest = new GetZASSession();
		GetZASSessionResponse sessionResponse = scs.getZASSession(sessionRequest);
		if (!sessionResponse.isSuccess()) {
			out.println("Unable to get ZAS session.");
			ClientCliUtils.logError(out, sessionResponse.getError());
			return;
		}
		out.println("ZAS sessionId: " + sessionResponse.getSession().getSessionId());

		ZAS zas = ClientCliUtils.createZASClient(dac, sessionResponse.getEndpoint());

		// -------------------------------------------------------------------------
		// Validation
		// -------------------------------------------------------------------------
		String fromLocalName = ClientCliUtils.getLocalName(from);
		String fromDomain = ClientCliUtils.getDomainName(from);
		String toLocalName = ClientCliUtils.getLocalName(to);
		String toDomain = ClientCliUtils.getDomainName(to);
		String toService = ClientCliUtils.getServiceName(to);

		// -------------------------------------------------------------------------
		// CLI FUNCTION
		// -------------------------------------------------------------------------
		org.tdmx.core.api.v01.zas.SearchChannel searchChannelRequest = new org.tdmx.core.api.v01.zas.SearchChannel();
		Page p = new Page();
		p.setNumber(pageNumber);
		p.setSize(pageSize);
		searchChannelRequest.setPage(p);

		ChannelDestinationFilter cdf = new ChannelDestinationFilter();
		cdf.setLocalname(toLocalName);
		cdf.setDomain(toDomain);
		cdf.setServicename(toService);

		ChannelEndpointFilter cef = new ChannelEndpointFilter();
		cef.setDomain(fromDomain);
		cef.setLocalname(fromLocalName);

		ChannelAuthorizationFilter caf = new ChannelAuthorizationFilter();
		caf.setDomain(domain);
		caf.setDestination(cdf);
		caf.setOrigin(cef);
		caf.setUnconfirmedFlag(unconfirmed);
		searchChannelRequest.setFilter(caf);

		searchChannelRequest.setSessionId(sessionResponse.getSession().getSessionId());

		org.tdmx.core.api.v01.zas.SearchChannelResponse searchChannelResponse = zas.searchChannel(searchChannelRequest);
		if (searchChannelResponse.isSuccess()) {
			out.println("Found " + searchChannelResponse.getChannelinfos().size() + " channels.");
			for (Channelinfo channel : searchChannelResponse.getChannelinfos()) {
				out.println(ClientCliLoggingUtils.toString(channel));
			}
			if (searchChannelResponse.getChannelinfos().size() == pageSize) {
				out.println(ClientCliLoggingUtils.truncatedMessage());
			}
		} else {
			ClientCliUtils.logError(out, searchChannelResponse.getError());
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
