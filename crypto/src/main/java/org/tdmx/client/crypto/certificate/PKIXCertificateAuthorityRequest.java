package org.tdmx.client.crypto.certificate;

import java.util.Calendar;
import java.util.List;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;

public class PKIXCertificateAuthorityRequest {
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private AsymmetricEncryptionAlgorithm keyAlgorithm;
	private String cn;
	private String org;
	private String country;
	private Calendar notBefore;
	private Calendar notAfter;
	private boolean subjectNameContraint;
	private List<String> dnsNameConstraints;
	private SignatureAlgorithm signatureAlgorithm;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	public PKIXCertificateAuthorityRequest(){
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

	public AsymmetricEncryptionAlgorithm getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(AsymmetricEncryptionAlgorithm keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
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

	public boolean isSubjectNameContraint() {
		return subjectNameContraint;
	}

	public void setSubjectNameContraint(boolean subjectNameContraint) {
		this.subjectNameContraint = subjectNameContraint;
	}

	public List<String> getDnsNameConstraints() {
		return dnsNameConstraints;
	}

	public void setDnsNameConstraints(List<String> dnsNameConstraints) {
		this.dnsNameConstraints = dnsNameConstraints;
	}

	public SignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

}
