package org.tdmx.console.application.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.console.application.domain.DomainObjectField;
import org.tdmx.console.application.domain.DomainObjectType;
import org.tdmx.console.application.domain.validation.FieldError;
import org.tdmx.console.application.domain.validation.OperationError.ERROR;

public class ValidationUtilsTest {

	private ERROR error = ERROR.INVALID;
	private List<FieldError> errors = new ArrayList<>();
	private DomainObjectField field = new DomainObjectField("fieldname", DomainObjectType.SystemPropertyList);
	
	@Before
	public void setUp() throws Exception {
		errors.clear();
	}

	@Test
	public void testMandatoryTextField_Missing() {
		ValidationUtils.mandatoryTextField("", field, error, errors);
		assertContainsError();
		ValidationUtils.mandatoryTextField(null, field, error, errors);
		assertContainsError();
	}

	@Test
	public void testMandatoryTextField_Present() {
		ValidationUtils.mandatoryTextField("hello", field, error, errors);
		assertNoError();
	}

	@Test
	public void testMandatoryNumberField_Missing() {
		ValidationUtils.mandatoryNumberField(null, field, error, errors);
		assertContainsError();
	}

	@Test
	public void testMandatoryNumberField_Present() {
		ValidationUtils.mandatoryNumberField(new Integer(1), field, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalTextFieldGroup_None() {
		ValidationUtils.optionalTextFieldGroup(new String[]{ null, null }, field, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "", null }, field, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ null, "" }, field, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "", "" }, field, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalTextFieldGroup_All() {
		ValidationUtils.optionalTextFieldGroup(new String[]{ "a", "b" }, field, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "a", "b", "c", "d" }, field, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalTextFieldGroup_Missing() {
		ValidationUtils.optionalTextFieldGroup(new String[]{ null, "b" }, field, error, errors);
		assertContainsError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "", "b" }, field, error, errors);
		assertContainsError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "a", null }, field, error, errors);
		assertContainsError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "a", "" }, field, error, errors);
		assertContainsError();
	}

	@Test
	public void testOptionalHostnameField_Valid() {
		ValidationUtils.optionalHostnameField("", field, error, errors);
		assertNoError();
		ValidationUtils.optionalHostnameField("google.ch", field, error, errors);
		assertNoError();
		ValidationUtils.optionalHostnameField("127.0.0.1", field, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalHostnameField_Invalid() {
		ValidationUtils.optionalHostnameField("a.sdf.fwe..vcw", field, error, errors);
		assertContainsError();
		ValidationUtils.optionalHostnameField("1.0.2.3.5", field, error, errors);
		assertContainsError();
	}

	@Test
	public void testOptionalEnumeratedTextField_Valid() {
		List<String> validList = new ArrayList<>();
		validList.add("a");
		validList.add("b");
		ValidationUtils.optionalEnumeratedTextField(null, validList, field, error, errors);
		assertNoError();
		ValidationUtils.optionalEnumeratedTextField("", validList, field, error, errors);
		assertNoError();
		ValidationUtils.optionalEnumeratedTextField("a", validList, field, error, errors);
		assertNoError();
		ValidationUtils.optionalEnumeratedTextField("b", validList, field, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalEnumeratedTextField_Invalid() {
		List<String> validList = new ArrayList<>();
		validList.add("a");
		validList.add("b");
		ValidationUtils.optionalEnumeratedTextField("c", validList, field, error, errors);
		assertContainsError();
	}

	
	
	private void assertNoError() {
		assertTrue( errors.isEmpty());
	}
	
	private void assertContainsError() {
		boolean found = false;
		for( FieldError e : errors) {
			if ( e.getError() == error ) {
				found = true;
				break;
			}
		}
		assertTrue( found);
		errors.clear();
	}
}
