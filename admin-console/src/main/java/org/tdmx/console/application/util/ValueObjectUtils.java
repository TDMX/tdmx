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
	 * 
	 * @param list
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E extends ValueObject> List<E> cloneList(List<E> list) {
		if (list == null) {
			return null;
		}
		List<E> result = new ArrayList<>();
		for (E vo : list) {
			;
			result.add((E) vo.copy());
		}
		return result;
	}

}
