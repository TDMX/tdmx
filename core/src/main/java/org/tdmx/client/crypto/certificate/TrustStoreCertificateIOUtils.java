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
package org.tdmx.client.crypto.certificate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

public class TrustStoreCertificateIOUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	private TrustStoreCertificateIOUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	public static String trustStoreEntryToPem(TrustStoreEntry entry) throws CryptoCertificateException {
		String fingerprint = entry.getCertificate().getFingerprint();
		StringWriter writer = new StringWriter();
		if (entry.getFriendlyName() != null) {
			writer.write(
					TrustStoreEntry.FRIENDLY_NAME + fingerprint + " " + entry.getFriendlyName() + TrustStoreEntry.NL);
		}
		if (entry.getComment() != null) {
			BufferedReader br = new BufferedReader(new StringReader(entry.getComment()));
			String commentLine;
			try {
				while ((commentLine = br.readLine()) != null) {
					writer.write(TrustStoreEntry.COMMENT_LINE + fingerprint + " " + commentLine + TrustStoreEntry.NL);
				}
			} catch (IOException e) {
				throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
			}

		}
		JcaPEMWriter pemWrtCer = new JcaPEMWriter(writer);
		try {
			pemWrtCer.writeObject(entry.getCertificate().getCertificate());
			pemWrtCer.close();
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}

		return writer.toString();
	}

	public static List<TrustStoreEntry> pemToTrustStoreEntries(String input) throws CryptoCertificateException {
		StringReader sr = new StringReader(input);
		PEMParser pp = new PEMParser(sr);

		// we make 2 passes , first extracting certs, 2nd extracting meta infos
		List<TrustStoreEntry> certList = new ArrayList<>();
		Object o = null;
		try {
			while ((o = pp.readObject()) != null) {
				if (o instanceof X509CertificateHolder) {
					X509CertificateHolder ch = (X509CertificateHolder) o;
					PKIXCertificate c = CertificateIOUtils.decodeX509(ch.getEncoded());
					certList.add(new TrustStoreEntry(c));
				}
			}
			pp.close();
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}

		BufferedReader br = new BufferedReader(new StringReader(input));
		String strLine;
		try {
			while ((strLine = br.readLine()) != null) {
				if (strLine.startsWith(TrustStoreEntry.FRIENDLY_NAME)) {
					String restofLine = strLine.substring(TrustStoreEntry.FRIENDLY_NAME.length());
					int separator = restofLine.indexOf(" ");
					if (separator != -1) {
						String fingerprint = restofLine.substring(0, separator);
						String text = restofLine.substring(separator + 1);

						for (TrustStoreEntry e : certList) {

							if (fingerprint.equals(e.getCertificate().getFingerprint())) {
								e.setFriendlyName(text);
							}
						}
					}
				}
				if (strLine.startsWith(TrustStoreEntry.COMMENT_LINE)) {
					String restofLine = strLine.substring(TrustStoreEntry.COMMENT_LINE.length());
					int separator = restofLine.indexOf(" ");
					if (separator != -1) {
						String fingerprint = restofLine.substring(0, separator);
						String text = restofLine.substring(separator + 1);

						for (TrustStoreEntry e : certList) {
							if (fingerprint.equals(e.getCertificate().getFingerprint())) {
								e.addComment(text);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}

		return certList;
	}

	public static X509TrustManager getDefaultPKIXTrustManager() throws CryptoCertificateException {
		X509TrustManager platformTm = null;
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init((KeyStore) null);
			TrustManager[] tmgs = tmf.getTrustManagers();

			// we get the first X509 trust manager and embedd it in our saving/testing wrapper.
			if (tmgs != null) {
				for (TrustManager m : tmgs) {
					if (m instanceof X509TrustManager) {
						platformTm = (X509TrustManager) m;
						break;
					}
				}
			}
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_ALGORITHM, e);
		} catch (KeyStoreException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_SYSTEM_TRUSTSTORE_EXCEPTION, e);
		}

		return platformTm;
	}

	/**
	 * Returns the list of system trusted CAs.
	 * 
	 * @return the list of system trusted CAs or empty list if there are none.
	 * @throws CryptoCertificateException
	 */
	public static List<TrustStoreEntry> getAllSystemTrustedCAs() throws CryptoCertificateException {
		List<TrustStoreEntry> caList = new ArrayList<>();

		X509TrustManager t = getDefaultPKIXTrustManager();
		X509Certificate[] issuers = t.getAcceptedIssuers();
		for (X509Certificate i : issuers) {
			PKIXCertificate pk = new PKIXCertificate(i);
			TrustStoreEntry e = new TrustStoreEntry(pk);
			caList.add(e);
		}
		return caList;
	}

	public static List<TrustStoreEntry> getAllSystemDisrustedCAs() throws CryptoCertificateException {
		List<TrustStoreEntry> distrustedCaList = new ArrayList<>();

		for (TrustStoreEntry e : UntrustedCertificates.untrustedCerts.values()) {
			distrustedCaList.add(e);
		}
		return distrustedCaList;
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
