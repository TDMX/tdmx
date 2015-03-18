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

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Zone;

/**
 * AuthorizationService for ZoneCredentials.
 * 
 * TODO The AuthorizationService must work in conjunction with the SecurityIncidentService to log any non successful
 * authorization attempts.
 * 
 * TODO Ip whitelisting
 * 
 * @author Peter
 * 
 */
public interface AgentCredentialAuthorizationService {

	public static enum AuthorizationFailureCode {
		/*
		 * A system problem caused authorization failure.
		 */
		SYSTEM,
		/*
		 * The certificates provided caused problems whilst processing.
		 */
		BAD_CERTIFICATE,
		/*
		 * Missing certificate
		 */
		MISSING_CERT,
		/*
		 * Non TDMX.
		 */
		NON_TDMX,
		/*
		 * The certificates provided indicate an unknown zone.
		 */
		UNKNOWN_ZONE,
		/*
		 * The certificates provided are not recognized as a valid Agent.
		 */
		UNKNOWN_AGENT,
		/*
		 * The Agent is currently suspended.
		 */
		AGENT_BLOCKED,
	}

	/**
	 * The result of an authorization check can be either successful, or provide a failure code.
	 */
	public static class AuthorizationResult {
		private final AuthorizationFailureCode failureCode;
		private final PKIXCertificate publicCertificate;
		private final AccountZone accountZone;
		private final Zone zone;

		public AuthorizationResult(PKIXCertificate publicCertificate, AccountZone accountZone, Zone zone) {
			this.publicCertificate = publicCertificate;
			this.failureCode = null;
			this.accountZone = accountZone;
			this.zone = zone;
		}

		public AuthorizationResult(AuthorizationFailureCode failureCode) {
			this.publicCertificate = null;
			this.failureCode = failureCode;
			this.accountZone = null;
			this.zone = null;
		}

		/**
		 * The public certificate of the successfully authorized agent.
		 * 
		 * @return
		 */
		public PKIXCertificate getPublicCertificate() {
			return publicCertificate;
		}

		/**
		 * The accountZone which holds what zonePartitionId is valid.
		 * 
		 * @return
		 */
		public AccountZone getAccountZone() {
			return accountZone;
		}

		/**
		 * The Zone holds the root entity of the zone data.
		 * 
		 * @return
		 */
		public Zone getZone() {
			return zone;
		}

		/**
		 * Whether there was an authentication failure.
		 * 
		 * @return null if successful, else the failure code.
		 */
		public AuthorizationFailureCode getFailureCode() {
			return failureCode;
		}

	}

	/**
	 * Whether the ZoneCredential identified by the X509Certificate chain is active.
	 * 
	 * ZoneCredential ( Users / DomainAdministrators / ZoneAdministrators ) may be suspended.
	 * 
	 * @param certChain
	 * @return the authorization result.
	 */
	public AuthorizationResult isAuthorized(X509Certificate[] certChain);

}
