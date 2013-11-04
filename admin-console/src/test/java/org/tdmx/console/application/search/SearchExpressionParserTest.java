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

public class SearchExpressionParserTest {

	private SearchExpressionFacade facade = new SearchExpressionFacade();
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testParseNext_NoInput() {
		SearchExpressionParser p = new SearchExpressionParser(null);
		assertNull(p.parseNext());
	}

	@Test
	public void testParseNext_EmptyInput() {
		SearchExpressionParser p = new SearchExpressionParser("");
		assertNull(p.parseNext());
	}

	@Test
	public void testParseNext_BlankInput() {
		SearchExpressionParser p = new SearchExpressionParser("     ");
		assertNull(p.parseNext());
	}

	@Test
	public void testParseNext_QuotedText() {
		SearchExpressionParser p = new SearchExpressionParser("\"Hello World\"");
		SearchExpression e = p.parseNext();
		assertNotNull(e);
		
		SearchExpression expected = facade.createQuotedTextExpression("hello world");
		facade.checkEquals(expected, e);
		
		assertNull(p.parseNext());
	}

	@Test
	public void testParseNext_QuotedText2() {
		SearchExpressionParser p = new SearchExpressionParser(" \"Hello World\"   \" Hello World2 \"  ");
		SearchExpression e = p.parseNext();
		assertNotNull(e);
		
		SearchExpression expected = facade.createQuotedTextExpression("hello world");
		facade.checkEquals(expected, e);
		
		SearchExpression e2 = p.parseNext();
		SearchExpression expected2 = facade.createQuotedTextExpression(" hello world2 ");
		facade.checkEquals(expected2, e2);
		
		assertNull(p.parseNext());
	}

	@Test
	public void testParseNext_Text() {
		SearchExpressionParser p = new SearchExpressionParser("Hello World");
		SearchExpression e = p.parseNext();
		assertNotNull(e);
		
		SearchExpression expected = facade.createTextExpression("hello");
		facade.checkEquals(expected, e);

		SearchExpression e2 = p.parseNext();
		assertNotNull(e2);

		SearchExpression expected2 = facade.createTextExpression("world");
		facade.checkEquals(expected2, e2);

	}

	@Test
	public void testValueTypeParser_TimeRange_NonRange() {
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
	
	private String getCurrentTime() {
		Calendar now = Calendar.getInstance();
		
		String nowTime = ValueTypeFormatter.getTime(now);
		return nowTime;
	}
	
}
