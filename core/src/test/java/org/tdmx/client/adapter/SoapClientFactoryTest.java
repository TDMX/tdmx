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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.tdmx.client.crypto.certificate.CertificateFacade;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.sp.mos.Submit;
import org.tdmx.core.api.v01.sp.mos.SubmitResponse;
import org.tdmx.core.api.v01.sp.mos.msg.Msg;
import org.tdmx.core.api.v01.sp.mos.tx.Transaction;
import org.tdmx.core.api.v01.sp.mos.ws.MOS;

@Ignore
public class SoapClientFactoryTest {

	// TODO test

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_MOS_submit() throws CryptoCertificateException {
		PKIXCredential zac = CertificateFacade.createZAC("zone.root", 10);
		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);
		final PKIXCredential uc = CertificateFacade.createUC(dac, 1);

		ClientCredentialProvider cp = new ClientCredentialProvider() {

			@Override
			public PKIXCredential getCredential() {
				return uc;
			}

		};
		ClientKeyManagerFactoryImpl kmf = new ClientKeyManagerFactoryImpl();
		kmf.setCredentialProvider(cp);

		SystemDefaultTrustedCertificateProvider tcp = new SystemDefaultTrustedCertificateProvider();
		ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(tcp);

		SoapClientFactory<MOS> mosFactory = new SoapClientFactory<>();
		// serviceprovider.tdmx.org
		mosFactory.setUrl("https://serviceprovider.tdmx.org/api/v1.0/sp/mos");
		mosFactory.setConnectionTimeoutMillis(10000);
		mosFactory.setKeepAlive(true);
		mosFactory.setClazz(MOS.class);
		mosFactory.setReceiveTimeoutMillis(10000);
		mosFactory.setDisableCNCheck(false);
		mosFactory.setKeyManagerFactory(kmf);
		mosFactory.setTrustManagerFactory(stfm);
		mosFactory.setTlsProtocolVersion("TLSv1.2");

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
		mosFactory.setEnabledCipherSuites(select_strong_ciphers);

		MOS client = mosFactory.createClient();
		assertNotNull(client);

		Submit submit = new Submit();
		submit.setMsg(new Msg());
		submit.setTransaction(new Transaction());
		SubmitResponse response = client.submit(submit);
		assertNotNull(response);
	}

}
