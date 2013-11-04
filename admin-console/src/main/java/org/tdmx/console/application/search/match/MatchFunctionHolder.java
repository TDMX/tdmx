package org.tdmx.console.application.search.match;

import java.util.Calendar;


public class MatchFunctionHolder {

	public static class NumberRangeHolder {
		public Number from;
		public Number to;
		public NumberRangeHolder( Number from, Number to) {
			this.from = from;
			this.to = to;
		}
	}

	public static class CalendarRangeHolder {
		public Calendar from;
		public Calendar to;
		
		public CalendarRangeHolder( Calendar from, Calendar to) {
			this.from = from;
			this.to = to;
		}
	}
}
