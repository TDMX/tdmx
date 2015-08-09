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
package org.tdmx.server.session;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.session.ServerSessionFactory.SeedAttribute;

public class ServerSessionManagerImpl<E extends ServerSession> implements ServerSessionManager,
		ServerSessionLookupService<E>, ServerSessionTrustManager {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	/**
	 * A Map of Certificate fingerprint to Certificate
	 */
	private final Map<String, PKIXCertificate> certificateMap = new ConcurrentHashMap<>();

	/**
	 * A Map of Certificate fingerprint to sessionID.
	 */
	private final Map<String, Set<String>> certificateSessionMap = new ConcurrentHashMap<>();;

	/**
	 * A Map of sessionId to ServerSession.
	 */
	private final Map<String, E> sessionMap = new ConcurrentHashMap<>();

	private ServerSessionFactory<E> sessionFactory;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public boolean isTrusted(PKIXCertificate cert) {
		if (cert == null) {
			return false;
		}
		PKIXCertificate existingCert = certificateMap.get(cert.getFingerprint());
		return existingCert != null && existingCert.isIdentical(cert);
	}

	@Override
	public void createSession(String sessionId, PKIXCertificate cert, Map<SeedAttribute, Long> seedAttributes) {

		E ss = sessionFactory.createServerSession(seedAttributes);
		ss.addAuthorizedCertificate(cert);

		sessionMap.put(sessionId, ss);
		associate(sessionId, cert);
	}

	@Override
	public void addCertificate(String sessionId, PKIXCertificate cert) {
		E ss = sessionMap.get(sessionId);
		ss.addAuthorizedCertificate(cert);
		associate(sessionId, cert);
	}

	@Override
	public void removeCertificate(String sessionId, PKIXCertificate cert) {
		ServerSession ss = sessionMap.get(sessionId);
		ss.removeAuthorizedCertificate(cert);

		disassociate(sessionId, cert);
	}

	@Override
	public int getSessionCount() {
		return sessionMap.size();
	}

	@Override
	public E getSession(String sessionId, PKIXCertificate cert) {
		E ss = sessionMap.get(sessionId);
		if (ss != null) {
			Set<String> sessionIds = certificateSessionMap.get(cert.getFingerprint());
			if (sessionIds != null && sessionIds.contains(sessionId)) {
				return ss;
			}
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private synchronized void associate(String sessionId, PKIXCertificate cert) {
		certificateMap.put(cert.getFingerprint(), cert);
		Set<String> certSessionList = certificateSessionMap.get(cert.getFingerprint());
		if (certSessionList == null) {
			certSessionList = new HashSet<>();
			certificateSessionMap.put(cert.getFingerprint(), certSessionList);
		}
		certSessionList.add(sessionId);
	}

	private synchronized void disassociate(String sessionId, PKIXCertificate cert) {
		Set<String> certSessionList = certificateSessionMap.get(cert.getFingerprint());
		if (certSessionList != null) {
			certSessionList.remove(sessionId);
			// no sessions left - remove the certificate completely.
			if (certSessionList.size() == 0) {
				certificateMap.remove(cert.getFingerprint());
			}
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public ServerSessionFactory<E> getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(ServerSessionFactory<E> sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
