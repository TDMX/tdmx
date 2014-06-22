package org.tdmx.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Locale;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JvmTest {

	private final Logger log = LoggerFactory.getLogger(JvmTest.class);

	@Test
	public void testLogging() {
		log.debug("DEBUG LOG");

		log.info("INFO LOG");

		log.warn("WARN LOG");
	}

	@Test
	public void testLocale() {

		assertEquals("en_US", Locale.getDefault().toString());
		assertEquals("de_CH", Locale.getDefault(Locale.Category.FORMAT).toString());
	}

	@Test
	public void testDateFormatLeniency() {

		DateFormat df = DateFormat.getDateInstance();
		assertTrue(df.isLenient());
	}
}
