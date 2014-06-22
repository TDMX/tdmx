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
package org.tdmx.console.application.dao;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;

public class SystemTrustStoreTest {

	private SystemTrustStoreImpl ts;

	@Before
	public void setUp() throws Exception {
		ts = new SystemTrustStoreImpl();
	}

	@Test
	public void testGetAllTrustedCAs() throws Exception {
		List<TrustStoreEntry> rootCAs = ts.getAllTrustedCAs();
		assertNotNull(rootCAs);

		for (TrustStoreEntry rootCA : rootCAs) {
			// TODO System.out.println(CertificateIOUtils.x509certToPem(rootCA.getCertificate()));
			// TODO System.out.println();
		}
	}

	@Test
	public void testGetAllDistrustedCAs() throws Exception {
		List<TrustStoreEntry> rootCAs = ts.getAllTrustedCAs();
		assertNotNull(rootCAs);

		for (TrustStoreEntry rootCA : rootCAs) {
			// TODO System.out.println(CertificateIOUtils.x509certToPem(rootCA.getCertificate()));
			// TODO System.out.println();
		}
	}
}
