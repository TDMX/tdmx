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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.Job;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.console.domain.ControlJobEntryFacade;
import org.tdmx.lib.control.domain.ControlJobEntry;
import org.tdmx.lib.control.domain.ControlJobEntrySearchCriteria;
import org.tdmx.lib.control.domain.ControlJobEntryStatus;
import org.tdmx.lib.control.job.JobConverter;
import org.tdmx.service.control.task.dao.ZoneTransferCommand;
import org.tdmx.service.control.task.dao.ZoneTransferTask;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager="tdmx.lib.control.TransactionManager")
// @Transactional("ControlDB")
public class ControlJobEntryServiceRepositoryUnitTest {

	@Autowired
	private JobConverter<ZoneTransferTask> cmdConverter;

	@Autowired
	private ControlJobEntryService service;

	private String jobId;

	@Before
	public void doSetup() throws Exception {
		ZoneTransferCommand cmd = new ZoneTransferCommand();
		cmd.setPassword("pwd");
		cmd.setUsername("un");
		ZoneTransferTask task = new ZoneTransferTask();
		task.setCommand(cmd);

		Job j = new Job();
		j.setType(cmdConverter.getType());
		cmdConverter.setData(j, task);

		ControlJobEntry je = ControlJobEntryFacade.createImmediateJob(j);
		jobId = je.getJobId();

		service.createOrUpdate(je);
	}

	@After
	public void doTeardown() {
		ControlJobEntry je = service.findById(jobId);
		if (je != null) {
			service.delete(je);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testFetchStatus() throws Exception {
		ControlJobEntrySearchCriteria sc = new ControlJobEntrySearchCriteria(new PageSpecifier(0, 10));
		sc.setStatus(ControlJobEntryStatus.ERR);
		List<ControlJobEntry> l = service.search(sc);
		assertEquals(0, l.size());

		sc.setStatus(ControlJobEntryStatus.NEW);
		l = service.search(sc);
		assertEquals(1, l.size());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		ControlJobEntry je = service.findById("gugus");
		assertNull(je);
	}

	@Test
	public void testReserveAndModify() throws Exception {
		List<ControlJobEntry> runnable = service.reserve(1);
		assertEquals(1, runnable.size());

		Thread.sleep(1000);

		ControlJobEntry j = runnable.get(0);
		assertEquals(jobId, j.getJobId());
		assertEquals(ControlJobEntryStatus.RUN, j.getStatus());

		j.setStatus(ControlJobEntryStatus.OK);
		service.createOrUpdate(j);

		j = service.findById(jobId);
		assertEquals(ControlJobEntryStatus.OK, j.getStatus());
	}

}