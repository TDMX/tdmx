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
package org.tdmx.server.rs.sas.resource;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.lib.control.domain.TrustStatus;
import org.tdmx.lib.control.domain.TrustedSslCertificate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sslcertificate")
@XmlType(name = "SslCertificate")
public class SSLCertificateResource {

	public enum FIELD {
		ID("id"),
		PEM("pem"),
		TRUST("trust"),
		COMMENT("comment"),
		FINGERPRINT("fingerprint"),
		VALIDFROM("validFrom"),
		VALIDTO("validTo"),
		DESCRIPTION("description"),;

		private final String n;

		private FIELD(String n) {
			this.n = n;
		}

		@Override
		public String toString() {
			return this.n;
		}
	}

	private Long id;
	private String pem;
	private String trust;
	private String comment;

	private String fingerprint; // R/O
	private Date validFrom; // R/O
	private Date validTo; // R/O
	private String description; // R/O

	public String getCliRepresentation() {
		StringBuilder buf = new StringBuilder();
		buf.append("SslCertificate");
		buf.append("; ").append(id);
		buf.append("; ").append(pem);
		buf.append("; ").append(trust);
		buf.append("; ").append(comment);
		buf.append("; ").append(fingerprint);
		buf.append("; ").append(description);
		return buf.toString();
	}

	public static TrustedSslCertificate mapTo(SSLCertificateResource cert) {
		if (cert == null) {
			return null;
		}
		TrustedSslCertificate c = new TrustedSslCertificate();
		c.setId(cert.getId());
		c.setTrustStatus(EnumUtils.mapTo(TrustStatus.class, cert.getTrust()));
		c.setComment(cert.getComment());

		PKIXCertificate[] certs = CertificateIOUtils.safePemToX509certs(cert.getPem());
		c.setCertificate(certs[0]);
		return c;
	}

	public static SSLCertificateResource mapFrom(TrustedSslCertificate cert) {
		if (cert == null) {
			return null;
		}
		SSLCertificateResource c = new SSLCertificateResource();
		c.setId(cert.getId());
		c.setTrust(EnumUtils.mapToString(cert.getTrustStatus()));
		c.setComment(cert.getComment());
		c.setPem(cert.getCertificatePem());
		c.setDescription(cert.getDescription());
		c.setValidFrom(cert.getValidFrom());
		c.setValidTo(cert.getValidTo());
		c.setFingerprint(cert.getFingerprint());
		return c;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPem() {
		return pem;
	}

	public void setPem(String pem) {
		this.pem = pem;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTrust() {
		return trust;
	}

	public void setTrust(String trust) {
		this.trust = trust;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

}
