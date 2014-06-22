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
package org.tdmx.client.crypto.certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateKeyIOUtilsTest {

	private final Logger log = LoggerFactory.getLogger(PrivateKeyIOUtilsTest.class);

	private final String privateKey = "" + "-----BEGIN RSA PRIVATE KEY-----" + TrustStoreEntry.NL
			+ "MIIEpAIBAAKCAQEAww8tL6orP7ZiG1Os25JOK4OwoJ+snUIvptimgInfzwJ1LhM4" + TrustStoreEntry.NL
			+ "Hxnw6TcpOUDsEI4wolNLu8243oKHDVe1IGpDKO912KboCz95Tj/R+SvAiV4+6EEQ" + TrustStoreEntry.NL
			+ "ktqlpG5PqSiEDg23RUS/QcYzAMEu0p1gPZ6eP7QVne6inbohHUcpvHRFCtRHT+jO" + TrustStoreEntry.NL
			+ "p9j6wlQKcP6lR+/iXcG0c2ElNMHBBSwtgBDjCplpgDy0KqQxKvK9yoAsm7InlU2Y" + TrustStoreEntry.NL
			+ "o5wXWxGdJkGZ3ZtGWGFxcj4rY2Ifcs9/8PddTpG9HKCT55mYZpc4XWbkCWrr9rBY" + TrustStoreEntry.NL
			+ "+XV02qr5L5hnd5ExXoi/GRK/Ro5PnadvP5iAIQIDAQABAoIBAQCroG7hwZjMNm2c" + TrustStoreEntry.NL
			+ "HcO6bsDZCMYgjl4TAGltJLNb5fRG8KKqJ775npmami5rcfRDnNit+xxn4lsHbVHn" + TrustStoreEntry.NL
			+ "K4TzQIxXOFs4haMmQnM5pm3aD+UY+RPgx18N19RnGah97mhC9U6MZDDkbr/xqgjI" + TrustStoreEntry.NL
			+ "yDAmB6q14n4iuBXdMihw2myepK8bFqCK4iKEXQIhtKRnkhoCMBPKOhpBEA4gfjH9" + TrustStoreEntry.NL
			+ "DT5yqnE5qHpeSFlU8482HnxAsSqBMqUynWuL8NF3oT2WOFlzAQ5B01SqmoKzmGkt" + TrustStoreEntry.NL
			+ "1JAZZRyJ5beX0tgNd0zk9F6JJjW7JW0YnUKQMFqod1XWmprHYn+lsobTZF45lKE3" + TrustStoreEntry.NL
			+ "xn8671wBAoGBAPwqnz1KvhHtm3rOk8ehgChEvfGIzks9ZawX/Ggep4gqhkO9aM7j" + TrustStoreEntry.NL
			+ "zw8UC7NfHNAjFnMLZKWxgI2bvIBRDJ6kr3jdG+RASz/2nIL7fYTdVxw5nNEvMjSA" + TrustStoreEntry.NL
			+ "8MOyyKMg4hM6yNTphH3XbLS+EzrsQsvKjaDZPRynXzL9v/kzmBtJ8NNNAoGBAMYG" + TrustStoreEntry.NL
			+ "TjJLNQU/4OFWmOM+HczgIJSf8a7Y5zkILqeMIinYSgmaL2wblnbb4o4d50oQNBym" + TrustStoreEntry.NL
			+ "Bu7IyHEh+/Gu5O31v8HRyCaiQSNV69dAxw0Oj0BODnyPVpPSRrB+GdyQUNM3WynQ" + TrustStoreEntry.NL
			+ "CaPPaSn7RRG8KH8csEiZh6wUB/+B+Wf5pxe3ys4lAoGBANWZ3Q10NJDHLr7WIcQm" + TrustStoreEntry.NL
			+ "f4KcyDDC68w8E9W086/W1562NslGtnMZ8ZkaJ9cnKCb+sdN9wSHBu904IHKpNQFl" + TrustStoreEntry.NL
			+ "CuZclXXBKHzkDyu8kFRtkY5tFvLzAZoL5uY+mUnlXttfHiOMMxjN1F097NgqrWIn" + TrustStoreEntry.NL
			+ "D9VCb0vIUxHEdEtJcv0aow65AoGARiehuGuwgwWYc/yr+YXeJOEaac0oYFtzv17I" + TrustStoreEntry.NL
			+ "uI8K42w+/pvjRReY/M71uoeZk0GeVK/1MM9tQ7dYfM0LScqQfugFUrqU/SHJhd7r" + TrustStoreEntry.NL
			+ "JhMMaSm+NB7L+165sHlcTCnMmKe54lxTrfcjOL5OP/Q7HJ1bTr0lta77DmhLpaxq" + TrustStoreEntry.NL
			+ "qBpy/QkCgYAI6v/WbvbihbKZmBn8puvhWey1mev1/rY/hwOH0vOVwbQEhEoVTF6o" + TrustStoreEntry.NL
			+ "VyXBKPSn+el1eOo7sn+nflmUc0GFCMx+If+9Bu8hRjuWSQCh0QfzJNJPQIK7Pogc" + TrustStoreEntry.NL
			+ "8GgcWKhqHHdOXHC/ziljmD/6TvSPYgj17Tdi03jLSNmO+DJHVaRNYg==" + TrustStoreEntry.NL
			+ "-----END RSA PRIVATE KEY-----" + TrustStoreEntry.NL;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPemToUnencryptedRSAPrivateKey() throws Exception {
		KeyPair kp = PrivateKeyIOUtils.pemToRSAPrivateKeyPair(privateKey);
		assertNotNull(kp);
		assertNotNull(kp.getPublic());
		assertTrue(kp.getPublic() instanceof RSAPublicKey);
		RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
		assertEquals(2048, pub.getModulus().bitLength());
		assertEquals(
				pub.getModulus(),
				new BigInteger(
						"24623953283675983724243650831159234472439791945740568058928199483569633195387901117930730793291306052308582799305238692791411171013752914012711024799481866818397234721709140397711816992807812416326074185749715377826560252495238919049471121985272732973716490528195539513342042825757299820490202869537140883874016346659618751044015351276770456316194362002482863477233275512783799650922154197999181990613462038370905317869783664422529509492544472997316026243945420781426682481002578830979615283943358555257021579826425835221107584374529447606670716704264453344774887108510758770147676365918395360064043147858624021823521"));
		assertEquals(pub.getPublicExponent(), new BigInteger("65537"));

		assertNotNull(kp.getPrivate());
		log.debug("PrivateKey " + kp.getPrivate());
	}

}
