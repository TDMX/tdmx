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
import org.tdmx.core.cli.display.ObjectPrettyPrinter;
import org.tdmx.core.cli.display.annotation.CliAttribute;
import org.tdmx.core.cli.display.annotation.CliRepresentation;

public class ObjectPrettyPrinterTest {

	private static final Logger log = LoggerFactory.getLogger(ObjectPrettyPrinterTest.class);

	private ObjectPrettyPrinter sut;

	@Before
	public void setUp() {
		sut = new ObjectPrettyPrinter();
	}

	@Test
	public void testAutowire() {
		assertNotNull(sut);
	}

	@Test
	public void testPrintVerbose() {
		MyObject o = new MyObject();
		o.verboseField0 = "v0";
		o.nonVerboseField1 = "nv1";

		sut.output(System.out, o, true);
	}

	@CliRepresentation(name = "myrep")
	private static class MyObject {

		@CliAttribute(name = "-verboseField0", verbose = true, order = 0)
		private String verboseField0;

		@CliAttribute(name = "-nonVerboseField1", verbose = false, order = 1)
		private String nonVerboseField1;
	}
}
