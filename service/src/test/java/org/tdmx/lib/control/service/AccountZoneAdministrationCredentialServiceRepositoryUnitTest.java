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
package org.tdmx.lib.control.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialID;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager="tdmx.lib.control.TransactionManager")
// @Transactional("ControlDB")
public class AccountZoneAdministrationCredentialServiceRepositoryUnitTest {

	@Autowired
	private AccountZoneAdministrationCredentialService service;

	// @Autowired
	// private AuthorizedAgentDao dao;
	private String accountId;
	private PKIXCredential zac;

	@Before
	public void doSetup() throws Exception {
		accountId = UUID.randomUUID().toString();
		byte[] zacFile = FileUtils.getFileContents("src/test/resources/zac.keystore");
		assertNotNull(zacFile);
		zac = KeyStoreUtils.getPrivateCredential(zacFile, "jks", "changeme", "client");
		String pem = CertificateIOUtils.x509certToPem(zac.getPublicCert());

		AccountZoneAdministrationCredential zoneAC = new AccountZoneAdministrationCredential(accountId, pem);

		assertNotNull(zoneAC);
		assertNotNull(zoneAC.getId());
		assertEquals(accountId, zoneAC.getId().getAccountId());
		assertEquals(zac.getPublicCert().getFingerprint(), zoneAC.getId().getSha1fingerprint());
		assertEquals(zac.getPublicCert().getTdmxZoneInfo().getZoneRoot(), zoneAC.getZoneApex());
		assertEquals(AccountZoneAdministrationCredentialStatus.PENDING, zoneAC.getCredentialStatus());
		service.createOrUpdate(zoneAC);
	}

	@After
	public void doTeardown() {
		List<AccountZoneAdministrationCredential> list = service
				.search(new org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialSearchCriteria(
						new PageSpecifier(0, 1000)));
		for (AccountZoneAdministrationCredential ac : list) {
			service.delete(ac);
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookupById() throws Exception {
		AccountZoneAdministrationCredentialID id = new AccountZoneAdministrationCredentialID(accountId, zac
				.getPublicCert().getFingerprint());
		AccountZoneAdministrationCredential zoneAC = service.findById(id);
		assertNotNull(zoneAC);
		assertEquals(zac.getPublicCert().getTdmxZoneInfo().getZoneRoot(), zoneAC.getZoneApex());
	}

	@Test
	public void testLookup_NotFound() throws Exception {
		AccountZoneAdministrationCredentialID id = new AccountZoneAdministrationCredentialID(accountId, UUID
				.randomUUID().toString());

		AccountZoneAdministrationCredential az = service.findById(id);
		assertNull(az);
	}

	@Test
	@Ignore
	public void testModify() throws Exception {
		// TODO
	}

}