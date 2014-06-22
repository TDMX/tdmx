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
package org.tdmx.console.application.search;

import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class DateFormatTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFormat() {
		Date d = new Date();

		// Date
		DateFormat df = DateFormat.getDateInstance();
		System.out.println(df.format(d));

		// DateTime
		df = DateFormat.getDateTimeInstance();
		System.out.println(df.format(d));

		// Time
		df = DateFormat.getTimeInstance();
		System.out.println(df.format(d));

	}

	@Test
	public void testParse_Time() {
		// Time
		DateFormat df = DateFormat.getTimeInstance();
		System.out.println(df.isLenient());

		expectParseSuccess(df, "11:39:19");
		expectParseFailure(df, "11:39");
		expectParseFailure(df, "11");
		expectParseFailure(df, "03.11.2013");
		expectParseFailure(df, "03.11.2013 11:39:19");
	}

	@Test
	public void testParse_DateTime() {
		// Time
		DateFormat df = DateFormat.getDateTimeInstance();
		System.out.println(df.isLenient());

		expectParseSuccess(df, "03.11.2013 11:39:19");
		expectParseFailure(df, "03.11.2013");
		expectParseFailure(df, "11:39:19");
		expectParseFailure(df, "11:39");
		expectParseFailure(df, "11");
	}

	@Test
	public void testParse_Date() {
		// Time
		DateFormat df = DateFormat.getDateInstance();
		System.out.println(df.isLenient());

		expectParseSuccess(df, "03.11.2013 11:39:19");
		expectParseSuccess(df, "03.11.2013");
		expectParseFailure(df, "11.2013");
		expectParseFailure(df, "11:39:19");
		expectParseFailure(df, "11:39");
		expectParseFailure(df, "11");
		expectParseFailure(df, "2013");
	}

	private void expectParseFailure(DateFormat format, String text) {
		try {
			Date d = format.parse(text);

			Calendar c = Calendar.getInstance();
			c.setTime(d);

			fail("Expected [" + text + "] to fail with " + format.toString() + " instead got " + c.toString());
		} catch (ParseException e) {
		}
	}

	private void expectParseSuccess(DateFormat format, String text) {
		try {
			Date d = format.parse(text);

			Calendar c = Calendar.getInstance();
			c.setTime(d);

			System.out.println(" formatted " + text + " to " + c.toString());
		} catch (ParseException e) {
			fail("Expected [" + text + "] to succeed with " + format.toString());
		}
	}

}
