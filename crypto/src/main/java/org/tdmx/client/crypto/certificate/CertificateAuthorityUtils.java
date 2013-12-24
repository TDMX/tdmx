package org.tdmx.client.crypto.certificate;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
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


public class CertificateAuthorityUtils {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final String TDMX_DOMAIN_CA_OU="tdmx-domain";
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	/**
	 * 
	 * @param req
	 * @return
	 * @throws CryptoCertificateException 
	 */
	public static PKIXCredential createCertificateAuthority( CertificateAuthoritySpecifier req ) throws CryptoCertificateException {
		KeyPair kp = null;
		try {
			kp = req.getKeyAlgorithm().generateNewKeyPair();
		} catch (CryptoException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_KEYPAIR_GENERATION, e);
		}
		
		PublicKey publicKey = kp.getPublic();
		PrivateKey privateKey = kp.getPrivate();
		
		X500NameBuilder subjectBuilder = new X500NameBuilder();
		subjectBuilder.addRDN(BCStyle.C, req.getCountry());
		subjectBuilder.addRDN(BCStyle.O, req.getOrg());
		subjectBuilder.addRDN(BCStyle.E, req.getEmailAddress());
		subjectBuilder.addRDN(BCStyle.TELEPHONE_NUMBER, req.getTelephoneNumber());
		subjectBuilder.addRDN(BCStyle.CN, req.getCn());
		X500Name subject = subjectBuilder.build();
		X500Name issuer = subject;
		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
				issuer, 
				new BigInteger("1"), 
				req.getNotBefore().getTime(),
				req.getNotAfter().getTime(), 
				subject, 
				publicKey);

		try {
			BasicConstraints cA = new BasicConstraints(1);
			certBuilder.addExtension(Extension.basicConstraints, true, cA);

			JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
			certBuilder.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(publicKey));
			certBuilder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(publicKey));

			KeyUsage ku = new KeyUsage(KeyUsage.digitalSignature|KeyUsage.keyCertSign);
			certBuilder.addExtension(Extension.keyUsage, false, ku);
			
			//RFC5280 http://tools.ietf.org/html/rfc5280#section-4.2.1.10
			//The CA has a CN which is not part of the name constraint - but we can constrain
			//any domain certificate issued to be limited to some OU under the O.
			X500NameBuilder subjectConstraintBuilder = new X500NameBuilder();
			subjectConstraintBuilder.addRDN(BCStyle.C, req.getCountry());
			subjectConstraintBuilder.addRDN(BCStyle.O, req.getOrg());
			subjectConstraintBuilder.addRDN(BCStyle.OU, TDMX_DOMAIN_CA_OU);
			X500Name nameConstraint = subjectConstraintBuilder.build();
			
			GeneralName snc = new GeneralName(GeneralName.directoryName, nameConstraint);
			GeneralSubtree snSubtree = new GeneralSubtree(snc,new BigInteger("0"),null);
			NameConstraints nc = new NameConstraints(new GeneralSubtree[]{snSubtree}, null);
			certBuilder.addExtension(Extension.nameConstraints, true, nc);
			
			ContentSigner signer = SignatureAlgorithm.getContentSigner(privateKey, req.getSignatureAlgorithm());
			byte[] certBytes = certBuilder.build(signer).getEncoded();
			
			PKIXCertificate c = CertificateIOUtils.decodeCertificate(certBytes);
			
			return new PKIXCredential(c, privateKey);
		} catch (CertIOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_CA_CERT_GENERATION, e);
		}
	}
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

}
