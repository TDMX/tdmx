package org.tdmx.client.crypto.certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
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

		PKIXCertificateAuthorityRequest req = new PKIXCertificateAuthorityRequest();
		req.setCn("name");
		req.setCountry("CH");
		req.setOrg("mycompany");
		req.setNotBefore(now);
		req.setNotAfter(later);
		List<String> dnsNameConstraints = new ArrayList<>();
		dnsNameConstraints.add("d.com");
		dnsNameConstraints.add("f.com");
		req.setDnsNameConstraints(dnsNameConstraints);
		req.setSubjectNameContraint(true);
		req.setKeyAlgorithm(AsymmetricEncryptionAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		PKIXCredential cred = CertificateAuthorityUtils.createCertificateAuthority(req);
		
		assertNotNull(cred);
		assertNotNull(cred.getCertificateChain());
		assertNotNull(cred.getPrivateKey());
		assertEquals(1, cred.getCertificateChain().length);
		
		PKIXCertificate c = cred.getCertificateChain()[0];
		assertEquals(req.getCn(), c.getCn());
		assertEquals(req.getCountry(), c.getCountry());
		assertEquals(req.getOrg(), c.getOrg());
		assertEquals(req.getNotAfter(), c.getNotAfter());
		assertEquals(req.getNotBefore(), c.getNotBefore());
		
		byte[] bs = CertificateIOUtils.encodeCertificate(c);
		FileUtils.storeFileContents("ca.crt", bs, ".tmp");
	}

}
