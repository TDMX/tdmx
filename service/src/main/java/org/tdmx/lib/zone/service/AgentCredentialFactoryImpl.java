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

package org.tdmx.lib.zone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.zone.domain.AgentCredential;

/**
 * Factory for AgentCredential Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class AgentCredentialFactoryImpl implements AgentCredentialFactory {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(AgentCredentialFactoryImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	@Override
	public AgentCredential createAgentCredential(PKIXCertificate[] certificateChain) {
		if (certificateChain == null || certificateChain.length <= 0) {
			log.error("certificateChain missing");
			return null;
		}
		try {
			AgentCredential c = new AgentCredential(certificateChain);
			if (c.getCredentialType() == null) {
				log.error("Invalid AgentCredentialType.");
				return null;
			}
			return c;
		} catch (CryptoCertificateException e) {
			log.error("Unable to createAgentCredential.", e);
		}
		return null;
	}

	@Override
	public AgentCredential createDAC(byte[] domainCert, byte[] zacCert) {
		try {
			PKIXCertificate dc = CertificateIOUtils.decodeX509(domainCert);

			PKIXCertificate zc = CertificateIOUtils.decodeX509(zacCert);

			AgentCredential c = new AgentCredential(new PKIXCertificate[] { dc, zc });
			if (c.getCredentialType() == null) {
				log.info("Invalid AgentCredentialType.");
				return null;
			}
			return c;
		} catch (CryptoCertificateException e) {
			log.info("Invalid Certificate " + e.getRc());
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

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
