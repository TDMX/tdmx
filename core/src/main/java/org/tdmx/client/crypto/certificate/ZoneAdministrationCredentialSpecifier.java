package org.tdmx.client.crypto.certificate;

import java.util.Calendar;

import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;

//TODO UserCertSpec.
public class ZoneAdministrationCredentialSpecifier {
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private TdmxZoneInfo zoneInfo;
	
	private String cn;
	private String telephoneNumber;
	private String emailAddress;
	private String orgUnit;
	private String org;
	private String location;
	private String country;

	private Calendar notBefore;
	private Calendar notAfter;
	private PublicKeyAlgorithm keyAlgorithm;
	private SignatureAlgorithm signatureAlgorithm;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	public ZoneAdministrationCredentialSpecifier(){
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

	public TdmxZoneInfo getZoneInfo() {
		return zoneInfo;
	}

	public void setZoneInfo(TdmxZoneInfo zoneInfo) {
		this.zoneInfo = zoneInfo;
	}

	public PublicKeyAlgorithm getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(PublicKeyAlgorithm keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getOrgUnit() {
		return orgUnit;
	}

	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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
