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
package org.tdmx.lib.control.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.control.domain.TrustStatus;
import org.tdmx.lib.control.domain.TrustedSslCertificate;
import org.tdmx.lib.control.domain.TrustedSslCertificateFacade;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TrustedSslCertificateRepositoryUnitTest {

	@Autowired
	private TrustedSslCertificateService service;

	private String rootFingerprint;
	private String intermediateFingerprint;

	@Before
	public void doSetup() throws Exception {

		{
			TrustedSslCertificate sslRoot = TrustedSslCertificateFacade
					.getDerCertificate("src/test/resources/startssl-root.cer", TrustStatus.TRUSTED);
			service.createOrUpdate(sslRoot);
			rootFingerprint = sslRoot.getFingerprint();

			TrustedSslCertificate sslIntermediate = TrustedSslCertificateFacade
					.getDerCertificate("src/test/resources/startssl-intermediate.cer", TrustStatus.TRUSTED);
			intermediateFingerprint = sslIntermediate.getFingerprint();
			service.createOrUpdate(sslIntermediate);

		}
	}

	@After
	public void doTeardown() {
		TrustedSslCertificate sslRoot = service.findByFingerprint(rootFingerprint);
		if (sslRoot != null) {
			service.delete(sslRoot);
		}
		TrustedSslCertificate sslIntermediate = service.findByFingerprint(intermediateFingerprint);
		if (sslIntermediate != null) {
			service.delete(sslIntermediate);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		TrustedSslCertificate rootCer = service.findByFingerprint(rootFingerprint);
		assertNotNull(rootCer);
		assertNotNull(rootCer.getCertificate());
		assertNotNull(rootCer.getCertificatePem());
		assertNotNull(rootCer.getFingerprint());
		assertNotNull(rootCer.getValidFrom());
		assertNotNull(rootCer.getValidTo());
		assertNotNull(rootCer.getDescription());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		TrustedSslCertificate rootCer = service.findByFingerprint("gugus");
		assertNull(rootCer);
	}

	@Test
	public void testModify() throws Exception {
		TrustedSslCertificate intCer = service.findByFingerprint(intermediateFingerprint);
		assertNotNull(intCer);
		assertEquals(TrustStatus.TRUSTED, intCer.getTrustStatus());

		intCer.setTrustStatus(TrustStatus.DISTRUSTED);
		service.createOrUpdate(intCer);

		TrustedSslCertificate intCer2 = service.findByFingerprint(intermediateFingerprint);
		assertNotNull(intCer2);
		assertEquals(TrustStatus.DISTRUSTED, intCer2.getTrustStatus());
	}

	@Test
	public void testLookupAll() throws Exception {
		List<TrustedSslCertificate> l = service.findAll();
		assertNotNull(l);
		assertTrue(l.size() >= 2);
	}

}