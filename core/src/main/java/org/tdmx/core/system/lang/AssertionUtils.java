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

import java.util.Date;

public class AssertionUtils {

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	private AssertionUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * @throws IllegalStateException
	 *             if try to change immutable fields.
	 */
	public static void assertSame(String field, Date target, Date actual) {
		if (target != null) {
			if (actual == null || target.getTime() != actual.getTime()) {
				throw new IllegalStateException("Field " + field + " is immutable, and cannot be changed or removed.");
			}
		} else if (actual != null) {
			throw new IllegalStateException("Field " + field + " is immutable, and cannot be set.");
		}
	}

	/**
	 * @throws IllegalStateException
	 *             if try to change immutable fields.
	 */
	public static void assertSame(String field, Object target, Object actual) {
		if (target != null) {
			if (!target.equals(actual)) {
				throw new IllegalStateException("Field " + field + " is immutable, and cannot be changed or removed.");
			}
		} else if (actual != null) {
			throw new IllegalStateException("Field " + field + " is immutable, and cannot be set.");
		}
	}

	/**
	 * @throws IllegalStateException
	 *             if try to change immutable fields.
	 */
	public static void assertSame(String field, int target, int actual) {
		if (target != actual) {
			throw new IllegalStateException("Field " + field + " is immutable, and cannot be changed. ");
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
}
