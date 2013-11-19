package org.tdmx.client.crypto.algorithm;

import static org.junit.Assert.*;

import org.junit.Test;

import org.tdmx.client.crypto.converters.NumberToOctetString;

public class LongToOctetStringTest {

	@Test
	public void testLongBoundary() {
		assertEquals( Long.MAX_VALUE, NumberToOctetString.bytesToLong(NumberToOctetString.longToBytes(Long.MAX_VALUE)));
		assertEquals( Long.MIN_VALUE, NumberToOctetString.bytesToLong(NumberToOctetString.longToBytes(Long.MIN_VALUE)));
		assertEquals( 0l, NumberToOctetString.bytesToLong(NumberToOctetString.longToBytes(0l)));
	}

	
}
