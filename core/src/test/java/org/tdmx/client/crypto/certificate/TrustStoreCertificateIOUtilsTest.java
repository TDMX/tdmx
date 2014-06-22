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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustStoreCertificateIOUtilsTest {
	private final Logger log = LoggerFactory.getLogger(TrustStoreCertificateIOUtilsTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetAllTrustedCAs() throws Exception {

		List<TrustStoreEntry> rootCAs = TrustStoreCertificateIOUtils.getAllSystemTrustedCAs();
		assertNotNull(rootCAs);
		assertTrue(rootCAs.size() > 0);

		for (TrustStoreEntry rootCA : rootCAs) {
			TrustStoreEntry e = new TrustStoreEntry(rootCA.getCertificate());
			e.setFriendlyName("friendlyname=" + e.getCertificate().getFingerprint());
			e.addComment("this is a comment1 " + e.getCertificate().getFingerprint());
			e.addComment("this is a comment2 " + e.getCertificate().getFingerprint());

			String s = TrustStoreCertificateIOUtils.trustStoreEntryToPem(e);
			assertNotNull(s);

			log.debug("Trusted CA " + s);
		}
	}

	@Test
	public void testPemToX509ListConversion() throws Exception {

		List<TrustStoreEntry> rootCAs = TrustStoreCertificateIOUtils.getAllSystemTrustedCAs();
		assertNotNull(rootCAs);

		List<TrustStoreEntry> trustEntries = new ArrayList<>();

		StringBuffer sb = new StringBuffer();
		for (TrustStoreEntry rootCA : rootCAs) {
			TrustStoreEntry e = new TrustStoreEntry(rootCA.getCertificate());
			e.setFriendlyName("friendlyname=" + e.getCertificate().getFingerprint());
			e.addComment("this is a comment1 " + e.getCertificate().getFingerprint());
			e.addComment("this is a comment2 " + e.getCertificate().getFingerprint());
			trustEntries.add(e);

			String s = TrustStoreCertificateIOUtils.trustStoreEntryToPem(e);
			assertNotNull(s);
			sb.append(s);
		}

		String pemList = sb.toString();
		List<TrustStoreEntry> readTrustEntries = TrustStoreCertificateIOUtils.pemToTrustStoreEntries(pemList);
		assertNotNull(readTrustEntries);
		assertEquals(readTrustEntries.size(), trustEntries.size());
		for (TrustStoreEntry e : readTrustEntries) {
			assertEquals("friendlyname=" + e.getCertificate().getFingerprint(), e.getFriendlyName());
			assertEquals("this is a comment1 " + e.getCertificate().getFingerprint() + TrustStoreEntry.NL
					+ "this is a comment2 " + e.getCertificate().getFingerprint(), e.getComment());
		}
	}
}
