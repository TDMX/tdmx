package org.tdmx.lib.control.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.lib.console.domain.CredentialFacade;
import org.tdmx.lib.control.domain.AuthorizationStatus;

@RunWith(SpringJUnit4ClassRunner.class)

@ContextConfiguration

//@TransactionConfiguration(transactionManager="tdmx.lib.control.TransactionManager")
//@Transactional("ControlDB")
public class AuthorizedAgentServiceRepositoryUnitTest {

	@Autowired
	private AuthorizedAgentService service;
	
//	@Autowired
//	private AuthorizedAgentDao dao;
	
	private PKIXCertificate c;
	
	@Before
	public void doSetup() throws Exception {
		PKIXCredential za = CredentialFacade.createZAC("zone.root");
		
		
		c = za.getPublicCert();
		
		service.createOrUpdate(c, AuthorizationStatus.ACTIVE);
	}
	
	@After
	public void doTeardown() {
		service.delete(c);
	}
	
	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testCheckAuthorization() throws Exception {
		AuthorizationStatus status = service.checkAuthorization(c);
		assertNotNull(status);
		assertEquals(AuthorizationStatus.ACTIVE, status);
	}

	@Test
	public void testCheckAuthorization_Unknown() throws Exception {
		PKIXCredential unknown = CredentialFacade.createZAC("unknown.zone.root");
		
		AuthorizationStatus status = service.checkAuthorization(unknown.getPublicCert());
		assertNotNull(status);
		assertEquals(AuthorizationStatus.UNKNOWN, status);
	}

	@Test
	public void testModify() throws Exception {
		service.createOrUpdate(c, AuthorizationStatus.BLOCKED);

		AuthorizationStatus status = service.checkAuthorization(c);
		assertNotNull(status);
		assertEquals(AuthorizationStatus.BLOCKED, status);
	}

}