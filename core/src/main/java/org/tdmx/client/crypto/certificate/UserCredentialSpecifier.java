package org.tdmx.client.crypto.certificate;

import java.util.Calendar;

import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;

public class UserCredentialSpecifier {
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private PKIXCredential domainAdministratorCredential;

	private String name;

	private Calendar notBefore;
	private Calendar notAfter;
	private PublicKeyAlgorithm keyAlgorithm;
	private SignatureAlgorithm signatureAlgorithm;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	public UserCredentialSpecifier(){
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

	public PKIXCredential getDomainAdministratorCredential() {
		return domainAdministratorCredential;
	}

	public void setDomainAdministratorCredential(
			PKIXCredential domainAdministratorCredential) {
		this.domainAdministratorCredential = domainAdministratorCredential;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PublicKeyAlgorithm getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(PublicKeyAlgorithm keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public Calendar getNotBefore() {
		return notBefore;
	}

	public void setNotBefore(Calendar notBefore) {
		this.notBefore = notBefore;
	}

	public Calendar getNotAfter() {
		return notAfter;
	}

	public void setNotAfter(Calendar notAfter) {
		this.notAfter = notAfter;
	}

	public SignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

}
