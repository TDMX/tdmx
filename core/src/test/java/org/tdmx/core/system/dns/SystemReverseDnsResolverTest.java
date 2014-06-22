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

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;

public class SystemReverseDnsResolverTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testReverseDnsLookup() throws Exception {
		SystemReverseDnsResolver r = new SystemReverseDnsResolver("173.194.116.63");
		String ip = r.reverseDns();
		System.out.println(ip);

		InetAddress addr = InetAddress.getByName("173.194.116.63");
		System.out.println(addr.getHostName());
	}

}
