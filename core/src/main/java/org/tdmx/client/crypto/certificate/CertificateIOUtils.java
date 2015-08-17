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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO rename -io
public class CertificateIOUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(CertificateIOUtils.class);

	private static final String X509CERTIFICATE_FACTORY_ALGORITHM = "X.509";
	private static final String PKIXCERTIFICATE_PATHVALIDATOR_ALGORITHM = "PKIX";

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	private CertificateIOUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	/**
	 * Cast PKIXCertificates array to X509Certificates array.
	 */
	public static X509Certificate[] cast(PKIXCertificate[] certs) {
		if (certs == null) {
			return null;
		}

		X509Certificate[] result = new X509Certificate[certs.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = certs[i].getCertificate();
		}
		return result;
	}

	/**
	 * Convert X509Certificates to PKIXCertificates.
	 */
	public static PKIXCertificate[] convert(X509Certificate[] certs) throws CryptoCertificateException {
		if (certs == null) {
			return null;
		}

		PKIXCertificate[] result = new PKIXCertificate[certs.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = new PKIXCertificate(certs[i]);
		}
		return result;
	}

	/**
	 * Convert PKIXCertificates to X509Certificates array.
	 */
	public static List<X509Certificate> cast(List<PKIXCertificate> certs) {
		List<X509Certificate> xs = new ArrayList<>();
		for (PKIXCertificate p : certs) {
			xs.add(p.getCertificate());
		}
		return xs;
	}

	/**
	 * Convert PKIXCertificates to X509Certificates array.
	 */
	public static List<PKIXCertificate> convert(List<X509Certificate> certs) throws CryptoCertificateException {
		List<PKIXCertificate> xs = new ArrayList<>();
		for (X509Certificate c : certs) {
			xs.add(new PKIXCertificate(c));
		}
		return xs;
	}

	public static PKIXCertificate[] decodeX509(byte[]... certChain) throws CryptoCertificateException {
		if (certChain == null) {
			return null;
		}
		PKIXCertificate[] pkcerts = new PKIXCertificate[certChain.length];
		for (int i = 0; i < certChain.length; i++) {
			pkcerts[i] = decodeX509(certChain[i]);
		}
		return pkcerts;
	}

	public static String safeX509certsToPem(byte[]... certChain) {
		try {
			PKIXCertificate[] certs = decodeX509(certChain);
			return x509certsToPem(certs);
		} catch (CryptoCertificateException e) {
			return null;
		}
	}

	public static String safeX509certsToPem(PKIXCertificate[] certs) {
		try {
			return x509certsToPem(certs);
		} catch (CryptoCertificateException e) {
			return null;
		}
	}

	public static String x509certsToPem(PKIXCertificate[] certs) throws CryptoCertificateException {
		StringWriter writer = new StringWriter();
		PEMWriter pemWrtCer = new PEMWriter(writer);
		try {
			for (PKIXCertificate cert : certs) {
				pemWrtCer.writeObject(cert.getCertificate());
			}
			pemWrtCer.close();
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}

		return writer.toString();
	}

	public static String x509certToPem(PKIXCertificate cert) throws CryptoCertificateException {
		return x509certsToPem(new PKIXCertificate[] { cert });
	}

	public static PKIXCertificate pemToX509cert(String input) throws CryptoCertificateException {
		PKIXCertificate[] certs = pemToX509certs(input);
		if (certs == null) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_CERTS);
		}
		if (certs.length != 1) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_TOO_MANY_CERTS);
		}
		return certs[0];
	}

	public static PKIXCertificate[] safePemToX509certs(String input) {
		try {
			return pemToX509certs(input);
		} catch (CryptoCertificateException e) {
			log.warn("safePemToX509certs failed to convert input.", e);
		}
		return null;
	}

	public static PKIXCertificate[] pemToX509certs(String input) throws CryptoCertificateException {
		StringReader sr = new StringReader(input);
		PEMParser pp = new PEMParser(sr);

		List<PKIXCertificate> certList = new ArrayList<>();
		Object o = null;
		try {
			while ((o = pp.readObject()) != null) {
				if (o instanceof X509CertificateHolder) {
					X509CertificateHolder ch = (X509CertificateHolder) o;
					PKIXCertificate c = decodeX509(ch.getEncoded());
					certList.add(c);
				}
			}
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		} finally {
			try {
				pp.close();
			} catch (IOException e) {
			}
		}
		return certList.toArray(new PKIXCertificate[0]);
	}

	/**
	 * Decode a binary DER encoded X509 certificate into a PKIXCertificate.
	 * 
	 * @param x509encodedValue
	 * @return null if some error took place.
	 */
	public static PKIXCertificate safeDecodeX509(byte[] x509encodedValue) {
		try {
			return decodeX509(x509encodedValue);
		} catch (CryptoCertificateException e) {
			log.warn("safeDecodeX509 failed to decode.", e);
		}
		return null;
	}

	/**
	 * Decode a binary DER encoded X509 certificate into a PKIXCertificate.
	 * 
	 * @param x509encodedValue
	 *            a binary DER encoded X509 certificate.
	 * @return a PKIXCertificate.
	 * @throws CryptoCertificateException
	 *             if there are any problems.
	 */
	public static PKIXCertificate decodeX509(byte[] x509encodedValue) throws CryptoCertificateException {
		CertificateFactory certFactory;
		try {
			certFactory = CertificateFactory.getInstance(X509CERTIFICATE_FACTORY_ALGORITHM);
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(
					x509encodedValue));
			return new PKIXCertificate(cert);
		} catch (CertificateException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
		}
	}

	public static boolean pkixValidate(X509Certificate[] certs, KeyStore trustStore) throws CryptoCertificateException {
		try {
			CertificateFactory cf = CertificateFactory.getInstance(X509CERTIFICATE_FACTORY_ALGORITHM);
			List<X509Certificate> mylist = new ArrayList<X509Certificate>();
			for (X509Certificate cert : certs) {
				mylist.add(cert);
			}
			CertPath cp = cf.generateCertPath(mylist);

			PKIXParameters params = new PKIXParameters(trustStore);
			// TDMX certificates do not have a revocation CRL mechanism
			params.setRevocationEnabled(false);
			CertPathValidator cpv = CertPathValidator.getInstance(PKIXCERTIFICATE_PATHVALIDATOR_ALGORITHM);
			cpv.validate(cp, params);
			return true;
		} catch (CertificateException | KeyStoreException | InvalidAlgorithmParameterException
				| NoSuchAlgorithmException e) {
			log.warn("pkixValidation unexpected problem.", e);
		} catch (CertPathValidatorException e) {
			log.debug("PKIX Certificate validation failed.", e);
		}
		return false;
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
