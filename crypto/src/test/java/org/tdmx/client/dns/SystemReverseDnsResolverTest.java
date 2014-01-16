package org.tdmx.client.dns;

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
		SystemReverseDnsResolver r2 = new SystemReverseDnsResolver("2a00:1450:400a:806::1018");
		String ip2 = r2.reverseDns();
		System.out.println(ip2);
	}

}
