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

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralSubtree;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.NameConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.core.system.lang.StringUtils;

public class CredentialUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final String TDMX_DOMAIN_CA_OU = "tdmx-domain";

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Create the credentials of a ZoneAdministrator.
	 * 
	 * The ZoneAdministrator credentials are long validity.
	 * 
	 * @param req
	 * @return
	 * @throws CryptoCertificateException
	 */
	public static PKIXCredential createZoneAdministratorCredential(ZoneAdministrationCredentialSpecifier req)
			throws CryptoCertificateException {
		KeyPair kp = null;
		try {
			kp = req.getKeyAlgorithm().generateNewKeyPair();
		} catch (CryptoException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_KEYPAIR_GENERATION, e);
		}

		PublicKey publicKey = kp.getPublic();
		PrivateKey privateKey = kp.getPrivate();

		X500NameBuilder subjectBuilder = new X500NameBuilder();
		if (StringUtils.hasText(req.getCountry())) {
			subjectBuilder.addRDN(BCStyle.C, req.getCountry());
		}
		if (StringUtils.hasText(req.getLocation())) {
			subjectBuilder.addRDN(BCStyle.L, req.getLocation());
		}
		if (StringUtils.hasText(req.getOrg())) {
			subjectBuilder.addRDN(BCStyle.O, req.getOrg());
		}
		if (StringUtils.hasText(req.getOrgUnit())) {
			if (TDMX_DOMAIN_CA_OU.equals(req.getOrgUnit())) {
				throw new CryptoCertificateException(CertificateResultCode.ERROR_INVALID_OU);
			}
			subjectBuilder.addRDN(BCStyle.OU, req.getOrgUnit());
		}
		if (StringUtils.hasText(req.getEmailAddress())) {
			subjectBuilder.addRDN(BCStyle.E, req.getEmailAddress());
		}
		if (StringUtils.hasText(req.getTelephoneNumber())) {
			subjectBuilder.addRDN(BCStyle.TELEPHONE_NUMBER, req.getTelephoneNumber());
		}
		if (StringUtils.hasText(req.getCn())) {
			subjectBuilder.addRDN(BCStyle.CN, req.getCn());
		}
		X500Name subject = subjectBuilder.build();
		X500Name issuer = subject;
		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, BigInteger.valueOf(req
				.getSerialNumber()), req.getNotBefore().getTime(), req.getNotAfter().getTime(), subject, publicKey);

		try {
			BasicConstraints cA = new BasicConstraints(1);
			certBuilder.addExtension(Extension.basicConstraints, true, cA);

			JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
			certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
					extUtils.createAuthorityKeyIdentifier(publicKey));
			certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
					extUtils.createSubjectKeyIdentifier(publicKey));

			KeyUsage ku = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign);
			certBuilder.addExtension(Extension.keyUsage, false, ku);

			// RFC5280 http://tools.ietf.org/html/rfc5280#section-4.2.1.10
			// The CA has a CN which is not part of the name constraint - but we can constrain
			// any domain certificate issued to be limited to some OU under the O.
			X500NameBuilder subjectConstraintBuilder = new X500NameBuilder();
			if (StringUtils.hasText(req.getCountry())) {
				subjectConstraintBuilder.addRDN(BCStyle.C, req.getCountry());
			}
			if (StringUtils.hasText(req.getLocation())) {
				subjectConstraintBuilder.addRDN(BCStyle.L, req.getLocation());
			}
			if (StringUtils.hasText(req.getOrg())) {
				subjectConstraintBuilder.addRDN(BCStyle.O, req.getOrg());
			}
			if (StringUtils.hasText(req.getOrgUnit())) {
				subjectConstraintBuilder.addRDN(BCStyle.OU, req.getOrgUnit());
			}
			subjectConstraintBuilder.addRDN(BCStyle.OU, TDMX_DOMAIN_CA_OU);
			X500Name nameConstraint = subjectConstraintBuilder.build();

			GeneralName snc = new GeneralName(GeneralName.directoryName, nameConstraint);
			GeneralSubtree snSubtree = new GeneralSubtree(snc, new BigInteger("0"), null);
			NameConstraints nc = new NameConstraints(new GeneralSubtree[] { snSubtree }, null);
			certBuilder.addExtension(Extension.nameConstraints, true, nc);

			certBuilder.addExtension(TdmxZoneInfo.tdmxZoneInfo, false, req.getZoneInfo());

			ContentSigner signer = SignatureAlgorithm.getContentSigner(privateKey, req.getSignatureAlgorithm());
			byte[] certBytes = certBuilder.build(signer).getEncoded();

			PKIXCertificate c = CertificateIOUtils.decodeX509(certBytes);

			return new PKIXCredential(c, privateKey);
		} catch (CertIOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		}
	}

	/**
	 * Create the credentials of a DomainAdministrator.
	 * 
	 * @param req
	 * @return
	 * @throws CryptoCertificateException
	 */
	public static PKIXCredential createDomainAdministratorCredential(DomainAdministrationCredentialSpecifier req)
			throws CryptoCertificateException {
		KeyPair kp = null;
		try {
			kp = req.getKeyAlgorithm().generateNewKeyPair();
		} catch (CryptoException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_KEYPAIR_GENERATION, e);
		}

		PublicKey publicKey = kp.getPublic();
		PrivateKey privateKey = kp.getPrivate();

		PKIXCredential issuerCredential = req.getZoneAdministratorCredential();
		PKIXCertificate issuerPublicCert = issuerCredential.getPublicCert();

		PublicKey issuerPublicKey = issuerPublicCert.getCertificate().getPublicKey();
		PrivateKey issuerPrivateKey = issuerCredential.getPrivateKey();

		X500NameBuilder subjectBuilder = new X500NameBuilder();
		if (StringUtils.hasText(issuerPublicCert.getCountry())) {
			subjectBuilder.addRDN(BCStyle.C, issuerPublicCert.getCountry());
		}
		if (StringUtils.hasText(issuerPublicCert.getLocation())) {
			subjectBuilder.addRDN(BCStyle.L, issuerPublicCert.getLocation());
		}
		if (StringUtils.hasText(issuerPublicCert.getOrganization())) {
			subjectBuilder.addRDN(BCStyle.O, issuerPublicCert.getOrganization());
		}
		if (StringUtils.hasText(issuerPublicCert.getOrgUnit())) {
			subjectBuilder.addRDN(BCStyle.OU, issuerPublicCert.getOrgUnit());
		}
		subjectBuilder.addRDN(BCStyle.OU, TDMX_DOMAIN_CA_OU);
		subjectBuilder.addRDN(BCStyle.CN, req.getDomainName());
		X500Name subject = subjectBuilder.build();
		X500Name issuer = issuerPublicCert.getSubjectName();
		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, BigInteger.valueOf(req
				.getSerialNumber()), req.getNotBefore().getTime(), req.getNotAfter().getTime(), subject, publicKey);

		try {
			BasicConstraints cA = new BasicConstraints(0);
			certBuilder.addExtension(Extension.basicConstraints, true, cA);

			JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
			certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
					extUtils.createAuthorityKeyIdentifier(issuerPublicKey));
			certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
					extUtils.createSubjectKeyIdentifier(publicKey));

			KeyUsage ku = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign);
			certBuilder.addExtension(Extension.keyUsage, false, ku);

			// RFC5280 http://tools.ietf.org/html/rfc5280#section-4.2.1.10
			// The CA has a CN which is not part of the name constraint - but we can constrain
			// any domain certificate issued to be limited to some OU under the O.
			X500NameBuilder subjectConstraintBuilder = new X500NameBuilder();
			if (StringUtils.hasText(issuerPublicCert.getCountry())) {
				subjectConstraintBuilder.addRDN(BCStyle.C, issuerPublicCert.getCountry());
			}
			if (StringUtils.hasText(issuerPublicCert.getLocation())) {
				subjectConstraintBuilder.addRDN(BCStyle.L, issuerPublicCert.getLocation());
			}
			if (StringUtils.hasText(issuerPublicCert.getOrganization())) {
				subjectConstraintBuilder.addRDN(BCStyle.O, issuerPublicCert.getOrganization());
			}
			if (StringUtils.hasText(issuerPublicCert.getOrgUnit())) {
				subjectConstraintBuilder.addRDN(BCStyle.OU, issuerPublicCert.getOrgUnit());
			}
			subjectConstraintBuilder.addRDN(BCStyle.OU, TDMX_DOMAIN_CA_OU);
			subjectConstraintBuilder.addRDN(BCStyle.OU, req.getDomainName());
			X500Name nameConstraint = subjectConstraintBuilder.build();

			GeneralName snc = new GeneralName(GeneralName.directoryName, nameConstraint);
			GeneralSubtree snSubtree = new GeneralSubtree(snc, new BigInteger("0"), null);
			NameConstraints nc = new NameConstraints(new GeneralSubtree[] { snSubtree }, null);
			certBuilder.addExtension(Extension.nameConstraints, true, nc);

			TdmxZoneInfo issuerZoneInfo = issuerPublicCert.getTdmxZoneInfo();
			TdmxZoneInfo dacZoneInfo = new TdmxZoneInfo(issuerZoneInfo.getVersion(), issuerZoneInfo.getZoneRoot(),
					TdmxCertificateType.DAC);
			certBuilder.addExtension(TdmxZoneInfo.tdmxZoneInfo, false, dacZoneInfo);

			ContentSigner signer = SignatureAlgorithm.getContentSigner(issuerPrivateKey, req.getSignatureAlgorithm());
			byte[] certBytes = certBuilder.build(signer).getEncoded();

			PKIXCertificate c = CertificateIOUtils.decodeX509(certBytes);

			return new PKIXCredential(c, issuerCredential.getCertificateChain(), privateKey);
		} catch (CertIOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		}
	}

	public static PKIXCredential createUserCredential(UserCredentialSpecifier req) throws CryptoCertificateException {
		KeyPair kp = null;
		try {
			kp = req.getKeyAlgorithm().generateNewKeyPair();
		} catch (CryptoException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_KEYPAIR_GENERATION, e);
		}

		PublicKey publicKey = kp.getPublic();
		PrivateKey privateKey = kp.getPrivate();

		PKIXCredential issuerCredential = req.getDomainAdministratorCredential();
		PKIXCertificate issuerPublicCert = issuerCredential.getPublicCert();
		PublicKey issuerPublicKey = issuerPublicCert.getCertificate().getPublicKey();
		PrivateKey issuerPrivateKey = issuerCredential.getPrivateKey();

		X500NameBuilder subjectBuilder = new X500NameBuilder();
		if (StringUtils.hasText(issuerPublicCert.getCountry())) {
			subjectBuilder.addRDN(BCStyle.C, issuerPublicCert.getCountry());
		}
		if (StringUtils.hasText(issuerPublicCert.getLocation())) {
			subjectBuilder.addRDN(BCStyle.L, issuerPublicCert.getLocation());
		}
		if (StringUtils.hasText(issuerPublicCert.getOrganization())) {
			subjectBuilder.addRDN(BCStyle.O, issuerPublicCert.getOrganization());
		}
		if (StringUtils.hasText(issuerPublicCert.getOrgUnit())) {
			subjectBuilder.addRDN(BCStyle.OU, issuerPublicCert.getOrgUnit());
		}
		subjectBuilder.addRDN(BCStyle.OU, TDMX_DOMAIN_CA_OU);
		subjectBuilder.addRDN(BCStyle.OU, issuerPublicCert.getCommonName());
		subjectBuilder.addRDN(BCStyle.CN, req.getName());
		X500Name subject = subjectBuilder.build();
		X500Name issuer = issuerPublicCert.getSubjectName();
		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, BigInteger.valueOf(req
				.getSerialNumber()), req.getNotBefore().getTime(), req.getNotAfter().getTime(), subject, publicKey);

		try {
			JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
			certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
					extUtils.createAuthorityKeyIdentifier(issuerPublicKey));
			certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
					extUtils.createSubjectKeyIdentifier(publicKey));

			KeyUsage ku = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation | KeyUsage.keyEncipherment);
			certBuilder.addExtension(Extension.keyUsage, false, ku);

			TdmxZoneInfo issuerZoneInfo = issuerPublicCert.getTdmxZoneInfo();
			TdmxZoneInfo ucZoneInfo = new TdmxZoneInfo(issuerZoneInfo.getVersion(), issuerZoneInfo.getZoneRoot(),
					TdmxCertificateType.UC);
			certBuilder.addExtension(TdmxZoneInfo.tdmxZoneInfo, false, ucZoneInfo);

			ContentSigner signer = SignatureAlgorithm.getContentSigner(issuerPrivateKey, req.getSignatureAlgorithm());
			byte[] certBytes = certBuilder.build(signer).getEncoded();

			PKIXCertificate c = CertificateIOUtils.decodeX509(certBytes);

			return new PKIXCredential(c, issuerCredential.getCertificateChain(), privateKey);
		} catch (CertIOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		}
	}

	public static boolean isValidUserCertificate(PKIXCertificate zac, PKIXCertificate dac, PKIXCertificate uc)
			throws CryptoCertificateException {

		// check the TDMX zone info extension exists and the TDMX certs are correctly formed.
		if (!zac.isTdmxZoneAdminCertificate() || !dac.isTdmxDomainAdminCertificate() || !uc.isTdmxUserCertificate()) {
			return false;
		}
		// check that the zone info is identical in ZAC,DAC,UC
		TdmxZoneInfo zi_zac = zac.getTdmxZoneInfo();
		TdmxZoneInfo zi_dac = dac.getTdmxZoneInfo();
		TdmxZoneInfo zi_uc = uc.getTdmxZoneInfo();
		if (zi_zac.getVersion() != zi_dac.getVersion() || zi_dac.getVersion() != zi_uc.getVersion()) {
			return false;
		}
		if (!zi_zac.getZoneRoot().equals(zi_dac.getZoneRoot()) || !zi_dac.getZoneRoot().equals(zi_uc.getZoneRoot())) {
			return false;
		}
		// check the signing of the chain terminating in the trust root anchor of the zac
		KeyStore trustStore = KeyStoreUtils.createTrustStore(new PKIXCertificate[] { zac }, "jks");
		PKIXCertificate[] publicCertChain = new PKIXCertificate[] { uc, dac };
		return CertificateIOUtils.pkixValidate(CertificateIOUtils.cast(publicCertChain), trustStore);
	}

	public static boolean isValidDomainAdministratorCertificate(PKIXCertificate zac, PKIXCertificate dac)
			throws CryptoCertificateException {

		// check the TDMX zone info extension exists and the TDMX certs are correctly formed.
		if (!zac.isTdmxZoneAdminCertificate() || !dac.isTdmxDomainAdminCertificate()) {
			return false;
		}
		// check that the zone info is identical in ZAC,DAC,UC
		TdmxZoneInfo zi_zac = zac.getTdmxZoneInfo();
		TdmxZoneInfo zi_dac = dac.getTdmxZoneInfo();
		if (zi_zac.getVersion() != zi_dac.getVersion()) {
			return false;
		}
		if (!zi_zac.getZoneRoot().equals(zi_dac.getZoneRoot())) {
			return false;
		}
		// check the signing of the chain terminating in the trust root anchor of the zac
		KeyStore trustStore = KeyStoreUtils.createTrustStore(new PKIXCertificate[] { zac }, "jks");
		PKIXCertificate[] publicCertChain = new PKIXCertificate[] { dac };
		return CertificateIOUtils.pkixValidate(CertificateIOUtils.cast(publicCertChain), trustStore);
	}

	public static boolean isValidZoneAdministratorCertificate(PKIXCertificate zac) throws CryptoCertificateException {

		// check the TDMX zone info extension exists and the TDMX certs are correctly formed.
		if (!zac.isTdmxZoneAdminCertificate()) {
			return false;
		}

		// check the signing of the chain terminating in the trust root anchor of the zac
		KeyStore trustStore = KeyStoreUtils.createTrustStore(new PKIXCertificate[] { zac }, "jks");
		PKIXCertificate[] publicCertChain = new PKIXCertificate[] { zac };
		return CertificateIOUtils.pkixValidate(CertificateIOUtils.cast(publicCertChain), trustStore);
		// TODO check that a ZAC which is not valid anymore will "fail"
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
