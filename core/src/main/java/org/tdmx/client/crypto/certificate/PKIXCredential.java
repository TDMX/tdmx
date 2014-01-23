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
