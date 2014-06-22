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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError.ERROR;

public class ValidationUtilsTest {

	private final ERROR error = ERROR.INVALID;
	private final List<FieldError> errors = new ArrayList<>();
	private final String fieldName = "fieldName!";

	@Before
	public void setUp() throws Exception {
		errors.clear();
	}

	@Test
	public void testMandatoryTextField_Missing() {
		ValidationUtils.mandatoryTextField("", fieldName, error, errors);
		assertContainsError();
		ValidationUtils.mandatoryTextField(null, fieldName, error, errors);
		assertContainsError();
	}

	@Test
	public void testMandatoryTextField_Present() {
		ValidationUtils.mandatoryTextField("hello", fieldName, error, errors);
		assertNoError();
	}

	@Test
	public void testMandatoryNumberField_Missing() {
		ValidationUtils.mandatoryNumberField(null, fieldName, error, errors);
		assertContainsError();
	}

	@Test
	public void testMandatoryNumberField_Present() {
		ValidationUtils.mandatoryNumberField(new Integer(1), fieldName, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalTextFieldGroup_None() {
		ValidationUtils.optionalTextFieldGroup(new String[] { null, null }, fieldName, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[] { "", null }, fieldName, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[] { null, "" }, fieldName, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[] { "", "" }, fieldName, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalTextFieldGroup_All() {
		ValidationUtils.optionalTextFieldGroup(new String[] { "a", "b" }, fieldName, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[] { "a", "b", "c", "d" }, fieldName, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalTextFieldGroup_Missing() {
		ValidationUtils.optionalTextFieldGroup(new String[] { null, "b" }, fieldName, error, errors);
		assertContainsError();
		ValidationUtils.optionalTextFieldGroup(new String[] { "", "b" }, fieldName, error, errors);
		assertContainsError();
		ValidationUtils.optionalTextFieldGroup(new String[] { "a", null }, fieldName, error, errors);
		assertContainsError();
		ValidationUtils.optionalTextFieldGroup(new String[] { "a", "" }, fieldName, error, errors);
		assertContainsError();
	}

	@Test
	public void testOptionalHostnameField_Valid() {
		ValidationUtils.optionalHostnameField("", fieldName, error, errors);
		assertNoError();
		ValidationUtils.optionalHostnameField("google.ch", fieldName, error, errors);
		assertNoError();
		ValidationUtils.optionalHostnameField("127.0.0.1", fieldName, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalHostnameField_Invalid() {
		ValidationUtils.optionalHostnameField("a.sdf.fwe..vcw", fieldName, error, errors);
		assertContainsError();
		ValidationUtils.optionalHostnameField("1.0.2.3.5", fieldName, error, errors);
		assertContainsError();
	}

	@Test
	public void testOptionalEnumeratedTextField_Valid() {
		List<String> validList = new ArrayList<>();
		validList.add("a");
		validList.add("b");
		ValidationUtils.optionalEnumeratedTextField(null, validList, fieldName, error, errors);
		assertNoError();
		ValidationUtils.optionalEnumeratedTextField("", validList, fieldName, error, errors);
		assertNoError();
		ValidationUtils.optionalEnumeratedTextField("a", validList, fieldName, error, errors);
		assertNoError();
		ValidationUtils.optionalEnumeratedTextField("b", validList, fieldName, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalEnumeratedTextField_Invalid() {
		List<String> validList = new ArrayList<>();
		validList.add("a");
		validList.add("b");
		ValidationUtils.optionalEnumeratedTextField("c", validList, fieldName, error, errors);
		assertContainsError();
	}

	private void assertNoError() {
		assertTrue(errors.isEmpty());
	}

	private void assertContainsError() {
		boolean found = false;
		for (FieldError e : errors) {
			if (e.getError() == error) {
				found = true;
				break;
			}
		}
		assertTrue(found);
		errors.clear();
	}
}
