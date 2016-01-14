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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.common.domain.ProcessingState;
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
	private LinkedList<RelayJobContext> pendingJobs = new LinkedList<>();
	private String mrsSessionId;

	// reference
	private final String pcsServerName;
	private final String channelKey;
	private final Zone zone;
	private final Domain domain;
	private final Long channelId;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public RelayChannelContext(String pcsServerName, String channelKey, Zone zone, Domain domain, Long channelId) {
		this.pcsServerName = pcsServerName;
		this.channelKey = channelKey;
		this.zone = zone;
		this.domain = domain;
		this.channelId = channelId;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * TODO
	 * 
	 * @param rjc
	 * @return the jobs to schedule.
	 */
	public List<RelayJobContext> addRelayJob(RelayJobContext rjc) {
		List<RelayJobContext> result = new ArrayList<>();
		switch (state) {
		case META:
			break;
		case DATA:
			break;
		case FETCH:
			break;
		case IDLE:
			scheduledJobs.add(rjc);
			result.add(rjc);
			if (RelayJobType.MetaDataRelay == rjc.getType()) {
				state = RelayContextState.META;
			} else
				if (RelayJobType.DeliveryReportFetch == rjc.getType() || RelayJobType.MessageFetch == rjc.getType()) {
				state = RelayContextState.FETCH;
			} else {
				state = RelayContextState.DATA;
			}
			break;
		default:
			break;

		}
		return result;
	}

	/**
	 * 
	 * @param rjc
	 * @return the jobs to schedule.
	 */
	public List<RelayJobContext> finishRelayJob(RelayJobContext rjc, ProcessingState ps) {
		List<RelayJobContext> result = new ArrayList<>();
		switch (state) {
		case META:
			break;
		case DATA:
			break;
		case FETCH:
			break;
		case IDLE:
			break;
		default:
			break;

		}
		return result;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getPcsServerName() {
		return pcsServerName;
	}

	public String getChannelKey() {
		return channelKey;
	}

	public Zone getZone() {
		return zone;
	}

	public Domain getDomain() {
		return domain;
	}

	public String getMrsSessionId() {
		return mrsSessionId;
	}

	public void setMrsSessionId(String mrsSessionId) {
		this.mrsSessionId = mrsSessionId;
	}

	public Long getChannelId() {
		return channelId;
	}

	public RelayContextState getState() {
		return state;
	}
}
