package org.tdmx.server.pcs;

import org.junit.Before;
import org.junit.Test;

public class RemoteControlServiceTest {

	RelayControlServiceImpl sut;

	@Before
	public void setUp() throws Exception {

		sut = new RelayControlServiceImpl();
	}

	@Test
	public void testRegisterRelayService() {
		sut.registerRelayServer("localhost:8447");

		// assertEquals(1, 1);
		// Mockito.verifyZeroInteractions(mockServer);
	}

}
