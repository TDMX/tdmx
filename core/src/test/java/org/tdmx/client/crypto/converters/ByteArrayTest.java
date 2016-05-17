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
package org.tdmx.client.crypto.converters;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ByteArrayTest {

	@Test
	public void test() {
		byte[] first = new byte[100];
		for (int i = 0; i < first.length; i++) {
			first[i] = (byte) (i * 2);
		}
		byte[] second = new byte[100];
		for (int i = 0; i < second.length; i++) {
			second[i] = (byte) (i * 2 + 1);
		}
		byte[] interleave = ByteArray.interleave(first, second);
		assertEquals(200, interleave.length);
		for (int i = 0; i < interleave.length; i++) {
			assertEquals((byte) i, interleave[i]);
		}
	}

}
