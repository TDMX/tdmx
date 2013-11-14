package org.tdmx.console.application.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

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
		
		SearchExpression expected = facade.createQuotedTextExpression("Hello World");
		facade.checkEquals(expected, e);
		
		assertNull(p.parseNext());
	}

	@Test
	public void testParseNext_QuotedText2() {
		SearchExpressionParser p = new SearchExpressionParser(" \"Hello World\"   \" Hello World2 \"  ");
		SearchExpression e = p.parseNext();
		assertNotNull(e);
		
		SearchExpression expected = facade.createQuotedTextExpression("Hello World");
		facade.checkEquals(expected, e);
		
		SearchExpression e2 = p.parseNext();
		SearchExpression expected2 = facade.createQuotedTextExpression(" Hello World2 ");
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
	public void testParseNext_Number() {
		SearchExpressionParser p = new SearchExpressionParser("123456");
		SearchExpression e = p.parseNext();
		assertNotNull(e);
		
		SearchExpression expected = facade.createNumberExpression(123456l);
		facade.checkEquals(expected, e);
	}

	@Test
	public void testParseNext_NumberRange_From() {
		SearchExpressionParser p = new SearchExpressionParser("123456..");
		SearchExpression e = p.parseNext();
		assertNotNull(e);
		
		SearchExpression expected = facade.createNumberRangeExpression(123456l, null);
		facade.checkEquals(expected, e);
	}

	@Test
	public void testParseNext_NumberRange_To() {
		SearchExpressionParser p = new SearchExpressionParser("..123456");
		SearchExpression e = p.parseNext();
		assertNotNull(e);
		
		SearchExpression expected = facade.createNumberRangeExpression(null, 123456l);
		facade.checkEquals(expected, e);
	}

	@Test
	public void testParseNext_NumberRange_Both() {
		SearchExpressionParser p = new SearchExpressionParser("012345..123456");
		SearchExpression e = p.parseNext();
		assertNotNull(e);
		
		SearchExpression expected = facade.createNumberRangeExpression(12345l, 123456l);
		facade.checkEquals(expected, e);
	}
	
	//TODO time
	
	//TODO timerange
	
	//TODO date
	
	//TODO daterange
	
	//TODO datetime
	
	//TODO datetimerange
	
}
