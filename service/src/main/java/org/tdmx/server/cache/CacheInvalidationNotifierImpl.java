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
package org.tdmx.server.cache;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.ws.ErrorCode;

/**
 * Notifies each segment's PCS of a cache invalidation.
 * 
 * @author Peter
 *
 */
public class CacheInvalidationNotifierImpl implements CacheInvalidationNotifier {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(CacheInvalidationNotifierImpl.class);

	private Manageable pcsClient;

	private CacheInvalidationListener pcsInformer;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public CacheInvalidationNotifierImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public ProcessingState invalidateCache(Segment segment, CacheInvalidationInstruction event) {

		try {
			informPCS(segment, event);
		} catch (Exception e) {
			log.warn("Unable to invalidate cache " + event + " on segment " + segment.getSegmentName(), e);
			String errorInfo = StringUtils.getExceptionSummary(e);
			ProcessingState error = ProcessingState.error(ErrorCode.ChunkDataLost.getErrorCode(),
					ErrorCode.CacheInvalidationFailed.getErrorDescription(event.getId(), event.getName(),
							event.getKey(), errorInfo));
			return error;
		}
		return ProcessingState.none();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private synchronized void informPCS(Segment s, CacheInvalidationInstruction event) {
		log.info("Notifying segment " + s.getSegmentName() + " of " + event);

		try {
			// connect the PCC to the segment
			pcsClient.start(s, Collections.emptyList());

			pcsInformer.invalidateCache(event);
		} finally {
			pcsClient.stop();
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public Manageable getPcsClient() {
		return pcsClient;
	}

	public void setPcsClient(Manageable pcsClient) {
		this.pcsClient = pcsClient;
	}

	public CacheInvalidationListener getPcsInformer() {
		return pcsInformer;
	}

	public void setPcsInformer(CacheInvalidationListener pcsInformer) {
		this.pcsInformer = pcsInformer;
	}

}
