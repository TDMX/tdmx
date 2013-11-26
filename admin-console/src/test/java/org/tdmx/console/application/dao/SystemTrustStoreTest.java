package org.tdmx.console.application.dao;

import static org.junit.Assert.*;

import java.security.cert.X509Certificate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;

public class SystemTrustStoreTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetAllTrustedCAs() throws Exception {
		SystemTrustStoreImpl ts = new SystemTrustStoreImpl();
		
		List<X509Certificate> rootCAs = ts.getAllTrustedCAs();
		assertNotNull(rootCAs);
		
		for( X509Certificate rootCA : rootCAs ) {
			System.out.println(CertificateIOUtils.x509certToPem(rootCA));
			System.out.println();
		}
	}

}
