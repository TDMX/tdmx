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

package org.tdmx.server.ws.security.service;

import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;

/**
 * AgentCredential Authorization Service can ascertain if an Agent exists and is valid.
 * 
 * @author Peter Klauser
 * 
 */
public class AgentCredentialAuthorizationServiceImpl implements AgentCredentialAuthorizationService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(AgentCredentialAuthorizationServiceImpl.class);

	private AgentCredentialService agentCredentialService;
	private AgentCredentialFactory agentCredentialFactory;
	private AccountZoneService accountZoneService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	// TODO caching

	@Override
	public AuthorizationResult isAuthorized(X509Certificate[] certChain) {
		if (certChain == null || certChain.length < 1) {
			return new AuthorizationResult(AuthorizationFailureCode.MISSING_CERT);
		}
		PKIXCertificate[] chain;
		try {
			chain = CertificateIOUtils.convert(certChain);
		} catch (CryptoCertificateException e) {
			log.warn("Unable to convert X509 certificate.", e);
			return new AuthorizationResult(AuthorizationFailureCode.BAD_CERTIFICATE);
		}
		PKIXCertificate cert = chain[0];

		if (cert.getTdmxZoneInfo() == null) {
			return new AuthorizationResult(AuthorizationFailureCode.NON_TDMX);
		}
		String zoneApex = cert.getTdmxZoneInfo().getZoneRoot();
		AccountZone accountZone = accountZoneService.findByZoneApex(zoneApex);
		if (accountZone == null) {
			return new AuthorizationResult(AuthorizationFailureCode.UNKNOWN_ZONE);
		}

		String fingerprint = cert.getFingerprint();
		AgentCredential agentCredential = getAgentCredentialService().findByFingerprint(fingerprint);
		if (agentCredential == null) {
			return new AuthorizationResult(AuthorizationFailureCode.UNKNOWN_AGENT);
		}
		if (AgentCredentialStatus.ACTIVE != agentCredential.getCredentialStatus()) {
			return new AuthorizationResult(AuthorizationFailureCode.AGENT_BLOCKED);
		}
		return new AuthorizationResult(cert, accountZone);
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

	public AgentCredentialService getAgentCredentialService() {
		return agentCredentialService;
	}

	public void setAgentCredentialService(AgentCredentialService agentCredentialService) {
		this.agentCredentialService = agentCredentialService;
	}

	public AccountZoneService getAccountZoneService() {
		return accountZoneService;
	}

	public void setAccountZoneService(AccountZoneService accountZoneService) {
		this.accountZoneService = accountZoneService;
	}

	public AgentCredentialFactory getAgentCredentialFactory() {
		return agentCredentialFactory;
	}

	public void setAgentCredentialFactory(AgentCredentialFactory agentCredentialFactory) {
		this.agentCredentialFactory = agentCredentialFactory;
	}
}
