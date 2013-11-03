package org.tdmx.console.application.search;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DateFormatTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFormat() {
		Date d = new Date();
		
		//Date
		DateFormat df = DateFormat.getDateInstance();
		System.out.println( df.format(d) );

		//DateTime
		df = DateFormat.getDateTimeInstance();
		System.out.println( df.format(d) );
		
		//Time
		df = DateFormat.getTimeInstance();
		System.out.println( df.format(d) );

	}

	@Test
	public void testParse_Time() {
		//Time
		DateFormat df = DateFormat.getTimeInstance();
		System.out.println(df.isLenient());
		
		expectParseSuccess(df, "11:39:19");
		expectParseFailure(df, "11:39");
		expectParseFailure(df,  "11");
		expectParseFailure(df, "03.11.2013");
		expectParseFailure(df, "03.11.2013 11:39:19");
	}
	
	@Test
	public void testParse_DateTime() {
		//Time
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
		//Time
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

	private void expectParseFailure( DateFormat format, String text ) {
		try {
			Date d = format.parse(text);
			
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			
			fail( "Expected [" + text + "] to fail with " + format.toString() + " instead got " + c.toString());
		} catch (ParseException e) {
		}
	}
	
	private void expectParseSuccess( DateFormat format, String text ) {
		try {
			Date d = format.parse(text);
			
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			
			System.out.println(" formatted " + text + " to " + c.toString());
		} catch (ParseException e) {
			fail( "Expected [" + text + "] to succeed with " + format.toString());
		}
	}
	
	
}
