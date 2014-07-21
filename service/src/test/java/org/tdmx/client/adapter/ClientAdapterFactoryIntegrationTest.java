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

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.api.v01.sp.mos.Submit;
import org.tdmx.core.api.v01.sp.mos.SubmitResponse;
import org.tdmx.core.api.v01.sp.mos.ws.MOS;
import org.tdmx.server.runtime.ServerContainer;

public class ClientAdapterFactoryIntegrationTest {

	private static final Logger log = LoggerFactory.getLogger(ServerContainer.class);

	@Test
	@Ignore
	public void test() throws Exception {
		KeystoreFileCredentialProvider cp = new KeystoreFileCredentialProvider();
		cp.setKeystoreAlias("client");
		cp.setKeystoreFilePath("src/test/resources/uc.keystore"); // USER
		cp.setKeystorePassphrase("changeme");
		cp.setKeystoreType("jks");

		/**
		 * Get hold of the PK of the UC
		 */
		// PKIXCredential uc = cp.getCredential();
		// String ucPem = CertificateIOUtils.x509certsToPem(uc.getCertificateChain());
		// to know what to setup.
		// log.warn("sha1" + uc.getPublicCert().getFingerprint());
		// log.warn(ucPem);

		ClientKeyManagerFactoryImpl kmf = new ClientKeyManagerFactoryImpl();
		kmf.setCredentialProvider(cp);

		KeystoreFileTrustedCertificateProvider tcp = new KeystoreFileTrustedCertificateProvider();
		tcp.setKeystoreType("jks");
		tcp.setKeystorePassphrase("changeme");
		tcp.setKeystoreFilePath("src/test/resources/cacerts.keystore");

		SystemDefaultTrustedCertificateProvider stcp = new SystemDefaultTrustedCertificateProvider();

		List<TrustedServerCertificateProvider> providers = new ArrayList<>();
		providers.add(tcp);
		providers.add(stcp);
		DelegatingTrustedCertificateProvider dtcp = new DelegatingTrustedCertificateProvider();
		dtcp.setDelegateProviders(providers);

		ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(dtcp);

		SoapClientFactory<MOS> mosCF = new SoapClientFactory<>();
		mosCF.setClazz(MOS.class);
		mosCF.setConnectionTimeoutMillis(10000);
		mosCF.setReceiveTimeoutMillis(10000);
		mosCF.setKeepAlive(true);
		mosCF.setTlsProtocolVersion("TLSv1.2");
		mosCF.setDisableCNCheck(true);
		mosCF.setKeyManagerFactory(kmf);
		mosCF.setTrustManagerFactory(stfm);
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
		// mosCF.setUrl("https://ec2-54-85-169-145.compute-1.amazonaws.com:8443/api/");
		mosCF.setUrl("https://localhost:8443/api/v1.0/sp/mos");

		MOS service = mosCF.createClient();

		Submit msg = new Submit();
		SubmitResponse response = service.submit(msg);

		assertNotNull(response);
	}

}
