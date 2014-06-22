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
package org.tdmx.client.crypto.certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.util.FileUtils;

public class CredentialUtilsTest {

	static {
		JCAProviderInitializer.init();
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateZoneAdminCredentials() throws Exception {
		createZAC();
	}

	@Test
	public void testCreateDomainAdminCert() throws Exception {
		PKIXCredential zac = createZAC();
		createDAC(zac);
	}

	@Test
	public void testUserCert() throws Exception {
		PKIXCredential zac = createZAC();
		byte[] bs = CertificateIOUtils.encodeCertificate(zac.getCertificateChain()[0]);
		FileUtils.storeFileContents("za.crt", bs, ".tmp");

		PKIXCredential dac = createDAC(zac);
		bs = CertificateIOUtils.encodeCertificate(dac.getCertificateChain()[0]);
		FileUtils.storeFileContents("da.crt", bs, ".tmp");

		PKIXCredential uc = createUC(dac);
		bs = CertificateIOUtils.encodeCertificate(uc.getCertificateChain()[0]);
		FileUtils.storeFileContents("uc.crt", bs, ".tmp");

	}

	private PKIXCredential createZAC() throws Exception {
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.MILLISECOND, 0);

		Calendar later = Calendar.getInstance();
		later.setTime(new Date());
		later.add(Calendar.YEAR, 10);
		later.set(Calendar.MILLISECOND, 0);

		TdmxZoneInfo zi = new TdmxZoneInfo(1, "zone.root", "https://mrsUrl/api");

		ZoneAdministrationCredentialSpecifier req = new ZoneAdministrationCredentialSpecifier();
		req.setZoneInfo(zi);

		req.setCn("name");
		req.setTelephoneNumber("0417100000");
		req.setEmailAddress("pjk@gmail.com");
		req.setOrgUnit("IT");
		req.setOrg("mycompany");
		req.setLocation("Zug");
		req.setCountry("CH");
		req.setNotBefore(now);
		req.setNotAfter(later);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		PKIXCredential cred = CredentialUtils.createZoneAdministratorCredential(req);

		assertNotNull(cred);
		assertNotNull(cred.getCertificateChain());
		assertNotNull(cred.getPrivateKey());
		assertEquals(1, cred.getCertificateChain().length);

		PKIXCertificate c = cred.getCertificateChain()[0];

		assertEquals(req.getCountry(), c.getCountry());
		assertEquals(req.getLocation(), c.getLocation());
		assertEquals(req.getOrg(), c.getOrganization());
		assertEquals(req.getOrgUnit(), c.getOrgUnit());
		assertEquals(req.getTelephoneNumber(), c.getTelephoneNumber());
		assertEquals(req.getEmailAddress(), c.getEmailAddress());
		assertEquals(req.getCn(), c.getCommonName());
		assertEquals(req.getNotAfter(), c.getNotAfter());
		assertEquals(req.getNotBefore(), c.getNotBefore());
		assertTrue(c.isTdmxZoneAdminCertificate());
		assertFalse(c.isTdmxDomainAdminCertificate());
		assertFalse(c.isTdmxUserCertificate());
		assertEquals("CN=name,TEL=0417100000,EMAIL=pjk@gmail.com,OU=IT,O=mycompany,L=Zug,C=CH", c.getSubject());
		assertEquals("CN=name,TEL=0417100000,EMAIL=pjk@gmail.com,OU=IT,O=mycompany,L=Zug,C=CH", c.getIssuer());
		return cred;
	}

	private PKIXCredential createDAC(PKIXCredential zac) throws Exception {
		PKIXCertificate issuer = zac.getPublicCert();

		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.MILLISECOND, 0);

		Calendar later = Calendar.getInstance();
		later.setTime(new Date());
		later.add(Calendar.YEAR, 2);
		later.set(Calendar.MILLISECOND, 0);

		DomainAdministrationCredentialSpecifier req = new DomainAdministrationCredentialSpecifier();
		req.setZoneAdministratorCredential(zac);
		req.setDomainName("subdomain." + issuer.getTdmxZoneInfo().getZoneRoot());
		req.setNotBefore(now);
		req.setNotAfter(later);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		PKIXCredential cred = CredentialUtils.createDomainAdministratorCredential(req);

		assertNotNull(cred);
		assertNotNull(cred.getCertificateChain());
		assertNotNull(cred.getPrivateKey());
		assertEquals(2, cred.getCertificateChain().length);

		PKIXCertificate c = cred.getPublicCert();

		assertEquals(issuer.getCountry(), c.getCountry());
		assertEquals(issuer.getLocation(), c.getLocation());
		assertEquals(issuer.getOrganization(), c.getOrganization());
		assertEquals(issuer.getOrgUnit(), c.getOrgUnit());
		assertEquals(req.getDomainName(), c.getCommonName());
		assertEquals(req.getNotAfter(), c.getNotAfter());
		assertEquals(req.getNotBefore(), c.getNotBefore());
		assertFalse(c.isTdmxZoneAdminCertificate());
		assertTrue(c.isTdmxDomainAdminCertificate());
		assertFalse(c.isTdmxUserCertificate());
		assertEquals("CN=" + req.getDomainName() + ",OU=tdmx-domain,OU=IT,O=mycompany,L=Zug,C=CH", c.getSubject());
		assertEquals(issuer.getSubject(), c.getIssuer());

		return cred;
	}

	private PKIXCredential createUC(PKIXCredential dac) throws Exception {
		PKIXCertificate issuer = dac.getPublicCert();

		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.MILLISECOND, 0);

		Calendar later = Calendar.getInstance();
		later.setTime(new Date());
		later.add(Calendar.YEAR, 1);
		later.set(Calendar.MILLISECOND, 0);

		UserCredentialSpecifier req = new UserCredentialSpecifier();
		req.setDomainAdministratorCredential(dac);
		req.setName("username123");
		req.setNotBefore(now);
		req.setNotAfter(later);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		PKIXCredential cred = CredentialUtils.createUserCredential(req);

		assertNotNull(cred);
		assertNotNull(cred.getCertificateChain());
		assertNotNull(cred.getPrivateKey());
		assertEquals(3, cred.getCertificateChain().length);

		PKIXCertificate c = cred.getPublicCert();

		assertEquals(issuer.getCountry(), c.getCountry());
		assertEquals(issuer.getLocation(), c.getLocation());
		assertEquals(issuer.getOrganization(), c.getOrganization());
		assertEquals(issuer.getOrgUnit(), c.getOrgUnit());
		assertEquals(req.getName(), c.getCommonName());
		assertEquals(req.getNotAfter(), c.getNotAfter());
		assertEquals(req.getNotBefore(), c.getNotBefore());
		assertFalse(c.isTdmxZoneAdminCertificate());
		assertFalse(c.isTdmxDomainAdminCertificate());
		assertTrue(c.isTdmxUserCertificate());
		assertEquals("CN=" + req.getName() + ",OU=" + issuer.getCommonName()
				+ ",OU=tdmx-domain,OU=IT,O=mycompany,L=Zug,C=CH", c.getSubject());
		assertEquals(issuer.getSubject(), c.getIssuer());

		return cred;
	}

}
