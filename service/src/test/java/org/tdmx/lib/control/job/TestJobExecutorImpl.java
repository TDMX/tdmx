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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.tdmx.service.control.task.dao.TestTask;

/**
 * The TestJobExecutorImpl will process a test task and sleep for the duration and then either succeed or throw a
 * runtime exception with the error message.
 * 
 * @author Peter
 * 
 */
public class TestJobExecutorImpl implements JobExecutor<TestTask> {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(TestJobExecutorImpl.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public TestJobExecutorImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String getType() {
		return TestTask.class.getName();
	}

	@Override
	public void execute(Long id, TestTask task) {
		if (StringUtils.hasText(task.getProcessMessage())) {
			log.info("FAILURE task " + id + " with " + task.getProcessTimeMs() + "ms delay - "
					+ task.getProcessMessage());
		} else {
			log.info("SUCCESS task " + id + " with " + task.getProcessTimeMs() + "ms delay");
		}
		if (task.getProcessTimeMs() > 0) {
			try {
				Thread.sleep(task.getProcessTimeMs());
			} catch (InterruptedException e) {
				// ignore ie.
			}
		}
		if (StringUtils.hasText(task.getProcessMessage())) {
			// usually the problem comes from deeper in the service layer.
			Exception causingException = new Exception(task.getProcessMessage());
			causingException.fillInStackTrace();
			throw new RuntimeException("Some Problem Occured.", causingException);
		}
		task.setProcessMessage("" + id);
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
