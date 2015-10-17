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
package org.tdmx.core.cli;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;

public class CommandFactoryTest {

	private static final Logger log = LoggerFactory.getLogger(CommandFactoryTest.class);

	private CommandDescriptorFactoryImpl factory;

	@Before
	public void setUp() {
		factory = new CommandDescriptorFactoryImpl(new Class[] { MyCmd.class });
	}

	@Test
	public void testAutowire() {
		assertNotNull(factory);
	}

	@Test
	public void testPrintUsage() {

		factory.printUsage(System.out);
	}

	@Cli(name = "mycmd", description = "mycmd-description", note = "mycmd-note")
	private static class MyCmd {

		@Parameter(name = "-stringField", description = "-stringField-description", required = true)
		private String stringField;

	}
}
