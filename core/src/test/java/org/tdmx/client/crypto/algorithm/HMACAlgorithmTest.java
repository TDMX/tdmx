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
package org.tdmx.client.crypto.algorithm;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.converters.ByteArray;

/**
 * 
 * http://www.ietf.org/rfc/rfc2104.txt for HMAC-SHA-MD5 http://csrc.nist.gov/publications/fips/fips198/fips-198a.pdf for
 * HMAC-SHA-1
 * 
 * http://tools.ietf.org/search/rfc4231 contains Test vectors for Identifiers and Test Vectors for HMAC-SHA-224,
 * HMAC-SHA-256, HMAC-SHA-384, and HMAC-SHA-512
 * 
 * @author Peter
 * 
 */
public class HMACAlgorithmTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHmac() throws Exception {
		String ks = "0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b";
		byte[] key = ByteArray.fromHex(ks);
		assertEquals(20, key.length);

		String dataS = "Hi There";
		byte[] data = dataS.getBytes("ASCII");
		assertEquals("4869205468657265", ByteArray.asHex(data));
		assertEquals(8, data.length);

		byte[] hMac = HMACAlgorithm.HMAC_SHA_256.hmac(data, key);
		String hexHmac = ByteArray.asHex(hMac);
		assertEquals("b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7", hexHmac);

		hMac = HMACAlgorithm.HMAC_SHA_384.hmac(data, key);
		hexHmac = ByteArray.asHex(hMac);
		assertEquals("afd03944d84895626b0825f4ab46907f15f9dadbe4101ec682aa034c7cebc59cfaea9ea9076ede7f4af152e8b2fa9cb6",
				hexHmac);

		hMac = HMACAlgorithm.HMAC_SHA_512.hmac(data, key);
		hexHmac = ByteArray.asHex(hMac);
		assertEquals(
				"87aa7cdea5ef619d4ff0b4241a1d6cb02379f4e2ce4ec2787ad0b30545e17cdedaa833b7d6b8a702038b274eaea3f4e4be9d914eeb61f1702e696c203a126854",
				hexHmac);
	}

}
