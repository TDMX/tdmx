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
package org.tdmx.console.application.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackgroundJobRegistryImpl implements BackgroundJobRegistry, BackgroundJobRegistrySPI {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final List<BackgroundJobSPI> backgroundJobs = new ArrayList<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public BackgroundJobRegistryImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public List<BackgroundJob> getJobs() {
		List<BackgroundJob> result = new ArrayList<>();
		for (BackgroundJobSPI bj : backgroundJobs) {
			result.add(bj);
		}
		return result;
	}

	@Override
	public List<BackgroundJobSPI> getAllBackgroundJobs() {
		return Collections.unmodifiableList(backgroundJobs);
	}

	@Override
	public void addBackgroundJob(BackgroundJobSPI job) {
		removeBackgroundJob(job); // just incase job with same name constructed
		backgroundJobs.add(job);
	}

	@Override
	public void removeBackgroundJob(BackgroundJobSPI job) {
		backgroundJobs.remove(job);
	}

	@Override
	public void shutdownAndClear() {
		for (BackgroundJobSPI j : backgroundJobs) {
			j.shutdown();
		}
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

}
