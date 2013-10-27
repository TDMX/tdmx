package org.tdmx.console.application.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ValidationUtilsTest {

	private Object error = new Object();
	private List<Object> errors = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		errors.clear();
	}

	@Test
	public void testMandatoryTextField_Missing() {
		ValidationUtils.mandatoryTextField("", error, errors);
		assertContainsError();
		ValidationUtils.mandatoryTextField(null, error, errors);
		assertContainsError();
	}

	@Test
	public void testMandatoryTextField_Present() {
		ValidationUtils.mandatoryTextField("hello", error, errors);
		assertNoError();
	}

	@Test
	public void testMandatoryNumberField_Missing() {
		ValidationUtils.mandatoryNumberField(null, error, errors);
		assertContainsError();
	}

	@Test
	public void testMandatoryNumberField_Present() {
		ValidationUtils.mandatoryNumberField(new Integer(1), error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalTextFieldGroup_None() {
		ValidationUtils.optionalTextFieldGroup(new String[]{ null, null }, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "", null }, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ null, "" }, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "", "" }, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalTextFieldGroup_All() {
		ValidationUtils.optionalTextFieldGroup(new String[]{ "a", "b" }, error, errors);
		assertNoError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "a", "b", "c", "d" }, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalTextFieldGroup_Missing() {
		ValidationUtils.optionalTextFieldGroup(new String[]{ null, "b" }, error, errors);
		assertContainsError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "", "b" }, error, errors);
		assertContainsError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "a", null }, error, errors);
		assertContainsError();
		ValidationUtils.optionalTextFieldGroup(new String[]{ "a", "" }, error, errors);
		assertContainsError();
	}

	@Test
	public void testOptionalHostnameField_Valid() {
		ValidationUtils.optionalHostnameField("", error, errors);
		assertNoError();
		ValidationUtils.optionalHostnameField("google.ch", error, errors);
		assertNoError();
		ValidationUtils.optionalHostnameField("127.0.0.1", error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalHostnameField_Invalid() {
		ValidationUtils.optionalHostnameField("a.sdf.fwe..vcw", error, errors);
		assertContainsError();
		ValidationUtils.optionalHostnameField("1.0.2.3.5", error, errors);
		assertContainsError();
	}

	@Test
	public void testOptionalPortField_Valid() {
		ValidationUtils.optionalPortField(new Integer(0), error, errors);
		assertNoError();
		ValidationUtils.optionalPortField(new Integer(80), error, errors);
		assertNoError();
		ValidationUtils.optionalPortField(new Integer(8080), error, errors);
		assertNoError();
		ValidationUtils.optionalPortField(new Integer(443), error, errors);
		assertNoError();
		ValidationUtils.optionalPortField(new Integer(65536), error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalPortField_Invalid() {
		ValidationUtils.optionalPortField(new Integer(-1), error, errors);
		assertContainsError();
		ValidationUtils.optionalPortField(new Integer(65537), error, errors);
		assertContainsError();
		ValidationUtils.optionalPortField(new Integer(Integer.MAX_VALUE), error, errors);
		assertContainsError();
		ValidationUtils.optionalPortField(new Integer(Integer.MIN_VALUE), error, errors);
		assertContainsError();
	}
	
	@Test
	public void testOptionalEnumeratedTextField_Valid() {
		List<String> validList = new ArrayList<>();
		validList.add("a");
		validList.add("b");
		ValidationUtils.optionalEnumeratedTextField(null, validList, error, errors);
		assertNoError();
		ValidationUtils.optionalEnumeratedTextField("", validList, error, errors);
		assertNoError();
		ValidationUtils.optionalEnumeratedTextField("a", validList, error, errors);
		assertNoError();
		ValidationUtils.optionalEnumeratedTextField("b", validList, error, errors);
		assertNoError();
	}

	@Test
	public void testOptionalEnumeratedTextField_Invalid() {
		List<String> validList = new ArrayList<>();
		validList.add("a");
		validList.add("b");
		ValidationUtils.optionalEnumeratedTextField("c", validList, error, errors);
		assertContainsError();
	}

	
	
	private void assertNoError() {
		assertTrue( errors.isEmpty());
	}
	
	private void assertContainsError() {
		assertTrue( errors.contains(error));
		errors.clear();
	}
}
