package org.tdmx.console.application.search;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.SearchExpression.ValueType;
import org.tdmx.console.application.search.match.DateDateTimeMatch;
import org.tdmx.console.application.search.match.DateEqualityMatch;
import org.tdmx.console.application.search.match.DateRangeDateMatch;
import org.tdmx.console.application.search.match.DateRangeDateTimeMatch;
import org.tdmx.console.application.search.match.DateTimeEqualityMatch;
import org.tdmx.console.application.search.match.DateTimeRangeDateMatch;
import org.tdmx.console.application.search.match.DateTimeRangeDateTimeMatch;
import org.tdmx.console.application.search.match.MatchFunctionHolder.CalendarRangeHolder;
import org.tdmx.console.application.search.match.MatchFunctionHolder.NumberRangeHolder;
import org.tdmx.console.application.search.match.MatchValueNormalizer;
import org.tdmx.console.application.search.match.NumberEqualityMatch;
import org.tdmx.console.application.search.match.NumberRangeNumberMatch;
import org.tdmx.console.application.search.match.QuotedTextMatch;
import org.tdmx.console.application.search.match.QuotedTextOrMatch;
import org.tdmx.console.application.search.match.StringLikeMatch;
import org.tdmx.console.application.search.match.TextEqualityMatch;
import org.tdmx.console.application.search.match.TextLikeMatch;
import org.tdmx.console.application.search.match.TextLikeOrMatch;
import org.tdmx.console.application.search.match.TimeDateTimeMatch;
import org.tdmx.console.application.search.match.TimeEqualityMatch;
import org.tdmx.console.application.search.match.TimeRangeDateTimeMatch;
import org.tdmx.console.application.search.match.TimeRangeTimeMatch;


/**
 * SearchExpression := (":"<Type>("."<fieldName>)?" ")?<value>
 * 
 * Parses SearchExpressions one by one from a input string.
 * 
 * Start by identifying if the optional part 
 *  (":"<Type>("."<fieldName>)?" ")? 
 * exists by identifying ":" to a WHITESPACE, and parsing it to identify the
 * Type and optional fieldName. 
 * 
 * The next token is then a <value>. The Value is read as QuotedText or Text.
 *  QuotedText ( starts with ", consumed until another " )
 *  Text ( !starting with ", consumed until next WHITESPACE )
 * 
 * From the Text, the 
 *  RangedText ( contains .. )
 *    TimeRange ( Time..Time, ..Time, Time.. )
 *    DateTimeRange ( DateTime..DateTime, ..DateTime, DateTime.. )
 *    DateRange ( Date..Date, ..Date, Date..  )
 *    NumberRange ( Number..Number, ..Number, Number.. )
 *  Time
 *  DateTime
 *  Date
 *  Number
 *  Text
 *  
 *  
 * @author Peter
 *
 */
