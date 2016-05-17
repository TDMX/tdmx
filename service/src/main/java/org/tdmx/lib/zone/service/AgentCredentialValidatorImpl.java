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
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;

/**
 * Factory for AgentCredential Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class AgentCredentialValidatorImpl implements AgentCredentialValidator {

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
	public boolean isValid(AgentCredentialDescriptor agentCredentialDescriptor) {
		if (agentCredentialDescriptor == null) {
			return false;
		}
		PKIXCertificate[] certChain = agentCredentialDescriptor.getCertificateChain();
		if (certChain == null || certChain.length == 0) {
			return false;
		}
		PKIXCertificate publicKey = certChain[0];
		try {
			if (publicKey.isTdmxZoneAdminCertificate()) {
				return CredentialUtils.isValidZoneAdministratorCertificate(certChain[0]);
			} else if (publicKey.isTdmxDomainAdminCertificate() && certChain.length == 2) {
				return CredentialUtils.isValidDomainAdministratorCertificate(certChain[1], certChain[0]);
			} else if (publicKey.isTdmxUserCertificate() && certChain.length == 3) {
				return CredentialUtils.isValidUserCertificate(certChain[2], certChain[1], certChain[0]);
			}
		} catch (CryptoCertificateException e) {
			log.warn("Unexpected CryptoException.", e);
			return false;
		}

		return false;

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
