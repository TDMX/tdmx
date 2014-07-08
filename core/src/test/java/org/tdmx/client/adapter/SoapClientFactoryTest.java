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
import org.junit.Test;
import org.tdmx.client.crypto.certificate.CertificateFacade;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.sp.mos.Submit;
import org.tdmx.core.api.v01.sp.mos.SubmitResponse;
import org.tdmx.core.api.v01.sp.mos.msg.Msg;
import org.tdmx.core.api.v01.sp.mos.tx.Transaction;
import org.tdmx.core.api.v01.sp.mos.ws.MOS;

public class SoapClientFactoryTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_MOS_submit() throws CryptoCertificateException {
		PKIXCredential zac = CertificateFacade.createZAC(10);
		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);
		final PKIXCredential uc = CertificateFacade.createUC(dac, 1);

		CredentialProvider cp = new CredentialProvider() {

			@Override
			public PKIXCredential getCredential() {
				return uc;
			}

		};

		SoapClientFactory<MOS> mosFactory = new SoapClientFactory<>();
		// serviceprovider.tdmx.org
		mosFactory.setUrl("https://localhost:8443/api/v1.0/sp/mos");
		mosFactory.setConnectionTimeoutMillis(10000);
		mosFactory.setKeepAlive(true);
		mosFactory.setClazz(MOS.class);
		mosFactory.setReceiveTimeoutMillis(10000);
		mosFactory.setCredentialProvider(cp);

		MOS client = mosFactory.createClient();
		assertNotNull(client);

		Submit submit = new Submit();
		submit.setMsg(new Msg());
		submit.setTransaction(new Transaction());
		SubmitResponse response = client.submit(submit);
		assertNotNull(response);
	}

}
