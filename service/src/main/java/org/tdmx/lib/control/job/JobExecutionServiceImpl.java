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

package org.tdmx.lib.control.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Peter Klauser
 * 
 */
public class JobExecutionServiceImpl implements Runnable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(JobExecutionServiceImpl.class);

	private List<JobConverter<?>> jobConverterList;

	private List<JobExecutor<?>> jobExecutorList;

	/**
	 * Delay in seconds.
	 */
	private int fixedDelay = 5;

	// internal //
	private final Map<String, JobExecutor<?>> jobExecutorMap = new HashMap<>();
	private final Map<String, JobConverter<?>> jobConverterMap = new HashMap<>();
	private ScheduledExecutorService scheduledThreadPool = null;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void init() {
		if (jobConverterList != null) {
			for (JobConverter<?> c : jobConverterList) {
				jobConverterMap.put(c.getType(), c);
			}
		}
		if (jobExecutorList != null) {
			for (JobExecutor<?> e : jobExecutorList) {
				jobExecutorMap.put(e.getType(), e);
			}
		}

		scheduledThreadPool = Executors.newSingleThreadScheduledExecutor();
		scheduledThreadPool.scheduleWithFixedDelay(this, getFixedDelay(), getFixedDelay(), TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		log.info("run start.");
		// TODO
		try {
			Thread.sleep(10000l);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("run finish.");
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

	public List<JobConverter<?>> getJobConverterList() {
		return jobConverterList;
	}

	public void setJobConverterList(List<JobConverter<?>> jobConverterList) {
		this.jobConverterList = jobConverterList;
	}

	public List<JobExecutor<?>> getJobExecutorList() {
		return jobExecutorList;
	}

	public void setJobExecutorList(List<JobExecutor<?>> jobExecutorList) {
		this.jobExecutorList = jobExecutorList;
	}

	public int getFixedDelay() {
		return fixedDelay;
	}

	public void setFixedDelay(int fixedDelay) {
		this.fixedDelay = fixedDelay;
	}

}
