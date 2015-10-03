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
package org.tdmx.core.system.dns;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.tdmx.core.system.dns.DnsUtils.DnsResultHolder;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;

public class DnsUtilTest {

	@Test
	public void testIsSubdomain() throws Exception {
		assertTrue(DnsUtils.isSubdomain("plus.google.com", "google.com"));
		assertFalse(DnsUtils.isSubdomain("plus.google.com", "plus.google.com"));
	}

	@Test
	public void testGetSubdomain() throws Exception {
		assertEquals("plus", DnsUtils.getSubdomain("plus.google.com", "google.com"));
		assertNull(DnsUtils.getSubdomain("plus.google.com", "plus.google.com"));
	}

	@Test
	public void testGetDomainHierarchy() throws Exception {
		List<String> result = DnsUtils.getDomainHierarchy("sucks.plus.google.com");
		assertEquals(3, result.size());
		assertEquals("sucks.plus.google.com", result.get(0));
		assertEquals("plus.google.com", result.get(1));
		assertEquals("google.com", result.get(2));
	}

	@Test
	public void testGetDomainHierarchy_TLD() throws Exception {
		List<String> result = DnsUtils.getDomainHierarchy("tdmx.com");
		assertEquals(1, result.size());
		assertEquals("tdmx.com", result.get(0));
	}

	@Test
	public void testGetDomainHierarchy_Invalid() throws Exception {
		List<String> result = DnsUtils.getDomainHierarchy("com");
		assertEquals(1, result.size());
		assertEquals("com", result.get(0));
	}

	@Test
	public void testAuthoritativeNameServers_KMT() throws Exception {
		String[] kmt = new String[] { "ns-1447.awsdns-52.org", "ns-1857.awsdns-40.co.uk", "ns-44.awsdns-05.com",
				"ns-748.awsdns-29.net" };

		DnsResultHolder h = DnsUtils.getNameServers("kidsmathstrainer.com", Arrays.asList(new String[] { "8.8.8.8" }));
		assertEquals("kidsmathstrainer.com", h.getApex());
		assertArrayEquals(kmt, h.getRecords().toArray(new String[0]));
	}

	@Test
	public void testNameServers_Google() throws Exception {
		String[] kmt = new String[] { "ns1.google.com", "ns2.google.com", "ns3.google.com", "ns4.google.com" };

		DnsResultHolder h = DnsUtils.getNameServers("google.com", Arrays.asList(new String[] { "8.8.8.8" }));
		assertEquals("google.com", h.getApex());
		assertArrayEquals(kmt, h.getRecords().toArray(new String[0]));
	}

	@Test
	public void testNameServers_GP() throws Exception {
		String[] kmt = new String[] { "ns1.google.com", "ns2.google.com", "ns3.google.com", "ns4.google.com" };

		DnsResultHolder h = DnsUtils.getNameServers("plus.google.com", Arrays.asList(new String[] { "8.8.8.8" }));
		assertEquals("google.com", h.getApex());
		assertArrayEquals(kmt, h.getRecords().toArray(new String[0]));
	}

	@Test
	public void testTXT_KMT() throws Exception {
		String[] txtRecords = new String[] {
				"tdmx version=1 zac=a4f13fef5ed15abce9689b28f23ec590085a1b82f9b5b9e4b00b77a9f36fd310 scs=https://www.thisisabloodylongdomainnamewithextension/api/v1.0/scs/" };
		DnsResultHolder h = DnsUtils.getTdmxZoneRecord("kidsmathstrainer.com",
				DnsUtils.getSystemDnsResolverAddresses());
		assertEquals("kidsmathstrainer.com", h.getApex());
		assertArrayEquals(txtRecords, h.getRecords().toArray(new String[0]));
	}

	@Test
	public void testMatchTxtRecord_OK() {
		assertTrue(DnsUtils.matchesTdmxZoneRecord(
				"tdmx version=1 zac=a4f13fef5ed15abce9689b28f23ec590085a1b82f9b5b9e4b00b77a9f36fd310 scs=https://www.thisisabloodylongdomainnamewithextension/api/v1.0/scs/"));
	}

	@Test
	public void testMatchTxtRecord_NOK() {
		assertFalse(DnsUtils.matchesTdmxZoneRecord(
				"tdmx version=nok zac=a4f13fef5ed15abce9689b28f23ec590085a1b82f9b5b9e4b00b77a9f36fd310 scs=https://www.thisisabloodylongdomainnamewithextension/api/v1.0/scs/"));
		assertFalse(DnsUtils.matchesTdmxZoneRecord(
				"dmx version=1 zac=a4f13fef5ed15abce9689b28f23ec590085a1b82f9b5b9e4b00b77a9f36fd310 scs=https://www.thisisabloodylongdomainnamewithextension/api/v1.0/scs/"));
	}

	@Test
	public void testParseTxtRecord() {
		TdmxZoneRecord zr = DnsUtils.parseTdmxZoneRecord(
				"tdmx version=1 zac=a4f13fef5ed15abce9689b28f23ec590085a1b82f9b5b9e4b00b77a9f36fd310 scs=https://www.thisisabloodylongdomainnamewithextension/api/v1.0/scs/");
		assertNotNull(zr);
		assertEquals(1, zr.getVersion());
		assertEquals("a4f13fef5ed15abce9689b28f23ec590085a1b82f9b5b9e4b00b77a9f36fd310", zr.getZacFingerprint());
		assertEquals("https://www.thisisabloodylongdomainnamewithextension/api/v1.0/scs/", zr.getScsUrl().toString());
	}
}
