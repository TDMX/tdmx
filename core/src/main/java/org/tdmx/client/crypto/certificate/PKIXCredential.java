package org.tdmx.client.crypto.certificate;

import java.security.PrivateKey;

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
	public PKIXCredential( PKIXCertificate publicCert, PKIXCertificate[] issuerCertChain, PrivateKey key ) throws CryptoCertificateException {
		this.certificateChain = new PKIXCertificate[issuerCertChain.length+1];
		certificateChain[0] = publicCert;
		for( int i = 0; i < issuerCertChain.length; i++) {
			certificateChain[i+1] = issuerCertChain[i];
		}
		this.privateKey = key;
	}
	
	public PKIXCredential( PKIXCertificate[] certChain, PrivateKey key ) throws CryptoCertificateException {
		this.certificateChain = certChain;
		this.privateKey = key;
	}
	
	public PKIXCredential( PKIXCertificate publicCert, PKIXCertificate issuerCert, PrivateKey key ) throws CryptoCertificateException {
		this.certificateChain = new PKIXCertificate[] {publicCert,issuerCert};
		this.privateKey = key;
	}
	
	public PKIXCredential( PKIXCertificate selfsignedCert, PrivateKey key ) throws CryptoCertificateException {
		this.certificateChain = new PKIXCertificate[] {selfsignedCert};
		this.privateKey = key;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	public PKIXCertificate getPublicCert() {
		return ( certificateChain != null && certificateChain.length > 0 ) ? certificateChain[0] : null;
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

	public PKIXCertificate[] getCertificateChain() {
		return certificateChain;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}
}
