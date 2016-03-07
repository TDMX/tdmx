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
import java.io.PrintStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tdmx.client.adapter.ClientCredentialProvider;
import org.tdmx.client.adapter.ClientKeyManagerFactoryImpl;
import org.tdmx.client.adapter.DelegatingTrustedCertificateProvider;
import org.tdmx.client.adapter.ServerTrustManagerFactoryImpl;
import org.tdmx.client.adapter.SingleTrustedCertificateProvider;
import org.tdmx.client.adapter.SoapClientFactory;
import org.tdmx.client.adapter.SslProbeService;
import org.tdmx.client.adapter.SslProbeService.ConnectionTestResult;
import org.tdmx.client.adapter.SystemDefaultTrustedCertificateProvider;
import org.tdmx.client.adapter.TrustedServerCertificateProvider;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.TrustStoreCertificateIOUtils;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.IntegratedCryptoScheme;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.mos.ws.MOS;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.UserIdentity;
import org.tdmx.core.api.v01.scs.Endpoint;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.api.v01.zas.ws.ZAS;
import org.tdmx.core.system.dns.DnsUtils;
import org.tdmx.core.system.dns.DnsUtils.DnsResultHolder;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;
import org.tdmx.core.system.env.StringEncrypter;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.core.system.lang.NetUtils;
import org.tdmx.core.system.lang.StringUtils;
import org.xbill.DNS.TextParseException;

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
	public static final String ZONE_DESCRIPTOR = "zone.tdmx";
	public static final String TRUSTED_SCS_CERT = "scs.crt";

	public static final String ZONE_TRUST_STORE = "trusted.store";
	public static final String ZONE_DISTRUST_STORE = "distrusted.store";
	public static final String ZONE_UNTRUST_STORE = "untrusted.store";
	public static final String STORE_ENCODING = "UTF-8";

	public static final String KEYSTORE_TYPE = "jks";

	public static final String ALIAS_ZAC = "zac";
	public static final String ALIAS_DAC = "dac";
	public static final String ALIAS_UC = "uc";

	public static final long MEGA = 1024 * 1024;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	//@formatter:off
	private static final String[] STRONG_CIPHERS = new String[] {
			"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
			"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
			"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
			"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
			"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
			"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
	};
	//@formatter:on
	private static final String TLS_VERSION = "TLSv1.2";
	private static final int CONNECTION_TIMEOUT_MS = 10000;
	private static final int READ_TIMEOUT_MS = 60000;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	private ClientCliUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - Representations
	// -------------------------------------------------------------------------

	public static String getUsername(String domain, String localName) {
		return localName + "@" + domain.toLowerCase();
	}

	public static String getDestination(String domain, String localName, String serviceName) {
		return localName + "@" + domain.toLowerCase() + "#" + serviceName;
	}

	public static boolean isValidUserName(String username) {
		String localName = getLocalName(username);
		String domainName = getDomainName(username);
		String serviceName = getServiceName(username);

		return StringUtils.hasText(localName) && StringUtils.hasText(domainName) && !StringUtils.hasText(serviceName);
	}

	public static void checkValidUserName(String username) {
		if (!isValidUserName(username)) {
			throw new IllegalStateException("Username " + username + " is not valid, like <localName>@<domainName>.");
		}
	}

	public static boolean isValidDestination(String destination) {
		String localName = getLocalName(destination);
		String domainName = getDomainName(destination);
		String serviceName = getServiceName(destination);

		return StringUtils.hasText(localName) && StringUtils.hasText(domainName) && StringUtils.hasText(serviceName);
	}

	public static void checkValidDestination(String destination) {
		if (!isValidDestination(destination)) {
			throw new IllegalStateException(
					"Destination " + destination + " is not valid, like <localName>@<domainName>#<serviceName>.");
		}
	}

	/**
	 * Return the localName part of an address or destination.
	 * 
	 * @param fullyQualifiedName
	 *            localname@domainName#serviceName
	 * @return the localName part of an address.
	 */
	public static String getLocalName(String fullyQualifiedName) {
		if (!StringUtils.hasText(fullyQualifiedName)) {
			return null;
		}
		String usernamePart = fullyQualifiedName;
		if (fullyQualifiedName != null && fullyQualifiedName.indexOf("@") != -1
				&& fullyQualifiedName.indexOf("@") == fullyQualifiedName.lastIndexOf("@")) {
			usernamePart = StringUtils
					.nullIfEmpty(fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf("@")));
		}
		if (!StringUtils.hasText(usernamePart)) {
			return null;
		}
		if (usernamePart.indexOf("#") != -1) {
			return null;
		}
		if (usernamePart.indexOf("@") != -1) {
			return null;
		}
		return StringUtils.nullIfEmpty(usernamePart);
	}

	/**
	 * Return the domainName part of an address or destination address.
	 * 
	 * @param fullyQualifiedName
	 *            localname@domainName#serviceName
	 * @return the domainName part of an address, or null if there is none.
	 */
	public static String getDomainName(String fullyQualifiedName) {
		if (fullyQualifiedName != null && fullyQualifiedName.indexOf("@") != -1) {
			String domainName = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf("@") + 1);
			if (domainName.indexOf("#") != -1) {
				return StringUtils.nullIfEmpty(domainName.substring(0, domainName.lastIndexOf("#")));
			}
			return StringUtils.nullIfEmpty(domainName);
		}
		return null;
	}

	/**
	 * Return the serviceName part of a destination address.
	 * 
	 * @param fullyQualifiedName
	 *            localname@domainName#serviceName
	 * @return the serviceName part of a destination address, or null if there is none.
	 */
	public static String getServiceName(String fullyQualifiedName) {
		if (fullyQualifiedName != null && fullyQualifiedName.indexOf("#") != -1) {
			return StringUtils.nullIfEmpty(fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf("#") + 1));
		}
		return null;
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
		String zoneApex = getMandatoryStringProperty(p, ZoneDescriptor.ZONE_APEX_PROPERTY, ZONE_DESCRIPTOR);
		int version = getMandatoryIntProperty(p, ZoneDescriptor.TDMX_VERSION_PROPERTY, ZONE_DESCRIPTOR);

		ZoneDescriptor zd = new ZoneDescriptor(zoneApex, version);

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

	/**
	 * Helper class to store trusted or distrusted Certificates.
	 */
	public static class ZoneTrustStore {
		// map of fingerprint->Certificate
		private final Map<String, TrustStoreEntry> certificateMap = new HashMap<>();

		public ZoneTrustStore(List<TrustStoreEntry> entries) {
			for (TrustStoreEntry entry : entries) {
				add(entry);
			}
		}

		public boolean contains(PKIXCertificate cert) {
			return certificateMap.containsKey(cert.getFingerprint());
		}

		public void add(TrustStoreEntry entry) {
			certificateMap.put(entry.getCertificate().getFingerprint(), entry);
		}

		public void remove(TrustStoreEntry entry) {
			certificateMap.remove(entry.getCertificate().getFingerprint());
		}

		public List<TrustStoreEntry> getCertificates() {
			List<TrustStoreEntry> result = new ArrayList<>();
			result.addAll(certificateMap.values());
			return result;
		}
	}

	public static class TrustStoreEntrySearchCriteria {
		private String fingerprint;
		private String domain;
		private String fullText;

		public TrustStoreEntrySearchCriteria() {
		}

		public TrustStoreEntrySearchCriteria(String fingerprint, String domain, String fullText) {
			this.fingerprint = fingerprint;
			this.domain = domain;
			this.fullText = fullText;
		}

		public boolean hasCriteria() {
			return StringUtils.hasText(fingerprint) || StringUtils.hasText(domain) || StringUtils.hasText(fullText);
		}

		public String getFingerprint() {
			return fingerprint;
		}

		public void setFingerprint(String fingerprint) {
			this.fingerprint = fingerprint;
		}

		public String getDomain() {
			return domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		public String getFullText() {
			return fullText;
		}

		public void setFullText(String fullText) {
			this.fullText = fullText;
		}

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - Trust/Distrust Stores
	// -------------------------------------------------------------------------

	public static boolean matchesTrustedCertificate(TrustStoreEntry entry, TrustStoreEntrySearchCriteria criteria) {
		boolean match = false;
		if (StringUtils.hasText(criteria.getFingerprint())) {
			match |= criteria.getFingerprint().equalsIgnoreCase(entry.getCertificate().getFingerprint());
		}
		if (StringUtils.hasText(criteria.getDomain())) {
			match |= criteria.getDomain().equalsIgnoreCase(entry.getCertificate().getTdmxDomainName());
		}
		if (StringUtils.hasText(criteria.getFullText())) {
			String cert = entry.getCertificate().toString().toLowerCase();
			match |= cert.contains(criteria.getFullText().toLowerCase());

			String comments = entry.getComment().toLowerCase();
			if (StringUtils.hasText(comments)) {
				match |= comments.contains(criteria.getFullText().toLowerCase());
			}

			String fn = entry.getFriendlyName().toLowerCase();
			if (StringUtils.hasText(fn)) {
				match |= fn.contains(criteria.getFullText().toLowerCase());
			}
		}
		return match;
	}

	public static ZoneTrustStore loadTrustedCertificates() {
		return loadStore(ZONE_TRUST_STORE);
	}

	public static ZoneTrustStore loadDistrustedCertificates() {
		return loadStore(ZONE_DISTRUST_STORE);
	}

	public static ZoneTrustStore loadUntrustedCertificates() {
		return loadStore(ZONE_UNTRUST_STORE);
	}

	private static ZoneTrustStore loadStore(String filename) {
		byte[] bytes;
		try {
			bytes = FileUtils.getFileContents(filename);
			if (bytes == null) {
				// 1st time - no file.
				return new ZoneTrustStore(Collections.emptyList());
			}
			String contents = new String(bytes, STORE_ENCODING);

			List<TrustStoreEntry> entries = TrustStoreCertificateIOUtils.pemToTrustStoreEntries(contents);
			return new ZoneTrustStore(entries);
		} catch (CryptoCertificateException | IOException e) {
			throw new IllegalStateException("Unable to load certificte store " + filename, e);
		}
	}

	public static void saveTrustedCertificates(ZoneTrustStore store) {
		saveStore(store, ZONE_TRUST_STORE);
	}

	public static void saveDistrustedCertificates(ZoneTrustStore store) {
		saveStore(store, ZONE_DISTRUST_STORE);
	}

	public static void saveUntrustedCertificates(ZoneTrustStore store) {
		saveStore(store, ZONE_UNTRUST_STORE);
	}

	private static void saveStore(ZoneTrustStore store, String filename) {
		try {
			StringBuilder contents = new StringBuilder();
			for (TrustStoreEntry entry : store.getCertificates()) {
				String entryPem = TrustStoreCertificateIOUtils.trustStoreEntryToPem(entry);
				contents.append(entryPem);
			}
			byte[] bytes = contents.toString().getBytes(STORE_ENCODING);
			FileUtils.storeFileContents(filename, bytes, ".tmp");
		} catch (IOException | CryptoCertificateException e) {
			throw new IllegalStateException("Unable to store certificate store " + filename, e);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - SCS
	// -------------------------------------------------------------------------

	public static void storeSCSTrustedCertificate(String filename, PKIXCertificate trustedCert) {
		try {
			FileUtils.storeFileContents(filename, trustedCert.getX509Encoded(), ".tmp");
		} catch (IOException e) {
			throw new IllegalStateException("Unable to store SCS trusted certificate.", e);
		}
	}

	public static PKIXCertificate loadSCSTrustedCertificate(String filename) {
		byte[] bytes;
		try {
			bytes = FileUtils.getFileContents(filename);
			return CertificateIOUtils.decodeX509(bytes);
		} catch (CryptoCertificateException | IOException e) {
			throw new IllegalStateException("Unable to load trusted SCS certificate file " + filename, e);
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
		String filename = getDACKeystoreFilename(domain, serialNumber);

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

	public static PKIXCertificate getDACPublicKey(String domainName, int serialNumber) {
		List<File> dacFiles = FileUtils.getFilesMatchingPattern(".",
				"^" + domainName + "-" + serialNumber + ".dac.crt$");
		if (!dacFiles.isEmpty()) {
			try {
				byte[] pkixCert = FileUtils.getFileContents(dacFiles.get(0).getPath());
				return CertificateIOUtils.safeDecodeX509(pkixCert);
			} catch (IOException e) {
				throw new IllegalStateException("Unable to read DAC public credential. " + e.getMessage(), e);
			}
		}
		return null;
	}

	public static String getDACKeystoreFilename(String domain, int serialNumber) {
		return domain + "-" + serialNumber + ".dac";
	}

	public static String getDACPublicCertificateFilename(String domain, int serialNumber) {
		return domain + "-" + serialNumber + ".dac.crt";
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - UC
	// -------------------------------------------------------------------------

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

	public static int getUCMaxSerialNumber(String domainName, String localName) {
		List<File> ucFiles = FileUtils.getFilesMatchingPattern(".", "^" + localName + "@" + domainName + "-.*.uc.crt$");
		int maxSerial = 0;
		Pattern ucCertPattern = Pattern.compile("^" + localName + "@" + domainName + "-(\\d+).*.uc.crt$");
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

	public static String getUCKeystoreFilename(String domain, String localName, int serialNumber) {
		return getUsername(domain, localName) + "-" + serialNumber + ".uc";
	}

	public static String getUCPublicCertificateFilename(String domain, String localName, int serialNumber) {
		return getUsername(domain, localName) + "-" + serialNumber + ".uc.crt";
	}

	public static PKIXCertificate getUCPublicKey(String domain, String localName, int serialNumber) {
		String ucFilename = getUCPublicCertificateFilename(domain, localName, serialNumber);
		try {
			byte[] pkixCert = FileUtils.getFileContents(ucFilename);
			if (pkixCert == null) {
				throw new IllegalStateException("Unable to read UC public key from " + ucFilename);
			}
			return CertificateIOUtils.safeDecodeX509(pkixCert);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read UC public credential. " + e.getMessage(), e);
		}
	}

	public static PKIXCredential getUC(String domain, String localName, int serialNumber, String password) {
		String filename = getUCKeystoreFilename(domain, localName, serialNumber);

		File ucFile = new File(filename);

		if (!ucFile.exists()) {
			throw new IllegalStateException(
					"No User keystore file exists. Expected filename " + filename + " in the working directory.");
		}

		byte[] ucContents;
		try {
			ucContents = FileUtils.getFileContents(ucFile.getPath());
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read User keystore file " + filename + ". " + e.getMessage(), e);
		}
		PKIXCredential uc;
		try {
			uc = KeyStoreUtils.getPrivateCredential(ucContents, KEYSTORE_TYPE, password, ALIAS_UC);
		} catch (CryptoCertificateException e) {
			throw new IllegalStateException("Unable to access User credential. " + e.getMessage(), e);
		}
		return uc;
	}

	public static String getDestinationDescriptorFilename(String destination) {
		return destination + ".dst";
	}

	public static boolean destinationDescriptorExists(String destination) {
		List<File> destFiles = FileUtils.getFilesMatchingPattern(".", getDestinationDescriptorFilename(destination));
		return !destFiles.isEmpty();
	}

	public static void checkDestinationDescriptorExists(String destination) {
		if (!destinationDescriptorExists(destination)) {
			throw new IllegalStateException(
					"Destination descriptor file " + getDestinationDescriptorFilename(destination)
							+ " not found. Use destination:configure command to set one up for each destination.");
		}
	}

	public static void checkDestinationDescriptorNotExists(String destination) {
		if (destinationDescriptorExists(destination)) {
			throw new IllegalStateException(
					"Destination descriptor file " + getDestinationDescriptorFilename(destination) + " exists.");
		}
	}

	public static void deleteDestinationDescriptor(String destination) {
		checkDestinationDescriptorExists(destination);
		try {
			FileUtils.deleteFile(getDestinationDescriptorFilename(destination));
		} catch (IOException e) {
			throw new IllegalStateException("Destination descriptor file "
					+ getDestinationDescriptorFilename(destination) + " cannot be deleted.", e);
		}
	}

	/**
	 * Load the DestinationDescriptor file for the destination. We try to unencrypt each session key, but keep any which
	 * we fail to decrypt because they belong to other users encrypted with other user's passwords.
	 * 
	 * @param destination
	 * @param userPassword
	 * @return
	 */
	public static DestinationDescriptor loadDestinationDescriptor(String destination, String userPassword) {
		checkDestinationDescriptorExists(destination);
		String filename = getDestinationDescriptorFilename(destination);

		Properties p = new Properties();
		try (FileInputStream fis = new FileInputStream(filename)) {
			p.load(fis);
		} catch (IOException e) {
			throw new IllegalStateException("Destination descriptor file "
					+ getDestinationDescriptorFilename(destination) + " cannot be loaded.", e);
		}
		String dataDir = getMandatoryStringProperty(p, DestinationDescriptor.DATADIR, filename);
		String salt = getMandatoryStringProperty(p, DestinationDescriptor.SALT, filename);
		String encScheme = getMandatoryStringProperty(p, DestinationDescriptor.ENCRYPTION_SCHEME, filename);
		IntegratedCryptoScheme es = IntegratedCryptoScheme.fromName(encScheme);
		if (es == null) {
			throw new IllegalStateException("Illegal property " + DestinationDescriptor.ENCRYPTION_SCHEME);
		}
		int sd = getMandatoryIntProperty(p, DestinationDescriptor.SESSION_DURATION_HOURS, filename);
		int sr = getMandatoryIntProperty(p, DestinationDescriptor.SESSION_RETENTION_DAYS, filename);

		DestinationDescriptor rd = new DestinationDescriptor();
		rd.setDataDirectory(dataDir);
		rd.setSalt(salt);
		rd.setEncryptionScheme(es);
		rd.setSessionDurationInHours(sd);
		rd.setSessionRetentionInDays(sr);

		// load session keys - decrypting the ones which have been encrypted with the same password as the user
		// provided.
		int idx = 1;
		String sessionProperty = null;
		while ((sessionProperty = p.getProperty(String.format(DestinationDescriptor.SESSION, idx))) != null) {
			UnencryptedSessionKey usk = UnencryptedSessionKey.decrypt(sessionProperty, ByteArray.fromHex(salt),
					userPassword);
			if (usk != null) {
				rd.getSessionKeys().add(usk);
			} else {
				rd.getEncryptedSessionKeys().add(sessionProperty);
			}
			idx++;
		}

		return rd;
	}

	public static void storeDestinationDescriptor(DestinationDescriptor zd, String destination, String userPassword) {
		Properties p = new Properties();
		p.setProperty(DestinationDescriptor.DATADIR, zd.getDataDirectory());
		p.setProperty(DestinationDescriptor.SESSION_DURATION_HOURS,
				String.format("%d", zd.getSessionDurationInHours()));
		p.setProperty(DestinationDescriptor.SESSION_RETENTION_DAYS,
				String.format("%d", zd.getSessionRetentionInDays()));
		p.setProperty(DestinationDescriptor.ENCRYPTION_SCHEME, zd.getEncryptionScheme().getName());
		p.setProperty(DestinationDescriptor.SALT, zd.getSalt());

		// store sessionkeys, the ones we control and then the other's
		int idx = 1;
		for (UnencryptedSessionKey usk : zd.getSessionKeys()) {
			p.setProperty(String.format(DestinationDescriptor.SESSION, idx),
					usk.toEncryptedString(ByteArray.fromHex(zd.getSalt()), userPassword));
			idx++;
		}
		for (String esk : zd.getEncryptedSessionKeys()) {
			p.setProperty(String.format(DestinationDescriptor.SESSION, idx), esk);
			idx++;
		}

		// dump to disk
		try (FileWriter fw = new FileWriter(getDestinationDescriptorFilename(destination))) {
			p.store(fw,
					"This is a destination descriptor file produced by the receive:configure CLI command. Do not edit.");

		} catch (IOException e) {
			throw new IllegalStateException(
					"Unable to save destination descriptor file " + getDestinationDescriptorFilename(destination), e);
		}
	}

	public static class UnencryptedSessionKey {
		private static final String DATE_FORMAT = "yyyyMMddHHmmss";

		private final String encryptionContextId;
		private final IntegratedCryptoScheme scheme;
		private final KeyPair sessionKeyPair;
		private final Date validFrom;

		public UnencryptedSessionKey(IntegratedCryptoScheme scheme, KeyPair sessionKeyPair, Date validFrom) {
			this.scheme = scheme;
			this.sessionKeyPair = sessionKeyPair;
			this.validFrom = validFrom;
			this.encryptionContextId = new SimpleDateFormat(DATE_FORMAT).format(validFrom);
		}

		public static UnencryptedSessionKey decrypt(String encryptedContext, byte[] salt, String password) {
			StringEncrypter dec = new StringEncrypter(salt, password);
			String unencrypted = dec.decrypt(encryptedContext);
			if (!StringUtils.hasText(unencrypted)) {
				return null;
			}
			try {
				List<String> params = StringUtils.convertCsvToStringList(unencrypted);
				Date validFrom = new SimpleDateFormat(DATE_FORMAT).parse(params.get(0));
				IntegratedCryptoScheme ies = IntegratedCryptoScheme.fromName(params.get(1));
				PublicKey publicKey = ies.getSessionKeyAlgorithm()
						.decodeX509EncodedPublicKey(ByteArray.fromHex(params.get(2)));
				PrivateKey privateKey = ies.getSessionKeyAlgorithm()
						.decodeX509EncodedPrivateKey(ByteArray.fromHex(params.get(3)));

				UnencryptedSessionKey sk = new UnencryptedSessionKey(ies, new KeyPair(publicKey, privateKey),
						validFrom);
				return sk;
			} catch (ParseException | CryptoException e) {
				throw new IllegalStateException(e);
			}
		}

		public Destinationsession getNewDestinationSession(PKIXCredential uc, String serviceName) {
			Destinationsession ds = new Destinationsession();
			ds.setEncryptionContextId(encryptionContextId);
			ds.setScheme(scheme.getName());
			try {
				ds.setSessionKey(scheme.getSessionKeyAlgorithm().encodeX509PublicKey(sessionKeyPair.getPublic()));
			} catch (CryptoException e) {
				throw new IllegalStateException(e);
			}

			SignatureUtils.createDestinationSessionSignature(uc, SignatureAlgorithm.SHA_256_RSA, validFrom, serviceName,
					ds);

			return ds;
		}

		/**
		 * Has the session validity expired?
		 * 
		 * @param hours
		 * @return
		 */
		public boolean isValidityExpired(int hours) {
			if (validFrom.getTime() + (hours * 3600 * 1000) /* ms in hour */ < System.currentTimeMillis()) {
				return true;
			}
			return false;
		}

		/**
		 * Has the session retention expired?
		 * 
		 * @param days
		 * @return
		 */
		public boolean isRetentionExpired(int days) {
			if (validFrom.getTime() + (days * 24 * 3600 * 1000) /* ms in day */ < System.currentTimeMillis()) {
				return true;
			}
			return false;
		}

		public String getEncryptionContextId() {
			return encryptionContextId;
		}

		public IntegratedCryptoScheme getScheme() {
			return scheme;
		}

		public KeyPair getSessionKeyPair() {
			return sessionKeyPair;
		}

		public Date getValidFrom() {
			return validFrom;
		}

		public String toEncryptedString(byte[] salt, String password) {
			StringEncrypter enc = new StringEncrypter(salt, password);
			return enc.encrypt(toString());
		}

		@Override
		public String toString() {
			List<String> params = new ArrayList<>();
			params.add(encryptionContextId);
			params.add(scheme.getName());
			params.add(ByteArray.asHex(getEncodedPublicKey()));
			params.add(ByteArray.asHex(getEncodedPrivateKey()));
			return StringUtils.convertStringListToCsv(params);
		}

		public byte[] getEncodedPublicKey() {
			try {
				return scheme.getSessionKeyAlgorithm().encodeX509PublicKey(sessionKeyPair.getPublic());
			} catch (CryptoException e) {
				throw new IllegalStateException(e);
			}
		}

		public byte[] getEncodedPrivateKey() {
			try {
				return scheme.getSessionKeyAlgorithm().encodeX509PrivateKey(sessionKeyPair.getPrivate());
			} catch (CryptoException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public static class DestinationDescriptor {
		public static String DATADIR = "directory";
		public static String SESSION_DURATION_HOURS = "duration.hours";
		public static String SESSION_RETENTION_DAYS = "retention.days";
		public static String ENCRYPTION_SCHEME = "scheme";
		public static String SALT = "salt";
		public static String SESSION = "SESSION[%d]";

		private String dataDirectory;
		private IntegratedCryptoScheme encryptionScheme;
		private String salt; // Hex 8 byte used to salt the encryption of the sessionKeys with the user's keystore
								// password.
		private int sessionDurationInHours;
		private int sessionRetentionInDays;

		private List<UnencryptedSessionKey> sessionKeys = new ArrayList<>();
		private List<String> encryptedSessionKeys = new ArrayList<>();

		public DestinationDescriptor() {
		}

		/**
		 * Create a new unencrypted session key and add it to the destination's session keys.
		 * 
		 * @return the new session key.
		 */
		public UnencryptedSessionKey createNewSessionKey() {
			Date validFromNow = new Date();
			try {
				UnencryptedSessionKey sk = new UnencryptedSessionKey(encryptionScheme,
						encryptionScheme.getSessionKeyAlgorithm().generateNewKeyPair(), validFromNow);

				sessionKeys.add(sk);
				return sk;
			} catch (CryptoException e) {
				throw new IllegalStateException(e);
			}
		}

		/**
		 * Find the SessionKey matching the Destinationsession's encryptionContextId.
		 * 
		 * @param ds
		 * @return null if not found.
		 */
		public UnencryptedSessionKey getSessionKey(Destinationsession ds) {
			if (sessionKeys == null) {
				return null;
			}
			for (UnencryptedSessionKey sk : sessionKeys) {
				if (sk.getEncryptionContextId().equals(ds.getEncryptionContextId())) {
					return sk;
				}
			}
			return null;
		}

		public String getDataDirectory() {
			return dataDirectory;
		}

		public void setDataDirectory(String dataDirectory) {
			this.dataDirectory = dataDirectory;
		}

		public int getSessionDurationInHours() {
			return sessionDurationInHours;
		}

		public void setSessionDurationInHours(int sessionDurationInHours) {
			this.sessionDurationInHours = sessionDurationInHours;
		}

		public int getSessionRetentionInDays() {
			return sessionRetentionInDays;
		}

		public void setSessionRetentionInDays(int sessionRetentionInDays) {
			this.sessionRetentionInDays = sessionRetentionInDays;
		}

		public IntegratedCryptoScheme getEncryptionScheme() {
			return encryptionScheme;
		}

		public void setEncryptionScheme(IntegratedCryptoScheme encryptionScheme) {
			this.encryptionScheme = encryptionScheme;
		}

		public String getSalt() {
			return salt;
		}

		public void setSalt(String salt) {
			this.salt = salt;
		}

		public List<UnencryptedSessionKey> getSessionKeys() {
			return sessionKeys;
		}

		public List<String> getEncryptedSessionKeys() {
			return encryptedSessionKeys;
		}

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - DNS
	// -------------------------------------------------------------------------

	/**
	 * Returns the DNS zone info for the domain.
	 * 
	 * @param domain
	 * @return the DNS zone info for the domain or null if none found.
	 */
	public static TdmxZoneRecord getSystemDnsInfo(String domain) {
		List<String> systemDnsResolver = DnsUtils.getSystemDnsResolverAddresses();
		try {
			DnsResultHolder rh = DnsUtils.getTdmxZoneRecord(domain, systemDnsResolver);
			if (rh == null) {
				return null;
			}
			return DnsUtils.parseTdmxZoneRecord(rh.getApex(), rh.getRecords().get(0));
		} catch (TextParseException e) {
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - Signature
	// -------------------------------------------------------------------------

	/**
	 * Checks the PKIX certificate chain of a user certificate ( up to the zone root certificate ).
	 * 
	 * @param ui
	 * @return
	 */
	public static PKIXCertificate[] getValidUserIdentity(UserIdentity ui) {
		PKIXCertificate uc = CertificateIOUtils.safeDecodeX509(ui.getUsercertificate());
		if (uc == null) {
			throw new IllegalStateException("User certificate invalid.");
		}
		PKIXCertificate dac = CertificateIOUtils.safeDecodeX509(ui.getDomaincertificate());
		if (dac == null) {
			throw new IllegalStateException("Domain administrator certificate invalid.");
		}
		PKIXCertificate zac = CertificateIOUtils.safeDecodeX509(ui.getRootcertificate());
		if (zac == null) {
			throw new IllegalStateException("Zone root certificate invalid.");
		}

		try {
			if (!CredentialUtils.isValidUserCertificate(zac, dac, uc)) {
				throw new IllegalStateException("User certificate chain invalid.");
			}
		} catch (CryptoCertificateException e) {
			throw new IllegalStateException("Problem checking User certificate chain.", e);
		}

		return new PKIXCertificate[] { uc, dac, zac };
	}

	/**
	 * Checks the PKIX certificate chain of an administrator certificate ( up to the zone root certificate ).
	 * 
	 * @param ai
	 * @return
	 */
	public static PKIXCertificate[] getValidAdministrator(AdministratorIdentity ai) {
		PKIXCertificate adac = CertificateIOUtils.safeDecodeX509(ai.getDomaincertificate());
		if (adac == null) {
			throw new IllegalStateException("Domain administrator certificate invalid.");
		}
		PKIXCertificate azac = CertificateIOUtils.safeDecodeX509(ai.getRootcertificate());
		if (azac == null) {
			throw new IllegalStateException("Zone root certificate invalid.");
		}

		try {
			if (!CredentialUtils.isValidDomainAdministratorCertificate(azac, adac)) {
				throw new IllegalStateException("Administrator certificate chain invalid.");
			}
		} catch (CryptoCertificateException e) {
			throw new IllegalStateException("Problem checking Administrator certificate chain.", e);
		}
		return new PKIXCertificate[] { adac, azac };
	}

	/**
	 * Return true if the user and admin have the same TDMX zone root certificate.
	 * 
	 * @param user
	 * @param admin
	 * @return true if the user and admin have the same TDMX zone root certificate.
	 */
	public static boolean isSameRootCertificate(PKIXCertificate[] user, PKIXCertificate[] admin) {
		PKIXCertificate userRoot = PKIXCertificate.getZoneRootPublicKey(user);
		PKIXCertificate adminRoot = PKIXCertificate.getZoneRootPublicKey(admin);

		if (userRoot != null && userRoot.isTdmxZoneAdminCertificate() && adminRoot != null
				&& adminRoot.isTdmxZoneAdminCertificate()) {
			return userRoot.isIdentical(adminRoot);
		}
		return false;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - API-s
	// -------------------------------------------------------------------------

	public static ConnectionTestResult sslTest(PKIXCredential credential, URL url,
			PKIXCertificate scsPublicCertificate) {
		ClientCredentialProvider cp = new ClientCredentialProvider() {

			@Override
			public PKIXCredential getCredential() {
				return credential;
			}

		};

		ClientKeyManagerFactoryImpl kmf = new ClientKeyManagerFactoryImpl();
		kmf.setCredentialProvider(cp);

		SingleTrustedCertificateProvider tcp = new SingleTrustedCertificateProvider(scsPublicCertificate);

		SystemDefaultTrustedCertificateProvider sdtcp = new SystemDefaultTrustedCertificateProvider();
		DelegatingTrustedCertificateProvider dtcp = new DelegatingTrustedCertificateProvider();

		dtcp.setDelegateProviders(Arrays.asList(new TrustedServerCertificateProvider[] { tcp, sdtcp }));

		ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(dtcp);

		SslProbeService sslprobe = new SslProbeService();
		sslprobe.setConnectionTimeoutMillis(CONNECTION_TIMEOUT_MS);
		sslprobe.setReadTimeoutMillis(READ_TIMEOUT_MS);
		sslprobe.setKeyManagerFactory(kmf);
		sslprobe.setTrustManagerFactory(stfm);
		sslprobe.setSslProtocol(TLS_VERSION);
		sslprobe.setEnabledCiphers(STRONG_CIPHERS);

		return sslprobe.testConnection(url.getHost(), url.getPort());

	}

	public static SCS createSCSClient(PKIXCredential credential, URL scsUrl, PKIXCertificate scsPublicCertificate) {
		ClientCredentialProvider cp = new ClientCredentialProvider() {

			@Override
			public PKIXCredential getCredential() {
				return credential;
			}

		};
		ClientKeyManagerFactoryImpl kmf = new ClientKeyManagerFactoryImpl();
		kmf.setCredentialProvider(cp);

		SystemDefaultTrustedCertificateProvider sdtcp = new SystemDefaultTrustedCertificateProvider();
		SingleTrustedCertificateProvider tcp = new SingleTrustedCertificateProvider(scsPublicCertificate);

		DelegatingTrustedCertificateProvider dtcp = new DelegatingTrustedCertificateProvider();
		dtcp.setDelegateProviders(Arrays.asList(new TrustedServerCertificateProvider[] { tcp, sdtcp }));

		ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(dtcp);

		SoapClientFactory<SCS> factory = new SoapClientFactory<>();
		factory.setUrl(scsUrl.toString());
		factory.setConnectionTimeoutMillis(CONNECTION_TIMEOUT_MS);
		factory.setKeepAlive(true);
		factory.setClazz(SCS.class);
		factory.setReceiveTimeoutMillis(READ_TIMEOUT_MS);
		factory.setDisableCNCheck(true); // FIXME
		factory.setKeyManagerFactory(kmf);
		factory.setTrustManagerFactory(stfm);
		factory.setTlsProtocolVersion(TLS_VERSION);

		factory.setEnabledCipherSuites(STRONG_CIPHERS);

		SCS client = factory.createClient();
		return client;
	}

	public static ZAS createZASClient(PKIXCredential uc, Endpoint endpoint) {
		ClientCredentialProvider cp = new ClientCredentialProvider() {

			@Override
			public PKIXCredential getCredential() {
				return uc;
			}

		};
		ClientKeyManagerFactoryImpl kmf = new ClientKeyManagerFactoryImpl();
		kmf.setCredentialProvider(cp);

		PKIXCertificate serverCert = CertificateIOUtils.safeDecodeX509(endpoint.getTlsCertificate());
		SingleTrustedCertificateProvider tcp = new SingleTrustedCertificateProvider(serverCert);
		ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(tcp);

		SoapClientFactory<ZAS> factory = new SoapClientFactory<>();
		factory.setUrl(endpoint.getUrl());
		factory.setConnectionTimeoutMillis(10000);
		factory.setKeepAlive(true);
		factory.setClazz(ZAS.class);
		factory.setReceiveTimeoutMillis(READ_TIMEOUT_MS);
		factory.setDisableCNCheck(false);
		factory.setKeyManagerFactory(kmf);
		factory.setTrustManagerFactory(stfm);
		factory.setTlsProtocolVersion(TLS_VERSION);

		factory.setEnabledCipherSuites(STRONG_CIPHERS);

		ZAS client = factory.createClient();
		return client;
	}

	public static MDS createMDSClient(PKIXCredential uc, Endpoint endpoint) {
		ClientCredentialProvider cp = new ClientCredentialProvider() {

			@Override
			public PKIXCredential getCredential() {
				return uc;
			}

		};
		ClientKeyManagerFactoryImpl kmf = new ClientKeyManagerFactoryImpl();
		kmf.setCredentialProvider(cp);

		PKIXCertificate serverCert = CertificateIOUtils.safeDecodeX509(endpoint.getTlsCertificate());
		SingleTrustedCertificateProvider tcp = new SingleTrustedCertificateProvider(serverCert);
		ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(tcp);

		SoapClientFactory<MDS> factory = new SoapClientFactory<>();
		factory.setUrl(endpoint.getUrl());
		factory.setConnectionTimeoutMillis(10000);
		factory.setKeepAlive(true);
		factory.setClazz(MDS.class);
		factory.setReceiveTimeoutMillis(READ_TIMEOUT_MS);
		factory.setDisableCNCheck(false);
		factory.setKeyManagerFactory(kmf);
		factory.setTrustManagerFactory(stfm);
		factory.setTlsProtocolVersion(TLS_VERSION);

		factory.setEnabledCipherSuites(STRONG_CIPHERS);

		MDS client = factory.createClient();
		return client;
	}

	public static MOS createMOSClient(PKIXCredential uc, Endpoint endpoint) {
		ClientCredentialProvider cp = new ClientCredentialProvider() {

			@Override
			public PKIXCredential getCredential() {
				return uc;
			}

		};
		ClientKeyManagerFactoryImpl kmf = new ClientKeyManagerFactoryImpl();
		kmf.setCredentialProvider(cp);

		PKIXCertificate serverCert = CertificateIOUtils.safeDecodeX509(endpoint.getTlsCertificate());
		SingleTrustedCertificateProvider tcp = new SingleTrustedCertificateProvider(serverCert);
		ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(tcp);

		SoapClientFactory<MOS> factory = new SoapClientFactory<>();
		factory.setUrl(endpoint.getUrl());
		factory.setConnectionTimeoutMillis(10000);
		factory.setKeepAlive(true);
		factory.setClazz(MOS.class);
		factory.setReceiveTimeoutMillis(READ_TIMEOUT_MS);
		factory.setDisableCNCheck(false);
		factory.setKeyManagerFactory(kmf);
		factory.setTrustManagerFactory(stfm);
		factory.setTlsProtocolVersion(TLS_VERSION);

		factory.setEnabledCipherSuites(STRONG_CIPHERS);

		MOS client = factory.createClient();
		return client;
	}

	public static void logError(PrintStream out, org.tdmx.core.api.v01.common.Error error) {
		out.println(ClientCliLoggingUtils.toString(error));
	}

	private static String getMandatoryStringProperty(Properties p, String name, String filename) {
		String value = p.getProperty(name);
		if (!StringUtils.hasText(value)) {
			throw new IllegalStateException(filename + " missing property " + name);
		}
		return value;
	}

	private static int getMandatoryIntProperty(Properties p, String name, String filename) {
		String value = p.getProperty(name);
		if (!StringUtils.hasText(value)) {
			throw new IllegalStateException(filename + " missing property " + name);
		}
		int v = 0;
		try {
			v = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalStateException(filename + " property " + name + " is not an integer.", nfe);
		}

		return v;
	}

}
