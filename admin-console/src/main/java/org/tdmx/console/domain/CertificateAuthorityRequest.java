package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateAuthoritySpecifier;
import org.tdmx.console.application.util.ValidationUtils;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError.ERROR;

public class CertificateAuthorityRequest implements Serializable {
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	private static enum FieldNames {
		keyAlgorithm,
		commonName,
		organization,
		country,
		notBefore,
		notAfter,
		signatureAlgorithm,
		
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private AsymmetricEncryptionAlgorithm keyAlgorithm;
	private String commonName;
	private String organization;
	private String country;
	private Date notBefore;
	private Date notAfter;
	private SignatureAlgorithm signatureAlgorithm;
	
	private String certificateAuthorityId;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	public CertificateAuthorityRequest(){
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();
		
		ValidationUtils.mandatoryTextField(getCommonName(), FieldNames.commonName.name(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getOrganization(), FieldNames.organization.name(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getCountry(), FieldNames.country.name(), ERROR.MISSING, errors);
		
		ValidationUtils.mandatoryDateField(getNotBefore(), FieldNames.notBefore.name(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryDateField(getNotAfter(), FieldNames.notAfter.name(), ERROR.MISSING, errors);

		ValidationUtils.mandatoryField(getKeyAlgorithm(), FieldNames.keyAlgorithm.name(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryField(getSignatureAlgorithm(), FieldNames.signatureAlgorithm.name(), ERROR.MISSING, errors);

		if ( errors.size() > 0 ) {
			return errors;
		}

		// TODO test signatureAlgorithm is valid depending on the keyAlgorithm
		
		// TODO test future notBefore
		
		// TODO test future notAfter
		
		// TODO test notAfter > notBefore
		
		return errors;
	}

	public CertificateAuthoritySpecifier domain() {
		CertificateAuthoritySpecifier o = new CertificateAuthoritySpecifier();
		//TODO map fields
		return o;
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

	public AsymmetricEncryptionAlgorithm getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(AsymmetricEncryptionAlgorithm keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String cn) {
		this.commonName = cn;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String org) {
		this.organization = org;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Date getNotBefore() {
		return notBefore;
	}

	public void setNotBefore(Date notBefore) {
		this.notBefore = notBefore;
	}

	public Date getNotAfter() {
		return notAfter;
	}

	public void setNotAfter(Date notAfter) {
		this.notAfter = notAfter;
	}

	public SignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

	public String getCertificateAuthorityId() {
		return certificateAuthorityId;
	}

	public void setCertificateAuthorityId(String certificateAuthorityId) {
		this.certificateAuthorityId = certificateAuthorityId;
	}

}
