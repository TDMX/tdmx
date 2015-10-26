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
package org.tdmx.client.cli.command.certificate;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.DomainAdministrationCredentialSpecifier;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.annotation.Result;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.core.system.lang.FileUtils;

@Cli(name = "certificate:domainadmin:create", description = "creates credentials of a domain administrator (DAC) in a keystore. The keystore filename is <domain>-<serialNumber>.dac, with the public certificate in the file <domain>-<serialNumber>.dac.crt.", note = "There may be many DACs for each domain, differentiated by their serialNumbers. The ZAC keystore file needs to be present in the working directory.")
public class CreateDomainAdministratorCommand implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "domain", required = true, description = "the domain name.")
	private String domain;
	@Parameter(name = "name", defaultValueText="<existing>+1", description = "the domain administrator's certificate serialNumber.")
	private Integer serialNumber;
	@Parameter(name = "password", required = true, description = "the domain administrator's keystore password.")
	private String password;

	
	@Parameter(name = "validityInYears", defaultValue="2", description = "the validity of the domain administrator's credential in years.")
	private int validityInYears;
	
	@Parameter(name = "zacPassword", required = true, description = "the zone administrator's keystore password.")
	private String zacPassword;

	@Result(name = "certificate", description = "the domain administrator's X509 public certificate" )
	private String certificate;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run() {
		PKIXCredential zac = getZAC(zacPassword);
		
		Calendar today = CalendarUtils.getDate(new Date());
		Calendar future = CalendarUtils.getDate(new Date());
		future.add(Calendar.YEAR, validityInYears);
		
		DomainAdministrationCredentialSpecifier req = new DomainAdministrationCredentialSpecifier(domain, zac);

		req.setNotBefore(today);
		req.setNotAfter(future);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_384_RSA);
		// the domain might be a sub-domain (prefix) which would have been extended 
		req.setSerialNumber(getNextSerialNumber(req.getDomainName()));
		
		int serial = getNextSerialNumber(req.getDomainName());
		if ( serialNumber != null ) {
			if ( serialNumber < serial ) {
				throw new IllegalArgumentException("serialNumber must be greater or equal to " + serial);
			}
			serial = serialNumber;
		}
		
		try {
			PKIXCredential dac = CredentialUtils.createDomainAdministratorCredential(req);
			
			PKIXCertificate publicCertificate = dac.getPublicCert();

			// save the keystore protected with the password
			byte[] ks = KeyStoreUtils.saveKeyStore(dac, "jks", password, "zac");
			FileUtils.storeFileContents(createKeystoreFilename(publicCertificate.getTdmxDomainName(), serial), ks, ".tmp");
			
			// save the public key separately alongside the keystore
			byte[] pc = publicCertificate.getX509Encoded();
			FileUtils.storeFileContents(createPublicCertificateFilename(publicCertificate.getTdmxDomainName(), serial), pc, ".tmp");

			// output the public key to the console
			certificate = CertificateIOUtils.safeX509certsToPem(new PKIXCertificate[]{publicCertificate});
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

	private int getNextSerialNumber(String domainName) {
		List<File> dacFiles = FileUtils.getFilesMatchingPattern(".", "^"+domainName+"-.*.dac.crt$");
		int maxSerial = 0;
		Pattern dacCertPattern = Pattern.compile("^"+domainName+"-(\\d+).*.dac.crt$");
		for( File dacCert : dacFiles ) {
			Matcher m = dacCertPattern.matcher(dacCert.getName());
			if ( m.matches() ) {
				String serialString = m.group(1);
				int serial = Integer.parseInt(serialString);
				if ( serial > maxSerial ) {
					maxSerial = serial;
				}
			}
		}
		return maxSerial+1;
	}
	
	private PKIXCredential getZAC(String zacPassword) {
		List<File> zacFiles = FileUtils.getFilesMatchingPattern(".", ".*.zac");
		if ( zacFiles.isEmpty()) {
			throw new IllegalStateException("No ZAC file exists. Expected filename <zone>.zac in the working directory.");
		}
		if ( zacFiles.size() != 1 ) {
			throw new IllegalStateException("Found more than one ZAC keystore file. Expected only one filename <zone>.zac in the working directory.");
		}
		
		byte[] zacContents;
		try {
			zacContents = FileUtils.getFileContents(zacFiles.get(0).getPath());
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read ZAC keystore file " + zacFiles.get(0).getPath() + ". "+ e.getMessage() , e);
		}
		PKIXCredential zac;
		try {
			zac = KeyStoreUtils.getPrivateCredential(zacContents, "jks", zacPassword, "zac");
		} catch (CryptoCertificateException e) {
			throw new IllegalStateException("Unable to access ZAC credential. " + e.getMessage(), e);
		}
		return zac;
	}
	
	private String createKeystoreFilename( String domain, int serialNumber ) {
		return domain+"-"+serialNumber+".dac";
	}
	
	private String createPublicCertificateFilename( String domain, int serialNumber ) {
		return domain+"-"+serialNumber+".dac.crt";
	}
	
	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
