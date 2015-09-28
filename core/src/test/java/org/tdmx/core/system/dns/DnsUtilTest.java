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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

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
	public void testAuthoritativeNameServers() throws Exception {
		// DnsUtils.getAuthNameServers("kidsmathstrainer.com", DnsUtils.getSystemDnsResolverAddresses());
		DnsUtils.getAuthNameServers("plus.google.com", DnsUtils.getSystemDnsResolverAddresses());
	}

}
