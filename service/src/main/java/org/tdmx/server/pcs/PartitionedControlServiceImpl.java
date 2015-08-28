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
package org.tdmx.server.pcs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.session.WebServiceSessionEndpoint;
import org.tdmx.server.ws.session.WebServiceApiName;

/**
 * 
 * @author Peter
 *
 */
public class PartitionedControlServiceImpl implements ControlService, Manageable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(PartitionedControlServiceImpl.class);

	/**
	 * Map keyed by WebServiceApiName to Map keyed by sessionKey to SessionHolder.
	 */
	private Map<WebServiceApiName, Map<String, SessionHolder>> sessionMap = new HashMap<>();

	/**
	 * Map keyed by Certificate fingerprint to List of SessionHolders.
	 */
	private Map<String, Set<SessionHolder>> certificateMap = new ConcurrentHashMap<>();

	/**
	 * The segment is determined at startup time.
	 */
	private String segment;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public PartitionedControlServiceImpl() {
		sessionMap.put(WebServiceApiName.MOS, new ConcurrentHashMap<>());
		sessionMap.put(WebServiceApiName.MDS, new ConcurrentHashMap<>());
		sessionMap.put(WebServiceApiName.MRS, new ConcurrentHashMap<>());
		sessionMap.put(WebServiceApiName.ZAS, new ConcurrentHashMap<>());
	}

	/**
	 * A helper value type holding the SessionHandle.
	 * 
	 * @author Peter
	 *
	 */
	public static class SessionHolder {
		private final SessionHandle handle;
		/**
		 * Set of Certificate fingerprints which share this Session.
		 */
		private Set<String> certificateSet = new HashSet<>();

		/**
		 * Where the session is situated ( running on a server ).
		 */
		private WebServiceSessionEndpoint sessionEndpoint;

		public SessionHolder(SessionHandle handle) {
			if (handle == null) {
				throw new IllegalArgumentException();
			}
			this.handle = handle;
		}

		public SessionHandle getHandle() {
			return handle;
		}

		public String getSessionKey() {
			return this.handle.getSessionKey();
		}

		public boolean hasCertificates() {
			return !certificateSet.isEmpty();
		}

		public void addCertificate(String fingerprint) {
			certificateSet.add(fingerprint);
		}

		public void removeCertificate(String fingerprint) {
			certificateSet.remove(fingerprint);
		}

		public boolean containsCertificate(String fingerprint) {
			return certificateSet.contains(fingerprint);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((handle.getSessionKey() == null) ? 0 : handle.getSessionKey().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SessionHolder other = (SessionHolder) obj;
			if (handle.getSessionKey() == null) {
				if (other.handle.getSessionKey() != null)
					return false;
			} else if (!handle.getSessionKey().equals(other.handle.getSessionKey()))
				return false;
			return true;
		}

		public WebServiceSessionEndpoint getSessionEndpoint() {
			return sessionEndpoint;
		}

		public void setSessionEndpoint(WebServiceSessionEndpoint sessionEndpoint) {
			this.sessionEndpoint = sessionEndpoint;
		}

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public WebServiceSessionEndpoint associateApiSession(SessionHandle sessionData, PKIXCertificate clientCertificate) {
		// first we check if we are allowed to handle the segment which is provided, or we are not "started"
		if (segment == null || !segment.equals(sessionData.getSegment())) {
			return null;
		}

		// we lookup in the sessions for the api requested
		Map<String, SessionHolder> apiSessionMap = sessionMap.get(sessionData.getApi());
		if (apiSessionMap != null) {
			// lookup any existing sessionholder
			SessionHolder existingSession = null;
			synchronized (this) {
				apiSessionMap.get(sessionData.getSessionKey());
				if (existingSession == null) {
					existingSession = new SessionHolder(sessionData);
					apiSessionMap.put(existingSession.getSessionKey(), existingSession);
				}
			}

			// we've hooked the session into the map, but it may be "new" and need to allocate the endpoint
			synchronized (existingSession) {
				if (existingSession.getSessionEndpoint() == null) {
					// associate the client with the session
					associate(existingSession, clientCertificate);

					// TODO we need to allocate the new session for the client on a backend server with the least load

					WebServiceSessionEndpoint wsse = new WebServiceSessionEndpoint(null, null, null);
					existingSession.setSessionEndpoint(wsse);
					return wsse;
				} else {
					// we have an existing session which is allocated on a server
					if (!existingSession.containsCertificate(clientCertificate.getFingerprint())) {
						associate(existingSession, clientCertificate);

						// TODO add the client certificate to the backend server's existing session
					}
				}
			}
		}
		return null;
	}

	private void associate(SessionHolder existingSession, PKIXCertificate clientCertificate) {
		synchronized (certificateMap) {
			existingSession.addCertificate(clientCertificate.getFingerprint());
			Set<SessionHolder> clientSessions = certificateMap.get(clientCertificate.getFingerprint());
			if (clientSessions == null) {
				clientSessions = new HashSet<>();
				clientSessions.add(existingSession);
				certificateMap.put(clientCertificate.getFingerprint(), clientSessions);
			}
		}
	}

	@Override
	public void start(String segment, List<WebServiceApiName> apis) {
		// the partition control service always handles ALL apis for a segment
		this.segment = segment;
		clear();
	}

	@Override
	public void stop() {
		this.segment = null;
		clear();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void clear() {
		for (Entry<WebServiceApiName, Map<String, SessionHolder>> entry : sessionMap.entrySet()) {
			entry.getValue().clear();
		}
		certificateMap.clear();
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getSegment() {
		return segment;
	}

}
