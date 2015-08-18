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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.common.domain.Job;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobStatus;
import org.tdmx.lib.control.service.ControlJobService;
import org.tdmx.lib.control.service.UniqueIdService;

/**
 * 
 * NOTE: each JobExecution.run method is called without a DB-Transaction.
 * 
 * @author Peter Klauser
 * 
 */
public class JobExecutionProcessImpl implements Process, JobFactory {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(JobExecutionProcessImpl.class);

	private UniqueIdService jobIdService;
	private ControlJobService jobService;

	private List<JobConverter<?>> jobConverterList;
	private List<JobExecutor<?>> jobExecutorList;
	private JobExceptionConverter exceptionConverter;

	/**
	 * Delay in seconds.
	 */
	private int longPollIntervalSec = 5;

	/**
	 * The max number of concurrent jobs running;
	 */
	private int maxConcurrentJobs = 5;

	/**
	 * The time to wait after a job is finished to start a new poll
	 */
	private int fastTriggerDelayMillis = 100;

	// internal //
	private boolean started = false;
	private final Map<String, JobExecutor<?>> jobExecutorMap = new HashMap<>();
	private final Map<String, JobConverter<?>> jobConverterMap = new HashMap<>();
	private ScheduledExecutorService scheduledThreadPool = null;
	private ExecutorService jobRunners = null;
	private final LinkedList<Future<?>> jobStatus = new LinkedList<>();
	private final Object syncObject = new Object();
	private Future<?> fastTrigger = null;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void init(String[] cmdLineArgs) {
	}

	public void init() {
		// setup primarily with spring bean init method.
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

		jobRunners = Executors.newFixedThreadPool(getMaxConcurrentJobs(), new NamedThreadFactory("JobRunner"));
	}

	@Override
	public void start() {
		started = true;
		scheduledThreadPool.scheduleWithFixedDelay(this, getLongPollIntervalSec(), getLongPollIntervalSec(),
				TimeUnit.SECONDS);
	}

	@Override
	public void stop() {
		started = false;
		scheduledThreadPool.shutdown();
		try {
			scheduledThreadPool.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("Interrupted whilst waiting for termination of scheduledThreadPool.", e);
		}
		jobRunners.shutdown();
		try {
			jobRunners.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("Interrupted whilst waiting for termination of jobRunners.", e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Job createJob(Object task) {
		if (task == null) {
			throw new IllegalArgumentException("missing task");
		}
		JobConverter converter = jobConverterMap.get(task.getClass().getName());
		if (converter == null) {
			throw new IllegalArgumentException("no JobConverter for " + task);
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

		synchronized (syncObject) {
			int freeSlots = cleanFinishedJobs();
			log.info("free slots " + freeSlots);

			if (freeSlots > 0) {
				List<ControlJob> jobsToRun = jobService.reserve(freeSlots);
				for (ControlJob j : jobsToRun) {
					Future<?> r = jobRunners.submit(new JobRunner(j));
					jobStatus.add(r);
				}
			}

		}
		log.info("run finish.");
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void fastTrigger() {
		if (fastTrigger != null && fastTrigger.isDone()) {
			fastTrigger = null;
		}
		if (fastTrigger == null && started) {
			// there is a tiny race condition here which we don't really care about, if it fails here
			// we just get 2 polls on the scheduledThread
			fastTrigger = scheduledThreadPool.schedule(this, fastTriggerDelayMillis, TimeUnit.MILLISECONDS);
		}
	}

	private int cleanFinishedJobs() {
		// we throw out any finished job and find out the max number of free slots
		Iterator<Future<?>> i = jobStatus.iterator();
		while (i.hasNext()) {
			Future<?> f = i.next();
			if (f.isDone()) {
				i.remove();
			}
		}
		return maxConcurrentJobs - jobStatus.size();
	}

	/**
	 * The JobRunner calls a JobExecutor with the Task information. Runs outside any transaction.
	 * 
	 * @author Peter
	 * 
	 */
	private class JobRunner implements Runnable {
		private final ControlJob job;

		public JobRunner(ControlJob job) {
			this.job = job;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void run() {
			Job j = job.getJob();
			j.setStartTimestamp(new Date());
			try {
				JobConverter jc = jobConverterMap.get(j.getType());
				if (jc == null) {
					throw new IllegalArgumentException("no JobConverter for " + job);
				}
				JobExecutor je = jobExecutorMap.get(j.getType());
				if (je == null) {
					throw new IllegalArgumentException("no JobExecutor for " + job);
				}
				Object task = jc.getData(j);
				je.execute(job.getId(), task);

				// the data in task will change during execute
				jc.setData(j, task);
				j.setEndTimestamp(new Date());

				job.setStatus(ControlJobStatus.OK);
				job.setJob(j);
			} catch (Exception e) {
				Throwable problem = e.getCause() != null ? e.getCause() : e;
				log.warn("Job " + job + " failed with reason=" + problem.getMessage(), e);

				exceptionConverter.setException(j, problem);

				j.setEndTimestamp(new Date());
				job.setStatus(ControlJobStatus.ERR);
				job.setJob(j);

			}
			jobService.createOrUpdate(job);

			// we triggerfast scheduling of the next poll
			fastTrigger();
		}

	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public UniqueIdService getJobIdService() {
		return jobIdService;
	}

	public void setJobIdService(UniqueIdService jobIdService) {
		this.jobIdService = jobIdService;
	}

	public ControlJobService getJobService() {
		return jobService;
	}

	public void setJobService(ControlJobService jobService) {
		this.jobService = jobService;
	}

	public JobExceptionConverter getExceptionConverter() {
		return exceptionConverter;
	}

	public void setExceptionConverter(JobExceptionConverter exceptionConverter) {
		this.exceptionConverter = exceptionConverter;
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

	public int getLongPollIntervalSec() {
		return longPollIntervalSec;
	}

	public void setLongPollIntervalSec(int longPollIntervalSec) {
		this.longPollIntervalSec = longPollIntervalSec;
	}

	public int getMaxConcurrentJobs() {
		return maxConcurrentJobs;
	}

	public void setMaxConcurrentJobs(int maxConcurrentJobs) {
		this.maxConcurrentJobs = maxConcurrentJobs;
	}

	public int getFastTriggerDelayMillis() {
		return fastTriggerDelayMillis;
	}

	public void setFastTriggerDelayMillis(int fastTriggerDelayMillis) {
		this.fastTriggerDelayMillis = fastTriggerDelayMillis;
	}

}
