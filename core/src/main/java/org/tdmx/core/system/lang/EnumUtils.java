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
package org.tdmx.core.system.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date is just a day YYYY-MM-DD DateTime is a timestamp with YYYY-MM-DDTHH:MI:SS, without milliseconds.
 * 
 * @author Peter
 * 
 */
public class EnumUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static Logger log = LoggerFactory.getLogger(EnumUtils.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private EnumUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public static <E extends Enum<E>> E mapTo(Class<E> enumType, String s) {
		if (!StringUtils.hasText(s)) {
			return null;
		}
		try {
			E e = Enum.valueOf(enumType, s);
			return e;
		} catch (IllegalArgumentException e) {
			log.debug("Invalid enum value " + s + " for " + enumType.getName());
		}

		return null;
	}

}
