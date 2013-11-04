package org.tdmx.console.application.search;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.SearchExpression.ValueType;
import org.tdmx.console.application.search.match.MatchFunction;
import org.tdmx.console.application.search.match.QuotedTextMatch;
import org.tdmx.console.application.search.match.TextEqualityMatch;
import org.tdmx.console.application.search.match.TextLikeMatch;


public class SearchExpressionFacade {

	public SearchExpression createQuotedTextExpression( String unquotedTextLowercase ) {
		SearchExpression exp = new SearchExpression();
		exp.valueType = ValueType.QuotedText;
		exp.add(FieldType.Text, new QuotedTextMatch(unquotedTextLowercase));
		return exp;
	}
	
	public SearchExpression createTextExpression( String textLowercase ) {
		SearchExpression exp = new SearchExpression();
		exp.valueType = ValueType.Text;
		exp.add(FieldType.Token, new TextEqualityMatch(textLowercase));
		exp.add(FieldType.Text, new TextLikeMatch(textLowercase));
		return exp;
	}
	
	public void checkEquals( SearchExpression expected, SearchExpression current ) {
		assertEquals( expected.objectType, current.objectType );
		assertEquals( expected.fieldName, current.fieldName );
		assertEquals( expected.valueType, current.valueType );
		
		Set<FieldType> expectedFieldSet = expected.matchFunctionMap.keySet();
		Set<FieldType> currentFieldSet = current.matchFunctionMap.keySet();
		assertEquals( expectedFieldSet, currentFieldSet);
		
		for( FieldType f : expectedFieldSet ) {
			checkEquals(expected.matchFunctionMap.get(f), current.matchFunctionMap.get(f));
		}
	}
	public void checkEquals( MatchFunction expected, MatchFunction current ) {
		assertEquals( expected.getClass().getName(), current.getClass().getName());
		assertEquals( expected.toString(), current.toString());
	}
}
