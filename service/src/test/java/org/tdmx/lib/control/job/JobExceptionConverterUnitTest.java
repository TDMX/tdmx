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
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.Job;
import org.tdmx.service.control.task.dao.ExceptionType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JobExceptionConverterUnitTest {

	@Autowired
	private JobExceptionConverter jobExceptionConverter;

	@Before
	public void doSetup() throws Exception {
	}

	@After
	public void doTeardown() {
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(jobExceptionConverter);
	}

	@Test
	public void testMarshal_Unmarshal_RTE() throws Exception {
		Job j = new Job();
		jobExceptionConverter.setException(j, new RuntimeException("RTE"));
		assertNotNull(j.getException());
		assertTrue(j.getException().length > 0);
		ExceptionType et = jobExceptionConverter.getException(j);
		assertNotNull(et);
		assertEquals("java.lang.RuntimeException", et.getType());
	}

};
