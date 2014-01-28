package org.tdmx.client.crypto.certificate;

import java.util.Calendar;

import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;

//TODO DomainAdminCS, UserCertSpec.
public class DomainAdministrationCredentialSpecifier {
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private PKIXCredential zoneAdministratorCredential;
	
	private String domainName;

	private Calendar notBefore;
	private Calendar notAfter;
	private PublicKeyAlgorithm keyAlgorithm;
	private SignatureAlgorithm signatureAlgorithm;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	public DomainAdministrationCredentialSpecifier(){
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

	public PKIXCredential getZoneAdministratorCredential() {
		return zoneAdministratorCredential;
	}

	public void setZoneAdministratorCredential(
			PKIXCredential zoneAdministratorCredential) {
		this.zoneAdministratorCredential = zoneAdministratorCredential;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
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
