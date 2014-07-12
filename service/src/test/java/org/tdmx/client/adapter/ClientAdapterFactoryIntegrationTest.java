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
package org.tdmx.client.adapter;

import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.tdmx.core.api.v01.sp.mos.Submit;
import org.tdmx.core.api.v01.sp.mos.SubmitResponse;
import org.tdmx.core.api.v01.sp.mos.ws.MOS;

public class ClientAdapterFactoryIntegrationTest {

	@Test
	@Ignore
	public void test() {

		SoapClientFactory<MOS> mosCF = new SoapClientFactory<>();
		mosCF.setClazz(MOS.class);
		mosCF.setConnectionTimeoutMillis(10000);
		mosCF.setReceiveTimeoutMillis(10000);
		mosCF.setKeepAlive(true);
		mosCF.setTlsProtocolVersion("TLSv1.2");
		mosCF.setDisableCNCheck(true);
		//@formatter:off
		String[] select_strong_ciphers = new String[] {
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
				"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
				"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
				
		};
		//@formatter:on
		mosCF.setEnabledCipherSuites(select_strong_ciphers);
		mosCF.setUrl("https://ec2-54-85-169-145.compute-1.amazonaws.com:8443/api/");

		MOS service = mosCF.createClient();

		Submit msg = new Submit();
		SubmitResponse response = service.submit(msg);

		assertNotNull(response);
	}

}
