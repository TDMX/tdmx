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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobSearchCriteria;
import org.tdmx.lib.control.domain.ControlJobType;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.service.ControlJobService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/tdmx/test-context.xml")
public class JobExecutionServiceUnitTest {

	@Autowired
	private ControlJobService jobService;
	@Autowired
	@Named("tdmx.lib.control.job.WaitJobExecutor")
	private JobExecutor jobExecutor;

	private JobExecutionProcessImpl service;
	private String segmentName = "default";

	@Before
	public void doSetup() throws Exception {

		Segment s = new Segment();
		s.setSegmentName(segmentName);

		service = new JobExecutionProcessImpl();
		service.setJobService(jobService);
		service.setJobExecutor(jobExecutor);
		service.setFastTriggerDelayMillis(100);
		service.setLongPollIntervalSec(1);
		service.setMaxConcurrentJobs(5);

		service.init();

		service.start(s, null);
	}

	@After
	public void doTeardown() {
		service.stop();

		ControlJobSearchCriteria sc = new ControlJobSearchCriteria(new PageSpecifier(0, 999));
		sc.setSegment(segmentName);
		sc.setJobType(ControlJobType.WAIT);
		List<ControlJob> jobs = jobService.search(sc);
		for (ControlJob j : jobs) {
			jobService.delete(j);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(jobService);
	}

	@Test
	public void testExecute_SingleJob_Success() throws Exception {
		ControlJob j = ControlJob.createWaitJob(segmentName).scheduleNow();
		assertNotNull(j.getScheduledTime());
		assertNull(j.getEndTimestamp());
		assertNull(j.getStartTimestamp());

		jobService.createOrUpdate(j);
		assertNotNull(j.getId());

		// give it time to execute, it's deleted afterwards
		Thread.sleep(2000);

		ControlJob storedJob = jobService.findById(j.getId());
		assertNull(storedJob);
	}

	@Test
	public void testExecute_MultipleJob_Success() throws Exception {
		int NUM = 100;

		ControlJob[] jobs = new ControlJob[NUM];
		for (int i = 0; i < NUM; i++) {
			jobs[i] = ControlJob.createWaitJob(segmentName).scheduleNow();

			jobService.createOrUpdate(jobs[i]);
			assertNotNull(jobs[i].getId());
		}

		// 5 at a time @ 0.1s
		Thread.sleep(5000);

		for (int i = 0; i < NUM; i++) {
			ControlJob storedJob = jobService.findById(jobs[i].getId());
			assertNull(storedJob);
		}
	}

};
