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

import java.util.HashMap;
import java.util.Map;

import org.tdmx.client.cli.ClientCliLoggingUtils;
import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.cli.ClientCliUtils.ZoneTrustStore;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;
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
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;

@Cli(name = "untrust:collect", description = "collects untrusted zone administration certificates known to the domain at the service provider.", note = "After collecting untrusted certificates, use trust:add or distrust:add to allow channel authorization.")
public class CollectUntrust implements CommandExecutable {

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

	@Parameter(name = "dacPassword", required = true, description = "the domain administrator's keystore password.")
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
	public void run(CliPrinter out) {
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
			ClientCliLoggingUtils.logError(out, sessionResponse.getError());
			return;
		}
		out.println("ZAS sessionId: " + sessionResponse.getSession().getSessionId());

		ZAS zas = ClientCliUtils.createZASClient(dac, sessionResponse.getEndpoint());

		// -------------------------------------------------------------------------
		// CLI FUNCTION
		// -------------------------------------------------------------------------

		Map<String, PKIXCertificate> authCertMap = new HashMap<>();

		int totalUnconfirmedChannels = 0;
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
			caf.setUnconfirmedFlag(true);
			caf.setDestination(new ChannelDestinationFilter());
			caf.setOrigin(new ChannelEndpointFilter());
			searchChannelRequest.setFilter(caf);

			searchChannelRequest.setSessionId(sessionResponse.getSession().getSessionId());

			org.tdmx.core.api.v01.zas.SearchChannelResponse searchChannelResponse = zas
					.searchChannel(searchChannelRequest);
			if (searchChannelResponse.isSuccess()) {
				totalUnconfirmedChannels += searchChannelResponse.getChannelinfos().size();

				for (Channelinfo channel : searchChannelResponse.getChannelinfos()) {
					PKIXCertificate cert = null;
					// we extract the zone root cert from all the authorization signatures
					if (channel.getChannelauthorization().getCurrent().getOriginPermission() != null) {
						cert = processSigner(channel.getChannelauthorization().getCurrent().getOriginPermission()
								.getAdministratorsignature());
					}
					if (channel.getChannelauthorization().getCurrent().getDestinationPermission() != null) {
						cert = processSigner(channel.getChannelauthorization().getCurrent().getDestinationPermission()
								.getAdministratorsignature());
					}
					if (channel.getChannelauthorization().getUnconfirmed().getOriginPermission() != null) {
						cert = processSigner(channel.getChannelauthorization().getUnconfirmed().getOriginPermission()
								.getAdministratorsignature());
					}
					if (channel.getChannelauthorization().getUnconfirmed().getDestinationPermission() != null) {
						cert = processSigner(channel.getChannelauthorization().getUnconfirmed()
								.getDestinationPermission().getAdministratorsignature());
					}
					if (cert != null) {
						authCertMap.put(cert.getFingerprint(), cert);
					} else {
						ClientCliLoggingUtils.log(out, "Unable to process certificate for ",
								ClientCliLoggingUtils.toLog(channel));
					}
				}
				again = searchChannelResponse.getChannelinfos().size() == batchsize;
			} else {
				ClientCliLoggingUtils.logError(out, searchChannelResponse.getError());
				again = false;
			}

		} while (again);

		// add any as yet unknown certificates to the untrusted store
		ZoneTrustStore trusted = ClientCliUtils.loadTrustedCertificates();
		ZoneTrustStore distrusted = ClientCliUtils.loadDistrustedCertificates();
		ZoneTrustStore untrusted = ClientCliUtils.loadUntrustedCertificates();

		boolean changed = false;
		for (PKIXCertificate cert : authCertMap.values()) {
			if (dac.getZoneRootPublicCert().isIdentical(cert)) {
				// skip own zone admin's certificate
				continue;
			}
			if (!trusted.contains(cert) && !distrusted.contains(cert)) {
				// add to untrusted store, friendly name and comment are not managed in the untrusted store.
				out.println("Added untrusted certificate: " + ClientCliLoggingUtils.toString(cert));
				TrustStoreEntry entry = new TrustStoreEntry(cert);
				untrusted.add(entry);
				changed = true;
			}
		}
		if (changed) {
			ClientCliUtils.saveUntrustedCertificates(untrusted);
		} else {
			out.println("No new untrusted certificates found. Checked " + totalUnconfirmedChannels
					+ " channels with unconfirmed permissions.");
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private PKIXCertificate processSigner(Administratorsignature signer) {
		if (signer == null || signer.getAdministratorIdentity() == null
				|| signer.getAdministratorIdentity().getRootcertificate() == null) {
			return null;
		}
		byte[] rootCert = signer.getAdministratorIdentity().getRootcertificate();
		return CertificateIOUtils.safeDecodeX509(rootCert);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
