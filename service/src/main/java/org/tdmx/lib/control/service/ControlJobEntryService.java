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

import org.tdmx.lib.control.domain.ControlJobEntry;
import org.tdmx.lib.control.domain.ControlJobEntrySearchCriteria;

/**
 * Management Services for a ControlJobEntry.
 * 
 * @author Peter
 * 
 */
public interface ControlJobEntryService {

	public void createOrUpdate(ControlJobEntry job);

	public ControlJobEntry findById(String jobId);

	public List<ControlJobEntry> search(ControlJobEntrySearchCriteria criteria);

	/**
	 * Pessimistically lock and update status from NEW to RUN for up to maxJobs which have scheduledTime in the past.
	 * 
	 * @param maxJobs
	 * @return
	 */
	public List<ControlJobEntry> reserve(int maxJobs);

	public void delete(ControlJobEntry address);

}
