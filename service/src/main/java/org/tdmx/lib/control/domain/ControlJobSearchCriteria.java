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
package org.tdmx.lib.control.domain;

import java.util.Date;

import org.tdmx.lib.common.domain.PageSpecifier;

/**
 * The SearchCriteria for a ControlJob.
 * 
 * @author Peter Klauser
 * 
 */
public class ControlJobSearchCriteria {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private final PageSpecifier pageSpecifier;

	private ControlJobStatus status;
	private String segment;
	private Date scheduledTimeBefore;
	private ControlJobType jobType;
	private Long owningEntityId;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public ControlJobSearchCriteria(PageSpecifier pageSpecifier) {
		if (pageSpecifier == null) {
			throw new IllegalArgumentException("Missing pageSpecifier");
		}
		this.pageSpecifier = pageSpecifier;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------
	public PageSpecifier getPageSpecifier() {
		return pageSpecifier;
	}

	public ControlJobStatus getStatus() {
		return status;
	}

	public void setStatus(ControlJobStatus status) {
		this.status = status;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	public Date getScheduledTimeBefore() {
		return scheduledTimeBefore;
	}

	public void setScheduledTimeBefore(Date scheduledTimeBefore) {
		this.scheduledTimeBefore = scheduledTimeBefore;
	}

	public ControlJobType getJobType() {
		return jobType;
	}

	public void setJobType(ControlJobType jobType) {
		this.jobType = jobType;
	}

	public Long getOwningEntityId() {
		return owningEntityId;
	}

	public void setOwningEntityId(Long owningEntityId) {
		this.owningEntityId = owningEntityId;
	}

}
