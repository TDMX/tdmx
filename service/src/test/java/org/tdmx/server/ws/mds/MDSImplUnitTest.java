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
package org.tdmx.server.ws.mds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.mds.GetDestinationSession;
import org.tdmx.core.api.v01.mds.GetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.ListChannel;
import org.tdmx.core.api.v01.mds.ListChannelResponse;
import org.tdmx.core.api.v01.mds.SetDestinationSession;
import org.tdmx.core.api.v01.mds.SetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DestinationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.MockZonePartitionIdInstaller;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedClientService;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;
import org.tdmx.server.ws.session.WebServiceSessionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MDSImplUnitTest {

	private static final Logger log = LoggerFactory.getLogger(MDSImplUnitTest.class);

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private AgentCredentialService agentCredentialService;
	@Autowired
	private AgentCredentialFactory agentCredentialFactory;

	@Autowired
	@Named("ws.MDS.SessionFactory")
	private WebServiceSessionFactory<MDSServerSession> serverSessionFactory;
	@Autowired
	@Named("ws.authenticatedClientService")
	private AuthenticatedClientService authenticatedClientService;
	@Autowired
	@Named("ws.MDS.ServerSessionManager")
	private WebServiceSessionManager serverSessionManager;

	@Autowired
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	@Autowired
	private ZoneService zoneService;
	@Autowired
	private DomainService domainService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private ServiceService serviceService;
	@Autowired
	private ChannelService channelService;
	@Autowired
	private DestinationService destinationService;

	@Autowired
	@Named("ws.MDS")
	private MDS mds;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;

	private Zone zone;
	private org.tdmx.lib.zone.domain.Domain domain;
	private org.tdmx.lib.zone.domain.Service service;
	private org.tdmx.lib.zone.domain.Address address;
	private AccountZone accountZone;
	private PKIXCredential zac;
	private PKIXCredential dac;
	private PKIXCredential uc;

	private final String UC_SESSION_ID = "UC-1";

	@Before
	public void doSetup() throws Exception {

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockZonePartitionIdInstaller.ZP1_S1);
		input.setNumZACs(1);
		input.setNumDomains(1);
		input.setNumDACsPerDomain(1);
		input.setNumAddressesPerDomain(1);
		input.setNumUsersPerAddress(1);

		data = dataGenerator.setUp(input);

		accountZone = data.getAccountZone();
		zone = data.getZone();
		zac = data.getZacs().get(0).getCredential();
		domain = data.getDomains().get(0).getDomain();
		service = data.getDomains().get(0).getServices().get(0).getService();
		dac = data.getDomains().get(0).getDacs().get(0).getCredential();
		address = data.getDomains().get(0).getAddresses().get(0).getAddress();
		uc = data.getDomains().get(0).getAddresses().get(0).getUcs().get(0).getCredential();

		Map<SeedAttribute, Long> seedAttributeMap = new HashMap<>();
		seedAttributeMap.put(SeedAttribute.AccountZoneId, accountZone.getId());
		seedAttributeMap.put(SeedAttribute.ZoneId, zone.getId());
		seedAttributeMap.put(SeedAttribute.DomainId, domain.getId());
		seedAttributeMap.put(SeedAttribute.ServiceId, service.getId());
		seedAttributeMap.put(SeedAttribute.AddressId, address.getId());

		serverSessionManager.createSession(UC_SESSION_ID, "pcs-1", uc.getPublicCert(), seedAttributeMap);
	}

	@After
	public void doTeardown() {
		authenticatedClientService.clearAuthenticatedClient();

		dataGenerator.tearDown(input, data);
	}

	@Test
	public void testAutowired() {
		assertNotNull(serverSessionFactory);
		assertNotNull(serverSessionManager);
		assertNotNull(authenticatedClientService);

		assertNotNull(agentCredentialService);
		assertNotNull(agentCredentialFactory);

		assertNotNull(zoneService);
		assertNotNull(domainService);
		assertNotNull(addressService);

		assertEquals(WebServiceApiName.MDS, serverSessionManager.getApiName());
		// the service under test...
		assertNotNull(mds);
	}

	@Test
	public void testListChannelAuthorization_All() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		ListChannel req = new ListChannel();
		req.setSessionId(UC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ListChannelResponse response = mds.listChannel(req);
		assertSuccess(response);

		// TODO others
		assertEquals(1, response.getChannelinfos().size());
	}

	@Test
	public void testSetDestinationSession() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		SetDestinationSession req = new SetDestinationSession();
		req.setSessionId(UC_SESSION_ID);

		Destinationsession ds = new Destinationsession();
		ds.setEncryptionContextId("id1");
		ds.setSessionKey(new byte[] { 1, 2, 3 });
		ds.setScheme("scheme-name");

		SignatureUtils.createDestinationSessionSignature(uc, SignatureAlgorithm.SHA_256_RSA, new Date(),
				service.getServiceName(), ds);

		req.setDestinationsession(ds);

		SetDestinationSessionResponse response = mds.setDestinationSession(req);
		assertSuccess(response);
		// TODO others

		// do getDestinationSession to confirm DS created
		GetDestinationSession getReq = new GetDestinationSession();
		getReq.setSessionId(UC_SESSION_ID);

		GetDestinationSessionResponse getRes = mds.getDestinationSession(getReq);
		assertSuccess(getRes);
		assertNotNull(getRes.getDestination());
		assertNotNull(getRes.getDestination().getDestinationsession());

		// tamper with signature doesn't work
		ds.getUsersignature().getSignaturevalue().setSignature("gugus");
		response = mds.setDestinationSession(req);
		assertError(ErrorCode.InvalidSignatureDestinationSession, response);

		// check that the channeldestinationsessions are set
		boolean setCft = false;
		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {
			boolean more = true;
			// fetch ALL Channels which have this Destination as Destination.
			for (int pageNo = 0; more; pageNo++) {
				ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(
						new PageSpecifier(pageNo, 5));
				sc.setDomain(domain);
				sc.getDestination().setLocalName(uc.getPublicCert().getCommonName());
				sc.getDestination().setDomainName(domain.getDomainName());
				sc.getDestination().setServiceName(service.getServiceName());

				List<Channel> channels = channelService.search(zone, sc);

				for (Channel channel : channels) {
					log.info("" + channel);
					if (channel.getSession() != null) {
						setCft = true;
					}
				}
				if (channels.isEmpty()) {
					more = false;
				}
			}

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
		assertTrue(setCft);
	}

	private void assertSuccess(Acknowledge ack) {
		assertNotNull(ack);
		String errorDesc = ack.getError() != null ? ack.getError().getDescription() : "ok";
		assertTrue("Error " + errorDesc, ack.isSuccess());
		assertNull(ack.getError());
	}

	private void assertError(ErrorCode expected, Acknowledge ack) {
		assertNotNull(ack);
		String errorDesc = ack.getError() != null ? ack.getError().getDescription() : "ok";
		assertFalse(errorDesc, ack.isSuccess());
		assertNotNull(ack.getError());
		assertEquals(expected.getErrorCode(), ack.getError().getCode());
		assertEquals(expected.getErrorDescription(), ack.getError().getDescription());
	}

}
