package org.tdmx.client.crypto.certificate;

import static org.junit.Assert.*;

import java.security.cert.X509Certificate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;

public class CertificateIOUtilsTest {

	private String cert = ""+
"-----BEGIN CERTIFICATE-----"+TrustStoreEntry.NL+
"MIIFwTCCA6mgAwIBAgIITrIAZwwDXU8wDQYJKoZIhvcNAQEFBQAwSTELMAkGA1UE"+TrustStoreEntry.NL+
"BhMCQ0gxFTATBgNVBAoTDFN3aXNzU2lnbiBBRzEjMCEGA1UEAxMaU3dpc3NTaWdu"+TrustStoreEntry.NL+
"IFBsYXRpbnVtIENBIC0gRzIwHhcNMDYxMDI1MDgzNjAwWhcNMzYxMDI1MDgzNjAw"+TrustStoreEntry.NL+
"WjBJMQswCQYDVQQGEwJDSDEVMBMGA1UEChMMU3dpc3NTaWduIEFHMSMwIQYDVQQD"+TrustStoreEntry.NL+
"ExpTd2lzc1NpZ24gUGxhdGludW0gQ0EgLSBHMjCCAiIwDQYJKoZIhvcNAQEBBQAD"+TrustStoreEntry.NL+
"ggIPADCCAgoCggIBAMrfogLi2vj8Bxax3mCq3pZcZB/HL37PZ/pEQtZ2Y5Wu669y"+TrustStoreEntry.NL+
"IIpFR4ZieIbWIDkm9K6j/SPnpZy1IiEZtzeTIsBQnIJ71NUERFzLtMKfkr4k2Htn"+TrustStoreEntry.NL+
"IuJpX+UFeNSH2XFwMyVTtIc7KZAoNppVRDBopIOXfw0enHb/FZ1glwCNioUD7IC+"+TrustStoreEntry.NL+
"6ixuEFGSzH7VozPY1kneWCqv9hbrS3uQMpe5up1Y8fhXSQQeol0GcN1x2/ndi5ob"+TrustStoreEntry.NL+
"jM89o03Oy3z2u5yg+gnOI2Ky6Q0f4nIoj5+saCB9bzuohTEJfwvH6GXp43gOCWcw"+TrustStoreEntry.NL+
"izSC+13gzJ2BbWLuCB4ELE6b7P6pT1/9aXjvCR+htL/68++QHkwFix7qepF6w9fl"+TrustStoreEntry.NL+
"+zC8bBsQWJj3Gl/QKTIDE0ZNYWqFTFJ0LwYfexHihJfGmfNtf9dng34TaNhxKFrY"+TrustStoreEntry.NL+
"zt3oEBSa/m0jh26OWnA81Y0JAKeqvLAxN23IhBQeW71FYyBrS3SMvds6DsHPWhaP"+TrustStoreEntry.NL+
"pZjydomyExI7C3d3rLvlPClKknLKYRorXkzig3R3+jVIeoVNjZpTxN94ypeRSCtF"+TrustStoreEntry.NL+
"KwH3HBqi7Ri6Cr2D+m+8jVeTO9TUps4e8aCxzqv9KyiaTxvXw3LbpMS/XUz13XuW"+TrustStoreEntry.NL+
"ae5ogObnmLo2t/5u7Su9IPhlGdpVCX4l3P5hYnL5fhgC72O00Puv5TtjjGePAgMB"+TrustStoreEntry.NL+
"AAGjgawwgakwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0O"+TrustStoreEntry.NL+
"BBYEFFCvzAeHFUdvOMW0ZdHelarp35zMMB8GA1UdIwQYMBaAFFCvzAeHFUdvOMW0"+TrustStoreEntry.NL+
"ZdHelarp35zMMEYGA1UdIAQ/MD0wOwYJYIV0AVkBAQEBMC4wLAYIKwYBBQUHAgEW"+TrustStoreEntry.NL+
"IGh0dHA6Ly9yZXBvc2l0b3J5LnN3aXNzc2lnbi5jb20vMA0GCSqGSIb3DQEBBQUA"+TrustStoreEntry.NL+
"A4ICAQAIhab1Fgz8RBrBY+D5VUYI/HAcQiiWjrfFwUF1TglxeeVtlspLpYhg0DB0"+TrustStoreEntry.NL+
"uMoI3LQwnkAHFmtllXcBrqS3NQuB2nEVqXQXOHtYyvkv+8Bldo1bAbl93oI9ZLi+"+TrustStoreEntry.NL+
"FHSjClTTLJUYFzX1UWs/j6KWYTl4a0vlpqD4U99REJNi54Av4tHgvI42Rncz7Lj7"+TrustStoreEntry.NL+
"jposiU0xEQ8mngS7twSNC/K5/FqdOxa3L8iYq/6KUFkuozv8KV2LwUvJ4ooTHbG/"+TrustStoreEntry.NL+
"u0IdUt1O2BReEMYxB+9xJ/cbOQncguqLs5WGXv312l0xpuAxtpTmREl0xRbl9x8D"+TrustStoreEntry.NL+
"YSjFyMsSoEJL+WuICI20MhjzdZ/EfwBPBZWcoxcCw7NTm6ogOSkrZvqdr16zktK1"+TrustStoreEntry.NL+
"puEa+S1BaYEUtLS17Yk9zvupnTVCRLEcFHOBzyoBNZox1S2PbYTfgE1X4z/FhHXa"+TrustStoreEntry.NL+
"icYwu+uPyyIIoK6q8QNsOktNCaUOcsZWayFCTiMlFGiudgp8DAdwZPmaL/YFOSbG"+TrustStoreEntry.NL+
"DI8Zf0NebvRbFS/bYV3mZy8/CJT5YLSYMdp08YSTcU1f+2BY0fvEwW2JorsgH51x"+TrustStoreEntry.NL+
"kcsymxM9Pn2SUjWskpSi0xjCfMfqr3YFFt1nJ8J+HAciIfNAChs0B0QTwoRqjt8Z"+TrustStoreEntry.NL+
"Wr9/6x3iGjjRXK9HkmuAtTClyY3YqzGBH9/CZjfTk6mFhnll0g=="+TrustStoreEntry.NL+
"-----END CERTIFICATE-----"+TrustStoreEntry.NL;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPemToX509Conversion() throws Exception {
		
		X509Certificate[] certs = CertificateIOUtils.pemToX509certs(cert);
		assertNotNull(certs);
		assertEquals(certs.length, 1);
		String c = CertificateIOUtils.x509certToPem(certs[0]);
		assertEquals(cert,c);
	}

	@Test
	public void testX509ToPemConversion() throws Exception {
		
		List<X509Certificate> rootCAs = TrustStoreCertificateIOUtils.getAllSystemTrustedCAs();
		assertNotNull(rootCAs);
		
		for( X509Certificate rootCA : rootCAs ) {
			String s = CertificateIOUtils.x509certToPem(rootCA);
			assertNotNull(s);
		}
	}

	@Test
	public void testPemToX509ListConversion() throws Exception {
		
		List<X509Certificate> rootCAs = TrustStoreCertificateIOUtils.getAllSystemTrustedCAs();
		assertNotNull(rootCAs);
		
		StringBuffer sb = new StringBuffer();
		for( X509Certificate rootCA : rootCAs ) {
			String s = CertificateIOUtils.x509certToPem(rootCA);
			assertNotNull(s);
			sb.append(s);
			sb.append("\n");
		}
		
		String pemList = sb.toString();
		X509Certificate[] certs = CertificateIOUtils.pemToX509certs(pemList);
		assertNotNull(certs);
		assertEquals(certs.length, rootCAs.size());
	}

}
