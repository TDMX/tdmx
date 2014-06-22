/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.console.application.util.ValidationUtils;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError.ERROR;
import org.tdmx.core.system.lang.CalendarUtils;

public class CertificateAuthorityRequest implements Serializable {

	private static final long serialVersionUID = 539527325141504999L;

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	private static enum FieldNames {
		keyAlgorithm,
		commonName,
		telephoneNumber,
		emailAddress,
		organization,
		country,
		notBefore,
		notAfter,
		signatureAlgorithm,

	}

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private PublicKeyAlgorithm keyAlgorithm;
	private String commonName;
	private String telephoneNumber;
	private String emailAddress;
	private String organization;
	private String country;
	private Date notBefore;
	private Date notAfter;
	private SignatureAlgorithm signatureAlgorithm;

	private String certificateAuthorityId;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public CertificateAuthorityRequest() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();

		ValidationUtils.mandatoryTextField(getCommonName(), FieldNames.commonName.name(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getTelephoneNumber(), FieldNames.telephoneNumber.name(), ERROR.MISSING,
				errors);
		ValidationUtils.mandatoryTextField(getEmailAddress(), FieldNames.emailAddress.name(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getOrganization(), FieldNames.organization.name(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getCountry(), FieldNames.country.name(), ERROR.MISSING, errors);

		ValidationUtils.mandatoryDateField(getNotBefore(), FieldNames.notBefore.name(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryDateField(getNotAfter(), FieldNames.notAfter.name(), ERROR.MISSING, errors);

		ValidationUtils.mandatoryField(getKeyAlgorithm(), FieldNames.keyAlgorithm.name(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryField(getSignatureAlgorithm(), FieldNames.signatureAlgorithm.name(), ERROR.MISSING,
				errors);

		if (errors.size() > 0) {
			return errors;
		}

		// TODO test signatureAlgorithm is valid depending on the keyAlgorithm

		// TODO test future notBefore

		// TODO test future notAfter

		// TODO test notAfter > notBefore

		return errors;
	}

	public ZoneAdministrationCredentialSpecifier domain() {
		ZoneAdministrationCredentialSpecifier o = new ZoneAdministrationCredentialSpecifier();
		o.setCn(getCommonName());
		o.setTelephoneNumber(getTelephoneNumber());
		o.setEmailAddress(getEmailAddress());
		o.setOrg(getOrganization());
		o.setCountry(getCountry());
		o.setNotBefore(CalendarUtils.getDate(getNotBefore()));
		o.setNotAfter(CalendarUtils.getDate(getNotAfter()));
		o.setKeyAlgorithm(getKeyAlgorithm());
		o.setSignatureAlgorithm(getSignatureAlgorithm());
		return o;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public PublicKeyAlgorithm getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(PublicKeyAlgorithm keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String cn) {
		this.commonName = cn;
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
