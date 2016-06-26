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

import java.net.InetAddress;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.core.system.lang.FileUtils;

public class CredentialUtilsTest {

	static {
		JCAProviderInitializer.init();
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateInvalidZoneAdminCredentials() throws Exception {
		Calendar from = CertificateFacade.getNowPlusYears(-2);
		Calendar to = CertificateFacade.getNowPlusYears(-1);
		ZoneAdministrationCredentialSpecifier req = CertificateFacade.createZACS(1, "zone.root", from, to);

		PKIXCredential cred = CredentialUtils.createZoneAdministratorCredential(req);
		assertFalse(CredentialUtils.isValidZoneAdministratorCertificate(cred.getPublicCert()));
	}

	@Test
	public void testCreateZoneAdminCredentials() throws Exception {

		Calendar now = CertificateFacade.getNow();
		Calendar to = CertificateFacade.getNowPlusYears(10);
		ZoneAdministrationCredentialSpecifier req = CertificateFacade.createZACS(1, "zone.root", now, to);
		req.setSerialNumber(100);

		PKIXCredential cred = CredentialUtils.createZoneAdministratorCredential(req);

		assertNotNull(cred);
		assertNotNull(cred.getCertificateChain());
		assertNotNull(cred.getPrivateKey());
		assertEquals(1, cred.getCertificateChain().length);

		PKIXCertificate c = cred.getCertificateChain()[0];

		assertEquals(100, req.getSerialNumber());
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

		assertEquals("1.2.840.113549.1.1.11", c.getSignatureAlgorithm().toString());
		assertEquals("CN=name,TEL=0417100000,EMAIL=pjk@gmail.com,OU=IT,O=mycompany,L=Zug,C=CH", c.getSubject());
		assertEquals("CN=name,TEL=0417100000,EMAIL=pjk@gmail.com,OU=IT,O=mycompany,L=Zug,C=CH", c.getIssuer());

		// test reverse mapping to spec
		ZoneAdministrationCredentialSpecifier spec = CredentialUtils.describeZoneAdministratorCertificate(c);
		assertNotNull(spec);

		assertEquals(SignatureAlgorithm.SHA_256_RSA, spec.getSignatureAlgorithm());
		assertEquals(PublicKeyAlgorithm.RSA2048, spec.getKeyAlgorithm());
		assertEquals(req.getSignatureAlgorithm(), spec.getSignatureAlgorithm());
		assertEquals(req.getCn(), spec.getCn());
		assertEquals(req.getCountry(), spec.getCountry());
		assertEquals(req.getEmailAddress(), spec.getEmailAddress());
		assertEquals(req.getTelephoneNumber(), spec.getTelephoneNumber());
		assertEquals(req.getLocation(), spec.getLocation());
		assertEquals(req.getKeyAlgorithm(), spec.getKeyAlgorithm());
		assertEquals(req.getNotAfter(), spec.getNotAfter());
		assertEquals(req.getNotBefore(), spec.getNotBefore());
		assertEquals(req.getSerialNumber(), spec.getSerialNumber());
		assertEquals(req.getOrg(), spec.getOrg());
		assertEquals(req.getOrgUnit(), spec.getOrgUnit());
	}

	@Test
	public void testCreateDomainAdminCert() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC("zone.root", 10);

		PKIXCertificate issuer = zac.getPublicCert();

		Calendar now = CertificateFacade.getNow();
		Calendar to = CertificateFacade.getNowPlusYears(2);

		DomainAdministrationCredentialSpecifier req = CertificateFacade.createDACS(zac, now, to);
		req.setSerialNumber(100);

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
		assertEquals(100, req.getSerialNumber());
		assertFalse(c.isTdmxZoneAdminCertificate());
		assertTrue(c.isTdmxDomainAdminCertificate());
		assertFalse(c.isTdmxUserCertificate());
		assertEquals("CN=" + req.getDomainName() + ",OU=tdmx-domain,OU=IT,O=mycompany,L=Zug,C=CH", c.getSubject());
		assertEquals(issuer.getSubject(), c.getIssuer());
	}

	@Test
	public void testCreateInvalidDomainAdminCert() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC("zone.root", 10);

		Calendar from = CertificateFacade.getNowPlusYears(-2);
		Calendar to = CertificateFacade.getNowPlusYears(-1);

		DomainAdministrationCredentialSpecifier req = CertificateFacade.createDACS(zac, from, to);
		req.setSerialNumber(100);

		PKIXCredential cred = CredentialUtils.createDomainAdministratorCredential(req);

