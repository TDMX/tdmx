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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.server.pcs.ServerSessionController.ServerServiceStatistics;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.session.WebServiceSessionEndpoint;
import org.tdmx.server.ws.session.WebServiceApiName;

/**
 * 
 * @author Peter
 *
 */
public class PartitionedControlServiceImpl implements ControlService, ControlServiceListener, Manageable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(PartitionedControlServiceImpl.class);

	/**
	 * The segment is determined at startup time.
	 */
	private String segment;

	/**
	 * The number of character length
	 */
	private int sessionIdLength = 24;

	/**
	 * Map keyed by WebServiceApiName to Map keyed by sessionKey to SessionHolder.
	 * 
	 * Note: the SessionHolder holds a set of all certificate fingerprints which are related to the session, so we can
	 * easily navigate from session to certificate.
	 */
	private Map<WebServiceApiName, Map<String, SessionHolder>> sessionMap = new HashMap<>();

	/**
	 * Map keyed by Certificate fingerprint to List of SessionHolders.
	 */
	private Map<String, Set<SessionHolder>> certificateMap = new ConcurrentHashMap<>();

	/**
	 * Map keyed by WebServiceApiName to ServerApiHolder.
	 */
	private Map<WebServiceApiName, ServerApiHolder> serverMap = new HashMap<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public PartitionedControlServiceImpl() {
		// initialize sessionMap
		sessionMap.put(WebServiceApiName.MOS, new ConcurrentHashMap<>());
		sessionMap.put(WebServiceApiName.MDS, new ConcurrentHashMap<>());
		sessionMap.put(WebServiceApiName.MRS, new ConcurrentHashMap<>());
		sessionMap.put(WebServiceApiName.ZAS, new ConcurrentHashMap<>());
		// initialize serverMap
		serverMap.put(WebServiceApiName.MOS, new ServerApiHolder());
		serverMap.put(WebServiceApiName.MDS, new ServerApiHolder());
		serverMap.put(WebServiceApiName.MRS, new ServerApiHolder());
		serverMap.put(WebServiceApiName.ZAS, new ServerApiHolder());
	}

	/**
	 * A helper value type holding the SessionHandle.
	 * 
	 * @author Peter
	 *
	 */
	private static class SessionHolder {
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

		public Set<String> getCertificates() {
			return certificateSet;
		}

		public String getHttpsUrl() {
			return this.sessionEndpoint != null ? this.sessionEndpoint.getHttpsUrl() : null;
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

	/**
	 * A helper value type holding the ServerHandle.
	 * 
	 * @author Peter
	 *
	 */
	private static class ServerHolder {
		private final ServiceHandle handle;
		private final ServerSessionController ssm;

		private int loadValue;

		public ServerHolder(ServiceHandle handle, ServerSessionController ssm) {
			if (handle == null) {
				throw new IllegalArgumentException();
			}
			if (ssm == null) {
				throw new IllegalArgumentException();
			}

			this.handle = handle;
			this.ssm = ssm;
		}

		public ServiceHandle getHandle() {
			return handle;
		}

		public int getLoadValue() {
			return loadValue;
		}

		public void setLoadValue(int loadValue) {
			this.loadValue = loadValue;
		}

		public ServerSessionController getSsm() {
			return ssm;
		}
	}

	/**
	 * A helper class holding all the Server information for each API.
	 * 
	 * @author Peter
	 *
	 */
	private static class ServerApiHolder {
		private final Map<String, ServerHolder> serverMap = new ConcurrentHashMap<>();
		private final AtomicInteger totalLoad = new AtomicInteger(0);
		private int roundRobinIndex = 0;

		/**
		 * Notify that a server's load value has changed.
		 * 
		 * @param httpsUrl
		 * @param newLoadValue
		 */
		public void adjustLoad(String httpsUrl, int newLoadValue) {
			ServerHolder server = serverMap.get(httpsUrl);
			if (server != null) {
				int oldLoadFactor = server.getLoadValue();
				int difference = newLoadValue - oldLoadFactor;
				server.setLoadValue(newLoadValue);

				totalLoad.addAndGet(difference);
			}
		}

		public Map<String, ServerHolder> getServerMap() {
			return serverMap;
		}

		/**
		 * Determine the Server to use for the next new Session assignment.
		 * 
		 * Algorithm:
		 * 
		 * 1) if any server exists which has less than half the average load, then this is used immediately ( fast ramp
		 * up )
		 * 
		 * 2) round robin select from the servers which have less than the average load
		 * 
		 * 3) (unlikely) first one if every server has exactly the same load :)
		 * 
		 * 
		 * @return
		 */
		public ServerHolder getLoadBalancedServer() {
			// find the average
			int numServers = Math.max(serverMap.size(), 1);
			int aveLoad = totalLoad.get() / numServers;
			int halfAverage = aveLoad / 2;

			List<ServerHolder> potentials = new ArrayList<>();
			for (Entry<String, ServerHolder> entry : serverMap.entrySet()) {
				int serverLoadValue = entry.getValue().getLoadValue();
				if (serverLoadValue < halfAverage) {
					return entry.getValue();
				} else if (serverLoadValue < aveLoad) {
					potentials.add(entry.getValue());
				}
			}
			// case 3 - very exceptional
			if (potentials.isEmpty() && serverMap.size() > 0) {
				for (Entry<String, ServerHolder> entry : serverMap.entrySet()) {
					return entry.getValue();
				}
			}
			// case 2 - next round robin
			roundRobinIndex += 1;
			return potentials.get(roundRobinIndex % potentials.size());
		}

		public void clear() {
			serverMap.clear();
			totalLoad.set(0);
			roundRobinIndex = 0;
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

		ServerApiHolder server = serverMap.get(sessionData.getApi());

		// we lookup in the sessions for the api requested
		Map<String, SessionHolder> apiSessionMap = sessionMap.get(sessionData.getApi());
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
				ServerHolder api = server.getLoadBalancedServer();
				if (api != null) {
					// create the sessionId
					String sessionId = generateSessionId();
					existingSession.getHandle().setSessionId(sessionId);

					// associate the client with the session
					associate(existingSession, clientCertificate);

					// we need to allocate the new session for the client on a backend server with the least load
					ServerServiceStatistics stats = api.getSsm().createSession(sessionData.getApi(), sessionId,
							clientCertificate, sessionData.getSeedAttributes());
					updateServerStats(api.getHandle().getHttpsUrl(), stats);

					WebServiceSessionEndpoint wsse = new WebServiceSessionEndpoint(sessionId,
							api.getHandle().getHttpsUrl(), api.getHandle().getPublicCertificate());
					existingSession.setSessionEndpoint(wsse);
					return wsse;
				} else {
					log.warn("No " + sessionData.getApi() + " services availible.");
				}
			} else {
				// we have an existing session which is allocated on a server
				if (!existingSession.containsCertificate(clientCertificate.getFingerprint())) {
					associate(existingSession, clientCertificate);

					// add the client certificate to the backend server's existing session
					ServerHolder existingServer = server.getServerMap()
							.get(existingSession.getSessionEndpoint().getHttpsUrl());
					ServerServiceStatistics stats = existingServer.getSsm().addCertificate(sessionData.getApi(),
							existingSession.getHandle().getSessionId(), clientCertificate);
					updateServerStats(existingSession.getSessionEndpoint().getHttpsUrl(), stats);
				}
				return existingSession.getSessionEndpoint();
			}
		}
		return null;
	}

	@Override
	public void registerService(ServiceHandle service, ServerSessionController ssm) {
		Map<String, ServerHolder> serviceMap = serverMap.get(service.getApi()).getServerMap();
		if (serviceMap.get(service.getHttpsUrl()) != null) {
			log.warn("Server exists. " + service.getHttpsUrl());
		} else {
			ServerHolder holder = new ServerHolder(service, ssm);
			serviceMap.put(service.getHttpsUrl(), holder);
		}
	}

	@Override
	public void unregisterService(ServiceHandle service) {
		Map<String, ServerHolder> serviceMap = serverMap.get(service.getApi()).getServerMap();
		if (serviceMap.get(service.getHttpsUrl()) == null) {
			log.warn("Server doesn't exist. " + service.getHttpsUrl());
		} else {
			serviceMap.remove(service.getHttpsUrl());
			removeServiceSessions(service.getApi(), service.getHttpsUrl());
		}
	}

	@Override
	public void notifySessionsRemoved(WebServiceApiName api, Set<String> sessionIds) {
		Map<String, SessionHolder> apiSessionMap = sessionMap.get(api);
		for (Iterator<Map.Entry<String, SessionHolder>> it = apiSessionMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, SessionHolder> entry = it.next();

			SessionHolder session = entry.getValue();
			if (sessionIds.contains(session.getHandle().getSessionId())) {
				it.remove();
				disassociate(session);
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

	private String generateSessionId() {
		// the session Id is a
		byte[] rnd = EntropySource.getRandomBytes(getSessionIdLength() / 2);
		return ByteArray.asHex(rnd);
	}

	private void clear() {
		// clear the session info
		for (Entry<WebServiceApiName, Map<String, SessionHolder>> entry : sessionMap.entrySet()) {
			entry.getValue().clear();
		}
		// clear the certificate info
		certificateMap.clear();

		// clear server info
		for (Entry<WebServiceApiName, ServerApiHolder> entry : serverMap.entrySet()) {
			entry.getValue().clear();
		}
	}

	/**
	 * Removes all Sessions related to a Service which has disconnected.
	 * 
	 * @param api
	 * @param httpsUrl
	 */
	private void removeServiceSessions(WebServiceApiName api, String httpsUrl) {
		Map<String, SessionHolder> apiSessionMap = sessionMap.get(api);
		for (Iterator<Map.Entry<String, SessionHolder>> it = apiSessionMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, SessionHolder> entry = it.next();

			SessionHolder session = entry.getValue();
			if (httpsUrl.equals(session.getHttpsUrl())) {
				it.remove();
				disassociate(session);
			}
		}
	}

	private void disassociate(SessionHolder existingSession) {
		synchronized (certificateMap) {
			for (String fingerprint : existingSession.getCertificates()) {
				Set<SessionHolder> clientSessions = certificateMap.get(fingerprint);
				if (clientSessions != null) {
					clientSessions.remove(existingSession);
					if (clientSessions.isEmpty()) {
						certificateMap.remove(fingerprint);
					}
				}
			}
		}
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

	private void updateServerStats(String httpsUrl, ServerServiceStatistics stats) {
		ServerApiHolder s = serverMap.get(WebServiceApiName.MOS);
		if (s != null) {
			s.adjustLoad(httpsUrl, stats.mosLoadValue);
		}
		s = serverMap.get(WebServiceApiName.MDS);
		if (s != null) {
			s.adjustLoad(httpsUrl, stats.mdsLoadValue);
		}
		s = serverMap.get(WebServiceApiName.MRS);
		if (s != null) {
			s.adjustLoad(httpsUrl, stats.mrsLoadValue);
		}
		s = serverMap.get(WebServiceApiName.ZAS);
		if (s != null) {
			s.adjustLoad(httpsUrl, stats.zasLoadValue);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getSegment() {
		return segment;
	}

	public int getSessionIdLength() {
		return sessionIdLength;
	}

	public void setSessionIdLength(int sessionIdLength) {
		this.sessionIdLength = sessionIdLength;
	}

}
