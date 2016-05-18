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

import java.math.BigInteger;
import java.util.Date;

import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelAuthorizationFilter;
import org.tdmx.core.api.v01.msg.ChannelDestination;
import org.tdmx.core.api.v01.msg.ChannelDestinationFilter;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.ChannelEndpointFilter;
import org.tdmx.core.api.v01.msg.Channelinfo;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Grant;
import org.tdmx.core.api.v01.msg.Limit;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.api.v01.msg.RequestedChannelAuthorization;
import org.tdmx.core.api.v01.scs.GetZASSession;
import org.tdmx.core.api.v01.scs.GetZASSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;

@Cli(name = "channel:authorize", description = "authorizes a channel in a domain at the service provider.")
public class AuthorizeChannel implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "from", required = true, description = "the address at the source endpoint of the channel.")
	private String from;

	@Parameter(name = "to", required = true, description = "the address at the source endpoint of the channel.")
	private String to;

	@Parameter(name = "maxSizeMb", defaultValue = "512", description = "the maximum message size sent (plaintext) in MB.")
	private int maxSizeMb;

	@Parameter(name = "highLimitMb", defaultValue = "1024", description = "the flow control high limit im MB.")
	private int highLimitMb;

	@Parameter(name = "lowLimitMb", defaultValue = "512", description = "the flow control low limit im MB.")
	private int lowLimitMb;

	@Parameter(name = "domain", required = true, description = "the domain name.")
	private String domain;

	@Parameter(name = "dacSerial", defaultValueText = "<greatest existing DAC serial>", description = "the domain administrator's certificate dacSerialNumber.")
	private Integer dacSerialNumber;

	@Parameter(name = "dacPassword", required = true, description = "the domain administrator's keystore password.")
	private String dacPassword;

	@Parameter(name = "scsTrustedCertFile", defaultValue = ClientCliUtils.TRUSTED_SCS_CERT, description = "the SCS server's trusted root certificate filename. Use scs:download to fetch it.")
	private String scsTrustedCertFile;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// TODO channel:authorize to confirm other's requested perms

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
			out.println("Unable to get ZAS session. ", sessionResponse.getError());
			return;
		}
		out.println("ZAS sessionId: " + sessionResponse.getSession().getSessionId());

		ZAS zas = ClientCliUtils.createZASClient(dac, sessionResponse.getEndpoint());

		// -------------------------------------------------------------------------
		// Validation
		// -------------------------------------------------------------------------
		ClientCliUtils.checkValidUserName(from);
		ClientCliUtils.checkValidDestination(to);
		String fromLocalName = ClientCliUtils.getLocalName(from);
		String fromDomain = ClientCliUtils.getDomainName(from);
		String toLocalName = ClientCliUtils.getLocalName(to);
		String toDomain = ClientCliUtils.getDomainName(to);
		String toService = ClientCliUtils.getServiceName(to);
		boolean isOrigin = fromDomain.equals(domain);
		boolean isDestination = toDomain.equals(domain);
		if (!isOrigin && !isDestination) {
			out.println("domain must match the from or to's domain.");
			return;
		}

		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setLocalname(fromLocalName);
		origin.setDomain(fromDomain);
		ChannelDestination dest = new ChannelDestination();
		dest.setLocalname(toLocalName);
		dest.setDomain(toDomain);
		dest.setServicename(toService);
		Channel c = new Channel();
		c.setOrigin(origin);
		c.setDestination(dest);

		// -------------------------------------------------------------------------
		// CLI FUNCTION
		// -------------------------------------------------------------------------

		// get the any current pending authorization to see if there is a pending authorization from the other party.
		RequestedChannelAuthorization rca = null;
		// we only create a new authorization if something has changed from before
		Currentchannelauthorization ca = null;

		org.tdmx.core.api.v01.zas.SearchChannel searchChannelRequest = new org.tdmx.core.api.v01.zas.SearchChannel();
		Page p = new Page();
		p.setNumber(0);
		p.setSize(1);
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
		searchChannelRequest.setFilter(caf);
		// we dont filter just unconfirmed because we also need to check if it's not a real change
		// and keep the existing permissions just to change say the flowcontrol limits.
		searchChannelRequest.setSessionId(sessionResponse.getSession().getSessionId());

		org.tdmx.core.api.v01.zas.SearchChannelResponse searchChannelResponse = zas.searchChannel(searchChannelRequest);
		if (searchChannelResponse.isSuccess()) {
			if (!searchChannelResponse.getChannelinfos().isEmpty()) {
				Channelinfo ci = searchChannelResponse.getChannelinfos().get(0);
				rca = ci.getChannelauthorization().getUnconfirmed();
				ca = ci.getChannelauthorization().getCurrent();
			}
		} else {
			out.println("Unable to search channels. ", searchChannelResponse.getError());
		}

		if (rca != null) {
			// TODO #87: we need to check if we "trust" the ZAC of the requested authorization
			out.println("TODO check trust of ", rca);
		}

		// set the new authorization - confirming the requested, trusted other parties auth
		org.tdmx.core.api.v01.zas.SetChannelAuthorization setChannelAuthRequest = new org.tdmx.core.api.v01.zas.SetChannelAuthorization();
		setChannelAuthRequest.setDomain(domain);
		setChannelAuthRequest.setChannel(c);

		// new current authorization
		if (ca == null) {
			ca = new Currentchannelauthorization();
			if (isOrigin) {
				Permission originPermission = new Permission();
				originPermission.setMaxPlaintextSizeBytes(BigInteger.valueOf(maxSizeMb * ClientCliUtils.MEGA));
				originPermission.setPermission(Grant.ALLOW);
				// sign the origination permission
				SignatureUtils.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_384_RSA, new Date(), c,
						originPermission);
				ca.setOriginPermission(originPermission);

				// take over the requested destination permission if there is one.
				if (rca != null && rca.getDestinationPermission() != null) {
					ca.setDestinationPermission(rca.getDestinationPermission());
				}
			}
			if (isDestination) {
				Permission destinationPermission = new Permission();
				destinationPermission.setMaxPlaintextSizeBytes(BigInteger.valueOf(maxSizeMb * ClientCliUtils.MEGA));
				destinationPermission.setPermission(Grant.ALLOW);
				// sign the destination permission
				SignatureUtils.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_384_RSA, new Date(), c,
						destinationPermission);
				ca.setDestinationPermission(destinationPermission);

				// take over the requested origin permission if one is requested
				if (rca != null && rca.getOriginPermission() != null) {
					ca.setOriginPermission(rca.getOriginPermission());
				}
			}
		} else {
			// existing authorization
			if (isOrigin) {
				Permission originPermission = new Permission();
				originPermission.setMaxPlaintextSizeBytes(BigInteger.valueOf(maxSizeMb * ClientCliUtils.MEGA));
				originPermission.setPermission(Grant.ALLOW);

				PKIXCertificate originalSigner = CertificateIOUtils.safeDecodeX509(ca.getOriginPermission()
						.getAdministratorsignature().getAdministratorIdentity().getDomaincertificate());

				// any significant change requiring re-signing the new permission
				if (originalSigner == null || originalSigner.getSerialNumber() != dac.getPublicCert().getSerialNumber()
						|| !originPermission.getMaxPlaintextSizeBytes()
								.equals(ca.getOriginPermission().getMaxPlaintextSizeBytes())
						|| originPermission.getPermission() != ca.getOriginPermission().getPermission()) {

					// sign the replacement origination permission
					SignatureUtils.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_384_RSA, new Date(), c,
							originPermission);
					ca.setOriginPermission(originPermission);
					out.println("Origin permission modified.");

				} else {
					out.println("Origin permission remains unchanged.");
				}

				// take over the requested destination permission if there is one.
				if (rca != null && rca.getDestinationPermission() != null) {
					ca.setDestinationPermission(rca.getDestinationPermission());
					out.println("Confirming requested destination permission.");
				}
			}
			if (isDestination) {
				Permission destinationPermission = new Permission();
				destinationPermission.setMaxPlaintextSizeBytes(BigInteger.valueOf(maxSizeMb * ClientCliUtils.MEGA));
				destinationPermission.setPermission(Grant.ALLOW);
				PKIXCertificate originalSigner = CertificateIOUtils.safeDecodeX509(ca.getDestinationPermission()
						.getAdministratorsignature().getAdministratorIdentity().getDomaincertificate());

				// any significant change requiring re-signing the new permission
				if (originalSigner == null || originalSigner.getSerialNumber() != dac.getPublicCert().getSerialNumber()
						|| !destinationPermission.getMaxPlaintextSizeBytes()
								.equals(ca.getOriginPermission().getMaxPlaintextSizeBytes())
						|| destinationPermission.getPermission() != ca.getOriginPermission().getPermission()) {
					// sign the replacement destination permission
					SignatureUtils.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_384_RSA, new Date(), c,
							destinationPermission);
					ca.setDestinationPermission(destinationPermission);
					out.println("Destination permission modified.");
				} else {
					out.println("Destination permission remains unchanged.");
				}
				// take over the requested origin permission if one is requested
				if (rca != null && rca.getOriginPermission() != null) {
					ca.setOriginPermission(rca.getOriginPermission());
					out.println("Confirming requested origin permission.");
				}
			}

		}

		Limit l = new Limit();
		l.setHighBytes(BigInteger.valueOf(highLimitMb * ClientCliUtils.MEGA));
		l.setLowBytes(BigInteger.valueOf(lowLimitMb * ClientCliUtils.MEGA));
		ca.setLimit(l);
		// sign the CA
		SignatureUtils.createChannelAuthorizationSignature(dac, SignatureAlgorithm.SHA_384_RSA, new Date(), c, ca);
		setChannelAuthRequest.setCurrentchannelauthorization(ca);

		setChannelAuthRequest.setSessionId(sessionResponse.getSession().getSessionId());

		org.tdmx.core.api.v01.zas.SetChannelAuthorizationResponse setChannelAuthResponse = zas
				.setChannelAuthorization(setChannelAuthRequest);
		if (setChannelAuthResponse.isSuccess()) {
			out.println("Authorization ", c, " successful.");
		} else {
			out.println("Authorization ", c, " failed.", setChannelAuthResponse.getError());
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
