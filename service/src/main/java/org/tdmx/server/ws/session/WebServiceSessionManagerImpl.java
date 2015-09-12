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
package org.tdmx.server.ws.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.ws.ServerRuntimeContextService;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;

public class WebServiceSessionManagerImpl<E extends WebServiceSession>
		implements WebServiceSessionManager, WebServiceSessionLookupService<E>, WebServiceSessionTrustManager {

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
	private final Map<String, Set<String>> certificateSessionMap = new ConcurrentHashMap<>();

	/**
	 * A Map of sessionId to WebServiceSession.
	 */
	private final Map<String, E> sessionMap = new ConcurrentHashMap<>();

	private WebServiceApiName apiName;
	private String apiEndpointPath;

	private ServerRuntimeContextService runtimeService;

	private WebServiceSessionFactory<E> sessionFactory;

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
	public int createSession(String sessionId, String controllerId, PKIXCertificate cert,
			Map<SeedAttribute, Long> seedAttributes) {
		E ss = sessionFactory.createServerSession(sessionId, seedAttributes);
		ss.setControllerId(controllerId);
		ss.addAuthorizedCertificate(cert);

		sessionMap.put(sessionId, ss);
		associate(sessionId, cert);
		return getSessionCount();
	}

	@Override
	public void disconnectController(String controllerId) {
		if (controllerId == null) {
			return;
		}
		List<String> sessionIds = new ArrayList<>();
		for (Entry<String, E> session : sessionMap.entrySet()) {
			if (controllerId.equals(session.getValue().getControllerId())) {
				sessionIds.add(session.getKey());
			}
		}
		for (String sessionId : sessionIds) {
			deleteSession(sessionId);
		}
	}

	@Override
	public int addCertificate(String sessionId, PKIXCertificate cert) {
		E ss = sessionMap.get(sessionId);
		ss.addAuthorizedCertificate(cert);
		associate(sessionId, cert);
		return getSessionCount();
	}

	@Override
	public int removeCertificate(PKIXCertificate cert) {
		Set<String> sessionIds = certificateSessionMap.remove(cert.getFingerprint());
		if (sessionIds != null) {
			for (String sessionId : sessionIds) {
				WebServiceSession ss = sessionMap.get(sessionId);
				ss.removeAuthorizedCertificate(cert);
			}
		}
		return getSessionCount();
	}

	@Override
	public E getSession(String sessionId, PKIXCertificate cert) {
		E ss = sessionMap.get(sessionId);
		if (ss != null) {
			Set<String> sessionIds = certificateSessionMap.get(cert.getFingerprint());
			if (sessionIds != null && sessionIds.contains(sessionId)) {
				ss.setLastUsedTimestamp(new Date());
				return ss;
			}
		}
		return null;
	}

	@Override
	public List<WebServiceSession> getIdleSessions(Date lastCutoffDate, Date creationCutoffDate) {
		List<WebServiceSession> result = new ArrayList<>();
		for (Entry<String, E> e : sessionMap.entrySet()) {
			WebServiceSession ss = e.getValue();
			if (creationCutoffDate != null && creationCutoffDate.before(ss.getCreationTimestamp())) {
				result.add(ss);
				continue;
			}
			if (lastCutoffDate != null && lastCutoffDate.after(ss.getLastUsedTimestamp())) {
				result.add(ss);
				continue;
			}
		}
		return result;
	}

	@Override
	public String getHttpsUrl() {
		StringBuilder url = new StringBuilder();
		url.append("https://").append(runtimeService.getServerLocalIPAddress()).append(":")
				.append(runtimeService.getHttpsPort());
		url.append("/").append(runtimeService.getContextPath());
		url.append("/").append(apiEndpointPath);
		return url.toString();
	}

	@Override
	public WebServiceApiName getApiName() {
		return apiName;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private int getSessionCount() {
		return sessionMap.size();
	}

	private void deleteSession(String sessionId) {
		E ss = sessionMap.remove(sessionId);
		if (ss != null) {
			for (PKIXCertificate authCert : ss.getAuthorizedCertificates()) {
				disassociate(sessionId, authCert);
			}
		}
	}

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

	public WebServiceSessionFactory<E> getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(WebServiceSessionFactory<E> sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setApiName(WebServiceApiName apiName) {
		this.apiName = apiName;
	}

	public void setRuntimeService(ServerRuntimeContextService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public void setApiEndpointPath(String apiEndpointPath) {
		this.apiEndpointPath = apiEndpointPath;
	}
}
