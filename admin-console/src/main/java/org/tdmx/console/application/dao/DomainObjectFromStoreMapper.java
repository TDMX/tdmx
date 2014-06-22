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
package org.tdmx.console.application.dao;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CertificateResultCode;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.console.application.domain.CertificateAuthorityDO;
import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.domain.SystemPropertiesVO;
import org.tdmx.console.application.domain.X509CertificateDO;

public class DomainObjectFromStoreMapper {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	public X509CertificateDO map(X509Certificate other) throws CryptoCertificateException {
		String id = other.getId();

		PKIXCertificate cert = CertificateIOUtils.pemToX509cert(other.getPemValue());

		X509CertificateDO o = new X509CertificateDO(cert);
		if (!o.getId().equals(id)) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_FINGERPRINT_TAMPERING);
		}
		return o;
	}

	public CertificateAuthorityDO map(ClientCA other) {
		CertificateAuthorityDO o = new CertificateAuthorityDO();
		o.setId(other.getId());
		o.setActive(other.isActive());
		o.setX509certificateId(other.getX509CertificateId());
		return o;
	}

	public DnsResolverListDO map(DNSResolverList other) {
		DnsResolverListDO o = new DnsResolverListDO();
		if (other.getId() != null) {
			o.setId(other.getId());
		}

		o.setName(other.getName());
		o.setHostnames(other.getResolverIp());
		o.setActive(other.isActive());
		return o;
	}

	public SystemPropertiesVO map(SystemPropertyList other) {
		if (other == null) {
			return null;
		}
		SystemPropertiesVO o = new SystemPropertiesVO();
		for (Property p : other.getProperty()) {
			o.add(p.getName(), p.getValue());
		}
		return o;
	}

	public ServiceProviderDO map(ServiceProvider other) {
		ServiceProviderDO o = new ServiceProviderDO();
		if (other.getId() != null) {
			o.setId(other.getId());
		}

		o.setSubjectIdentifier(other.getSubjectIdentity());
		o.setVersion(other.getApiVersion());

		if (other.getMas() != null) {
			o.setMasHostname(other.getMas().getHostname());
			o.setMasPort(other.getMas().getPort());
		}
		o.setMasStatus(null);

		if (other.getMrs() != null) {
			o.setMrsHostname(other.getMrs().getHostname());
			o.setMrsPort(other.getMrs().getPort());
		}
		o.setMrsStatus(null);

		if (other.getMos() != null) {
			o.setMosHostname(other.getMos().getHostname());
			o.setMosPort(other.getMos().getPort());
		}
		o.setMosStatus(null);

		if (other.getMds() != null) {
			o.setMdsHostname(other.getMds().getHostname());
			o.setMdsPort(other.getMds().getPort());
		}
		o.setMdsStatus(null);

		// TODO domain
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

}
