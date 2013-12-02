package org.tdmx.console.application.dao;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;

public class SystemTrustStoreTest {

	private SystemTrustStoreImpl ts;
	
	@Before
	public void setUp() throws Exception {
		ts = new SystemTrustStoreImpl();
	}

	@Test
	public void testGetAllTrustedCAs() throws Exception {
		List<TrustStoreEntry> rootCAs = ts.getAllTrustedCAs();
		assertNotNull(rootCAs);
		
		for( TrustStoreEntry rootCA : rootCAs ) {
			System.out.println(CertificateIOUtils.x509certToPem(rootCA.getCertificate()));
			System.out.println();
		}
	}

	@Test
	public void testGetAllDistrustedCAs() throws Exception {
		List<TrustStoreEntry> rootCAs = ts.getAllTrustedCAs();
		assertNotNull(rootCAs);
		
		for( TrustStoreEntry rootCA : rootCAs ) {
			System.out.println(CertificateIOUtils.x509certToPem(rootCA.getCertificate()));
			System.out.println();
		}
	}
}
