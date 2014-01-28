package org.tdmx.client.crypto.certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.util.FileUtils;

public class CertificateAuthorityUtilsTest {

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
		byte[] bs = CertificateIOUtils.encodeCertificate(zac.getCertificateChain()[0]);
		FileUtils.storeFileContents("za.crt", bs, ".tmp");
		
		PKIXCredential dac = createDAC(zac);
		bs = CertificateIOUtils.encodeCertificate(dac.getCertificateChain()[0]);
		FileUtils.storeFileContents("da.crt", bs, ".tmp");
	}

	private PKIXCredential createZAC() throws Exception  {
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
		PKIXCredential cred = CertificateAuthorityUtils.createZoneAdministratorCredential(req);
		
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
		assertEquals("CN=name,TEL=0417100000,EMAIL=pjk@gmail.com,OU=IT,O=mycompany,L=Zug,C=CH", c.getSubject());
		assertEquals("CN=name,TEL=0417100000,EMAIL=pjk@gmail.com,OU=IT,O=mycompany,L=Zug,C=CH", c.getIssuer());
		return cred;
	}

	private PKIXCredential createDAC( PKIXCredential zac ) throws Exception {
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.MILLISECOND, 0);

		Calendar later = Calendar.getInstance();
		later.setTime(new Date());
		later.add(Calendar.YEAR, 2);
		later.set(Calendar.MILLISECOND, 0);

		DomainAdministrationCredentialSpecifier req = new DomainAdministrationCredentialSpecifier();
		req.setZoneAdministratorCredential(zac);
		req.setDomainName("dom.name");
		req.setNotBefore(now);
		req.setNotAfter(later);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		PKIXCredential cred = CertificateAuthorityUtils.createDomainAdministratorCredential(req);
		
		assertNotNull(cred);
		assertNotNull(cred.getCertificateChain());
		assertNotNull(cred.getPrivateKey());
		assertEquals(2, cred.getCertificateChain().length);
		
		PKIXCertificate c = cred.getCertificateChain()[0];

//		assertEquals(req.getCountry(), c.getCountry());
//		assertEquals(req.getLocation(), c.getLocation());
//		assertEquals(req.getOrg(), c.getOrganization());
//		assertEquals(req.getOrgUnit(), c.getOrgUnit());
//		assertEquals(req.getTelephoneNumber(), c.getTelephoneNumber());
//		assertEquals(req.getEmailAddress(), c.getEmailAddress());
		assertEquals(req.getDomainName(), c.getCommonName());
		assertEquals(req.getNotAfter(), c.getNotAfter());
		assertEquals(req.getNotBefore(), c.getNotBefore());
//		assertTrue(c.isTdmxZoneAdminCertificate());
//		assertEquals("CN=name,TEL=0417100000,EMAIL=pjk@gmail.com,OU=IT,O=mycompany,L=Zug,C=CH", c.getSubject());
//		assertEquals("CN=name,TEL=0417100000,EMAIL=pjk@gmail.com,OU=IT,O=mycompany,L=Zug,C=CH", c.getIssuer());
		
		return cred;
	}
	
}
