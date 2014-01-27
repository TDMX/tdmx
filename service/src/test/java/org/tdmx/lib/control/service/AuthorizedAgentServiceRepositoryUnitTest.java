package org.tdmx.lib.control.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;
import org.tdmx.lib.control.dao.AuthorizedAgentDao;
import org.tdmx.lib.control.domain.AuthorizationStatus;
import org.tdmx.lib.control.domain.AuthorizedAgent;
import org.tdmx.lib.control.service.AuthorizedAgentService;

@RunWith(SpringJUnit4ClassRunner.class)

@ContextConfiguration

@TransactionConfiguration(transactionManager="tdmx.lib.control.TransactionManager", defaultRollback=false)
@Transactional("ControlDB")
public class AuthorizedAgentServiceRepositoryUnitTest {

	@Autowired
	private AuthorizedAgentService service;
	
	@Autowired
	private AuthorizedAgentDao dao;
	
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
	private PKIXCertificate c;
	
	@Before
	public void doSetup() throws Exception {
		c = CertificateIOUtils.pemToX509cert(cert);
		
		AuthorizedAgent agent = new AuthorizedAgent();
		agent.setSha1fingerprint(c.getFingerprint());
		agent.setCertificatePem(cert);
		agent.setZoneApex("todo");
		agent.setAuthorizationStatus(AuthorizationStatus.ACTIVE);
		dao.persist(agent);
	}
	
	@After
	public void doTeardown() {
		AuthorizedAgent agent = dao.loadByFingerprint(c.getFingerprint());
		dao.delete(agent);
	}
	
	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(dao);
		assertNotNull(service);
	}

	@Test
	public void testLoadAuthorizedAgent() throws Exception {
		AuthorizedAgent agent = dao.loadByFingerprint(c.getFingerprint());
		assertNotNull(agent);
		assertEquals(c.getFingerprint(),agent.getSha1fingerprint());
		assertEquals(AuthorizationStatus.ACTIVE, agent.getAuthorizationStatus());
		assertNotNull(agent.getCertificatePem());
		
		PKIXCertificate loadedC = CertificateIOUtils.pemToX509cert(agent.getCertificatePem());
		assertNotNull(loadedC);
		assertTrue(c.isIdentical(loadedC));
	}
/*	
	@Test
	public void testSetProperties() throws Exception {
		GetPropertiesRequest.Builder request = GetPropertiesRequest.newBuilder();
		request.setFilename("UNIT-TEST");
		GetPropertiesResponse response = service.getProperties(request.build());
		assertNotNull(response);
		assertEquals(GetPropertiesResponse.ErrorCode.SUCCESS, response.getErrorCode());
		assertNotNull(response.getPropertiesList());
		assertEquals(6, response.getPropertiesList().size());
		
		List<Property> newList = new ArrayList<Property>();
		int i = 0;
		for( Property value : response.getPropertiesList()) {
			if (i > 0) { // skip the first property
				newList.add(Property.newBuilder(value).build());
			}
			i++;
		}
		Property.Builder newProperty = Property.newBuilder();
		newProperty.setName("newProperty");
		newProperty.setType(PropertyType.STRING);
		newProperty.setValue("newValue");
		newList.add(newProperty.build());
		
		SetPropertiesRequest.Builder setrequest = SetPropertiesRequest.newBuilder();
		setrequest.setFilename("UNIT-TEST");
		setrequest.addAllProperties(newList);
		SetPropertiesResponse setresponse = service.setProperties(setrequest.build());
		assertEquals(SetPropertiesResponse.ErrorCode.SUCCESS, setresponse.getErrorCode());
		
		request = GetPropertiesRequest.newBuilder();
		request.setFilename("UNIT-TEST");
		GetPropertiesResponse response2 = service.getProperties(request.build());
		assertNotNull(response2);
		assertEquals(GetPropertiesResponse.ErrorCode.SUCCESS, response2.getErrorCode());
		assertNotNull(response2.getPropertiesList());
		assertEquals(6, response2.getPropertiesList().size());
		
		assertEquals(ConfigurationUtils.getJavaProperties(response2.getPropertiesList()).get("newProperty"), "newValue");
	}
	
	@Test
	public void testFetchFile() throws Exception {
		GetPropertiesRequest.Builder request = GetPropertiesRequest.newBuilder();
		request.setFilename("UNIT-TEST");
		GetPropertiesResponse response = service.getProperties(request.build());
		assertNotNull(response);
		assertEquals(GetPropertiesResponse.ErrorCode.SUCCESS, response.getErrorCode());
		assertNotNull(response.getPropertiesList());
		assertEquals(6, response.getPropertiesList().size());

		Properties props = ConfigurationUtils.getJavaProperties(response.getPropertiesList());
		assertNotNull(props);
		
		assertEquals("v1", props.get("str1"));
		assertEquals(Integer.parseInt("1001"), props.get("int1"));
		assertEquals(Long.parseLong("1001001001"), props.get("long1"));
		
		SimpleDateFormat dayF = new SimpleDateFormat("yyyy-MM-dd");
		Date day = dayF.parse("2010-01-21");
		assertEquals(day, props.get("day1"));
		
		SimpleDateFormat dateF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		Date date = dateF.parse("2010-01-21 22:09:01 +0100");
		assertEquals(date, props.get("date1"));
		
		assertEquals(5, props.entrySet().size());
	}

	@Test
	public void testFetchFileNotFound() throws Exception {
		GetPropertiesRequest.Builder request = GetPropertiesRequest.newBuilder();
		request.setFilename("gugus");
		GetPropertiesResponse response = service.getProperties(request.build());
		assertNotNull(response);
		assertEquals(GetPropertiesResponse.ErrorCode.SUCCESS, response.getErrorCode());
		assertNotNull(response.getPropertiesList());
		assertEquals(0, response.getPropertiesList().size());
	}
*/
}