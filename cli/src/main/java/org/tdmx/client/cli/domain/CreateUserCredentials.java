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
import org.tdmx.client.crypto.certificate.UserCredentialSpecifier;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.core.system.lang.FileUtils;

@Cli(name = "user:create", description = "creates credentials of a user (UC) in a keystore. The keystore filename is <domain>/<localname>-<userSerial>.uc, with the public certificate in the file <domain>/<localname>-<serialNumber>.dac.crt.", note = "There may be many UCs for each user, differentiated by their increasing serialNumbers. The DAC keystore file needs to be present in the working directory.")
public class CreateUserCredentials implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "username", required = true, description = "the user's local name. Format: <localname>@<domain>")
	private String username;

	@Parameter(name = "userSerial", defaultValueText = "<greatest existing UC serial>+1", description = "the user credential's certificate serialNumber.")
	private Integer serialNumber;
	@Parameter(name = "userPassword", required = true, masked = true, description = "the user credential's keystore password.")
	private String userPassword;

	@Parameter(name = "validityInDays", defaultValue = "365", description = "the validity of the user's credential in days.")
	private int validityInDays;

	@Parameter(name = "dacSerial", defaultValueText = "<greatest existing DAC serial>", description = "the domain administrator's certificate serialNumber.")
	private Integer dacSerialNumber;
	@Parameter(name = "dacPassword", required = true, masked = true, description = "the domain administrator's keystore password.")
	private String dacPassword;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(CliPrinter out) {
		ClientCliUtils.checkValidUserName(username);
		String domainName = ClientCliUtils.getDomainName(username);
		String localName = ClientCliUtils.getLocalName(username);

		ClientCliUtils.createDomainDirectory(domainName);

		int dacSerial = ClientCliUtils.getDACMaxSerialNumber(domainName);
		if (dacSerialNumber != null) {
			dacSerial = dacSerialNumber;
		}

		PKIXCredential dac = ClientCliUtils.getDAC(domainName, dacSerial, dacPassword);

		Calendar today = CalendarUtils.getDate(new Date());
		Calendar future = CalendarUtils.getDate(new Date());
		future.add(Calendar.DATE, validityInDays);

		UserCredentialSpecifier req = new UserCredentialSpecifier();
		req.setName(localName);
		req.setDomainAdministratorCredential(dac);

		req.setNotBefore(today);
		req.setNotAfter(future);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_384_RSA);

		int serial = ClientCliUtils.getUCMaxSerialNumber(domainName, localName) + 1;
		if (serialNumber != null) {
			if (serialNumber < serial) {
				throw new IllegalArgumentException("serialNumber must be greater or equal to " + serial);
			}
			serial = serialNumber;
		}

		try {
			PKIXCredential uc = CredentialUtils.createUserCredential(req);

			PKIXCertificate publicCertificate = uc.getPublicCert();

			// save the keystore protected with the password
			byte[] ks = KeyStoreUtils.saveKeyStore(uc, ClientCliUtils.KEYSTORE_TYPE, userPassword,
					ClientCliUtils.ALIAS_UC);
			FileUtils.storeFileContents(ClientCliUtils.getUCKeystoreFilename(publicCertificate.getTdmxDomainName(),
					publicCertificate.getCommonName(), serial), ks, ".tmp");

			// save the public key separately alongside the keystore
			byte[] pc = publicCertificate.getX509Encoded();
			FileUtils.storeFileContents(ClientCliUtils.getUCPublicCertificateFilename(
					publicCertificate.getTdmxDomainName(), publicCertificate.getCommonName(), serial), pc, ".tmp");

			// output the public key to the console
			out.println("fingerprint=" + publicCertificate.getFingerprint());
			out.println("certificate="
					+ CertificateIOUtils.safeX509certsToPem(new PKIXCertificate[] { publicCertificate }));
			out.println("serialNumber=" + serial);
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
