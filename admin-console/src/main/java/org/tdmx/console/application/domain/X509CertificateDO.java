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

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.console.application.search.FieldDescriptor;
import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.service.ObjectRegistry;

/**
 * An X509Certificate.
 * 
 * @author Peter
 * 
 */
public class X509CertificateDO extends AbstractDO {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final DomainObjectField F_CERTIFICATE = new DomainObjectField("certificate",
			DomainObjectType.X509Certificate);

	public static final class X509CertificateSO {
		public static final FieldDescriptor FINGERPRINT = new FieldDescriptor(DomainObjectType.X509Certificate,
				"fingerprint", FieldType.String);
		public static final FieldDescriptor INFO = new FieldDescriptor(DomainObjectType.X509Certificate, "info",
				FieldType.Text);
		public static final FieldDescriptor FROM = new FieldDescriptor(DomainObjectType.X509Certificate, "from",
				FieldType.Date);
		public static final FieldDescriptor TO = new FieldDescriptor(DomainObjectType.X509Certificate, "to",
				FieldType.Date);
	}

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final PKIXCertificate certificate;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public X509CertificateDO(PKIXCertificate certificate) {
		setId(certificate.getFingerprint());
		this.certificate = certificate;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public DomainObjectType getType() {
		return DomainObjectType.X509Certificate;
	}

	@Override
	public void updateSearchFields(ObjectRegistry registry) {
		ObjectSearchContext ctx = new ObjectSearchContext();
		ctx.sof(this, X509CertificateSO.FINGERPRINT, getId());
		ctx.sof(this, X509CertificateSO.FROM, certificate.getNotBefore());
		ctx.sof(this, X509CertificateSO.TO, certificate.getNotAfter());
		ctx.sof(this, X509CertificateSO.INFO, certificate.getInfo());
		setSearchFields(ctx.getSearchFields());
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

	public PKIXCertificate getCertificate() {
		return certificate;
	}

}
