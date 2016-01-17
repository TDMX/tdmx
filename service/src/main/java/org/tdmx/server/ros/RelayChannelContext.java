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
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;

/**
 * The control state of a channel's relaying activity.
 * 
 * META - we have a meta relaying job scheduled. No data or fetching allowed.
 * 
 * FETCH - we have a data fetching job scheduled. Only one data fetching job can be scheduled at one time.
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
			return Long.compare(o1.getTimestamp(), o2.getTimestamp());
		}
	};

	// internal
	private RelayContextState state = RelayContextState.IDLE;

	private LinkedList<RelayJobContext> scheduledJobs = new LinkedList<>();
	private LinkedList<RelayJobContext> queuedJobs = new LinkedList<>();
	private String mrsSessionId;

	// reference
	private final String pcsServerName;
	private final String channelKey;
	private final AccountZone accountZone;

	private final Zone zone;
	private final Domain domain;
	private final Channel channel;
	private final RelayDirection direction;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public RelayChannelContext(String pcsServerName, String channelKey, AccountZone az, Zone zone, Domain domain,
			Channel channel, RelayDirection direction) {
		this.pcsServerName = pcsServerName;
		this.channelKey = channelKey;
		this.accountZone = az;
		this.zone = zone;
		this.domain = domain;
		this.channel = channel;
		this.direction = direction;
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

	public boolean isShutdown() {
		return RelayContextState.SHUTDOWN == state;
	}

	public void shutdown() {
		state = RelayContextState.SHUTDOWN;
		queuedJobs.clear();
	}

	// TODO #95 relay DR

	public synchronized List<RelayJobContext> finishJob(RelayJobContext finishedJob) {
		scheduledJobs.remove(finishedJob);
		handleFinish(finishedJob);
		return schedule(transition());
	}

	public synchronized void queueChannelMessages(List<ChannelMessage> msgs) {
		log.debug("queueChannelMessages");
		for (ChannelMessage msg : msgs) {
			RelayJobContext existingJob = getPendingJob(RelayJobType.Data, msg.getId());
			if (existingJob != null) {
				// enrich the existing relay job with a found Msg
				existingJob.setChannelMessage(msg);
			} else {
				RelayJobContext newCtx = new RelayJobContext(this, RelayJobType.Data, msg.getId());
				newCtx.setChannelMessage(msg);
				queuedJobs.add(newCtx);
			}
		}
		Collections.sort(queuedJobs, ORDER);
	}

	public synchronized List<RelayJobContext> relayChannelMessage(Long messageId) {
		log.debug("relayChannelMessage " + channelKey);
		RelayJobContext existingJob = getPendingJob(RelayJobType.Data, messageId);
		if (existingJob == null) {
			RelayJobContext newCtx = new RelayJobContext(this, RelayJobType.Data, messageId);
			queuedJobs.add(newCtx);
		}
		return schedule(transition());
	}

	public synchronized List<RelayJobContext> relayChannelAuthorization(Long channelId) {
		log.debug("relayChannelAuthorization " + channelId);
		RelayJobContext existingJob = getPendingJob(RelayJobType.MetaData, channelId);
		if (existingJob == null) {
			RelayJobContext newCtx = new RelayJobContext(this, RelayJobType.MetaData, channelId);
			queuedJobs.add(newCtx);
		}
		return schedule(transition());
	}

	public synchronized List<RelayJobContext> relayChannelFlowControl(Long quotaId) {
		log.debug("relayChannelFlowControl " + quotaId);
		RelayJobContext existingJob = getPendingJob(RelayJobType.MetaData, quotaId);
		if (existingJob == null) {
			RelayJobContext newCtx = new RelayJobContext(this, RelayJobType.MetaData, quotaId);
			queuedJobs.add(newCtx);
		}
		return schedule(transition());
	}

	public synchronized List<RelayJobContext> relayChannelDestinationSession(Long channelId) {
		log.debug("relayChannelDestinationSession " + channelId);
		RelayJobContext existingJob = getPendingJob(RelayJobType.MetaData, channelId);
		if (existingJob == null) {
			RelayJobContext newCtx = new RelayJobContext(this, RelayJobType.MetaData, channelId);
			queuedJobs.add(newCtx);
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
		RelayJobContext metaJob = getFirstPendingJob(RelayJobType.MetaData);
		if (metaJob != null && removePendingJob(metaJob)) {
			result.add(metaJob);
			state = RelayContextState.META;
		}

		// TODO Data

		// TODO Fetch

		// TODO catch IDLE

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
		return queuedJobs.remove(job);
	}

	/**
	 * Add the job from the pending job queue.
	 * 
	 * @param ctx
	 */
	private void addPendingJob(RelayJobContext job) {
		queuedJobs.add(job);
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

	public String getMrsSessionId() {
		return mrsSessionId;
	}

	public void setMrsSessionId(String mrsSessionId) {
		this.mrsSessionId = mrsSessionId;
	}

	public RelayDirection getDirection() {
		return direction;
	}

}
