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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.job.NamedThreadFactory;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.ROSClient.RelayStatistic;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.ws.session.WebServiceApiName;

/**
 * The RelayControlService keeps track of which ROS servers are managing which channels.
 * 
 * When creating new Relay Sessions for channels, it determines which ROS shall service the Session.
 * 
 * Performs periodic checking of all attached ROS to update load statistics. This is needed because the individual
 * createRelaySession calls only get the load statistics back of the ROS chosen so the PCS doesn't have a good idea of
 * what the other ROS's load are.
 * 
 * @author Peter
 *
 */
public class RelayControlServiceImpl implements Manageable, Runnable, RelayControlService, RelayControlServiceListener {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayControlServiceImpl.class);

	/**
	 * The segment is determined at startup time.
	 */
	private Segment segment;

	/**
	 * The attached ROS server's mapped by their rosTcpEndpoint addresses.
	 */
	private final Map<String, ServerHolder> serverMap = new ConcurrentHashMap<>();

	/**
	 * The total load over all ROS servers.
	 */
	private final AtomicInteger totalLoad = new AtomicInteger(0);

	/**
	 * An index for round robin load balancing.
	 */
	private int roundRobinIndex = 0;

	/**
	 * The maximum number of servers to consider for roundrobin load allocation. Having a small value here means that
	 * less servers are load balanced which have capacity and we can find these servers faster.
	 */
	private int maximumRoundRobinSize = 4;

	/**
	 * Map keyed by ChannelKey to SessionHolder.
	 * 
	 * Note: the SessionHolder holds the ROS client address and any cached MRS sessionId.
	 */
	private final Map<String, SessionHolder> sessionMap = new HashMap<>();

	/**
	 * Delay in seconds between load statistic checks.
	 */
	private int loadStatisticsCheckIntervalSec = 60;

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

		private String rosTcpEndpoint;

		public String getRosTcpEndpoint() {
			return rosTcpEndpoint;
		}

		public void setRosTcpEndpoint(String rosTcpEndpoint) {
			this.rosTcpEndpoint = rosTcpEndpoint;
		}
	}

	/**
	 * A helper value type holding the reverse RPC service to the ROS and it's load.
	 * 
	 * @author Peter
	 *
	 */
	public static class ServerHolder {
		private final String rosTcpEndpoint;
		private final RelayOutboundServiceController ros;

		private int loadValue;

		public ServerHolder(String rosTcpEndpoint, RelayOutboundServiceController ros) {
			if (rosTcpEndpoint == null) {
				throw new IllegalArgumentException();
			}
			if (ros == null) {
				throw new IllegalArgumentException();
			}

			this.rosTcpEndpoint = rosTcpEndpoint;
			this.ros = ros;
		}

		public int getLoadValue() {
			return loadValue;
		}

		public void setLoadValue(int loadValue) {
			this.loadValue = loadValue;
		}

		public String getRosTcpEndpoint() {
			return rosTcpEndpoint;
		}

		public RelayOutboundServiceController getRos() {
			return ros;
		}

	}

	/**
	 * Determine the Server to use for the next new Session assignment.
	 * 
	 * Algorithm:
	 * 
	 * 1) if any server exists which has less than half the average load, then this is used immediately ( fast ramp up )
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
				if (potentials.size() >= maximumRoundRobinSize) {
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
		roundRobinIndex %= maximumRoundRobinSize;
		return potentials.get(roundRobinIndex % potentials.size());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String assignRelayServer(String channelKey, String callerSegment, Map<AttributeId, Long> attributes) {
		if (segment == null || !segment.getSegmentName().equals(callerSegment)) {
			return null;
		}

		SessionHolder existingSession = getSessionHolder(channelKey);

		// we've hooked the session into the map, but it may be "new" and need to allocate the endpoint
		// since it takes some time, we want to make sure we only do it once when called concurrently.
		synchronized (existingSession) {
			if (existingSession.getRosTcpEndpoint() == null) {
				ServerHolder ros = getLoadBalancedServer();
				if (ros != null) {
					existingSession.setRosTcpEndpoint(ros.getRosTcpEndpoint());

					// we need to allocate the new relay session for the client on a ROS server with the least load
					RelayStatistic stat = ros.getRos().createRelaySession(channelKey, attributes);
					if (stat != null) {
						updateServerStats(ros, stat.getLoadValue());

						existingSession.setRosTcpEndpoint(ros.getRosTcpEndpoint());
					} else {
						log.warn("Unable to create relay session on remote ROS server "
								+ existingSession.getRosTcpEndpoint());
					}
				} else {
					log.warn("No relay servers available.");
				}
			} else {
				// we have an existing session which is allocated on a server
				log.debug("Found exising ROS session " + channelKey + " on " + existingSession.getRosTcpEndpoint());
			}
		}
		return existingSession.getRosTcpEndpoint();
	}

	@Override
	public void registerRelayServer(String rosTcpEndpoint, String segment, RelayOutboundServiceController ros) {
		ServerHolder serverHolder = serverMap.get(rosTcpEndpoint);
		if (serverHolder != null) {
			log.warn("ROS connection should not be known " + rosTcpEndpoint);
			unregisterRelayServer(rosTcpEndpoint);
		}
		serverHolder = new ServerHolder(rosTcpEndpoint, ros);
		serverMap.put(rosTcpEndpoint, serverHolder);
	}

	@Override
	public void unregisterRelayServer(String rosTcpEndpoint) {
		serverMap.remove(rosTcpEndpoint);

		for (SessionHolder sh : sessionMap.values()) {
			if (rosTcpEndpoint.equals(sh.getRosTcpEndpoint())) {
				sh.setRosTcpEndpoint(null);
			}
		}
	}

	@Override
	public void notifySessionsRemoved(String rosTcpEndpoint, List<String> channelKeys) {
		for (String channelKey : channelKeys) {
			log.debug("Removing session " + channelKey + " from " + rosTcpEndpoint);
			SessionHolder sh = sessionMap.get(channelKey);
			if (sh != null) {
				if (rosTcpEndpoint.equals(sh.getRosTcpEndpoint())) {
					sh.setRosTcpEndpoint(null);
				} else {
					log.warn("Existing channel tcpEndpoint " + sh.getRosTcpEndpoint()
							+ " does not match disconnecting claimant " + rosTcpEndpoint);
				}
			} else {
				log.warn("Could not find existing session for channel " + channelKey);
			}
		}
	}

	@Override
	public void start(Segment segment, List<WebServiceApiName> apis) {
		// the partition control service always handles ALL apis for a segment
		this.segment = segment;
		// initialize sessionMap

		clear();

		scheduledThreadPool = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("PCS-RelayLoadCheckScheduler"));

		statisticCheckExecutor = Executors.newFixedThreadPool(1,
				new NamedThreadFactory("PCS-RelayLoadStatisticCheckExecutor"));
		scheduledThreadPool.scheduleWithFixedDelay(this, loadStatisticsCheckIntervalSec, loadStatisticsCheckIntervalSec,
				TimeUnit.SECONDS);

	}

	@Override
	/**
	 * Collect statistics from all ROS.
	 */
	public void run() {
		// we iterate through all ServerHolders which we then get the statistic from.
		log.info("Gathering relay server load statistics.");
		for (ServerHolder server : serverMap.values()) {
			RelayStatistic stats = server.getRos().getStatistics();
			if (stats != null) {
				updateServerStats(server, stats.getLoadValue());
			}
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

	private SessionHolder getSessionHolder(String channelKey) {
		SessionHolder sh = sessionMap.get(channelKey);
		if (sh == null) {
			synchronized (sessionMap) {
				sh = sessionMap.get(channelKey);
				if (sh == null) {
					sh = new SessionHolder();
					sessionMap.put(channelKey, sh);
				}
			}
		}
		return sh;
	}

	private void clear() {
		serverMap.clear();
		sessionMap.clear();
		totalLoad.set(0);
		roundRobinIndex = 0;
	}

	private void updateServerStats(ServerHolder server, int newLoadValue) {
		int oldLoadFactor = server.getLoadValue();
		int difference = newLoadValue - oldLoadFactor;

		log.info("Load stats changed for " + server.rosTcpEndpoint + " from " + oldLoadFactor + " to " + newLoadValue);
		server.setLoadValue(newLoadValue);

		totalLoad.addAndGet(difference);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public Segment getSegment() {
		return segment;
	}

	public int getTotalLoad(WebServiceApiName api) {
		return totalLoad.get();
	}

	public int getMaximumRoundRobinSize() {
		return maximumRoundRobinSize;
	}

	public void setMaximumRoundRobinSize(int maximumRoundRobinSize) {
		this.maximumRoundRobinSize = maximumRoundRobinSize;
	}

	public int getLoadStatisticsCheckIntervalSec() {
		return loadStatisticsCheckIntervalSec;
	}

	public void setLoadStatisticsCheckIntervalSec(int loadStatisticsCheckIntervalSec) {
		this.loadStatisticsCheckIntervalSec = loadStatisticsCheckIntervalSec;
	}

}
