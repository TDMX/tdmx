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

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.Job;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobSearchCriteria;
import org.tdmx.lib.control.domain.ControlJobStatus;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.service.ControlJobService;
import org.tdmx.lib.control.service.UniqueIdService;
import org.tdmx.service.control.task.dao.ExceptionType;
import org.tdmx.service.control.task.dao.TestTask;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JobExecutionServiceUnitTest {

	@Autowired
	@Qualifier("tdmx.lib.control.JobIdService")
	private UniqueIdService jobIdService;
	@Autowired
	private ControlJobService jobService;
	@Autowired
	private JobScheduler jobScheduler;
	@Autowired
	private JobExceptionConverter exceptionConverter;

	private JobConverter<TestTask> testJobConverter;
	private JobExecutionProcessImpl service;
	private String segmentName = "default";

	@Before
	public void doSetup() throws Exception {

		Segment s = new Segment();
		s.setSegmentName(segmentName);

		service = new JobExecutionProcessImpl();
		service.setJobIdService(jobIdService);
		service.setJobService(jobService);
		service.setFastTriggerDelayMillis(100);
		service.setLongPollIntervalSec(1);
		service.setMaxConcurrentJobs(5);

		testJobConverter = new TestJobConverterImpl();
		JobExecutor<TestTask> je = new TestJobExecutorImpl();

		List<JobConverter<?>> jcL = new ArrayList<>();
		jcL.add(testJobConverter);
		List<JobExecutor<?>> jeL = new ArrayList<>();
		jeL.add(je);

		service.setJobConverterList(jcL);
		service.setJobExecutorList(jeL);
		service.setExceptionConverter(exceptionConverter);

		service.init();

		service.start(s, null);
	}

	@After
	public void doTeardown() {
		service.stop();

		ControlJobSearchCriteria sc = new ControlJobSearchCriteria(new PageSpecifier(0, 999));
		sc.setJobType(TestTask.class.getName());
		List<ControlJob> jobs = jobService.search(sc);
		for (ControlJob j : jobs) {
			jobService.delete(j);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(jobIdService);
		assertNotNull(jobService);
		assertNotNull(jobScheduler);
		assertNotNull(exceptionConverter);
	}

	@Test
	public void testExecute_SingleJob_Success() throws Exception {
		TestTask t = new TestTask();
		t.setProcessTimeMs(0);

		Job j = service.createJob(t);
		jobScheduler.scheduleImmediate(segmentName, j);

		Thread.sleep(2000);

		ControlJobSearchCriteria sc = new ControlJobSearchCriteria(new PageSpecifier(0, 999));
		sc.setJobId(j.getJobId());
		List<ControlJob> cjL = jobService.search(sc);
		assertNotNull(cjL);
		assertEquals(1, cjL.size());
		ControlJob cj = cjL.get(0);
		assertEquals(ControlJobStatus.OK, cj.getStatus());
		assertNotNull(cj.getJob().getEndTimestamp());
		assertNull(cj.getJob().getException());
		TestTask tt = testJobConverter.getData(cj.getJob());
		assertEquals("" + cj.getId(), tt.getProcessMessage());
	}

	@Test
	public void testExecute_SingleJob_Exception() throws Exception {
		TestTask t = new TestTask();
		t.setProcessTimeMs(0);
		t.setProcessMessage("EXCEPTION THROWN!!!");

		Job j = service.createJob(t);
		jobScheduler.scheduleImmediate(segmentName, j);

		Thread.sleep(2000);

		ControlJobSearchCriteria sc = new ControlJobSearchCriteria(new PageSpecifier(0, 999));
		sc.setJobId(j.getJobId());
		List<ControlJob> cjL = jobService.search(sc);
		assertNotNull(cjL);
		assertEquals(1, cjL.size());
		ControlJob cj = cjL.get(0);
		assertEquals(ControlJobStatus.ERR, cj.getStatus());
		assertNotNull(cj.getJob().getEndTimestamp());
		assertNotNull(cj.getJob().getException());
		ExceptionType et = exceptionConverter.getException(cj.getJob());
		assertNotNull(et);

		assertEquals("EXCEPTION THROWN!!!", et.getMessage());
	}

	@Test
	public void testExecute_MultipleJob_Success() throws Exception {
		int NUM = 100;

		TestTask t = new TestTask();
		t.setProcessTimeMs(0);

		for (int i = 0; i < NUM; i++) {
			Job j = service.createJob(t);
			jobScheduler.scheduleImmediate(segmentName, j);
		}

		// 5 at a time @ 0.1s
		Thread.sleep(5000);

		ControlJobSearchCriteria sc = new ControlJobSearchCriteria(new PageSpecifier(0, 999));
		sc.setJobType(TestTask.class.getName());
		List<ControlJob> cjL = jobService.search(sc);
		assertNotNull(cjL);
		assertEquals(NUM, cjL.size());

		for (int i = 0; i < NUM; i++) {
			ControlJob cj = cjL.get(i);
			assertEquals(ControlJobStatus.OK, cj.getStatus());
			assertNotNull(cj.getJob().getEndTimestamp());
			assertNull(cj.getJob().getException());
			// test that it's marshalled after processing
			TestTask tt = testJobConverter.getData(cj.getJob());
			assertEquals("" + cj.getId(), tt.getProcessMessage());
		}
	}

};
