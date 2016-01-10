package org.tdmx.server.ros;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RosAddressUtilsTest {

	@Test
	public void testGetRosHost() {
		assertEquals("hostname", RosAddressUtils.getRosHost("hostname:444"));
	}

	@Test
	public void testGetRosPort() {
		assertEquals(444, RosAddressUtils.getRosPort("hostname:444"));
	}

	@Test
	public void testGetRosAddress() {
		assertEquals("hostname:444", RosAddressUtils.getRosAddress("hostname", 444));
	}
}
