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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.adapter.SslProbeService.ConnectionTestResult;
import org.tdmx.client.adapter.SslProbeService.ConnectionTestResult.TestStep;
import org.tdmx.client.crypto.certificate.CertificateFacade;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCredential;

public class SslProbeServiceTest {

	private static final Logger log = LoggerFactory.getLogger(SslProbeServiceTest.class);

	SslProbeService service = null;

	@Before
	public void setUp() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC(10);
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

		SystemDefaultTrustedCertificateProvider stcp = new SystemDefaultTrustedCertificateProvider();

		KeystoreFileTrustedCertificateProvider tcp = new KeystoreFileTrustedCertificateProvider();
		tcp.setKeystoreType("jks");
		tcp.setKeystorePassphrase("changeme");
		tcp.setKeystoreFilePath("src/test/resources/cacerts.keystore");

		List<TrustedServerCertificateProvider> providers = new ArrayList<>();
		providers.add(tcp);
		providers.add(stcp);
		DelegatingTrustedCertificateProvider dtcp = new DelegatingTrustedCertificateProvider();
		dtcp.setDelegateProviders(providers);

		ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(dtcp);

		//@formatter:off
		/*
		String[] all_strong_ciphers = new String[] {
		"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256",
		"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
		"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
		"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
		"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
		"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
		"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
		"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
		"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
		"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
		"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
		"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
		"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
		"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
		"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
		"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
		"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
		"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
		"TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
		"TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
		"TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
		"TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
		"TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
		"TLS_RSA_WITH_AES_256_GCM_SHA384",
		"TLS_RSA_WITH_AES_256_CBC_SHA256",
		"TLS_RSA_WITH_AES_256_CBC_SHA",
		"TLS_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_RSA_WITH_AES_128_CBC_SHA256",
		"TLS_RSA_WITH_AES_128_CBC_SHA",
		"TLS_RSA_WITH_3DES_EDE_CBC_SHA"
		}
		;
		*/
		String[] select_strong_ciphers = new String[] {
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
				"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
				"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
				
		};
		//@formatter:on

		service = new SslProbeService();
		service.setKeyManagerFactory(kmf);
		service.setTrustManagerFactory(stfm);
		service.setSslProtocol("TLSv1.2");
		service.setEnabledCiphers(select_strong_ciphers);
		service.setConnectionTimeoutMillis(10000);
		service.setReadTimeoutMillis(10000);
	}

	@Test
	public void test_SSL_valid() throws CryptoCertificateException {
		ConnectionTestResult result = service.testConnection("www.youtube.com", 443);
		assertNotNull(result);
		assertEquals(TestStep.COMPLETE, result.getTestStep());
		assertNotNull(result.getServerCertChain());
		log.info(result.toString());
	}

	@Test
	public void test_SSL_pkixvalidationfailed() throws CryptoCertificateException {
		ConnectionTestResult result = service.testConnection("serviceprovider.tdmx.org", 443);
		assertNotNull(result);
		assertEquals(TestStep.COMPLETE, result.getTestStep());
		assertNotNull(result.getServerCertChain());
		log.info(result.toString());
	}

	@Test
	public void test_SSL_hostnotfound() throws CryptoCertificateException {
		ConnectionTestResult result = service.testConnection("gugushostnamewhichdoesntexist.com", 443);
		assertNotNull(result);
		assertEquals(TestStep.SOCKET_CONNECT, result.getTestStep());
		assertNull(result.getServerCertChain());
		log.info(result.toString());
	}

}
