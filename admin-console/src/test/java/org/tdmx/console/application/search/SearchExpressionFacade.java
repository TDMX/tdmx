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

import java.util.Set;

import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.SearchExpression.ValueType;
import org.tdmx.console.application.search.match.MatchFunction;
import org.tdmx.console.application.search.match.MatchValueFormatter;
import org.tdmx.console.application.search.match.NumberEqualityMatch;
import org.tdmx.console.application.search.match.NumberRangeNumberMatch;
import org.tdmx.console.application.search.match.QuotedTextMatch;
import org.tdmx.console.application.search.match.StringLikeMatch;
import org.tdmx.console.application.search.match.TextEqualityMatch;
import org.tdmx.console.application.search.match.TextLikeMatch;
import org.tdmx.console.application.search.match.TextLikeOrMatch;

public class SearchExpressionFacade {

	public SearchExpression createNumberExpression(Long num) {
		SearchExpression exp = new SearchExpression();
		exp.valueType = ValueType.Number;
		exp.add(FieldType.Number, new NumberEqualityMatch(num));
		exp.add(FieldType.String, new StringLikeMatch(MatchValueFormatter.getNumber(num)));
		exp.add(FieldType.Text, new TextLikeMatch(MatchValueFormatter.getNumber(num)));
		return exp;
	}

	public SearchExpression createNumberRangeExpression(Long from, Long to) {
		SearchExpression exp = new SearchExpression();
		exp.valueType = ValueType.NumberRange;
		exp.add(FieldType.Number, new NumberRangeNumberMatch(from, to));
		exp.add(FieldType.Text, new TextLikeOrMatch(MatchValueFormatter.getStringNumberList(from, to)));
		return exp;
	}

	public SearchExpression createQuotedTextExpression(String unquotedText) {
		SearchExpression exp = new SearchExpression();
		exp.valueType = ValueType.QuotedText;
		exp.add(FieldType.String, new StringLikeMatch(unquotedText.toLowerCase()));
		exp.add(FieldType.Token, new QuotedTextMatch(unquotedText));
		exp.add(FieldType.Text, new QuotedTextMatch(unquotedText));
		return exp;
	}

	public SearchExpression createTextExpression(String textLowercase) {
		SearchExpression exp = new SearchExpression();
		exp.valueType = ValueType.Text;
		exp.add(FieldType.String, new StringLikeMatch(textLowercase));
		exp.add(FieldType.Token, new TextEqualityMatch(textLowercase));
		exp.add(FieldType.Text, new TextLikeMatch(textLowercase));
		return exp;
	}

	public void checkEquals(SearchExpression expected, SearchExpression current) {
		assertEquals(expected.objectType, current.objectType);
		assertEquals(expected.fieldName, current.fieldName);
		assertEquals(expected.valueType, current.valueType);

		Set<FieldType> expectedFieldSet = expected.matchFunctionMap.keySet();
		Set<FieldType> currentFieldSet = current.matchFunctionMap.keySet();
		assertEquals(expectedFieldSet, currentFieldSet);

		for (FieldType f : expectedFieldSet) {
			checkEquals(expected.matchFunctionMap.get(f), current.matchFunctionMap.get(f));
		}
	}

	public void checkEquals(MatchFunction expected, MatchFunction current) {
		assertEquals(expected.getClass().getName(), current.getClass().getName());
		assertEquals(expected.toString(), current.toString());
	}
}
