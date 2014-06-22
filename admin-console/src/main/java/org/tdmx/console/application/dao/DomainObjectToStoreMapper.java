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

import java.util.Map;
import java.util.Map.Entry;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.console.application.domain.CertificateAuthorityDO;
import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.domain.SystemPropertiesVO;
import org.tdmx.console.application.domain.X509CertificateDO;

public class DomainObjectToStoreMapper {

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

	public X509Certificate map(X509CertificateDO other) throws CryptoCertificateException {
		X509Certificate o = new X509Certificate();
		o.setId(other.getId());
		o.setPemValue(CertificateIOUtils.x509certToPem(other.getCertificate()));
		return o;
	}

	public ClientCA map(CertificateAuthorityDO other) {
		ClientCA o = new ClientCA();
		o.setId(other.getId());
		o.setActive(other.isActive());
		o.setX509CertificateId(other.getX509certificateId());
		return o;
	}

	public SystemPropertyList map(SystemPropertiesVO other) {
		SystemPropertyList o = new SystemPropertyList();
		Map<String, String> m = other.getProperties();
		for (Entry<String, String> e : m.entrySet()) {
			Property p = new Property();
			p.setName(e.getKey());
			p.setValue(e.getValue());
			o.getProperty().add(p);
		}
		return o;
	}

	public DNSResolverList map(DnsResolverListDO other) {
		DNSResolverList o = new DNSResolverList();
		o.setId(other.getId());
		o.setActive(other.isActive());
		o.setName(other.getName());
		if (other.getHostnames() != null) {
			o.getResolverIp().addAll(other.getHostnames());
		}
		return o;
	}

	public ServiceProvider map(ServiceProviderDO other) {
		ServiceProvider o = new ServiceProvider();
		o.setId(other.getId());

		o.setSubjectIdentity(other.getSubjectIdentifier());
		o.setApiVersion(other.getVersion());

		EndPoint masEp = new EndPoint();
		masEp.setHostname(other.getMasHostname());
		masEp.setPort(other.getMasPort());
		o.setMas(masEp);

		EndPoint mrsEp = new EndPoint();
		mrsEp.setHostname(other.getMrsHostname());
		mrsEp.setPort(other.getMrsPort());
		o.setMrs(mrsEp);

		EndPoint mosEp = new EndPoint();
		mosEp.setHostname(other.getMosHostname());
		mosEp.setPort(other.getMosPort());
		o.setMos(mosEp);

		EndPoint mdsEp = new EndPoint();
		mdsEp.setHostname(other.getMdsHostname());
		mdsEp.setPort(other.getMdsPort());
		o.setMds(mdsEp);

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
