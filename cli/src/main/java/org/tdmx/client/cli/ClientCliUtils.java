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
package org.tdmx.client.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.core.system.lang.NetUtils;
import org.tdmx.core.system.lang.StringUtils;

/**
 * Utilities for all Client CLI commands.
 * 
 * @author Peter
 *
 */
public class ClientCliUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	public static final String ZONE_DESCRIPTOR = "zone.tdmx";

	public static final String KEYSTORE_TYPE = "jks";

	public static final String ALIAS_ZAC = "zac";
	public static final String ALIAS_DAC = "dac";
	public static final String ALIAS_UC = "uc";

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	private ClientCliUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - Zone Descriptor
	// -------------------------------------------------------------------------

	public static boolean zoneDescriptorExists() {
		List<File> zacFiles = FileUtils.getFilesMatchingPattern(".", ZONE_DESCRIPTOR);
		return !zacFiles.isEmpty();
	}

	public static void checkZoneDescriptorExists() {
		if (!zoneDescriptorExists()) {
			throw new IllegalStateException(
					"Zone descriptor file " + ZONE_DESCRIPTOR + " not found. Use zone:create command to create one.");
		}
	}

	public static void checkZoneDescriptorNotExists() {
		if (zoneDescriptorExists()) {
			throw new IllegalStateException("Zone descriptor file " + ZONE_DESCRIPTOR + " exists.");
		}
	}

	public static void deleteZoneDescriptor() {
		checkZoneDescriptorExists();
		try {
			FileUtils.deleteFile(ZONE_DESCRIPTOR);
		} catch (IOException e) {
			throw new IllegalStateException("Zone descriptor file " + ZONE_DESCRIPTOR + " cannot be deleted.", e);
		}
	}

	public static ZoneDescriptor loadZoneDescriptor() {
		checkZoneDescriptorExists();
		Properties p = new Properties();
		try (FileInputStream fis = new FileInputStream(ZONE_DESCRIPTOR)) {
			p.load(fis);
		} catch (IOException e) {
			throw new IllegalStateException("Zone descriptor file " + ZONE_DESCRIPTOR + " cannot be loaded.", e);
		}
		String zoneApex = p.getProperty(ZoneDescriptor.ZONE_APEX_PROPERTY);
		if (!StringUtils.hasText(zoneApex)) {
			throw new IllegalStateException(
					"Zone descriptor file missing property " + ZoneDescriptor.ZONE_APEX_PROPERTY);
		}
		String version = p.getProperty(ZoneDescriptor.TDMX_VERSION_PROPERTY);
		if (!StringUtils.hasText(version)) {
			throw new IllegalStateException(
					"Zone descriptor file missing property " + ZoneDescriptor.TDMX_VERSION_PROPERTY);
		}

		int v = 0;
		try {
			v = Integer.parseInt(version);
		} catch (NumberFormatException nfe) {
			throw new IllegalStateException("Invalid TDMX version property " + ZoneDescriptor.ZONE_APEX_PROPERTY, nfe);
		}

		ZoneDescriptor zd = new ZoneDescriptor(zoneApex, v);

		String scsUrl = p.getProperty(ZoneDescriptor.SCS_URL_PROPERTY);
		zd.setScsUrl(NetUtils.getURL(scsUrl));
		return zd;
	}

	public static void storeZoneDescriptor(ZoneDescriptor zd) {
		Properties p = new Properties();
		p.setProperty(ZoneDescriptor.ZONE_APEX_PROPERTY, zd.getZoneApex());
		p.setProperty(ZoneDescriptor.TDMX_VERSION_PROPERTY, String.format("%d", zd.getVersion()));
		if (zd.getScsUrl() != null) {
			p.setProperty(ZoneDescriptor.SCS_URL_PROPERTY, zd.getScsUrl().toString());
		}

		try (FileWriter fw = new FileWriter(ZONE_DESCRIPTOR)) {
			p.store(fw, "This is a ZoneDescriptor file produced by the zone:create CLI command. Do not edit.");

		} catch (IOException e) {
			throw new IllegalStateException("Unable to save Zone descriptor file " + ZONE_DESCRIPTOR, e);
		}
	}

	public static class ZoneDescriptor {
		public static String ZONE_APEX_PROPERTY = "zoneapex";
		public static String TDMX_VERSION_PROPERTY = "version";
		public static String SCS_URL_PROPERTY = "scsUrl";

		private final String zoneApex;
		private final int version;
		private URL scsUrl;

		public ZoneDescriptor(String zoneApex, int version) {
			this.zoneApex = zoneApex;
			this.version = version;
		}

		public URL getScsUrl() {
			return scsUrl;
		}

		public void setScsUrl(URL scsUrl) {
			this.scsUrl = scsUrl;
		}

		public String getZoneApex() {
			return zoneApex;
		}

		public int getVersion() {
			return version;
		}

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - ZAC
	// -------------------------------------------------------------------------

	public static PKIXCredential getZAC(String zacPassword) {
		List<File> zacFiles = FileUtils.getFilesMatchingPattern(".", ".*.zac");
		if (zacFiles.isEmpty()) {
			throw new IllegalStateException(
					"No ZAC file exists. Expected filename <zone>.zac in the working directory.");
		}
		if (zacFiles.size() != 1) {
			throw new IllegalStateException(
					"Found more than one ZAC keystore file. Expected only one filename <zone>.zac in the working directory.");
		}

		byte[] zacContents;
		try {
			zacContents = FileUtils.getFileContents(zacFiles.get(0).getPath());
		} catch (IOException e) {
			throw new IllegalStateException(
					"Unable to read ZAC keystore file " + zacFiles.get(0).getPath() + ". " + e.getMessage(), e);
		}
		PKIXCredential zac;
		try {
			zac = KeyStoreUtils.getPrivateCredential(zacContents, KEYSTORE_TYPE, zacPassword, ALIAS_ZAC);
		} catch (CryptoCertificateException e) {
			throw new IllegalStateException("Unable to access ZAC credential. " + e.getMessage(), e);
		}
		return zac;
	}

	public static boolean zoneZACexists(String zone) {
		List<File> zacFiles = FileUtils.getFilesMatchingPattern(".", createZACKeystoreFilename(zone));
		return !zacFiles.isEmpty();
	}

	public static void checkZACNotExists(String zone) {
		if (zoneZACexists(zone)) {
			throw new IllegalStateException("ZAC file exists. " + createZACKeystoreFilename(zone));
		}
	}

	public static String createZACKeystoreFilename(String zone) {
		return zone + ".zac";
	}

	public static String createZACPublicCertificateFilename(String zone) {
		return zone + ".zac.crt";
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - DAC
	// -------------------------------------------------------------------------

	public static PKIXCredential getDAC(String domain, int serialNumber, String dacPassword) {
		String filename = createDACKeystoreFilename(domain, serialNumber);

		File dacFile = new File(filename);

		if (!dacFile.exists()) {
			throw new IllegalStateException(
					"No DAC file exists. Expected filename " + filename + " in the working directory.");
		}

		byte[] dacContents;
		try {
			dacContents = FileUtils.getFileContents(dacFile.getPath());
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read DAC keystore file " + filename + ". " + e.getMessage(), e);
		}
		PKIXCredential dac;
		try {
			dac = KeyStoreUtils.getPrivateCredential(dacContents, KEYSTORE_TYPE, dacPassword, ALIAS_DAC);
		} catch (CryptoCertificateException e) {
			throw new IllegalStateException("Unable to access DAC credential. " + e.getMessage(), e);
		}
		return dac;
	}

	public static int getDACMaxSerialNumber(String domainName) {
		List<File> dacFiles = FileUtils.getFilesMatchingPattern(".", "^" + domainName + "-.*.dac.crt$");
		int maxSerial = 0;
		Pattern dacCertPattern = Pattern.compile("^" + domainName + "-(\\d+).*.dac.crt$");
		for (File dacCert : dacFiles) {
			Matcher m = dacCertPattern.matcher(dacCert.getName());
			if (m.matches()) {
				String serialString = m.group(1);
				int serial = Integer.parseInt(serialString);
				if (serial > maxSerial) {
					maxSerial = serial;
				}
			}
		}
		return maxSerial;
	}

	public static String createDACKeystoreFilename(String domain, int serialNumber) {
		return domain + "-" + serialNumber + ".dac";
	}

	public static String createDACPublicCertificateFilename(String domain, int serialNumber) {
		return domain + "-" + serialNumber + ".dac.crt";
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - UC
	// -------------------------------------------------------------------------

	public static boolean isValidUserName(String username) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(username);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}

	public static void createDomainDirectory(String domainName) {
		File domainDir = new File(domainName);
		if (domainDir.exists()) {
			if (!domainDir.isDirectory()) {
				throw new IllegalStateException("The file " + domainName + " is not a directory.");
			}
		} else {
			if (!domainDir.mkdir()) {
				throw new IllegalStateException("The directory " + domainName + " can't be created.");
			}
		}
	}

	public static void checkValidUserName(String username) {
		if (!isValidUserName(username)) {
			throw new IllegalStateException("Username " + username + " is not valid (email address).");
		}
	}

	public static String splitDomainName(String username) {
		return username.substring(username.indexOf("@") + 1, username.length());
	}

	public static String splitLocalName(String username) {
		return username.substring(0, username.indexOf("@"));
	}

	public static int getUCMaxSerialNumber(String domainName, String localName) {
		List<File> ucFiles = FileUtils.getFilesMatchingPattern("./" + domainName, "^" + localName + "-.*.uc.crt$");
		int maxSerial = 0;
		Pattern ucCertPattern = Pattern.compile("^" + localName + "-(\\d+).*.uc.crt$");
		for (File ucCert : ucFiles) {
			Matcher m = ucCertPattern.matcher(ucCert.getName());
			if (m.matches()) {
				String serialString = m.group(1);
				int serial = Integer.parseInt(serialString);
				if (serial > maxSerial) {
					maxSerial = serial;
				}
			}
		}
		return maxSerial;
	}

	public static String createUCKeystoreFilename(String domain, String localName, int serialNumber) {
		return domain + "/" + localName + "-" + serialNumber + ".uc";
	}

	public static String createUCPublicCertificateFilename(String domain, String localName, int serialNumber) {
		return domain + "/" + localName + "-" + serialNumber + ".uc.crt";
	}

}
