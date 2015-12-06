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

import java.io.IOException;
import java.io.PrintStream;

import org.tdmx.client.cli.ClientCliLoggingUtils;
import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.msg.Administratorsignature;
import org.tdmx.core.api.v01.msg.ChannelAuthorizationFilter;
import org.tdmx.core.api.v01.msg.ChannelDestinationFilter;
import org.tdmx.core.api.v01.msg.ChannelEndpointFilter;
import org.tdmx.core.api.v01.msg.Channelinfo;
import org.tdmx.core.api.v01.scs.GetZASSession;
import org.tdmx.core.api.v01.scs.GetZASSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;
import org.tdmx.core.system.lang.FileUtils;

@Cli(name = "certificate:collect", description = "collects zone administration certificates known to the domain at the service provider.", note = "Zone administration certificates with undecided trust have the filename <zone>.undecided.crt. The ZACs are fetched by traversing all channel authorizations of the domain.")
public class CollectCertificate implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "domain", required = true, description = "the domain name.")
	private String domain;

	@Parameter(name = "serial", defaultValueText = "<greatest existing DAC serial>", description = "the domain administrator's certificate serialNumber.")
	private Integer serialNumber;

	@Parameter(name = "password", required = true, description = "the domain administrator's keystore password.")
	private String dacPassword;

	@Parameter(name = "scsTrustedCertFile", defaultValue = ClientCliUtils.TRUSTED_SCS_CERT, description = "the SCS server's trusted root certificate filename. Use scs:download to fetch it.")
	private String scsTrustedCertFile;

	private int batchsize = 100;
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
		if (serialNumber == null) {
			serialNumber = ClientCliUtils.getDACMaxSerialNumber(domain);
		}
		PKIXCredential dac = ClientCliUtils.getDAC(domain, serialNumber, dacPassword);

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
		// CLI FUNCTION
		// -------------------------------------------------------------------------

		String ownZoneApex = dac.getPublicCert().getTdmxZoneInfo().getZoneRoot();
		// TODO skip own zone
		// TODO load all "trusted", "untrusted" and "undefined" via UTILS.

		int iteration = 0;
		boolean again = false;
		do {
			org.tdmx.core.api.v01.zas.SearchChannel searchChannelRequest = new org.tdmx.core.api.v01.zas.SearchChannel();
			Page p = new Page();
			p.setNumber(iteration);
			p.setSize(batchsize);
			searchChannelRequest.setPage(p);

			ChannelAuthorizationFilter caf = new ChannelAuthorizationFilter();
			caf.setDomain(domain);
			caf.setDestination(new ChannelDestinationFilter());
			caf.setOrigin(new ChannelEndpointFilter());
			searchChannelRequest.setFilter(caf);

			searchChannelRequest.setSessionId(sessionResponse.getSession().getSessionId());

			org.tdmx.core.api.v01.zas.SearchChannelResponse searchChannelResponse = zas
					.searchChannel(searchChannelRequest);
			if (searchChannelResponse.isSuccess()) {
				for (Channelinfo channel : searchChannelResponse.getChannelinfos()) {
					// we extract the zone root cert from all the authorization signatures
					if (channel.getChannelauthorization().getCurrent().getOriginPermission() != null) {
						processSigner(out, channel.getChannelauthorization().getCurrent().getOriginPermission()
								.getAdministratorsignature());
					}
					if (channel.getChannelauthorization().getCurrent().getDestinationPermission() != null) {
						processSigner(out, channel.getChannelauthorization().getCurrent().getDestinationPermission()
								.getAdministratorsignature());
					}
					if (channel.getChannelauthorization().getUnconfirmed().getOriginPermission() != null) {
						processSigner(out, channel.getChannelauthorization().getUnconfirmed().getOriginPermission()
								.getAdministratorsignature());
					}
					if (channel.getChannelauthorization().getUnconfirmed().getDestinationPermission() != null) {
						processSigner(out, channel.getChannelauthorization().getUnconfirmed().getDestinationPermission()
								.getAdministratorsignature());
					}
				}
				again = searchChannelResponse.getChannelinfos().size() == batchsize;
			} else {
				ClientCliUtils.logError(out, searchChannelResponse.getError());
				again = false;
			}

		} while (again);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
	private void processSigner(PrintStream out, Administratorsignature signer) {
		if (signer == null || signer.getAdministratorIdentity() == null
				|| signer.getAdministratorIdentity().getRootcertificate() == null) {
			return;
		}
		byte[] rootCert = signer.getAdministratorIdentity().getRootcertificate();
		PKIXCertificate pk = CertificateIOUtils.safeDecodeX509(rootCert);
		if (pk == null) {
			handleCertificate("unknown", "" + System.currentTimeMillis(), "error", rootCert);
			return;
		}
		String zoneApex = pk.getTdmxZoneInfo().getZoneRoot();
		String fingerprint = pk.getFingerprint();
		out.println("Discovered " + ClientCliLoggingUtils.toString(pk));
		handleCertificate(zoneApex, fingerprint, "undefined", rootCert);
	}

	private void handleCertificate(String zoneApex, String fingerprint, String suffix, byte[] bytes) {
		try {
			FileUtils.storeFileContents(zoneApex + "-" + fingerprint + "." + suffix + ".crt", bytes, ".tmp");
		} catch (IOException e) {
			throw new IllegalStateException("Unable to save certificate.", e);
		}
	}
	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
