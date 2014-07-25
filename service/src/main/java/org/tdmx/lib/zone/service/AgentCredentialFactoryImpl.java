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
import org.tdmx.lib.zone.domain.AgentCredentialID;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.domain.AgentCredentialType;

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
	public AgentCredential createAgentCredential(PKIXCertificate[] certificateChain, AgentCredentialStatus status) {
		if (certificateChain == null || certificateChain.length < 1) {
			log.error("createAgentCredential called without certificateChain.");
			return null;
		}
		PKIXCertificate publicKey = certificateChain[0];

		AgentCredentialID id = new AgentCredentialID(publicKey.getTdmxZoneInfo().getZoneRoot(),
				publicKey.getFingerprint());
		AgentCredential c = new AgentCredential(id);
		c.setCredentialStatus(status);

		if (publicKey.isTdmxZoneAdminCertificate()) {
			c.setCredentialType(AgentCredentialType.ZAC);
		} else if (publicKey.isTdmxDomainAdminCertificate()) {
			c.setCredentialType(AgentCredentialType.DAC);
		} else if (publicKey.isTdmxUserCertificate()) {
			c.setCredentialType(AgentCredentialType.UC);
		} else {
			log.error("createAgentCredential called with non TDMX certificateChain.");
			return null;
		}

		try {
			c.setCertificateChainPem(CertificateIOUtils.x509certsToPem(certificateChain));
		} catch (CryptoCertificateException e) {
			log.error("createAgentCredential failed to serialize certificateChain.", e);
			return null;
		}

		return c;
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
