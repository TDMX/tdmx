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
package org.tdmx.server.ros;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.job.NamedThreadFactory;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;

/**
 * Handles the outbound relay.
 * 
 * @author Peter
 *
 */
public class RelayOutboundServiceImpl implements RelayOutboundService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayOutboundServiceImpl.class);

	/**
	 * Provides all data for the ROS.
	 */
	private RelayDataService relayDataService;

	/**
	 * Idle timeout ( 5min )
	 */
	private long idleTimeoutMillis = 300000;

	/**
	 * The max concurrent relays per channel.
	 * 
	 * TODO LATER: the number of concurrent messages relayed at any one time should be a ChannelAuthorization property.
	 */
	private int maxConcurrentRelaysPerChannel = 5;

	/**
	 * The execution service for jobs.
	 */
	private RelayJobExecutionService jobExecutionService;

	private int coreRelayThreads = 10;

	private int maxRelayThreads = 200;

	// internal
	/**
	 * The executor service which provides the threads to run the relay jobs, bounded by coreRelayThreads and
	 * maxRelayThreads.
	 */
	private ExecutorService jobRunner = null;

	/**
	 * The current load value.
	 */
	private final AtomicInteger loadValue = new AtomicInteger(0);

	/**
	 * Map of all RelayChannelContext's keyed by channelKey.
	 */
	private final Map<String, RelayChannelContext> contextMap = new HashMap<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public class RelayJobRunner implements Runnable {

		private final RelayJobContext job;

		public RelayJobRunner(RelayJobContext job) {
			this.job = job;
		}

		@Override
		public void run() {
			loadValue.incrementAndGet();
			try {
				RelayJobExecutionService jes = getJobExecutionService();
				if (jes != null) {
					jes.executeJob(job);
					job.getChannelContext().finishJob(job);
				}
			} catch (RuntimeException re) {
				log.error("Relay failed.", re);
			} finally {
				loadValue.decrementAndGet();
			}
		}

	}
	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void start(String segmentScsUrl) {
		log.info("Starting RelayOutboundService.");
		// we start with an emtpy context map.
		contextMap.clear();

		// like cachedThreadPoolRunner but with bounded max.
		jobRunner = new ThreadPoolExecutor(coreRelayThreads, maxRelayThreads, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), new NamedThreadFactory("RelayJobRunner"));
	}

	@Override
	public void stop() {
		log.info("Stopping RelayOutboundService.");

		// shutdown the relaying threads
		if (jobRunner != null) {
			jobRunner.shutdown();
			try {
				jobRunner.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.warn("Interrupted whilst waiting for termination of jobRunner.", e);
			}
		}
		jobRunner = null;

		for (Entry<String, RelayChannelContext> ctxEntry : contextMap.entrySet()) {
			RelayChannelContext rc = ctxEntry.getValue();

			if (!rc.isShutdown()) {
				rc.shutdown();
			}
		}
	}

	@Override
	public int getCurrentLoad() {
		int currentLoad = loadValue.get();
		log.debug("Current load " + currentLoad);
		return currentLoad;
	}

	@Override
	public void startRelaySession(String channelKey, Map<AttributeId, Long> attributes, String pcsServerName) {
		log.info("Start relay session " + channelKey);

		// lookup the domain objects.
		AccountZone az = relayDataService.getAccountZone(attributes.get(AttributeId.AccountZoneId));
		Zone z = relayDataService.getZone(az, attributes.get(AttributeId.ZoneId));
		Domain d = relayDataService.getDomain(az, z, attributes.get(AttributeId.DomainId));
		Channel c = relayDataService.getChannel(az, z, d, attributes.get(AttributeId.ChannelId));

		RelayDirection dir = c.isSameDomain() ? RelayDirection.Both
				: c.isSend() ? RelayDirection.Fowards : RelayDirection.Backwards;

		RelayChannelContext rc = new RelayChannelContext(pcsServerName, channelKey, az, z, d, c, dir,
				maxConcurrentRelaysPerChannel);
		// take over existing mrs sessionId if provided by PCS.
		contextMap.put(channelKey, rc);
	}

	@Override
	public List<String> removeIdleRelaySessions(String pcsServerName) {
		log.info("Remove idle relay sessions for " + pcsServerName);
		List<String> idleSessions = new ArrayList<>();
		for (Entry<String, RelayChannelContext> ctxEntry : contextMap.entrySet()) {
			RelayChannelContext rc = ctxEntry.getValue();
			if (pcsServerName.equals(rc.getPcsServerName()) && rc.isIdleTimeout(idleTimeoutMillis)) {
				idleSessions.add(ctxEntry.getKey());
			}
		}
		List<String> result = new ArrayList<>();
		for (String channelKey : idleSessions) {
			contextMap.remove(channelKey);
			result.add(channelKey);
		}
		log.info("Removed " + result.size() + " idle relay sessions for " + pcsServerName);
		return result;
	}

	@Override
	public List<String> shutdownRelaySessions(String pcsServerName) {
		log.info("Shutdown relay sessions for " + pcsServerName);
		List<String> sessions = new ArrayList<>();
		for (Entry<String, RelayChannelContext> ctxEntry : contextMap.entrySet()) {
			RelayChannelContext rc = ctxEntry.getValue();
			if (pcsServerName.equals(rc.getPcsServerName())) {
				rc.shutdown();
				sessions.add(ctxEntry.getKey());
			}
		}
		List<String> result = new ArrayList<>();
		for (String channelKey : sessions) {
			contextMap.remove(channelKey);
			result.add(channelKey);
		}
		log.info("Shutdown " + result.size() + " relay sessions for " + pcsServerName);
		return result;
	}

	@Override
	public boolean relayChannelAuthorization(String channelKey, Long channelId) {
		log.info("relayChannelAuthorization " + channelKey);
		RelayChannelContext rc = contextMap.get(channelKey);
		if (rc == null) {
			// normal if ROS session has idle timed-out, client must get new ROS session
			log.debug("relayChannelAuthorization " + channelKey + " could not find relay context.");
			return false;
		}
		// add the CA to the relay context
		schedule(rc.relayChannelAuthorization(channelId));
		return true;
	}

	@Override
	public boolean relayChannelFlowControl(String channelKey, Long quotaId) {
		log.info("relayChannelFlowControl " + channelKey);
		RelayChannelContext rc = contextMap.get(channelKey);
		if (rc == null) {
			// normal if ROS session has idle timed-out, client must get new ROS session
			log.debug("relayChannelFlowControl " + channelKey + " could not find relay context.");
			return false;
		}
		// add the FC to the relay context
		schedule(rc.relayChannelFlowControl(quotaId));
		return true;
	}

	@Override
	public boolean relayChannelMessage(String channelKey, Long messageId) {
		log.info("relayChannelMessage " + channelKey);
		RelayChannelContext rc = contextMap.get(channelKey);
		if (rc == null) {
			// normal if ROS session has idle timed-out, client must get new ROS session
			log.debug("relayChannelMessage " + channelKey + " could not find relay context.");
			return false;
		}
		// add the MSG to the relay context
		schedule(rc.relayChannelMessage(messageId));
		return true;
	}

	@Override
	public boolean relayChannelDestinationSession(String channelKey, Long channelId) {
		log.info("relayChannelDestinationSession " + channelKey);
		RelayChannelContext rc = contextMap.get(channelKey);
		if (rc == null) {
			// normal if ROS session has idle timed-out, client must get new ROS session
			log.debug("relayChannelDestinationSession " + channelKey + " could not find relay context.");
			return false;
		}
		// add the DS to the relay context
		schedule(rc.relayChannelDestinationSession(channelId));
		return true;
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void schedule(List<RelayJobContext> jobs) {
		// submit all newly determined runnable jobs to the relay executor
		for (RelayJobContext job : jobs) {
			log.debug("Scheduling " + job);
			jobRunner.submit(new RelayJobRunner(job));
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public RelayDataService getRelayDataService() {
		return relayDataService;
	}

	public void setRelayDataService(RelayDataService relayDataService) {
		this.relayDataService = relayDataService;
	}

	public RelayJobExecutionService getJobExecutionService() {
		return jobExecutionService;
	}

	public void setJobExecutionService(RelayJobExecutionService jobExecutionService) {
		this.jobExecutionService = jobExecutionService;
	}

	public long getIdleTimeoutMillis() {
		return idleTimeoutMillis;
	}

	public void setIdleTimeoutMillis(long idleTimeoutMillis) {
		this.idleTimeoutMillis = idleTimeoutMillis;
	}

	public int getMaxConcurrentRelaysPerChannel() {
		return maxConcurrentRelaysPerChannel;
	}

	public void setMaxConcurrentRelaysPerChannel(int maxConcurrentRelaysPerChannel) {
		this.maxConcurrentRelaysPerChannel = maxConcurrentRelaysPerChannel;
	}

	public int getCoreRelayThreads() {
		return coreRelayThreads;
	}

	public void setCoreRelayThreads(int coreRelayThreads) {
		this.coreRelayThreads = coreRelayThreads;
	}

	public int getMaxRelayThreads() {
		return maxRelayThreads;
	}

	public void setMaxRelayThreads(int maxRelayThreads) {
		this.maxRelayThreads = maxRelayThreads;
	}

}
