package org.tdmx.server.ros;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.tdmx.server.runtime.RpcAddressUtils;

public class RosAddressUtilsTest {

	@Test
	public void testGetRosHost() {
		assertEquals("hostname", RpcAddressUtils.getRosHost("hostname:444"));
	}

	@Test
	public void testGetRosPort() {
		assertEquals(444, RpcAddressUtils.getRosPort("hostname:444"));
	}

	@Test
	public void testGetRosAddress() {
		assertEquals("hostname:444", RpcAddressUtils.getRosAddress("hostname", 444));
	}
}
