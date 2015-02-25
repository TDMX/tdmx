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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.Job;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobStatus;
import org.tdmx.lib.control.service.ControlJobService;
import org.tdmx.service.control.task.dao.ZoneInstallTask;
import org.tdmx.service.control.task.dao.ZoneTransferCommand;
import org.tdmx.service.control.task.dao.ZoneTransferTask;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JobSchedulerUnitTest {

	@Autowired
	private JobScheduler jobScheduler;

	@Autowired
	private JobFactory jobFactory;

	@Autowired
	private ControlJobService jobService;

	private ControlJob je;

	@Before
	public void doSetup() throws Exception {
	}

	@After
	public void doTeardown() {
		if (je != null && je.getId() != null) {
			ControlJob j = jobService.findById(je.getId());
			jobService.delete(j);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(jobService);
		assertNotNull(jobScheduler);
		assertNotNull(jobFactory);
	}

	@Test
	public void testSchedule_Immediate_ZoneInstallTask() throws Exception {
		ZoneInstallTask task = new ZoneInstallTask();
		task.setAccountId("1");
		task.setZoneApex("z");
		Job j = jobFactory.createJob(task);
		je = jobScheduler.scheduleImmediate(j);

		checkImmediate(task.getClass().getName(), je);
	}

	@Test
	public void testSchedule_Immediate_ZoneTransferTask() throws Exception {
		ZoneTransferTask task = new ZoneTransferTask();
		ZoneTransferCommand cmd = new ZoneTransferCommand();
		cmd.setUsername("u");
		cmd.setPassword("p");
		task.setCommand(cmd);
		Job j = jobFactory.createJob(task);
		je = jobScheduler.scheduleImmediate(j);

		checkImmediate(task.getClass().getName(), je);
	}

	private void checkImmediate(String jobType, ControlJob j) {
		assertNotNull(j);
		assertNotNull(j.getId());
		assertNotNull(j.getScheduledTime());
		assertNotNull(j.getJob());
		assertEquals(ControlJobStatus.NEW, j.getStatus());
		Date scheduledTime = j.getScheduledTime();

		Job job = j.getJob();
		assertNotNull(job.getJobId());
		assertNotNull(job.getType());
		assertEquals(jobType, job.getType());
		assertNotNull(job.getData());
		assertNull(job.getStartTimestamp());
		assertNull(job.getEndTimestamp());
		assertNull(job.getException());

		assertTrue(scheduledTime.getTime() <= System.currentTimeMillis());
	}
};
