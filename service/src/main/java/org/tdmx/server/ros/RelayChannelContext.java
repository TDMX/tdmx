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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.FlowControlStatus;
import org.tdmx.lib.zone.domain.Zone;

/**
 * The control state of a channel's relaying activity.
 * 
 * META - we have a meta relaying job scheduled. No data or fetching allowed.
 * 
 * FETCH - we have a data fetching job scheduled. Only one data fetching job can be scheduled at one time.
 * 
 * When FlowControl is "closed" by the receiving end at the sending end ( ROS receives FC-closed on relay out - clears
 * all msg in queued jobs ), then the FlowQuota relay status is changed at the sender. The WS ros clients which initiate
 * relaying will not refrain from sending relay data to the ROS session, which will not queue these.The ROS session will
 * only time-out if the MOS agent stops sending or send buffer quota is exceede.
 * 
 * When FlowControl is "opened" by the receiving end at the sending end ( MRS receives relay in of FC-open ), then the
 * MRS initiates a "relayFlowControl" to the ROS which transitions the IDLE ROS session to re-evaluate if CA/CDS
 * (Metadata) needs relaying and continuing to fetch and relay messages.
 * 
 * @author Peter
 *
 */
public class RelayChannelContext {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayChannelContext.class);

	private final Comparator<RelayJobContext> ORDER = new Comparator<RelayJobContext>() {
		@Override
		public int compare(RelayJobContext o1, RelayJobContext o2) {
			return Long.compare(o1.getObjectId(), o2.getObjectId());
		}
	};

	// internal
	private RelayContextState state = RelayContextState.IDLE;
	private FlowControlStatus relayFlowStatus = FlowControlStatus.OPEN;

	private LinkedList<RelayJobContext> scheduledJobs = new LinkedList<>();
	private LinkedList<RelayJobContext> queuedJobs = new LinkedList<>();
	private MRSSessionHolder mrsSession;

	private long lastActivityTimestamp = 0L;

	// reference
	private final String pcsServerName;
	private final String channelKey;
	private final AccountZone accountZone;

	private final Zone zone;
	private final Domain domain;
	private final Channel channel;
	private final RelayDirection direction;
	private final int maxConcurrentMessages;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public RelayChannelContext(String pcsServerName, String channelKey, AccountZone az, Zone zone, Domain domain,
			Channel channel, RelayDirection direction, int maxConcurrentMessages) {
		this.pcsServerName = pcsServerName;
		this.channelKey = channelKey;
		this.accountZone = az;
		this.zone = zone;
		this.domain = domain;
		this.channel = channel;
		this.direction = direction;
		this.maxConcurrentMessages = maxConcurrentMessages;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RelayChannelContext [channelKey=").append(channelKey);
		builder.append(" pcsServerName=").append(pcsServerName);
		builder.append(" state=").append(state);
		builder.append(" dir=").append(direction);
		builder.append(" az=").append(accountZone);
		builder.append("]");
		return builder.toString();
	}

	public boolean isIdle() {
		return RelayContextState.IDLE == state;
	}

	public boolean isIdleTimeout(long millisSinceLastActivity) {
		return RelayContextState.IDLE == state
				&& System.currentTimeMillis() - millisSinceLastActivity > lastActivityTimestamp;
	}

	public boolean isShutdown() {
		return RelayContextState.SHUTDOWN == state;
	}

	public void shutdown() {
		state = RelayContextState.SHUTDOWN;
		queuedJobs.clear();
	}

	public synchronized List<RelayJobContext> finishJob(RelayJobContext finishedJob) {
		// remove the finished job from the scheduledJobs
		Iterator<RelayJobContext> sj = scheduledJobs.iterator();
		while (sj.hasNext()) {
			if (sj.next() == finishedJob) {
				sj.remove();
				break;
			}
		}
		handleFinish(finishedJob);
		lastActivityTimestamp = System.currentTimeMillis();
		return schedule(transition());
	}

	public synchronized List<RelayJobContext> relayChannelMessage(Long messageId) {
		log.debug("relayChannelMessage " + channelKey);
		RelayJobContext existingJob = getPendingJob(RelayJobType.Data, messageId);
		if (existingJob == null) {
			RelayJobContext newCtx = new RelayJobContext(this, RelayJobType.Data, messageId);
			addPendingJob(newCtx);
		}
		return schedule(transition());
	}

	public synchronized List<RelayJobContext> relayChannelAuthorization(Long channelId) {
		log.debug("relayChannelAuthorization " + channelId);
		RelayJobContext existingJob = getPendingJob(RelayJobType.MetaData, channelId);
		if (existingJob == null) {
			RelayJobContext newCtx = new RelayJobContext(this, RelayJobType.MetaData, channelId);
			addPendingJob(newCtx);
		}
		return schedule(transition());
	}

	public synchronized List<RelayJobContext> relayChannelFlowControl(Long quotaId) {
		log.debug("relayChannelFlowControl " + quotaId);
		RelayJobContext existingJob = getPendingJob(RelayJobType.MetaData, quotaId);
		if (existingJob == null) {
			RelayJobContext newCtx = new RelayJobContext(this, RelayJobType.MetaData, quotaId);
			addPendingJob(newCtx);
		}
		return schedule(transition());
	}

	public synchronized List<RelayJobContext> relayChannelDestinationSession(Long channelId) {
		log.debug("relayChannelDestinationSession " + channelId);
		RelayJobContext existingJob = getPendingJob(RelayJobType.MetaData, channelId);
		if (existingJob == null) {
			RelayJobContext newCtx = new RelayJobContext(this, RelayJobType.MetaData, channelId);
			addPendingJob(newCtx);
		}
		return schedule(transition());
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void handleFinish(RelayJobContext finishedJob) {
		// we take the job's relay status and transpose this onto the relaychannelcontext
		if (finishedJob.getFlowStatus() != null) {
			setRelayFlowStatus(finishedJob.getFlowStatus());

			// if we finish a task and the state of flow control is now closed, we discard any queued job.
			if (getRelayFlowStatus() == FlowControlStatus.CLOSED) {
				log.info("Skipping " + scheduledJobs.size() + " scheduled jobs due to flow control close.");
				scheduledJobs.clear();
				// we transition to idle state so if multiple data relay jobs concurrently finish we don't trigger
				// a new fetch, since we don't track the flow control status when fetching.
				state = RelayContextState.IDLE;
			}
		}

		switch (state) {
		case META:
			if (RelayJobType.MetaData == finishedJob.getType()
					&& 0 == getCountPendingJob(Arrays.asList(RelayJobType.MetaData))) {
				// finished relaying MetaData, if there are no messages to be sent pending,then we must check
				// by fetching from the DB.
				if (0 == getCountPendingJob(Arrays.asList(RelayJobType.Data))) {
					RelayJobContext fetch = new RelayJobContext(this, RelayJobType.Fetch, finishedJob.getObjectId());
					addPendingJob(fetch);
				}
			}
			break;
		case DATA:
			if (RelayJobType.Data == finishedJob.getType()
					&& 0 == getCountPendingJob(Arrays.asList(RelayJobType.Data))) {
				// finished relaying last Data, we need to check again by fetching
				if (0 == getCountPendingJob(Arrays.asList(RelayJobType.Data))) {
					RelayJobContext fetch = new RelayJobContext(this, RelayJobType.Fetch, finishedJob.getObjectId());
					addPendingJob(fetch);
				}
			}
			break;
		case FETCH:
			if (RelayJobType.Fetch == finishedJob.getType()) {
				for (Long stateId : finishedJob.getObjectIds()) {
					RelayJobContext existingJob = getPendingJob(RelayJobType.Data, stateId);
					if (existingJob == null) {
						RelayJobContext newCtx = new RelayJobContext(this, RelayJobType.Data, stateId);
						queuedJobs.add(newCtx);
					}
				}
				Collections.sort(queuedJobs, ORDER);
			}
			break;
		case IDLE:
			break;
		case SHUTDOWN:
			break;
		default:
			break;

		}
	}

	private List<RelayJobContext> transition() {
		List<RelayJobContext> result = new ArrayList<>();

		// relay any meta data before any messages.
		RelayJobContext job = getFirstPendingJob(RelayJobType.MetaData);
		if (job != null && removePendingJob(job)) {
			result.add(job);
			state = RelayContextState.META;
			return result;
		}

		// we look how many messages we can send at any one time
		int runningCount = getCountScheduledJobs();
		int pendingCount = getCountPendingJob(Arrays.asList(RelayJobType.Data));
		int availibleCount = Math.min(maxConcurrentMessages - runningCount, pendingCount);
		removePendingJobs(RelayJobType.Data, availibleCount, result);
		// if we still have data to send, we stay in data state even if this time we schedule no new transfers.
		if (availibleCount > 0 && runningCount > 0) {
			state = RelayContextState.DATA;
			return result;
		}

		// No data but an instruction to fetch more exists
		job = getFirstPendingJob(RelayJobType.Fetch);
		if (job != null && removePendingJob(job)) {
			result.add(job);
			state = RelayContextState.FETCH;
			return result;
		}

		// we must be left in IDLE state
		state = RelayContextState.IDLE;

		return result;
	}

	private List<RelayJobContext> schedule(List<RelayJobContext> jobs) {
		scheduledJobs.addAll(jobs);
		return jobs;
	}

	/**
	 * Remove the job from the pending job queue.
	 * 
	 * @param ctx
	 * @return true if the job was removed from the pending queue.
	 */
	private boolean removePendingJob(RelayJobContext job) {
		Iterator<RelayJobContext> rji = queuedJobs.iterator();
		while (rji.hasNext()) {
			if (rji.next() == job) {
				rji.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Add the job from the pending job queue.
	 * 
	 * @param ctx
	 */
	private void addPendingJob(RelayJobContext job) {
		if (state == RelayContextState.IDLE && RelayJobType.Data == job.getType()) {
			// If flow control is blocking relay to the destination, we cannot take on any new data to relay out.
			log.debug("Ignoring relay message since relay flow is closed.");
		} else {
			queuedJobs.add(job);
		}
	}

	/**
	 * Find the first pending job with a matching type.
	 * 
	 * @param type
	 * @return the first pending job with a matching type.
	 */
	private RelayJobContext getFirstPendingJob(RelayJobType type) {
		for (RelayJobContext ctx : queuedJobs) {
			if (type == ctx.getType()) {
				return ctx;
			}
		}
		return null;
	}

	/**
	 * Remove a number of pending job with a matching type.
	 * 
	 * @param type
	 * @param number
	 *            the max number of jobs to remove.
	 * @param result
	 *            container for the removed jobs.
	 */
	private void removePendingJobs(RelayJobType type, int number, List<RelayJobContext> result) {
		int i = 0;
		Iterator<RelayJobContext> queuedIterator = queuedJobs.iterator();
		while (queuedIterator.hasNext() && i < number) {
			RelayJobContext ctx = queuedIterator.next();
			if (type == ctx.getType()) {
				i++;
				result.add(ctx);
				queuedIterator.remove();
			}
		}
	}

	/**
	 * Count the pending jobs with the types specified.
	 * 
	 * @param types
	 * @return the number of pending jobs with the types specified.
	 */
	private int getCountPendingJob(List<RelayJobType> types) {
		int count = 0;
		for (RelayJobContext ctx : queuedJobs) {
			if (types.contains(ctx.getType())) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Count the scheduled jobs with the types specified.
	 * 
	 * @return the number of scheduled jobs irrespective of type.
	 */
	private int getCountScheduledJobs() {
		return scheduledJobs.size();
	}

	/**
	 * Find a pending job matching the parameters.
	 * 
	 * @param type
	 * @param id
	 * @return
	 */
	private RelayJobContext getPendingJob(RelayJobType type, Long id) {
		for (RelayJobContext ctx : queuedJobs) {
			if (type == ctx.getType() && id.equals(ctx.getObjectId())) {
				return ctx;
			}
		}
		return null;
	}
	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getPcsServerName() {
		return pcsServerName;
	}

	public String getChannelKey() {
		return channelKey;
	}

	public AccountZone getAccountZone() {
		return accountZone;
	}

	public Zone getZone() {
		return zone;
	}

	public Domain getDomain() {
		return domain;
	}

	public Channel getChannel() {
		return channel;
	}

	public RelayContextState getState() {
		return state;
	}

	public void setState(RelayContextState state) {
		this.state = state;
	}

	public FlowControlStatus getRelayFlowStatus() {
		return relayFlowStatus;
	}

	public void setRelayFlowStatus(FlowControlStatus relayFlowStatus) {
		this.relayFlowStatus = relayFlowStatus;
	}

	public MRSSessionHolder getMrsSession() {
		return mrsSession;
	}

	public void setMrsSession(MRSSessionHolder mrsSession) {
		this.mrsSession = mrsSession;
	}

	public RelayDirection getDirection() {
		return direction;
	}

	public int getMaxConcurrentMessages() {
		return maxConcurrentMessages;
	}

}
