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

import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.cli.ClientCliUtils.DestinationDescriptor;
import org.tdmx.client.cli.ClientCliUtils.UnencryptedSessionKey;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.mds.GetDestinationSession;
import org.tdmx.core.api.v01.mds.GetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.SetDestinationSession;
import org.tdmx.core.api.v01.mds.SetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.scs.GetMDSSession;
import org.tdmx.core.api.v01.scs.GetMDSSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;

@Cli(name = "receive:poll", description = "performs a single receive from a destination", note = "Configure a destination first with destination:configure before receive.")
public class PollReceive implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "destination", required = true, description = "the destination address. Format: <localname>@<domain>#<service>")
	private String destination;

	@Parameter(name = "userSerial", defaultValueText = "<greatest existing User serial>", description = "the destination user's certificate serialNumber.")
	private Integer userSerialNumber;

	@Parameter(name = "userPassword", required = true, description = "the destination user's keystore password.")
	private String userPassword;

	@Parameter(name = "scsTrustedCertFile", defaultValue = ClientCliUtils.TRUSTED_SCS_CERT, description = "the SCS server's trusted root certificate filename. Use scs:download to fetch it.")
	private String scsTrustedCertFile;
	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		ClientCliUtils.checkValidDestination(destination);

		String domain = ClientCliUtils.getDomainName(destination);
		String localName = ClientCliUtils.getLocalName(destination);
		String service = ClientCliUtils.getServiceName(destination);

		TdmxZoneRecord domainInfo = ClientCliUtils.getSystemDnsInfo(domain);
		if (domainInfo == null) {
			out.println("No TDMX DNS TXT record found for " + domain);
			return;
		}
		out.println("Domain info: " + domainInfo);

		// -------------------------------------------------------------------------
		// GET RECEIVER CONTEXT
		// -------------------------------------------------------------------------

		int ucSerial = ClientCliUtils.getUCMaxSerialNumber(domain, localName);
		if (userSerialNumber != null) {
			ucSerial = userSerialNumber;
		}
		PKIXCredential uc = ClientCliUtils.getUC(domain, localName, ucSerial, userPassword);

		DestinationDescriptor dd = null;
		if (ClientCliUtils.destinationDescriptorExists(destination)) {
			dd = ClientCliUtils.loadDestinationDescriptor(destination, userPassword);
		} else {
			out.println("Destination not configured yet. Use destination:configure command to initialize it.");
			return;
		}

		// -------------------------------------------------------------------------
		// GET MDS SESSION
		// -------------------------------------------------------------------------

		PKIXCertificate scsPublicCertificate = ClientCliUtils.loadSCSTrustedCertificate(scsTrustedCertFile);
		SCS scs = ClientCliUtils.createSCSClient(uc, domainInfo.getScsUrl(), scsPublicCertificate);

		GetMDSSession sessionRequest = new GetMDSSession();
		sessionRequest.setServicename(service);
		GetMDSSessionResponse sessionResponse = scs.getMDSSession(sessionRequest);
		if (!sessionResponse.isSuccess()) {
			out.println("Unable to get MDS session.");
			ClientCliUtils.logError(out, sessionResponse.getError());
			return;
		}
		out.println("ZAS sessionId: " + sessionResponse.getSession().getSessionId());

		MDS mds = ClientCliUtils.createMDSClient(uc, sessionResponse.getEndpoint());

		// -------------------------------------------------------------------------
		// CLI FUNCTION
		// -------------------------------------------------------------------------

		GetDestinationSession destReq = new GetDestinationSession();
		destReq.setSessionId(sessionResponse.getSession().getSessionId());

		GetDestinationSessionResponse destRes = mds.getDestinationSession(destReq);
		if (!destRes.isSuccess()) {
			out.println("Unable to get current destination session.");
			ClientCliUtils.logError(out, destRes.getError());
			return;
		}
		Destinationsession ds = destRes.getDestination().getDestinationsession();

		boolean uploadNewDs = false;
		if (ds == null) {
			out.println("No current destination session - initialization");
			uploadNewDs = true;
		} else {
			UnencryptedSessionKey sk = dd.getSessionKey(ds);

			if (sk == null) {
				out.println("Current destination session at service provider unknow.");
				// there is a foreign DS
				PKIXCertificate otherUC = CertificateIOUtils
						.safeDecodeX509(ds.getUsersignature().getUserIdentity().getUsercertificate());
				if (uc.getPublicCert().getSerialNumber() >= otherUC.getSerialNumber()) {
					out.println(
							"Re-initialize destination session at service provider since our user is same or more recent.");
					uploadNewDs = true;
				} else {
					out.println(
							"Our serialnumber is not uptodate - destination session at service provider not changed.");
				}

			} else if (sk.getSignerSerial() > uc.getPublicCert().getSerialNumber()) {
				out.println(
						"Current destination session at service provider defined by more recent user - standing down.");
			} else if (sk.isValidityExpired(dd.getSessionDurationInHours())) {
				out.println("Current destination session at service provider expired - renew.");
				uploadNewDs = true;
			}
		}

		if (uploadNewDs) {
			UnencryptedSessionKey sk = dd.createNewSessionKey();

			ds = sk.getNewDestinationSession(uc, service);
			// if we change the DD we must store it immediately.
			ClientCliUtils.storeDestinationDescriptor(dd, destination, userPassword);

			// upload new session to ServiceProvider.
			SetDestinationSession setDestReq = new SetDestinationSession();
			setDestReq.setSessionId(sessionResponse.getSession().getSessionId());
			setDestReq.setDestinationsession(ds);
			SetDestinationSessionResponse setDestRes = mds.setDestinationSession(setDestReq);
			if (!setDestRes.isSuccess()) {
				out.println("Unable to set new current destination session.");
				ClientCliUtils.logError(out, setDestRes.getError());
				return;
			}
			out.println("New session set at the service provider.");
		}

		// TODO #95 do receive!

		// TODO #49 do send.
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
