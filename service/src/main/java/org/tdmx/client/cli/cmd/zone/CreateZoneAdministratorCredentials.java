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
package org.tdmx.client.cli.cmd.zone;

import java.util.Calendar;
import java.util.Date;

import org.tdmx.client.cli.cmd.AbstractCliCommand;
import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.system.lang.CalendarUtils;

@Cli(name = "zoneadmin:create", description = "creates credentials of a zone administrator (ZAC) in a keystore. The keystore filename is <zone>.zac, with the public certificate in the file <zone>.zac.crt.", note = "There may only be one ZAC at any one time.")
public class CreateZoneAdministratorCredentials extends AbstractCliCommand {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "zone", required = true, description = "the zone apex.")
	private String zone;

	@Parameter(name = "name", required = true, description = "the zone administrator's full name.")
	private String name;
	@Parameter(name = "email", required = true, description = "the zone administrator's email address.")
	private String email;
	@Parameter(name = "telephone", required = true, description = "the zone administrator's telephone number.")
	private String telephone;
	@Parameter(name = "location", required = true, description = "the zone administrator's company location.")
	private String location;
	@Parameter(name = "country", required = true, description = "the zone administrator's company country.")
	private String country;
	@Parameter(name = "department", required = true, description = "the zone administrator's department.")
	private String department;
	@Parameter(name = "organization", required = true, description = "the zone administrator's company.")
	private String organization;

	@Parameter(name = "validityInYears", defaultValue = "10", description = "the validity of the zone administrator's credential in years.")
	private int validityInYears;

	@Parameter(name = "zacPassword", required = true, masked = true, description = "the zone administrator's keystore password.")
	private String zacPassword;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		// TODO not logged-in.

		boolean exists = getZacService().existsZAC(zone);

		Calendar today = CalendarUtils.getDate(new Date());
		Calendar future = CalendarUtils.getDate(new Date());
		future.add(Calendar.YEAR, validityInYears);

		ZoneAdministrationCredentialSpecifier req = new ZoneAdministrationCredentialSpecifier(1, zone);

		req.setCn(name);
		req.setTelephoneNumber(telephone);
		req.setEmailAddress(email);
		req.setOrgUnit(department);
		req.setOrg(organization);
		req.setLocation(location);
		req.setCountry(country);
		req.setNotBefore(today);
		req.setNotAfter(future);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA4096);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_384_RSA);

		PKIXCredential zac;
		try {
			zac = CredentialUtils.createZoneAdministratorCredential(req);
			getZacService().storeZAC(zac, zone, zacPassword);

			PKIXCertificate publicCertificate = zac.getPublicCert();

			// output the public key to the console
			out.println("zone=" + zone);
			out.println("certificate="
					+ CertificateIOUtils.safeX509certsToPem(new PKIXCertificate[] { publicCertificate }));
			out.println("fingerprint=" + publicCertificate.getFingerprint());
		} catch (CryptoCertificateException e) {
			throw new IllegalStateException(e);
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
