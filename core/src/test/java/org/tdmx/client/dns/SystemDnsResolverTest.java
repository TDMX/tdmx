package org.tdmx.client.dns;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.core.dns.SystemDnsResolver;

public class SystemDnsResolverTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetSearchHostnames() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetAuthNameServers() throws Exception {
		SystemDnsResolver r = new SystemDnsResolver("plus.google.com");
		r.getAuthNameServers();
	}

}
