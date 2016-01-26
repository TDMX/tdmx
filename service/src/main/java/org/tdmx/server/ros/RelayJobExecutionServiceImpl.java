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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.common.domain.ProcessingStatus;
import org.tdmx.lib.zone.domain.Channel;

/**
 * Handles the execution of individual relay jobs.
 * 
 * @author Peter
 *
 */
public class RelayJobExecutionServiceImpl implements RelayJobExecutionService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayJobExecutionServiceImpl.class);

	/**
	 * Provides all data from the DB for the ROS.
	 */
	private RelayDataService relayDataService;

	/**
	 * Provides the MRS web service client and the remove MRS sessionId.
	 */
	private RelayConnectionProvider relayConnectionProvider;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void executeJob(RelayJobContext job) {
		switch (job.getType()) {
		case Data:
			break;
		case Fetch:
			break;
		case MetaData:
			relayMetaData(job.getChannelContext(), job);
			break;
		default:
			break;
		}
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void relayMetaData(RelayChannelContext ctx, RelayJobContext job) {
		// fetch the channel from the DB with uptodate info
		Channel c = relayDataService.getChannel(ctx.getAccountZone(), ctx.getZone(), ctx.getDomain(),
				job.getObjectId());
		if (c == null) {
			log.warn("Unable to find channel for meta data relay. " + job);
			return;
		}

		// relay the CA
		if (c.getAuthorization().getProcessingState().getStatus() == ProcessingStatus.PENDING) {
			// CA sender to receiver

			// CA receiver to sender
		}

		// relay the CDS receiver to sender
		if (c.getProcessingState().getStatus() == ProcessingStatus.PENDING) {

		}

		// relay the FC-open receiver to sender
		if (c.getQuota().getProcessingState().getStatus() == ProcessingStatus.PENDING) {

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

	public RelayConnectionProvider getRelayConnectionProvider() {
		return relayConnectionProvider;
	}

	public void setRelayConnectionProvider(RelayConnectionProvider relayConnectionProvider) {
		this.relayConnectionProvider = relayConnectionProvider;
	}

}
