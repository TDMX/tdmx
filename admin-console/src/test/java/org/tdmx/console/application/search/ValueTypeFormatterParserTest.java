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
		assertNull( ValueTypeParser.parseTimeRange("."));
		assertNull( ValueTypeParser.parseTimeRange(".."));
		assertNull( ValueTypeParser.parseTimeRange("..."));
		assertNull( ValueTypeParser.parseTimeRange("...."));
		assertNull( ValueTypeParser.parseTimeRange("bla.."));
		assertNull( ValueTypeParser.parseTimeRange("..bla"));
		assertNull( ValueTypeParser.parseTimeRange("..bla.."));
		assertNull( ValueTypeParser.parseTimeRange("...bla..."));
	}
	
	@Test
	public void testValueTypeParser_TimeRange_Both() {
		String nowTime = getCurrentTime();
		String text = nowTime+".."+nowTime;
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
		String text = nowTime+"..";
		CalendarRangeHolder range = ValueTypeParser.parseTimeRange(text);
		assertNotNull(range);
		assertNull(range.from);
		String toTime = ValueTypeFormatter.getTime(range.to);
		assertEquals(nowTime, toTime);
	}
	
	@Test
	public void testValueTypeParser_TimeRange_To() {
		String nowTime = getCurrentTime();
		String text = ".."+nowTime;
		CalendarRangeHolder range = ValueTypeParser.parseTimeRange(text);
		assertNotNull(range);
		String fromTime = ValueTypeFormatter.getTime(range.from);
		assertEquals(nowTime, fromTime);
		assertNull(range.to);
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
		assertNull( ValueTypeParser.parseNumberRange("."));
		assertNull( ValueTypeParser.parseNumberRange(".."));
		assertNull( ValueTypeParser.parseNumberRange("..."));
		assertNull( ValueTypeParser.parseNumberRange("...."));
		assertNull( ValueTypeParser.parseNumberRange("bla.."));
		assertNull( ValueTypeParser.parseNumberRange("..bla"));
		assertNull( ValueTypeParser.parseNumberRange("..bla.."));
		assertNull( ValueTypeParser.parseNumberRange("...bla..."));
	}
	
	@Test
	public void testValueTypeParser_NumberRange_Both() {
		String n = ""+System.currentTimeMillis();
		String text = n+".."+n;
		NumberRangeHolder range = ValueTypeParser.parseNumberRange(text);
		assertNotNull(range);
		String fromN = ValueTypeFormatter.getNumber(range.from);
		assertEquals(n, fromN);
		String toN = ValueTypeFormatter.getNumber(range.to);
		assertEquals(n, toN);
	}
	
	@Test
	public void testValueTypeParser_NumberRange_From() {
		String n = ""+System.currentTimeMillis();
		String text = n+"..";
		NumberRangeHolder range = ValueTypeParser.parseNumberRange(text);
		assertNotNull(range);
		assertNull(range.from);
		String toN = ValueTypeFormatter.getNumber(range.to);
		assertEquals(n, toN);
	}
	
	@Test
	public void testValueTypeParser_NumberRange_To() {
		String n = ""+System.currentTimeMillis();
		String text = ".."+n;
		NumberRangeHolder range = ValueTypeParser.parseNumberRange(text);
		assertNotNull(range);
		String fromN = ValueTypeFormatter.getNumber(range.from);
		assertEquals(n, fromN);
		assertNull(range.to);
	}
	
	@Test
	public void testValueTypeParser_Number() {
		String n = ""+System.currentTimeMillis();
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
