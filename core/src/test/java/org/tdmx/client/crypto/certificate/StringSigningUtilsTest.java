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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;

public class StringSigningUtilsTest {

	static {
		JCAProviderInitializer.init();
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_StringSignature_ZAC() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC("zone.root", 10);
		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);
		PKIXCredential uc = CertificateFacade.createUC(dac, 1);

		String valueToSign = "test";

		String signatureHex = StringSigningUtils.getHexSignature(zac.getPrivateKey(), SignatureAlgorithm.SHA_256_RSA,
				valueToSign);
		assertNotNull(signatureHex);

		assertTrue(StringSigningUtils.checkHexSignature(zac.getPublicCert().getCertificate().getPublicKey(),
				SignatureAlgorithm.SHA_256_RSA, valueToSign, signatureHex));
		assertFalse(StringSigningUtils.checkHexSignature(zac.getPublicCert().getCertificate().getPublicKey(),
				SignatureAlgorithm.SHA_256_RSA, valueToSign, "gugus"));
		assertFalse(StringSigningUtils.checkHexSignature(zac.getPublicCert().getCertificate().getPublicKey(),
				SignatureAlgorithm.SHA_256_RSA, "gugus", signatureHex));
		assertFalse(StringSigningUtils.checkHexSignature(dac.getPublicCert().getCertificate().getPublicKey(),
				SignatureAlgorithm.SHA_256_RSA, valueToSign, signatureHex));
		assertFalse(StringSigningUtils.checkHexSignature(uc.getPublicCert().getCertificate().getPublicKey(),
				SignatureAlgorithm.SHA_256_RSA, valueToSign, signatureHex));
	}

}
