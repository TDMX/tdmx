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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.job.NamedThreadFactory;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.session.WebServiceSessionEndpoint;
import org.tdmx.server.ws.session.WebServiceApiName;

/**
 * The RemoteControlService keeps track of which Sessions are attached to which Service and the Session's certificates.
 * 
 * When creating new Sessions, it determines which Service shall service the Session. The algorithm is documented in
 * {@link ServerApiHolder#getLoadBalancedServer()}.
 * 
 * Performs periodic checking of all attached ServerSessionManagers to update statistics of all Services. This is needed
 * because the individual createSession or addCertificate calls only get the load statistics back of the Service
 * affected so the PCS doesn't have a good idea of what the other Services load is. This is a performance compromise
 * because returning all the services statistics on each individual call is expected to cause too much IO.
 * 
 * @author Peter
 *
 */
public class RemoteControlServiceImpl implements ControlService, ControlServiceListener, Manageable, Runnable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RemoteControlServiceImpl.class);

	/**
	 * The segment is determined at startup time.
	 */
	private Segment segment;

	/**
	 * The number of character length
	 */
	private int sessionIdLength = 24;

	/**
	 * The maximum number of servers to consider for roundrobin load allocation. Having a small value here means that
	 * less servers are load balanced which have capacity and we can find these servers faster.
	 */
	private int maximumRoundRobinSize = 4;

	/**
	 * Map keyed by WebServiceApiName to Map keyed by sessionKey to SessionHolder.
	 * 
	 * Note: the SessionHolder holds a set of all certificate fingerprints which are related to the session, so we can
	 * easily navigate from session to certificate.
	 */
	private final Map<WebServiceApiName, Map<String, SessionHolder>> sessionMap = new HashMap<>();

	/**
	 * Map keyed by Certificate fingerprint to List of SessionHolders.
	 */
	private final Map<String, Set<SessionHolder>> certificateMap = new ConcurrentHashMap<>();

	/**
	 * Map keyed by WebServiceApiName to ServerApiHolder.
	 */
	private final Map<WebServiceApiName, ServerApiHolder> serverMap = new HashMap<>();

	/**
	 * Delay in seconds between session statistic checks.
	 */
	private int sessionStatisticsCheckIntervalSec = 60;

	// - internal
	private ScheduledExecutorService scheduledThreadPool = null;
	private ExecutorService statisticCheckExecutor = null;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

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

		public Set<String> getCertificates() {
			return certificateSet;
		}

		public String getHttpsUrl() {
			return this.sessionEndpoint != null ? this.sessionEndpoint.getHttpsUrl() : null;
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
	public static class ServerHolder {
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
	public static class ServerApiHolder {
		private final Map<String, ServerHolder> serverMap = new ConcurrentHashMap<>();
		private final AtomicInteger totalLoad = new AtomicInteger(0);
		private final int roundRobinLen;
		private int roundRobinIndex = 0;

		public ServerApiHolder(int roundRobinLen) {
			this.roundRobinLen = roundRobinLen;
		}

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
		 * 2) round robin select from up to roundRobinLen servers which have less than the average load
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
					if (potentials.size() >= roundRobinLen) {
						break;
					}
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
			roundRobinIndex %= roundRobinLen;
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
		if (segment == null || !segment.getSegmentName().equals(sessionData.getSegment())) {
			return null;
		}

		ServerApiHolder servers = serverMap.get(sessionData.getApi());

		// we lookup in the sessions for the api requested
		Map<String, SessionHolder> apiSessionMap = sessionMap.get(sessionData.getApi());
		// lookup any existing sessionholder
		SessionHolder existingSession = null;
		synchronized (this) {
			existingSession = apiSessionMap.get(sessionData.getSessionKey());
			if (existingSession == null) {
				existingSession = new SessionHolder(sessionData);
				apiSessionMap.put(existingSession.getSessionKey(), existingSession);
			}
		}

		// we've hooked the session into the map, but it may be "new" and need to allocate the endpoint
		synchronized (existingSession) {
			if (existingSession.getSessionEndpoint() == null) {
				ServerHolder api = servers.getLoadBalancedServer();
				if (api != null) {
					// create the sessionId
					String sessionId = generateSessionId();
					existingSession.getHandle().setSessionId(sessionId);

					// associate the client with the session
					associateCert(existingSession, clientCertificate);

					// we need to allocate the new session for the client on a backend server with the least load
					ServiceStatistic stat = api.getSsm().createSession(sessionData.getApi(), sessionId,
							clientCertificate, sessionData.getSeedAttributes());
					updateServerStat(stat);

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
					associateCert(existingSession, clientCertificate);

					// add the client certificate to the backend server's existing session
					ServerHolder existingServer = servers.getServerMap()
							.get(existingSession.getSessionEndpoint().getHttpsUrl());
					ServiceStatistic stat = existingServer.getSsm().addCertificate(sessionData.getApi(),
							existingSession.getHandle().getSessionId(), clientCertificate);
					updateServerStat(stat);
				}
				return existingSession.getSessionEndpoint();
			}
		}
		return null;
	}

	@Override
	public void registerServer(List<ServiceHandle> services, ServerSessionController ssm) {
		for (ServiceHandle service : services) {
			registerService(service, ssm);
		}
	}

	@Override
	public void unregisterServer(List<ServiceHandle> services) {
		for (ServiceHandle service : services) {
			unregisterService(service);
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
				disassociateSession(session);
			}
		}
	}

	@Override
	public void invalidateCertificate(PKIXCertificate cert) {
		Set<SessionHolder> sessions = certificateMap.remove(cert.getFingerprint());

		// the set of servers which need to have the certificate removed
		Set<ServerSessionController> serverControllers = new HashSet<>();

		if (sessions != null) {
			for (SessionHolder session : sessions) {
				ServerApiHolder servers = serverMap.get(session.getHandle().getApi());
				ServerHolder server = servers.getServerMap().get(session.getSessionEndpoint().getHttpsUrl());
				if (server != null) {
					// we remove the cert from the session , potentially orphaning the session which will be
					// removed by #notifySessionsRemoved later
					session.removeCertificate(cert.getFingerprint());

					// add the server to the list to remove the certificate
					serverControllers.add(server.getSsm());
				}
			}
		}

		for (ServerSessionController serverController : serverControllers) {
			ServerServiceStatistics stats = serverController.removeCertificate(cert);
			updateServerStats(stats);
		}
	}

	@Override
	public void start(Segment segment, List<WebServiceApiName> apis) {
		// the partition control service always handles ALL apis for a segment
		this.segment = segment;
		// initialize sessionMap
		sessionMap.put(WebServiceApiName.MOS, new ConcurrentHashMap<>());
		sessionMap.put(WebServiceApiName.MDS, new ConcurrentHashMap<>());
		sessionMap.put(WebServiceApiName.MRS, new ConcurrentHashMap<>());
		sessionMap.put(WebServiceApiName.ZAS, new ConcurrentHashMap<>());
		// initialize serverMap
		serverMap.put(WebServiceApiName.MOS, new ServerApiHolder(getMaximumRoundRobinSize()));
		serverMap.put(WebServiceApiName.MDS, new ServerApiHolder(getMaximumRoundRobinSize()));
		serverMap.put(WebServiceApiName.MRS, new ServerApiHolder(getMaximumRoundRobinSize()));
		serverMap.put(WebServiceApiName.ZAS, new ServerApiHolder(getMaximumRoundRobinSize()));

		clear();

		scheduledThreadPool = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("PCS-StatisticCheckScheduler"));

		statisticCheckExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("PCS-StatisticCheckExecutor"));
		scheduledThreadPool.scheduleWithFixedDelay(this, sessionStatisticsCheckIntervalSec,
				sessionStatisticsCheckIntervalSec, TimeUnit.SECONDS);

	}

	@Override
	/**
	 * Collect statistics from all WS.
	 */
	public void run() {
		// we iterate through all ServerHolders and accumulate distinct ServerSessionControllers
		// which we then get the statistic from ( for all apis )
		log.info("Gathering statistics.");
		Set<ServerSessionController> serverControllers = new HashSet<>();
		for (WebServiceApiName api : WebServiceApiName.values()) {
			ServerApiHolder servers = serverMap.get(api);
			for (Entry<String, ServerHolder> serverHolderEntry : servers.getServerMap().entrySet()) {
				serverControllers.add(serverHolderEntry.getValue().getSsm());
			}
		}

		for (ServerSessionController serverController : serverControllers) {
			ServerServiceStatistics stats = serverController.getStatistics();
			updateServerStats(stats);
			log.info("Gathered " + stats);
		}

	}

	@Override
	public void stop() {
		this.segment = null;
		clear();

		if (scheduledThreadPool != null) {
			scheduledThreadPool.shutdown();
			try {
				scheduledThreadPool.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.warn("Interrupted whilst waiting for termination of scheduledThreadPool.", e);
			}
			scheduledThreadPool = null;
		}

		if (statisticCheckExecutor != null) {
			statisticCheckExecutor.shutdown();
			try {
				statisticCheckExecutor.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.warn("Interrupted whilst waiting for termination of statisticCheckExecutor.", e);
			}
			statisticCheckExecutor = null;
		}
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

	private void registerService(ServiceHandle service, ServerSessionController ssm) {
		Map<String, ServerHolder> serviceMap = serverMap.get(service.getApi()).getServerMap();
		if (serviceMap.get(service.getHttpsUrl()) != null) {
			log.warn("Server exists. " + service.getHttpsUrl());
		} else {
			ServerHolder holder = new ServerHolder(service, ssm);
			serviceMap.put(service.getHttpsUrl(), holder);
		}
	}

	private void unregisterService(ServiceHandle service) {
		Map<String, ServerHolder> serviceMap = serverMap.get(service.getApi()).getServerMap();
		if (serviceMap.get(service.getHttpsUrl()) == null) {
			log.warn("Server doesn't exist. " + service.getHttpsUrl());
		} else {
			serviceMap.remove(service.getHttpsUrl());
			removeServiceSessions(service.getApi(), service.getHttpsUrl());
		}
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
				disassociateSession(session);
			}
		}
	}

	private void disassociateSession(SessionHolder existingSession) {
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

	private void associateCert(SessionHolder existingSession, PKIXCertificate clientCertificate) {
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

	private void updateServerStats(ServerServiceStatistics stats) {
		for (ServiceStatistic stat : stats.getStatistics()) {
			updateServerStat(stat);
		}
	}

	private void updateServerStat(ServiceStatistic stat) {
		ServerApiHolder s = serverMap.get(stat.getApi());
		s.adjustLoad(stat.getHttpsUrl(), stat.getLoadValue());
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public Segment getSegment() {
		return segment;
	}

	public int getSessionIdLength() {
		return sessionIdLength;
	}

	public void setSessionIdLength(int sessionIdLength) {
		this.sessionIdLength = sessionIdLength;
	}

	public List<SessionHolder> getApiSessions(WebServiceApiName api) {
		List<SessionHolder> result = new ArrayList<>();
		result.addAll(sessionMap.get(api).values());
		return Collections.unmodifiableList(result);
	}

	public List<String> getCertificateFingerprints() {
		List<String> result = new ArrayList<>();
		result.addAll(certificateMap.keySet());
		return Collections.unmodifiableList(result);
	}

	public List<SessionHolder> getCertificateSessions(String fingerprint) {
		List<SessionHolder> result = new ArrayList<>();
		result.addAll(certificateMap.get(fingerprint));
		return Collections.unmodifiableList(result);
	}

	public List<ServerHolder> getServers(WebServiceApiName api) {
		List<ServerHolder> result = new ArrayList<>();
		result.addAll(serverMap.get(api).serverMap.values());
		return Collections.unmodifiableList(result);
	}

	public int getTotalLoad(WebServiceApiName api) {
		return serverMap.get(api).totalLoad.get();
	}

	public int getMaximumRoundRobinSize() {
		return maximumRoundRobinSize;
	}

	public void setMaximumRoundRobinSize(int maximumRoundRobinSize) {
		this.maximumRoundRobinSize = maximumRoundRobinSize;
	}

	public int getSessionStatisticsCheckIntervalSec() {
		return sessionStatisticsCheckIntervalSec;
	}

	public void setSessionStatisticsCheckIntervalSec(int sessionStatisticsCheckIntervalSec) {
		this.sessionStatisticsCheckIntervalSec = sessionStatisticsCheckIntervalSec;
	}
}
