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
package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.console.application.search.FieldDescriptor;
import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.search.SearchableObjectField;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.util.ValidationUtils;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError.ERROR;

/**
 * A CertificateAuthority is an internal self-signed X509Certificate and asymmetric keypair which can be used to sign
 * one's own DomainCertificates.
 * 
 * Active CertificateAuthorities are automatically placed in the ca-trusted RootCAList. Inactive CertificateAuthorities
 * are automatically placed in the ca-distrusted RootCAList.
 * 
 * The CA is represented by a single X509Certificate because it shall be used as a RootCA without intermediate CAs.
 * 
 * <pre>
 * X509 fields:
 * Subject { CN = "name", O = "organization", C = "country" } == Issuer
 * Validity <= 10yrs
 * BasicConstraints: { Subject Type=CA, Path Length Constraint=1 }
 * KeyUage {Certificate Signing, Digital Signature }
 * SubjectKeyIdentifier { }, AuthorityKeyIdentifier { KeyID=... }
 * </pre>
 * 
 * NOTE: no crl list is used - the "revocation" takes place by removal from DNS
 * 
 * @author Peter
 * 
 */
public class CertificateAuthorityDO extends AbstractDO {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final DomainObjectField F_ACTIVE = new DomainObjectField("active",
			DomainObjectType.CertificateAuthority);
	public static final DomainObjectField F_CERTIFICATE_ID = new DomainObjectField("x509certificate-id",
			DomainObjectType.CertificateAuthority);

	//@formatter:off
	public static final class CertificateAuthoritySO {
		public static final FieldDescriptor STATE		= new FieldDescriptor(DomainObjectType.CertificateAuthority, "state", FieldType.Token);
		public static final FieldDescriptor FINGERPRINT 	= new FieldDescriptor(DomainObjectType.CertificateAuthority, "fingerprint", FieldType.String);
		public static final FieldDescriptor SUBJECT 		= new FieldDescriptor(DomainObjectType.CertificateAuthority, "subject", FieldType.String);
		public static final FieldDescriptor INFO	 		= new FieldDescriptor(DomainObjectType.CertificateAuthority, "info", FieldType.Text);
		public static final FieldDescriptor FROM	 		= new FieldDescriptor(DomainObjectType.CertificateAuthority, "from", FieldType.Date);
		public static final FieldDescriptor TO		 		= new FieldDescriptor(DomainObjectType.CertificateAuthority, "to", FieldType.Date);
	}
	//@formatter:on

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private Boolean active = Boolean.TRUE;
	private String x509certificateId;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public CertificateAuthorityDO() {
		super();
	}

	public CertificateAuthorityDO(CertificateAuthorityDO original) {
		setId(original.getId());
		setActive(original.isActive());
		setX509certificateId(original.getX509certificateId());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public DomainObjectType getType() {
		return DomainObjectType.CertificateAuthority;
	}

	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		CertificateAuthorityDO o = narrow(other);
		DomainObjectFieldChanges holder = new DomainObjectFieldChanges(this);
		setActive(conditionalSet(isActive(), o.isActive(), F_ACTIVE, holder));
		setX509certificateId(conditionalSet(getX509certificateId(), o.getX509certificateId(), F_CERTIFICATE_ID, holder));
		return holder;
	}

	@Override
	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();

		ValidationUtils.mandatoryField(isActive(), F_ACTIVE.getName(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getX509certificateId(), F_CERTIFICATE_ID.getName(), ERROR.MISSING, errors);
		return errors;
	}

	@Override
	public void updateSearchFields(ObjectRegistry registry) {
		ObjectSearchContext ctx = new ObjectSearchContext();
		ctx.sof(this, CertificateAuthoritySO.STATE, isActive() ? SearchableObjectField.TOKEN_TRUSTED
				: SearchableObjectField.TOKEN_REVOKED);

		X509CertificateDO cert = registry.getX509Certificate(getX509certificateId());
		if (cert != null) {
			PKIXCertificate c = cert.getCertificate();
			ctx.sof(this, CertificateAuthoritySO.FINGERPRINT, c.getFingerprint());
			ctx.sof(this, CertificateAuthoritySO.FROM, c.getNotBefore());
			ctx.sof(this, CertificateAuthoritySO.TO, c.getNotAfter());
			ctx.sof(this, CertificateAuthoritySO.INFO, c.getInfo());
		}
		setSearchFields(ctx.getSearchFields());
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private <E extends DomainObject> CertificateAuthorityDO narrow(E other) {
		return (CertificateAuthorityDO) other;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getX509certificateId() {
		return x509certificateId;
	}

	public void setX509certificateId(String x509certificateId) {
		this.x509certificateId = x509certificateId;
	}

}
