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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.util.FileUtils;

public class KeyStoreUtilsTest {

	static {
		JCAProviderInitializer.init();
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void storeCreateClientKeystore() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC(10);
		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);
		PKIXCredential uc = CertificateFacade.createUC(dac, 1);

		byte[] contents = KeyStoreUtils.saveKeyStore(uc, "jks", "changeme", "client");
		FileUtils.storeFileContents("client.keystore", contents, ".tmp");
	}

	@Test
	public void testLoadClientKeystore() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC(10);
		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);
		PKIXCredential uc = CertificateFacade.createUC(dac, 1);

		byte[] contents = KeyStoreUtils.saveKeyStore(uc, "jks", "changeme", "client");

		PKIXCredential loadedUC = KeyStoreUtils.getPrivateCredential(contents, "jks", "changeme", "client");

		assertTrue(uc.getPublicCert().isIdentical(loadedUC.getPublicCert()));

	}

	@Test
	public void testLoadInvalidKeystore() throws Exception {
		byte[] randomBytes = EntropySource.getRandomBytes(1000);

		try {
			KeyStoreUtils.getPrivateCredential(randomBytes, "jks", "changeme", "client");
			fail();
		} catch (CryptoCertificateException e) {

		}
	}

	@Test
	public void testLoadTruncatedKeystore() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC(10);
		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);
		PKIXCredential uc = CertificateFacade.createUC(dac, 1);

		byte[] contents = KeyStoreUtils.saveKeyStore(uc, "jks", "changeme", "client");

		byte[] part = new byte[contents.length / 2];
		System.arraycopy(contents, 0, part, 0, part.length);
		try {
			KeyStoreUtils.getPrivateCredential(part, "jks", "changeme", "client");
			fail();
		} catch (CryptoCertificateException e) {

		}
	}

}
