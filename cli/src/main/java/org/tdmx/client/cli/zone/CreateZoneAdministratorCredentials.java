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
package org.tdmx.client.cli.zone;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;

import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.core.system.lang.FileUtils;

@Cli(name = "zoneadmin:create", description = "creates credentials of a zone administrator (ZAC) in a keystore. The keystore filename is <zone>.zac, with the public certificate in the file <zone>.zac.crt.", note = "There may only be one ZAC at any one time.")
public class CreateZoneAdministratorCredentials implements CommandExecutable {

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

	@Parameter(name = "password", required = true, description = "the zone administrator's keystore password.")
	private String password;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		ClientCliUtils.checkZACNotExists(zone);

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

		try {
			PKIXCredential zac = CredentialUtils.createZoneAdministratorCredential(req);

			PKIXCertificate publicCertificate = zac.getPublicCert();

			// save the keystore protected with the password
			byte[] ks = KeyStoreUtils.saveKeyStore(zac, "jks", password, "zac");
			FileUtils.storeFileContents(ClientCliUtils.createZACKeystoreFilename(zone), ks, ".tmp");

			// save the public key separately alongside the keystore
			byte[] pc = publicCertificate.getX509Encoded();
			FileUtils.storeFileContents(ClientCliUtils.createZACPublicCertificateFilename(zone), pc, ".tmp");

			// output the public key to the console
			out.println("certificate="
					+ CertificateIOUtils.safeX509certsToPem(new PKIXCertificate[] { publicCertificate }));
			out.println("fingerprint=" + publicCertificate.getFingerprint());
		} catch (CryptoCertificateException | IOException e) {
			throw new RuntimeException(e);
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
