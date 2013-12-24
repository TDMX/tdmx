package org.tdmx.console.application.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.console.application.domain.ValueObject;

public class ValueObjectUtils {

	private static Logger log = LoggerFactory.getLogger(ValueObjectUtils.class);

	/**
	 * Clone a list of ValueObjects
	 * @param list
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E extends ValueObject> List<E> cloneList( List<E> list ) {
		if ( list == null ) {
			return null;
		}
		List<E> result = new ArrayList<>();
		for( E vo : list ) {
			;
			result.add((E)vo.copy());
		}
		return result;
	}
	
}