public final class SearchExpressionParser {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	static final String RANGE = "..";
	
	
	private String input;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SearchExpressionParser(String input) {
		this.input = input;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	/**
	 * @return the next SearchExpression or null if no further exist.
	 */
	public SearchExpression parseNext() {
		// if we've reached the end of the input
		if ( input == null || input.length() == 0 ) {
			return null;
		}
		
		SearchExpression exp = null;
		char[] i = input.toCharArray();
		int pos = 0;
		// trim initial whitespace
		while( pos < i.length && Character.isWhitespace(i[pos])) {
			pos++;
		}
		if ( pos < i.length ) {
			// we have some real text to work with.
			exp = new SearchExpression();
			
			//TODO parse the :Type and optional .fieldName
			
			if ( i[pos] == '\"') {
				// QuotedText
				int qtStart = pos++;
				while ( pos < i.length && i[pos] != '\"') {
					pos++;
				}
				if ( i[pos] == '\"') { // just incase we end on a " not end of input
					pos++;
				}
				// reached the end quote or end of input
				String quotedText = new String(i, qtStart, pos-qtStart);
				parseQuotedValue(quotedText, exp);
			} else {
				// Text
				int tStart = pos;
				while ( pos < i.length && !Character.isWhitespace(i[pos])) {
					pos++;
				}
				// reached the end quote or end of input
				String text = new String(i, tStart, pos-tStart);
				parseValue(text, exp);
			}
		}
		
		// leave the remainder of the input for the next parse
		if ( pos >= i.length ) {
			input = null;
		} else {
			input = new String(i, pos, i.length-pos);
		}
		return exp;
	}
	
	/**
	 * ValueTypeFormatter uses the system's Locale to format Time,DateTime,Date
	 * and Number values.
	 */
	public static class ValueTypeFormatter {
		
		public static String getTimeRange( CalendarRangeHolder range ) {
			DateFormat timeFormatter = DateFormat.getTimeInstance();
			return getRange(timeFormatter, range);
		}
		
		public static String getDateTimeRange( CalendarRangeHolder range ) {
			DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance();
			return getRange(dateTimeFormatter, range);
		}
		
		public static String getDateRange( CalendarRangeHolder range ) {
			DateFormat dateFormatter = DateFormat.getDateInstance();
			return getRange(dateFormatter, range);
		}
		
		private static String getRange( DateFormat formatter, CalendarRangeHolder range ) {
			String fromTime = "";
			String toTime = "";
			if ( range.from != null ) {
				fromTime = formatter.format(range.from.getTime());
			}
			if ( range.to != null ) {
				toTime = formatter.format(range.to.getTime());
			}
			return fromTime + RANGE + toTime;
		}
		
		public static String getTime( Calendar time ) {
			DateFormat timeFormatter = DateFormat.getTimeInstance();
			return getCalendar(timeFormatter, time);
		}
		
		public static String getDateTime( Calendar dateTime ) {
			DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance();
			return getCalendar(dateTimeFormatter, dateTime);
		}
		
		public static String getDate( Calendar date ) {
			DateFormat dateFormatter = DateFormat.getDateInstance();
			return getCalendar(dateFormatter, date);
		}
		
		private static String getCalendar( DateFormat formatter, Calendar date ) {
			if ( date == null ) {
				return null;
			}
			return formatter.format(date.getTime());
		}
		
		public static String getNumber( Number num ) {
			if ( num == null ) {
				return null;
			}
			return num.toString();
		}
	}

	/**
	 * Parse Values according to the following rules:
	 * 
	 *  RangedText ( contains .. )
	 *    TimeRange ( Time..Time, ..Time, Time.. )
	 *    DateTimeRange ( DateTime..DateTime, ..DateTime, DateTime.. )
	 *    DateRange ( Date..Date, ..Date, Date..  )
	 *    NumberRange ( Number..Number, ..Number, Number.. )
	 *    
	 */
	public static class ValueTypeParser {
		
		
		public static Calendar parseTime( String text ) {
			DateFormat timeFormatter = DateFormat.getTimeInstance();
			return parse( timeFormatter, text );
		}
		
		public static Calendar parseDateTime( String text ) {
			DateFormat timeFormatter = DateFormat.getDateTimeInstance();
			return parse( timeFormatter, text );
		}
		
		public static Calendar parseDate( String text ) {
			DateFormat timeFormatter = DateFormat.getDateInstance();
			return parse( timeFormatter, text );
		}
		
		public static Number parseNumber( String text ) {
			try {
				Long number = Long.valueOf(text);
				return number;
			} catch ( NumberFormatException nfe ) {
				return null;
			}
		}

		public static NumberRangeHolder parseNumberRange(String text) {
			if ( text.indexOf(RANGE) == -1 ) {
				return null;
			}
			String fromT = null;
			Number fromNum = null;
			String toT = null;
			Number toNum = null;
			if ( text.startsWith(RANGE)) {
				// can only be ..Number
				toT = text.substring(RANGE.length());
			} else if ( text.endsWith(RANGE)) {
				// can only be Number..
				fromT = text.substring(0,text.length()-RANGE.length());
			} else {
				// likely Number..Number
				int pos = text.indexOf(RANGE);
				toT = text.substring(0, pos);
				fromT = text.substring(pos+RANGE.length(), text.length());
			}

			if ( toT != null ) {
				try {
					toNum = Long.valueOf(toT);
				} catch (NumberFormatException e) {
					return null;
				}
			}
			if ( fromT != null ) {
				try {
					fromNum = Long.valueOf(fromT);
				} catch (NumberFormatException e) {
					return null;
				}
			}
			return new NumberRangeHolder(fromNum, toNum);
		}
		
		private static Calendar parse(DateFormat formatter, String text ) {
			try {
				Date d = formatter.parse(text);
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				return cal;
			} catch ( ParseException e) {
				return null;
			}
		}
		
		public static CalendarRangeHolder parseTimeRange( String text ) {
			DateFormat timeFormatter = DateFormat.getTimeInstance();
			return parseRange( timeFormatter, text);
		}
		
		public static CalendarRangeHolder parseDateTimeRange( String text ) {
			DateFormat timeFormatter = DateFormat.getDateTimeInstance();
			return parseRange( timeFormatter, text);
		}
		
		public static CalendarRangeHolder parseDateRange( String text ) {
			DateFormat timeFormatter = DateFormat.getDateInstance();
			return parseRange( timeFormatter, text);
		}
		
		private static CalendarRangeHolder parseRange(DateFormat formatter, String text ) {
			if ( text.indexOf(RANGE) == -1 ) {
				return null;
			}
			String fromT = null;
			Calendar fromCal = null;
			String toT = null;
			Calendar toCal = null;
			if ( text.startsWith(RANGE)) {
				// can only be ..Time
				toT = text.substring(RANGE.length());
			} else if ( text.endsWith(RANGE)) {
				// can only be Time..
				fromT = text.substring(0,text.length()-RANGE.length());
			} else {
				// likely Time..Time
				int pos = text.indexOf(RANGE);
				toT = text.substring(0, pos);
				fromT = text.substring(pos+RANGE.length(), text.length());
			}

			if ( toT != null ) {
				try {
					Date toTime = formatter.parse(toT);
					toCal = Calendar.getInstance();
					toCal.setTime(toTime);
				} catch (ParseException e) {
					return null;
				}
			}
			if ( fromT != null ) {
				try {
					Date fromTime = formatter.parse(fromT);
					fromCal = Calendar.getInstance();
					fromCal.setTime(fromTime);
				} catch (ParseException e) {
					return null;
				}
			}
			
			return new CalendarRangeHolder(fromCal, toCal);
		}
	}
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private void parseValue( String text, SearchExpression exp ) {
		if ( parseCalendarText(text, exp)) {
			return;
		}
		if ( parseNumberText(text, exp)) {
			return;
		}
		exp.valueType = ValueType.Text;
		exp.add(FieldType.Token, new TextEqualityMatch(MatchValueNormalizer.getStringFromString(text)));
		exp.add(FieldType.String, new StringLikeMatch(MatchValueNormalizer.getStringFromString(text)));
		exp.add(FieldType.Text, new TextLikeMatch(MatchValueNormalizer.getStringFromString(text))) ;
		
	}
	
	private void parseQuotedValue( String text, SearchExpression exp ) {
		//Trim the quotes
		if ( text.startsWith("\"") ) {
			if ( text.endsWith("\"")) {
				text = text.substring(1, text.length()-1);
			} else {
				text = text.substring(1);
			}
		}
		// try to parse any ranged or simple calendar values ( can have SPACES - so need quotes )
		if ( parseCalendarText(text, exp)) {
			return;
		}
		exp.valueType = ValueType.QuotedText;
		exp.add(FieldType.Token, new QuotedTextMatch(text)); //keep case sensitivity in Quoted Text
		exp.add(FieldType.String, new StringLikeMatch(MatchValueNormalizer.getStringFromString(text)));
		exp.add(FieldType.Text, new QuotedTextMatch(text)); //keep case sensitivity in Quoted Text
	}
	
	private boolean parseNumberText( String text, SearchExpression exp ) {
		NumberRangeHolder numberRange = ValueTypeParser.parseNumberRange(text);
		if ( numberRange != null ) {
			exp.valueType = ValueType.NumberRange;
			exp.add(FieldType.Number, new NumberRangeNumberMatch(MatchValueNormalizer.getNumber(numberRange.from), MatchValueNormalizer.getNumber(numberRange.to)));
			exp.add(FieldType.Text, new TextLikeOrMatch(MatchValueNormalizer.getStringNumberList(numberRange.from, numberRange.to)));
			return true;
		}
		Number number = ValueTypeParser.parseNumber(text);
		if ( number != null ) {
			exp.valueType = ValueType.Number;
			exp.add(FieldType.Number, new NumberEqualityMatch(MatchValueNormalizer.getNumber(number)));
			exp.add(FieldType.String, new StringLikeMatch(MatchValueNormalizer.getStringFromNumber(number)));
			exp.add(FieldType.Text, new TextLikeMatch(MatchValueNormalizer.getStringFromNumber(number)));
			return true;
		}
		return false;
	}
	
	private boolean parseCalendarText( String text, SearchExpression exp ) {
		CalendarRangeHolder timeRange = ValueTypeParser.parseTimeRange(text);
		if ( timeRange != null ) {
			exp.valueType = ValueType.TimeRange;
			exp.add(FieldType.Time, new TimeRangeTimeMatch(MatchValueNormalizer.getTime(timeRange.from), MatchValueNormalizer.getTime(timeRange.to)));
			exp.add(FieldType.DateTime, new TimeRangeDateTimeMatch(MatchValueNormalizer.getTime(timeRange.from), MatchValueNormalizer.getTime(timeRange.to)));
			exp.add(FieldType.Text, new QuotedTextOrMatch(MatchValueNormalizer.getStringTimeList(timeRange.from, timeRange.to)));
			return true;
		}
		CalendarRangeHolder dateTimeRange = ValueTypeParser.parseDateTimeRange(text);
		if ( dateTimeRange != null ) {
			exp.valueType = ValueType.DateTimeRange;
			exp.add(FieldType.DateTime, new DateTimeRangeDateTimeMatch(MatchValueNormalizer.getDateTimeTS(dateTimeRange.from), MatchValueNormalizer.getDateTimeTS(dateTimeRange.to)));
			exp.add(FieldType.Date, new DateTimeRangeDateMatch(MatchValueNormalizer.getDate(dateTimeRange.from), MatchValueNormalizer.getDate(dateTimeRange.to)));
			exp.add(FieldType.Text, new QuotedTextOrMatch(MatchValueNormalizer.getStringTimeList(dateTimeRange.from, dateTimeRange.to)));
			return true;
		}
		CalendarRangeHolder dateRange = ValueTypeParser.parseDateRange(text);
		if ( dateRange != null ) {
			exp.valueType = ValueType.DateRange;
			exp.add(FieldType.DateTime, new DateRangeDateTimeMatch(MatchValueNormalizer.getDate(dateRange.from), MatchValueNormalizer.getDate(dateRange.to)));
			exp.add(FieldType.Date, new DateRangeDateMatch(MatchValueNormalizer.getDate(dateRange.from), MatchValueNormalizer.getDate(dateRange.to)));
			exp.add(FieldType.Text, new QuotedTextOrMatch(MatchValueNormalizer.getStringDateList(dateRange.from, dateRange.to)));
			return true;
		}
		Calendar time = ValueTypeParser.parseTime(text);
		if ( time != null ) {
			exp.valueType = ValueType.Time;
			exp.add(FieldType.Time, new TimeEqualityMatch(MatchValueNormalizer.getTime(time)));
			exp.add(FieldType.DateTime, new TimeDateTimeMatch(MatchValueNormalizer.getTime(time)));
			exp.add(FieldType.Text, new QuotedTextMatch(MatchValueNormalizer.getStringFromTime(time)));
			return true;
		}
		Calendar dateTime = ValueTypeParser.parseDateTime(text);
		if ( dateTime != null ) {
			exp.valueType = ValueType.DateTime;
			exp.add(FieldType.Time, new TimeEqualityMatch(MatchValueNormalizer.getTime(dateTime)));
			exp.add(FieldType.DateTime, new DateTimeEqualityMatch(MatchValueNormalizer.getDateTimeTS(dateTime)));
			exp.add(FieldType.Date, new DateEqualityMatch(MatchValueNormalizer.getDate(dateTime)));
			exp.add(FieldType.Text, new QuotedTextMatch(MatchValueNormalizer.getStringFromDate(dateTime)));
			return true;
		}
		Calendar date = ValueTypeParser.parseDate(text);
		if ( date != null ) {
			exp.valueType = ValueType.Date;
			exp.add(FieldType.DateTime, new DateDateTimeMatch(MatchValueNormalizer.getDate(date)));
			exp.add(FieldType.Date, new DateEqualityMatch(MatchValueNormalizer.getDate(date)));
			exp.add(FieldType.Text, new QuotedTextMatch(MatchValueNormalizer.getStringFromDate(date)));
			return true;
		}
		return false;
	}

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------
	
}
