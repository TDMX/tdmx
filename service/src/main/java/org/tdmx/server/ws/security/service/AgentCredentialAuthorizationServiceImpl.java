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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneSearchCriteria;
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
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// TODO #25 implement caching ehCache?

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
		String fingerprint = cert.getFingerprint();

		String zoneApex = cert.getTdmxZoneInfo().getZoneRoot();

		// different accounts could have the same zone on different zoneDBs
		AccountZoneSearchCriteria sc = new AccountZoneSearchCriteria(new PageSpecifier(0, 4));
		sc.setZoneApex(zoneApex);
		List<AccountZone> accountZones = accountZoneService.search(sc);
		if (accountZones.isEmpty()) {
			return new AuthorizationResult(AuthorizationFailureCode.UNKNOWN_ZONE);
		}

		AgentCredential agentCredential = null;
		AccountZone agentAccountZone = null;
		for (AccountZone accountZone : accountZones) {
			try {
				getZonePartitionIdProvider().setPartitionId(accountZone.getZonePartitionId());
				agentCredential = getAgentCredentialService().findByFingerprint(accountZone.getZoneReference(),
						fingerprint);
				if (agentCredential != null) {
					agentAccountZone = accountZone;
					break;
				}
			} finally {
				getZonePartitionIdProvider().clearPartitionId();
			}
		}
		if (agentCredential == null || agentAccountZone == null) {
			return new AuthorizationResult(AuthorizationFailureCode.UNKNOWN_AGENT);
		}
		if (AgentCredentialStatus.ACTIVE != agentCredential.getCredentialStatus()) {
			return new AuthorizationResult(AuthorizationFailureCode.AGENT_BLOCKED);
		}
		// check exact match of the certificates stored and provided - since a SHA1 match is not enough
		// to prove we "know" the client
		PKIXCertificate storedPublicKey = agentCredential.getPublicKey();
		if (storedPublicKey == null) {
			log.warn("Stored public key missing for credential with fingerprint=" + fingerprint);
			return new AuthorizationResult(AuthorizationFailureCode.SYSTEM);

		} else if (!storedPublicKey.isIdentical(cert)) {
			log.warn("Certificate unequal but matched fingerprint=" + fingerprint + " suspect cert: " + cert);
			return new AuthorizationResult(AuthorizationFailureCode.BAD_CERTIFICATE);
		}
		return new AuthorizationResult(cert, agentAccountZone);
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

	public ThreadLocalPartitionIdProvider getZonePartitionIdProvider() {
		return zonePartitionIdProvider;
	}

	public void setZonePartitionIdProvider(ThreadLocalPartitionIdProvider zonePartitionIdProvider) {
		this.zonePartitionIdProvider = zonePartitionIdProvider;
	}

}
