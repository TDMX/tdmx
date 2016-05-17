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
import org.tdmx.lib.control.dao.MaxValueDao;
import org.tdmx.lib.control.domain.MaxValue;

/**
 * A transactional service managing Locks.
 * 
 * @author Peter Klauser
 * 
 */
public class MaxValueServiceRepositoryImpl implements MaxValueService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(MaxValueServiceRepositoryImpl.class);

	private MaxValueDao maxValueDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	@Transactional(value = "ControlDB")
	public MaxValue increment(String key, int increment) {
		MaxValue storedValue = getMaxValueDao().lockById(key);
		if (storedValue != null) {
			// we MUST have a pessimistic lock on the value
			storedValue.setValue(storedValue.getValue() + increment);
		}
		// auto-commit immediately.
		return storedValue;
	}

	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(MaxValue value) {
		MaxValue storedValue = getMaxValueDao().lockById(value.getKey());
		if (storedValue == null) {
			getMaxValueDao().persist(value);
		} else {
			getMaxValueDao().merge(value);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(MaxValue value) {
		MaxValue storedValue = getMaxValueDao().lockById(value.getKey());
		if (storedValue != null) {
			getMaxValueDao().delete(storedValue);
		} else {
			log.warn("Unable to find MaxValue to delete with id " + value.getKey());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public MaxValue findById(String key) {
		return getMaxValueDao().lockById(key);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<MaxValue> findAll() {
		return getMaxValueDao().loadAll();
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

	public MaxValueDao getMaxValueDao() {
		return maxValueDao;
	}

	public void setMaxValueDao(MaxValueDao maxValueDao) {
		this.maxValueDao = maxValueDao;
	}

}
