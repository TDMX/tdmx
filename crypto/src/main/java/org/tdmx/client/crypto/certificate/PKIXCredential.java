package org.tdmx.client.crypto.certificate;

import java.security.PrivateKey;
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

public class PKIXCredential {
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private PKIXCertificate[] certificateChain;

	private PrivateKey privateKey;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	public PKIXCredential( PKIXCertificate[] certChain, PrivateKey key ) throws CryptoCertificateException {
		this.certificateChain = certChain;
		this.privateKey = key;
	}
	
	public PKIXCredential( PKIXCertificate cert, PrivateKey key ) throws CryptoCertificateException {
		this.certificateChain = new PKIXCertificate[] {cert};
		this.privateKey = key;
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

	public PKIXCertificate[] getCertificateChain() {
		return certificateChain;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}
}
