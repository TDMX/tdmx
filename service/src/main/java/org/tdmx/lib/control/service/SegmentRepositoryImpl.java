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

package org.tdmx.lib.control.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.control.dao.SegmentDao;
import org.tdmx.lib.control.domain.Segment;

/**
 * A transactional service managing the Segment information.
 * 
 * @author Peter Klauser
 * 
 */
public class SegmentRepositoryImpl implements SegmentService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SegmentRepositoryImpl.class);

	private SegmentDao segmentDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(Segment segment) {
		if (segment.getId() != null) {
			Segment storedSegment = getSegmentDao().loadById(segment.getId());
			if (storedSegment != null) {
				getSegmentDao().merge(segment);
			} else {
				log.warn("Unable to find Segment with id " + segment.getId());
			}
		} else {
			getSegmentDao().persist(segment);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(Segment segment) {
		Segment storedSegment = getSegmentDao().loadById(segment.getId());
		if (storedSegment != null) {
			getSegmentDao().delete(storedSegment);
		} else {
			log.warn("Unable to find Segment to delete with id " + segment.getId());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public Segment findBySegment(String segment) {
		return getSegmentDao().loadBySegment(segment);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<Segment> findAll() {
		return getSegmentDao().loadAll();
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

	public SegmentDao getSegmentDao() {
		return segmentDao;
	}

	public void setSegmentDao(SegmentDao segmentDao) {
		this.segmentDao = segmentDao;
	}

}
