package org.tdmx.client.dns;

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
