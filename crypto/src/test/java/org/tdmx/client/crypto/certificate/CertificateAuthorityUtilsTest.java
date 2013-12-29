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
import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.util.FileUtils;

public class CertificateAuthorityUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateCACert() throws Exception {
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.MILLISECOND, 0);

		Calendar later = Calendar.getInstance();
		later.setTime(new Date());
		later.add(Calendar.YEAR, 10);
		later.set(Calendar.MILLISECOND, 0);

		CertificateAuthoritySpecifier req = new CertificateAuthoritySpecifier();
		req.setCn("name");
		req.setTelephoneNumber("0417100000");
		req.setEmailAddress("pjk@gmail.com");
		req.setOrg("mycompany");
		req.setCountry("CH");
		req.setNotBefore(now);
		req.setNotAfter(later);
		List<String> dnsNameConstraints = new ArrayList<>();
		dnsNameConstraints.add("d.com");
		dnsNameConstraints.add("f.com");
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		PKIXCredential cred = CertificateAuthorityUtils.createCertificateAuthority(req);
		
		assertNotNull(cred);
		assertNotNull(cred.getCertificateChain());
		assertNotNull(cred.getPrivateKey());
		assertEquals(1, cred.getCertificateChain().length);
		
		PKIXCertificate c = cred.getCertificateChain()[0];
		assertEquals(req.getCn(), c.getCommonName());
		assertEquals(req.getCountry(), c.getCountry());
		assertEquals(req.getOrg(), c.getOrganization());
		assertEquals(req.getNotAfter(), c.getNotAfter());
		assertEquals(req.getNotBefore(), c.getNotBefore());
		assertTrue(c.isTdmxDomainCA());
		assertEquals("CN=name,TEL=0417100000,EMAIL=pjk@gmail.com,O=mycompany,C=CH", c.getSubject());
		assertEquals("CN=name,TEL=0417100000,EMAIL=pjk@gmail.com,O=mycompany,C=CH", c.getIssuer());
		byte[] bs = CertificateIOUtils.encodeCertificate(c);
		FileUtils.storeFileContents("ca.crt", bs, ".tmp");
	}

}
