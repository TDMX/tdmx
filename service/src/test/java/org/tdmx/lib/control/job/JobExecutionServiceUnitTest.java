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
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobSearchCriteria;
import org.tdmx.lib.control.service.ControlJobService;
import org.tdmx.lib.control.service.UniqueIdService;
import org.tdmx.service.control.task.dao.ZoneInstallTask;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JobExecutionServiceUnitTest {

	@Autowired
	@Qualifier("tdmx.lib.control.JobIdService")
	private UniqueIdService jobIdService;
	@Autowired
	private ControlJobService jobService;

	private JobExecutionServiceImpl service;

	@Before
	public void doSetup() throws Exception {

		service = new JobExecutionServiceImpl();
		service.setJobIdService(jobIdService);
		service.setJobService(jobService);
		service.setFastTriggerDelayMillis(100);
		service.setLongPollIntervalSec(5);
		service.setMaxConcurrentJobs(5);

		JobConverter<ZoneInstallTask> jc = new TestJobConverterImpl();
		JobExecutor<ZoneInstallTask> je = new TestJobExecutorImpl();

		List<JobConverter<?>> jcL = new ArrayList<>();
		jcL.add(jc);
		List<JobExecutor<?>> jeL = new ArrayList<>();
		jeL.add(je);

		service.setJobConverterList(jcL);
		service.setJobExecutorList(jeL);

		service.init();

		service.start();
	}

	@After
	public void doTeardown() {
		service.stop();

		ControlJobSearchCriteria sc = new ControlJobSearchCriteria(new PageSpecifier(0, 999));
		sc.setJobType("test"); // TODO
		List<ControlJob> jobs = jobService.search(sc);
		for (ControlJob j : jobs) {
			jobService.delete(j);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(jobIdService);
		assertNotNull(jobService);
	}

	@Test
	public void testExecuteJob() throws Exception {
		// TODO make XML type for <workDurationMs> <exceptionMsg>

		// TODO test server launcher
	}

};
