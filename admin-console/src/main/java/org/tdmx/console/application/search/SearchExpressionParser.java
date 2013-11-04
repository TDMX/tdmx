package org.tdmx.console.application.search;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.SearchExpression.ValueType;
import org.tdmx.console.application.search.match.MatchFunctionHolder;
import org.tdmx.console.application.search.match.MatchFunctionHolder.CalendarRangeHolder;
import org.tdmx.console.application.search.match.QuotedTextMatch;
import org.tdmx.console.application.search.match.TextEqualityMatch;
import org.tdmx.console.application.search.match.TextLikeMatch;


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
	
	public static class ValueTypeFormatter {
		public static String getTime( Calendar time ) {
			if ( time == null ) {
				return null;
			}
			DateFormat timeFormatter = DateFormat.getTimeInstance();
			return timeFormatter.format(time.getTime());
		}
	}
	
	public static class ValueTypeParser {
		/*
		 *  RangedText ( contains .. )
		 *    TimeRange ( Time..Time, ..Time, Time.. )
		 *    DateTimeRange ( DateTime..DateTime, ..DateTime, DateTime.. )
		 *    DateRange ( Date..Date, ..Date, Date..  )
		 *    NumberRange ( Number..Number, ..Number, Number.. )
		*/
		
		private static final String RANGE = "..";
		
		//TODO parse Number
		
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
			if ( text.indexOf("..") == -1 ) {
				return null;
			}
			String fromT = null;
			Calendar fromCal = null;
			String toT = null;
			Calendar toCal = null;
			if ( text.startsWith(RANGE)) {
				// can only be ..Time
				fromT = text.substring(RANGE.length());
			} else if ( text.endsWith(RANGE)) {
				// can only be Time..
				toT = text.substring(0,text.length()-RANGE.length());
			} else {
				// likely Time..Time
				int pos = text.indexOf(RANGE);
				fromT = text.substring(0, pos);
				toT = text.substring(pos+RANGE.length(), text.length());
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
		//TODO other types
		CalendarRangeHolder timeRange = ValueTypeParser.parseTimeRange(text);
		if ( timeRange != null ) {
			exp.valueType = ValueType.TimeRange;
			//TODO
			return;
		}
		exp.valueType = ValueType.Text;
		String matchValue = text.toLowerCase();
		exp.add(FieldType.Token, new TextEqualityMatch(matchValue));
		exp.add(FieldType.Text, new TextLikeMatch(matchValue)) ;
		
	}
	
	private void parseQuotedValue( String text, SearchExpression exp ) {
		exp.valueType = ValueType.QuotedText;
		String matchValue = text.toLowerCase();
		//Trim the quotes
		if ( matchValue.startsWith("\"") ) {
			if ( matchValue.endsWith("\"")) {
				matchValue = matchValue.substring(1, matchValue.length()-1);
			} else {
				matchValue = matchValue.substring(1);
			}
		}
		exp.add(FieldType.Text, new QuotedTextMatch(matchValue));
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------
	
}
