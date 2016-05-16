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
import org.tdmx.core.cli.display.PrintableObject;
import org.tdmx.core.cli.display.annotation.CliAttribute;
import org.tdmx.core.cli.display.annotation.CliRepresentation;

public class ObjectPrettyPrinterTest {

	private static final Logger log = LoggerFactory.getLogger(ObjectPrettyPrinterTest.class);

	private ObjectPrettyPrinter sut;

	@Before
	public void setUp() {
		sut = new ObjectPrettyPrinter(System.out, true, null);
	}

	@Test
	public void testAutowire() {
		assertNotNull(sut);
	}

	@Test
	public void testPrintAbleObject() {
		PrintableObject o = new PrintableObject("po");
		o.add("a1", "StringValue").add("a2", 100L);

		sut.println(o);
	}

	@Test
	public void testPrint_SingleObject() {
		MyObject o = new MyObject();
		o.verboseStringField0 = "v0";
		o.nonVerboseStringField1 = "nv1";

		sut.println(o);
	}

	@Test
	public void testPrint_MultiObject() {
		MyObject o1 = new MyObject();
		o1.verboseStringField0 = "o1_v0";
		o1.nonVerboseStringField1 = "o1_nv1";
		o1.nonVerboseStringField4 = "o1_nv4";

		MyObject o2 = new MyObject();
		o2.verboseStringField0 = "o2_v0";
		o2.nonVerboseStringField1 = "o2_nv1";
		o2.nonVerboseStringField4 = "o2_nv4";

		o1.verboseField2 = o2;
		o1.nonVerboseField3 = o2;

		sut.println(o1);
	}

	@Test
	public void testPrint_UndeclaredObject() {
		MyUndeclaredObject o1 = new MyUndeclaredObject();
		o1.stringField0 = "o1_nv0";

		sut.println(o1);
	}

	@CliRepresentation()
	private static class MyUndeclaredObject {
		@CliAttribute(order = 0)
		private String stringField0;
	}

	@CliRepresentation(name = "myrep")
	private static class MyObject {

		@CliAttribute(name = "-verboseField0", verbose = true, order = 1)
		private String verboseStringField0;

		@CliAttribute(name = "-nonVerboseField1", verbose = false, order = 0)
		private String nonVerboseStringField1;

		@CliAttribute(name = "-verboseField2", verbose = true, order = 2)
		private MyObject verboseField2;

		@CliAttribute(name = "-nonVerboseField3", verbose = false, order = 3)
		private MyObject nonVerboseField3;

		@CliAttribute(verbose = false, order = 4)
		private String nonVerboseStringField4;
	}
}
