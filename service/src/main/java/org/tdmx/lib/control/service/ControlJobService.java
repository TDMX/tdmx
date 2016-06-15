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

import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobSearchCriteria;

/**
 * Management Services for a ControlJobEntry.
 * 
 * @author Peter
 * 
 */
public interface ControlJobService {

	public void createOrUpdate(ControlJob job);

	public ControlJob findById(Long id);

	public ControlJob findByJobId(String jobId);

	public List<ControlJob> search(ControlJobSearchCriteria criteria);

	/**
	 * Pessimistic lock and update status from NEW to RUN for up to maxJobs which have scheduledTime in the past.
	 * 
	 * @param segment
	 * @param maxJobs
	 * @return
	 */
	public List<ControlJob> reserve(String segment, int maxJobs);

	public void delete(ControlJob job);

}
