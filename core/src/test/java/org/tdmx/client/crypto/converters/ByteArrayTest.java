package org.tdmx.client.crypto.converters;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ByteArrayTest {


	@Test
	public void test() {
		byte[] first = new byte[100];
		for( int i = 0; i < first.length; i++) {
			first[i] = (byte)(i*2);
		}
		byte[] second = new byte[100];
		for( int i = 0; i < second.length; i++) {
			second[i] = (byte)(i*2 + 1);
		}
		byte[] interleave = ByteArray.interleave(first, second);
		assertEquals( 200, interleave.length);
		for ( int i = 0;i < interleave.length; i++) {
			assertEquals( (byte)i, interleave[i]);
		}
	}

}
