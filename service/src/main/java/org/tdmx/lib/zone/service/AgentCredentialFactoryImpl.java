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

import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialType;

/**
 * Implementation of the AgentCredentialValidator.
 * 
 * AgentCredentials are pkix validated (signature chain and time validity) only when they are "created", otherwise we
 * allow to search for previously created credentials even if they have become invalid due to time.
 * 
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
	private static final Logger log = LoggerFactory.getLogger(AgentCredentialValidatorImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	@Override
	public AgentCredentialDescriptor createAgentCredential(X509Certificate... certChain) {
		PKIXCertificate[] chain;
		try {
			chain = CertificateIOUtils.convert(certChain);
		} catch (CryptoCertificateException e) {
			log.info("Unable to convert X509 certificate.", e);
			return null;
		}
		return createAgentCredential(chain);
	}

	@Override
	public AgentCredentialDescriptor createAgentCredential(PKIXCertificate... certChain) {
		if (certChain == null || certChain.length == 0 || certChain.length > 3) {
			log.info("Invalid parameters provided to createAgentCredential");
			return null;
		}
		try {
			AgentCredentialDescriptor acd = new AgentCredentialDescriptor();
			acd.setCertificateChain(certChain);
			acd.setCertificateChainPem(CertificateIOUtils.x509certsToPem(certChain));

			PKIXCertificate publicKey = certChain[0];
			acd.setFingerprint(publicKey.getFingerprint());

			if (publicKey.isTdmxZoneAdminCertificate()) {
				if (certChain.length != 1) {
					return null;
				}
				acd.setCredentialType(AgentCredentialType.ZAC);
				acd.setZoneApex(publicKey.getTdmxZoneInfo().getZoneRoot());
				if (!CredentialUtils.isValidZoneAdministratorCertificate(certChain[0])) {
					log.info("Invalid ZAC PKIX Certificate.");
					return null;
				}

			} else if (publicKey.isTdmxDomainAdminCertificate()) {
				if (certChain.length != 2) {
					return null;
				}
				acd.setCredentialType(AgentCredentialType.DAC);
				acd.setZoneApex(publicKey.getTdmxZoneInfo().getZoneRoot());
				acd.setDomainName(publicKey.getCommonName());
				if (!CredentialUtils.isValidDomainAdministratorCertificate(certChain[1], certChain[0])) {
					log.info("Invalid DAC PKIX CertificateChain.");
					return null;
				}

			} else if (publicKey.isTdmxUserCertificate()) {
				if (certChain.length != 3) {
					return null;
				}
				acd.setCredentialType(AgentCredentialType.UC);
				acd.setZoneApex(publicKey.getTdmxZoneInfo().getZoneRoot());
				acd.setAddressName(publicKey.getCommonName());
				acd.setDomainName(certChain[1].getCommonName());
				if (!CredentialUtils.isValidUserCertificate(certChain[2], certChain[1], certChain[0])) {
					log.info("Invalid User PKIX CertificateChain.");
					return null;
				}

			} else {
				log.info("Invalid AgentCredentialType.");
				return null;
			}

			return acd;
		} catch (CryptoCertificateException e) {
			log.info("Invalid Certificate " + e.getRc());
		}
		return null;
	}

	@Override
	public AgentCredentialDescriptor createAgentCredential(byte[]... certChain) {
		PKIXCertificate[] chain;
		try {
			chain = new PKIXCertificate[certChain.length];
			for (int i = 0; i < certChain.length; i++) {
				chain[i] = CertificateIOUtils.decodeX509(certChain[i]);
			}
		} catch (CryptoCertificateException e) {
			log.info("Invalid Certificate " + e.getRc());
			return null;
		}
		return createAgentCredential(chain);
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
