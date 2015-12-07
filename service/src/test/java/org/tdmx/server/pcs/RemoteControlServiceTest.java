package org.tdmx.server.pcs;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

public class RemoteControlServiceTest {

	RelayControlServiceImpl sut;

	@Before
	public void setUp() throws Exception {

		sut = new RelayControlServiceImpl();
	}

	@Test
	public void testRegisterRelayService() {
		RpcClientChannel mockServer = mock(RpcClientChannel.class);

		sut.registerRelayServer(mockServer);

		// assertEquals(1, 1);
		// Mockito.verifyZeroInteractions(mockServer);
	}

}
