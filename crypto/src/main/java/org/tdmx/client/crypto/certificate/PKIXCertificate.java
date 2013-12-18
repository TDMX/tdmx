package org.tdmx.client.crypto.certificate;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.scheme.CryptoException;

public class PKIXCertificate {
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private X509Certificate certificate;
	
	private String fingerprint;
	private String subject;
	private String cn;
	private String org;
	private String country;
	private Calendar notBefore;
	private Calendar notAfter;
	private String signatureAlgorithm;
	private String signature;
	private String info;
	//TODO public key - type + leyken ie RSA(2048bit) AsymmetricEncryptionAlgorithm
	//TODO basic constraints - CA? certificate chain length
	//TODO issuer
	//TODO subject key identifier
	//TODO issuer key identifier
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	public PKIXCertificate( X509Certificate cert ) throws CryptoCertificateException {
		try {
			certificate = cert;
			
			JcaX509CertificateHolder h = new JcaX509CertificateHolder(certificate);
			{
				byte[] tbsCert = cert.getTBSCertificate();
				byte[] sha1 = DigestAlgorithm.SHA_1.kdf(tbsCert);
				fingerprint = ByteArray.asHex(sha1);
			}
			subject = cert.getSubjectX500Principal().getName();
			
			X500Name x500name = h.getSubject();
			RDN[] rdns = x500name.getRDNs(BCStyle.CN);
			if ( rdns != null && rdns.length > 0 ) {
				cn = IETFUtils.valueToString(rdns[0].getFirst().getValue());		
			}
			
			rdns = x500name.getRDNs(BCStyle.C);
			if ( rdns != null && rdns.length > 0 ) {
				country = IETFUtils.valueToString(rdns[0].getFirst().getValue());		
			}
			
			rdns = x500name.getRDNs(BCStyle.O);
			if ( rdns != null && rdns.length > 0 ) {
				org = IETFUtils.valueToString(rdns[0].getFirst().getValue());		
			}
			
			if ( h.getNotAfter() != null ) {
				notAfter = Calendar.getInstance();
				notAfter.setTime(h.getNotAfter());
			}
			if ( h.getNotBefore() != null ) {
				notBefore = Calendar.getInstance();
				notBefore.setTime(h.getNotBefore());
			}
			if ( h.getSignatureAlgorithm() != null ) {
				signatureAlgorithm = IETFUtils.valueToString(h.getSignatureAlgorithm());
			}
			if ( h.getSignature() != null ) {
				signature = ByteArray.asHex(h.getSignature());
			}
			
			info = cert.toString();
		} catch ( CryptoException e ) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
		} catch (CertificateEncodingException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_ENCODING, e);
		}
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public X509Certificate getCertificate() {
		return certificate;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public String getSubject() {
		return subject;
	}

	public String getCn() {
		return cn;
	}

	public String getOrg() {
		return org;
	}

	public String getCountry() {
		return country;
	}

	public Calendar getNotBefore() {
		return notBefore;
	}

	public Calendar getNotAfter() {
		return notAfter;
	}

	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public String getSignature() {
		return signature;
	}

	public String getInfo() {
		return info;
	}


}
