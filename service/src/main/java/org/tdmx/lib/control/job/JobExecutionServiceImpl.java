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

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.common.domain.Job;
import org.tdmx.lib.control.service.UniqueIdService;

/**
 * 
 * NOTE: each JobExecution.run method is called without a DB-Transaction.
 * 
 * @author Peter Klauser
 * 
 */
public class JobExecutionServiceImpl implements Runnable, JobFactory {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(JobExecutionServiceImpl.class);

	private UniqueIdService jobIdService;
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

		scheduledThreadPool = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("JobExecutionService"));
		scheduledThreadPool.scheduleWithFixedDelay(this, getFixedDelay(), getFixedDelay(), TimeUnit.SECONDS);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Job createJob(Object task) {
		if (task == null) {
			throw new IllegalArgumentException("missing task");
		}
		JobConverter converter = jobConverterMap.get(task.getClass().getName());
		if (converter == null) {
			throw new IllegalArgumentException("no converter for " + task);
		}
		Job job = new Job();
		job.setJobId(getJobIdService().getNextId());
		job.setType(converter.getType());
		try {
			converter.setData(job, task);
		} catch (JAXBException e) {
			throw new IllegalArgumentException(e);
		}
		return job;
	}

	@Override
	public void run() {
		log.info("run start.");
		// TODO lock service?
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
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

	public UniqueIdService getJobIdService() {
		return jobIdService;
	}

	public void setJobIdService(UniqueIdService jobIdService) {
		this.jobIdService = jobIdService;
	}

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
