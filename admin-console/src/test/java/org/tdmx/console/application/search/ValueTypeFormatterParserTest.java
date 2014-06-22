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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.console.application.search.SearchExpressionParser.ValueTypeFormatter;
import org.tdmx.console.application.search.SearchExpressionParser.ValueTypeParser;
import org.tdmx.console.application.search.match.MatchFunctionHolder.CalendarRangeHolder;
import org.tdmx.console.application.search.match.MatchFunctionHolder.NumberRangeHolder;

public class ValueTypeFormatterParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testValueTypeParser_TimeRange_NonRange() {
		assertNull(ValueTypeParser.parseTimeRange("."));
		assertNull(ValueTypeParser.parseTimeRange(".."));
		assertNull(ValueTypeParser.parseTimeRange("..."));
		assertNull(ValueTypeParser.parseTimeRange("...."));
		assertNull(ValueTypeParser.parseTimeRange("bla.."));
		assertNull(ValueTypeParser.parseTimeRange("..bla"));
		assertNull(ValueTypeParser.parseTimeRange("..bla.."));
		assertNull(ValueTypeParser.parseTimeRange("...bla..."));
	}

	@Test
	public void testValueTypeParser_TimeRange_Both() {
		String nowTime = getCurrentTime();
		String text = nowTime + ".." + nowTime;
		CalendarRangeHolder range = ValueTypeParser.parseTimeRange(text);
		assertNotNull(range);
		String fromTime = ValueTypeFormatter.getTime(range.from);
		assertEquals(nowTime, fromTime);
		String toTime = ValueTypeFormatter.getTime(range.to);
		assertEquals(nowTime, toTime);
	}

	@Test
	public void testValueTypeParser_TimeRange_From() {
		String nowTime = getCurrentTime();
		String text = nowTime + "..";
		CalendarRangeHolder range = ValueTypeParser.parseTimeRange(text);
		assertNotNull(range);
		assertNull(range.to);
		String fromTime = ValueTypeFormatter.getTime(range.from);
		assertEquals(nowTime, fromTime);
	}

	@Test
	public void testValueTypeParser_TimeRange_To() {
		String nowTime = getCurrentTime();
		String text = ".." + nowTime;
		CalendarRangeHolder range = ValueTypeParser.parseTimeRange(text);
		assertNotNull(range);
		String toTime = ValueTypeFormatter.getTime(range.to);
		assertEquals(nowTime, toTime);
		assertNull(range.from);
	}

	@Test
	public void testValueTypeParser_TimeNotLost() {
		String nowTime = getCurrentTime();
		Calendar nowCal = ValueTypeParser.parseTime(nowTime);
		String nowTime2 = ValueTypeFormatter.getTime(nowCal);

		assertEquals(nowTime2, nowTime);
	}

	@Test
	public void testValueTypeParser_Time() {
		String nowTime = getCurrentTime();
		Calendar parsedCal = ValueTypeParser.parseTime(nowTime);
		assertNotNull(parsedCal);
		String time = ValueTypeFormatter.getTime(parsedCal);
		assertEquals(nowTime, time);
	}

	@Test
	public void testValueTypeParser_NumberRange_NonRange() {
		assertNull(ValueTypeParser.parseNumberRange("."));
		assertNull(ValueTypeParser.parseNumberRange(".."));
		assertNull(ValueTypeParser.parseNumberRange("..."));
		assertNull(ValueTypeParser.parseNumberRange("...."));
		assertNull(ValueTypeParser.parseNumberRange("bla.."));
		assertNull(ValueTypeParser.parseNumberRange("..bla"));
		assertNull(ValueTypeParser.parseNumberRange("..bla.."));
		assertNull(ValueTypeParser.parseNumberRange("...bla..."));
	}

	@Test
	public void testValueTypeParser_NumberRange_Both() {
		String n = "" + System.currentTimeMillis();
		String text = n + ".." + n;
		NumberRangeHolder range = ValueTypeParser.parseNumberRange(text);
		assertNotNull(range);
		String fromN = ValueTypeFormatter.getNumber(range.from);
		assertEquals(n, fromN);
		String toN = ValueTypeFormatter.getNumber(range.to);
		assertEquals(n, toN);
	}

	@Test
	public void testValueTypeParser_NumberRange_From() {
		String n = "" + System.currentTimeMillis();
		String text = n + "..";
		NumberRangeHolder range = ValueTypeParser.parseNumberRange(text);
		assertNotNull(range);
		assertNull(range.to);
		String fromN = ValueTypeFormatter.getNumber(range.from);
		assertEquals(n, fromN);
	}

	@Test
	public void testValueTypeParser_NumberRange_To() {
		String n = "" + System.currentTimeMillis();
		String text = ".." + n;
		NumberRangeHolder range = ValueTypeParser.parseNumberRange(text);
		assertNotNull(range);
		String toN = ValueTypeFormatter.getNumber(range.to);
		assertEquals(n, toN);
		assertNull(range.from);
	}

	@Test
	public void testValueTypeParser_Number() {
		String n = "" + System.currentTimeMillis();
		Number num = ValueTypeParser.parseNumber(n);
		assertNotNull(num);
		String parsedNum = ValueTypeFormatter.getNumber(num);
		assertEquals(n, parsedNum);
	}

	private String getCurrentTime() {
		Calendar now = Calendar.getInstance();

		String nowTime = ValueTypeFormatter.getTime(now);
		return nowTime;
	}

}
