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
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.msg.UserFilter;
import org.tdmx.core.api.v01.msg.UserIdentity;
import org.tdmx.core.api.v01.scs.GetZASSession;
import org.tdmx.core.api.v01.scs.GetZASSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;

@Cli(name = "user:deactivate", description = "deactivates a user at the service provider.")
public class DeactivateUserCredentials implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "username", required = true, description = "the user's local name. Format: <localname>@<domain>")
	private String username;

	@Parameter(name = "userSerial", defaultValueText = "<greatest existing User serial>", description = "the user's certificate serialNumber.")
	private Integer userSerialNumber;

	@Parameter(name = "dacSerial", defaultValueText = "<greatest existing DAC serial>", description = "the domain administrator's certificate serialNumber.")
	private Integer dacSerialNumber;

	@Parameter(name = "dacPassword", required = true, description = "the domain administrator's keystore password.")
	private String dacPassword;

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
		ClientCliUtils.checkValidUserName(username);
		String domainName = ClientCliUtils.getDomainName(username);
		String localName = ClientCliUtils.getLocalName(username);

		TdmxZoneRecord domainInfo = ClientCliUtils.getSystemDnsInfo(domainName);
		if (domainInfo == null) {
			out.println("No TDMX DNS TXT record found for " + domainName);
			return;
		}
		out.println("Domain info: " + domainInfo);

		ClientCliUtils.createDomainDirectory(domainName);

		int dacSerial = ClientCliUtils.getDACMaxSerialNumber(domainName);
		if (dacSerialNumber != null) {
			dacSerial = dacSerialNumber;
		}

		// get the DAC keystore
		PKIXCredential dac = ClientCliUtils.getDAC(domainName, dacSerial, dacPassword);

		int ucSerial = ClientCliUtils.getUCMaxSerialNumber(domainName, localName);
		if (userSerialNumber != null) {
			ucSerial = userSerialNumber;
		}

		PKIXCertificate userCertificate = ClientCliUtils.getUCPublicKey(domainName, localName, ucSerial);

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
		// CLI FUNCTION
		// -------------------------------------------------------------------------

		UserIdentity id = new UserIdentity();
		id.setUsercertificate(userCertificate.getX509Encoded());
		id.setDomaincertificate(dac.getPublicCert().getX509Encoded());
		id.setRootcertificate(dac.getZoneRootPublicCert().getX509Encoded());

		org.tdmx.core.api.v01.zas.SearchUser searchUserRequest = new org.tdmx.core.api.v01.zas.SearchUser();
		Page p = new Page();
		p.setNumber(0);
		p.setSize(1);
		searchUserRequest.setPage(p);
		UserFilter df = new UserFilter();
		df.setUserIdentity(id);
		searchUserRequest.setFilter(df);
		searchUserRequest.setSessionId(sessionResponse.getSession().getSessionId());

		org.tdmx.core.api.v01.zas.SearchUserResponse searchUserResponse = zas.searchUser(searchUserRequest);
		if (searchUserResponse.isSuccess() && !searchUserResponse.getUsers().isEmpty()) {

			org.tdmx.core.api.v01.zas.DeleteUser deleteUserRequest = new org.tdmx.core.api.v01.zas.DeleteUser();
			deleteUserRequest.setUserIdentity(id);
			deleteUserRequest.setSessionId(sessionResponse.getSession().getSessionId());

			org.tdmx.core.api.v01.zas.DeleteUserResponse deleteUserResponse = zas.deleteUser(deleteUserRequest);
			if (deleteUserResponse.isSuccess()) {
				out.println("User " + username + " with fingerprint " + userCertificate.getFingerprint() + " deleted.");
			} else {
				ClientCliUtils.logError(out, deleteUserResponse.getError());
			}

		} else {
			out.println(
					"User " + username + " with fingerprint " + userCertificate.getFingerprint() + " was not found.");
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