		assertNotNull(cred);
		assertFalse(CredentialUtils.isValidDomainAdministratorCertificate(zac.getPublicCert(), cred.getPublicCert()));
	}

	@Test
	public void testUserCert() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC("zone.root", 10);
		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);
		PKIXCertificate issuer = dac.getPublicCert();

		UserCredentialSpecifier req = CertificateFacade.createUCS(dac, CertificateFacade.getNow(),
				CertificateFacade.getNowPlusYears(1));
		req.setSerialNumber(100);

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
		assertEquals(100, req.getSerialNumber());
		assertFalse(c.isTdmxZoneAdminCertificate());
		assertFalse(c.isTdmxDomainAdminCertificate());
		assertTrue(c.isTdmxUserCertificate());
		assertEquals("CN=" + req.getName() + ",OU=" + issuer.getCommonName()
				+ ",OU=tdmx-domain,OU=IT,O=mycompany,L=Zug,C=CH", c.getSubject());
		assertEquals(issuer.getSubject(), c.getIssuer());

	}

	@Test
	public void test_PKIXValidation_DAC_UC() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC("zone.root", 10);
		// default serialnum
		assertEquals(1, zac.getPublicCert().getSerialNumber());

		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);
		// default serialnum
		assertEquals(1, dac.getPublicCert().getSerialNumber());

		PKIXCredential uc = CertificateFacade.createUC(dac, 1);
		// default serialnum
		assertEquals(1, uc.getPublicCert().getSerialNumber());

		assertTrue(
				CredentialUtils.isValidUserCertificate(zac.getPublicCert(), dac.getPublicCert(), uc.getPublicCert()));
		assertTrue(CredentialUtils.isValidDomainAdministratorCertificate(zac.getPublicCert(), dac.getPublicCert()));

		assertFalse(
				CredentialUtils.isValidUserCertificate(zac.getPublicCert(), uc.getPublicCert(), dac.getPublicCert()));
		assertFalse(CredentialUtils.isValidDomainAdministratorCertificate(zac.getPublicCert(), uc.getPublicCert()));
	}

	@Test
	public void testInvalidUserCert() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC("zone.root", 10);
		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);

		UserCredentialSpecifier req = CertificateFacade.createUCS(dac, CertificateFacade.getNowPlusYears(-2),
				CertificateFacade.getNowPlusYears(-1));
		req.setSerialNumber(100);

		PKIXCredential cred = CredentialUtils.createUserCredential(req);

		assertFalse(
				CredentialUtils.isValidUserCertificate(zac.getPublicCert(), dac.getPublicCert(), cred.getPublicCert()));
	}

	@Test
	public void testServerIpCert() throws Exception {
		String ipAddress = "10.10.10.10";
		PKIXCredential serverCred = CertificateFacade.createServerIp(ipAddress, 1);

		assertEquals(1, serverCred.getCertificateChain().length);
		assertEquals(ipAddress, serverCred.getPublicCert().getCommonName());

		assertTrue(CredentialUtils.isValidServerIpCertificate(serverCred.getPublicCert()));
	}

	@Test
	public void testServerIpCertLocalhost() throws Exception {
		InetAddress inetAddress = InetAddress.getLocalHost();

		PKIXCredential serverCred = CertificateFacade.createServerIp(inetAddress.getHostAddress(), 1);

		assertEquals(1, serverCred.getCertificateChain().length);
		assertEquals(inetAddress.getHostAddress(), serverCred.getPublicCert().getCommonName());

		assertTrue(CredentialUtils.isValidServerIpCertificate(serverCred.getPublicCert()));
	}

	@Test
	public void testInvalidServerIpCert() throws Exception {
		String ipAddress = "10.10.10.10";
		Calendar from = CertificateFacade.getNowPlusYears(-2);
		Calendar to = CertificateFacade.getNowPlusYears(-1);

		ServerIpCredentialSpecifier spec = CertificateFacade.createServerIp(ipAddress, from, to);
		PKIXCredential serverCred = CredentialUtils.createServerIpCredential(spec);

		assertEquals(1, serverCred.getCertificateChain().length);
		assertEquals(ipAddress, serverCred.getPublicCert().getCommonName());

		assertFalse(CredentialUtils.isValidServerIpCertificate(serverCred.getPublicCert()));
	}

	@Test
	public void dumpUserCert() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC("zone.root", 10);
		byte[] bs = zac.getPublicCert().getX509Encoded();
		FileUtils.storeFileContents("za.crt", bs, ".tmp");

		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);
		bs = dac.getPublicCert().getX509Encoded();
		FileUtils.storeFileContents("da.crt", bs, ".tmp");

		PKIXCredential uc = CertificateFacade.createUC(dac, 1);
		bs = uc.getPublicCert().getX509Encoded();
		FileUtils.storeFileContents("uc.crt", bs, ".tmp");

		PKIXCredential serverIp = CertificateFacade.createServerIp(InetAddress.getLocalHost().getHostAddress(), 1);
		bs = serverIp.getPublicCert().getX509Encoded();
		FileUtils.storeFileContents("srv.crt", bs, ".tmp");
	}

}
