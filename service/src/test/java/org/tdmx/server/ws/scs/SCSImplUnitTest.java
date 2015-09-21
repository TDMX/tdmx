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
package org.tdmx.server.ws.scs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;

import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelDestination;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.scs.GetMDSSession;
import org.tdmx.core.api.v01.scs.GetMDSSessionResponse;
import org.tdmx.core.api.v01.scs.GetMOSSession;
import org.tdmx.core.api.v01.scs.GetMOSSessionResponse;
import org.tdmx.core.api.v01.scs.GetMRSSession;
import org.tdmx.core.api.v01.scs.GetMRSSessionResponse;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.DomainZoneApexInfo;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.control.service.DomainZoneResolutionService;
import org.tdmx.lib.control.service.SegmentService;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
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
import org.tdmx.server.session.WebServiceSessionEndpoint;
import org.tdmx.server.session.allocation.MockServerSessionAllocationServiceImpl;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedClientService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SCSImplUnitTest {

	private static final Logger log = LoggerFactory.getLogger(SCSImplUnitTest.class);

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private AgentCredentialService agentCredentialService;
	@Autowired
	private AgentCredentialFactory agentCredentialFactory;

	@Autowired
	@Named("scs.authenticatedClientService")
	private AuthenticatedClientService authenticatedClientService;

	@Autowired
	private SegmentService segmentService;
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
	private MockServerSessionAllocationServiceImpl mockServerSesionAllocationService;
	@Autowired
	private DomainZoneResolutionService mockDomainZoneResolutionService;

	@Autowired
	@Named("ws.SCS")
	private SCSImpl scs;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;

	private Zone zone;
	private org.tdmx.lib.zone.domain.Domain domain1;
	private org.tdmx.lib.zone.domain.Domain domain2;
	private org.tdmx.lib.zone.domain.ChannelAuthorization recvAuth2;
	private org.tdmx.lib.zone.domain.Service service1;
	private org.tdmx.lib.zone.domain.Service service2;
	private org.tdmx.lib.zone.domain.Address address1;
	private org.tdmx.lib.zone.domain.Address address2;
	private AccountZone accountZone;
	private PKIXCredential zac;
	private PKIXCredential dac1;
	private PKIXCredential dac2;
	private PKIXCredential uc1;
	private PKIXCredential uc2;

	private WebServiceSessionEndpoint sse;

	private final DomainToApiMapper d2a = new DomainToApiMapper();
	private final ApiToDomainMapper a2d = new ApiToDomainMapper();

	@Before
	public void doSetup() throws Exception {

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockZonePartitionIdInstaller.ZP1_S1);
		input.setNumZACs(1);
		input.setNumDomains(2);
		input.setNumServicesPerDomain(1);
		input.setNumDACsPerDomain(1);
		input.setNumAddressesPerDomain(1);
		input.setNumUsersPerAddress(1);

		data = dataGenerator.setUp(input);

		accountZone = data.getAccountZone();
		zone = data.getZone();
		zac = data.getZacs().get(0).getCredential();
		domain1 = data.getDomains().get(0).getDomain();
		domain2 = data.getDomains().get(1).getDomain();
		service1 = data.getDomains().get(0).getServices().get(0).getService();
		service2 = data.getDomains().get(1).getServices().get(0).getService();
		dac1 = data.getDomains().get(0).getDacs().get(0).getCredential();
		dac2 = data.getDomains().get(1).getDacs().get(0).getCredential();
		address1 = data.getDomains().get(0).getAddresses().get(0).getAddress();
		address2 = data.getDomains().get(1).getAddresses().get(0).getAddress();
		recvAuth2 = data.getDomains().get(1).getAuths().get(0);
		uc1 = data.getDomains().get(0).getAddresses().get(0).getUcs().get(0).getCredential();
		uc2 = data.getDomains().get(1).getAddresses().get(0).getUcs().get(0).getCredential();

		sse = new WebServiceSessionEndpoint("SID" + System.currentTimeMillis(),
				"https://" + System.currentTimeMillis() + "/scs", uc2.getPublicCert());
		mockServerSesionAllocationService.setEndpoint(sse);

		reset(mockDomainZoneResolutionService);

		Segment s1 = segmentService.findBySegment(MockZonePartitionIdInstaller.S1);
		scs.start(s1, null);
	}

	@After
	public void doTeardown() {
		authenticatedClientService.clearAuthenticatedClient();

		scs.stop();
		dataGenerator.tearDown(input, data);
	}

	@Test
	public void testAutowired() {
		assertNotNull(authenticatedClientService);
		assertNotNull(mockServerSesionAllocationService);
		assertNotNull(zonePartitionIdProvider);

		assertNotNull(agentCredentialService);
		assertNotNull(agentCredentialFactory);

		assertNotNull(zoneService);
		assertNotNull(domainService);
		assertNotNull(addressService);

		// the service under test...
		assertNotNull(scs);
	}

	@Test
	public void test_getMOSSession() {
		authenticatedClientService.setAuthenticatedClient(uc1.getPublicCert());

		GetMOSSession req = new GetMOSSession();

		GetMOSSessionResponse response = scs.getMOSSession(req);
		assertSuccess(response);

		assertNotNull(response.getEndpoint());
		assertEquals(sse.getHttpsUrl(), response.getEndpoint().getUrl());
		assertArrayEquals(sse.getPublicCertificate().getX509Encoded(), response.getEndpoint().getTlsCertificate());
		assertNotNull(response.getSession());
		assertEquals(sse.getSessionId(), response.getSession().getSessionId());
		assertEquals(zone.getZoneApex(), response.getSession().getZoneapex());
		assertEquals(address1.getLocalName(), response.getSession().getLocalname());
		assertEquals(address1.getDomain().getDomainName(), response.getSession().getDomain());
		assertNull(response.getSession().getServiceprovider()); // TODO maybe change future to set with own SP
		assertNull(response.getSession().getServicename());
	}

	@Test
	public void test_getMOSSession_SuspendedUser() {
		authenticatedClientService.setAuthenticatedClient(uc1.getPublicCert());
		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {

			AgentCredential user = agentCredentialService.findByFingerprint(uc1.getPublicCert().getFingerprint());
			user.setCredentialStatus(AgentCredentialStatus.SUSPENDED);
			agentCredentialService.createOrUpdate(user);

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		GetMOSSession req = new GetMOSSession();

		GetMOSSessionResponse response = scs.getMOSSession(req);
		assertError(ErrorCode.SuspendedAccess, response);

		assertNull(response.getEndpoint());
		assertNull(response.getSession());
	}

	@Test
	public void test_getMOSSession_NonExistentUser() {
		authenticatedClientService.setAuthenticatedClient(uc1.getPublicCert());
		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {

			AgentCredential user = agentCredentialService.findByFingerprint(uc1.getPublicCert().getFingerprint());
			agentCredentialService.delete(user);

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		GetMOSSession req = new GetMOSSession();

		GetMOSSessionResponse response = scs.getMOSSession(req);
		assertError(ErrorCode.UserCredentialNotFound, response);

		assertNull(response.getEndpoint());
		assertNull(response.getSession());
	}

	@Test
	public void test_getMDSSession() {
		authenticatedClientService.setAuthenticatedClient(uc2.getPublicCert());

		GetMDSSession req = new GetMDSSession();
		req.setServicename(service2.getServiceName());

		GetMDSSessionResponse response = scs.getMDSSession(req);
		assertSuccess(response);

		assertNotNull(response.getEndpoint());
		assertEquals(sse.getHttpsUrl(), response.getEndpoint().getUrl());
		assertArrayEquals(sse.getPublicCertificate().getX509Encoded(), response.getEndpoint().getTlsCertificate());
		assertNotNull(response.getSession());
		assertEquals(sse.getSessionId(), response.getSession().getSessionId());
		assertEquals(zone.getZoneApex(), response.getSession().getZoneapex());
		assertEquals(address2.getLocalName(), response.getSession().getLocalname());
		assertEquals(address2.getDomain().getDomainName(), response.getSession().getDomain());
		assertEquals(service2.getServiceName(), response.getSession().getServicename());
		assertNull(response.getSession().getServiceprovider()); // TODO maybe change future to set with own SP
	}

	@Test
	public void test_getMDSSession_SuspendedUser() {
		authenticatedClientService.setAuthenticatedClient(uc1.getPublicCert());
		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {

			AgentCredential user = agentCredentialService.findByFingerprint(uc1.getPublicCert().getFingerprint());
			user.setCredentialStatus(AgentCredentialStatus.SUSPENDED);
			agentCredentialService.createOrUpdate(user);

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		GetMOSSession req = new GetMOSSession();

		GetMOSSessionResponse response = scs.getMOSSession(req);
		assertError(ErrorCode.SuspendedAccess, response);

		assertNull(response.getEndpoint());
		assertNull(response.getSession());
	}

	@Test
	public void test_getMDSSession_NonExistentUser() {
		authenticatedClientService.setAuthenticatedClient(uc2.getPublicCert());
		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {

			AgentCredential user = agentCredentialService.findByFingerprint(uc2.getPublicCert().getFingerprint());
			agentCredentialService.delete(user);

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		GetMDSSession req = new GetMDSSession();
		req.setServicename(service2.getServiceName());

		GetMDSSessionResponse response = scs.getMDSSession(req);
		assertError(ErrorCode.UserCredentialNotFound, response);

		assertNull(response.getEndpoint());
		assertNull(response.getSession());
	}

	@Test
	public void test_getMDSSession_ServiceNotFound() {
		authenticatedClientService.setAuthenticatedClient(uc2.getPublicCert());

		GetMDSSession req = new GetMDSSession();
		req.setServicename("gugus");

		GetMDSSessionResponse response = scs.getMDSSession(req);
		assertError(ErrorCode.ServiceNotFound, response);

		assertNull(response.getEndpoint());
		assertNull(response.getSession());
	}

	@Test
	public void test_getMDSSession_MissingService() {
		authenticatedClientService.setAuthenticatedClient(uc2.getPublicCert());

		GetMDSSession req = new GetMDSSession();
		req.setServicename("");

		GetMDSSessionResponse response = scs.getMDSSession(req);
		assertError(ErrorCode.MissingServiceName, response);

		assertNull(response.getEndpoint());
		assertNull(response.getSession());
	}

	@Test
	public void test_getMRSSession_MissingChannel() throws Exception {
		byte[] pkixKeystoreContents = FileUtils.getFileContents("src/test/resources/pkixtest.keystore");

		PKIXCredential pkixCred = KeyStoreUtils.getPrivateCredential(pkixKeystoreContents, "jks", "changeme",
				"kidsmathstrainer");
		authenticatedClientService.setAuthenticatedClient(pkixCred.getPublicCert());

		GetMRSSession req = new GetMRSSession();

		GetMRSSessionResponse response = scs.getMRSSession(req);
		assertError(ErrorCode.MissingChannel, response);

	}

	@Test
	public void test_getMRSSession_MissingOriginZoneInfo() throws Exception {
		byte[] pkixKeystoreContents = FileUtils.getFileContents("src/test/resources/pkixtest.keystore");

		PKIXCredential pkixCred = KeyStoreUtils.getPrivateCredential(pkixKeystoreContents, "jks", "changeme",
				"kidsmathstrainer");
		authenticatedClientService.setAuthenticatedClient(pkixCred.getPublicCert());

		Mockito.when(mockDomainZoneResolutionService.resolveDomain("tdmx.kidsmathstrainer.com")).thenReturn(null);

		DomainZoneApexInfo dzi2 = new DomainZoneApexInfo();
		dzi2.setDomainName(domain1.getDomainName());
		dzi2.setZoneApex(zone.getZoneApex());
		dzi2.setScsHostname(MockZonePartitionIdInstaller.SCS_S1);
		Mockito.when(mockDomainZoneResolutionService.resolveDomain(domain1.getDomainName())).thenReturn(dzi2);

		// channel
		Channel channel = new Channel();
		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain("tdmx.kidsmathstrainer.com");
		origin.setLocalname("user1");
		channel.setOrigin(origin);

		ChannelDestination dest = new ChannelDestination();
		dest.setDomain(domain1.getDomainName());
		dest.setLocalname(address1.getLocalName());
		dest.setServicename(service1.getServiceName());
		channel.setDestination(dest);

		GetMRSSession req = new GetMRSSession();
		req.setChannel(channel);

		GetMRSSessionResponse response = scs.getMRSSession(req);
		assertError(ErrorCode.DnsZoneApexMissing, response);
	}

	@Test
	public void test_getMRSSession_RequestToSend() throws Exception {
		byte[] pkixKeystoreContents = FileUtils.getFileContents("src/test/resources/pkixtest.keystore");

		PKIXCredential pkixCred = KeyStoreUtils.getPrivateCredential(pkixKeystoreContents, "jks", "changeme",
				"kidsmathstrainer");
		authenticatedClientService.setAuthenticatedClient(pkixCred.getPublicCert());

		DomainZoneApexInfo dzi1 = new DomainZoneApexInfo();
		dzi1.setDomainName("tdmx.kidsmathstrainer.com");
		dzi1.setZoneApex("kidsmathstrainer.com");
		dzi1.setScsHostname("default.scs.kidsmathstrainer.com");
		Mockito.when(mockDomainZoneResolutionService.resolveDomain("tdmx.kidsmathstrainer.com")).thenReturn(dzi1);

		DomainZoneApexInfo dzi2 = new DomainZoneApexInfo();
		dzi2.setDomainName(domain1.getDomainName());
		dzi2.setZoneApex(zone.getZoneApex());
		dzi2.setScsHostname(MockZonePartitionIdInstaller.SCS_S1);
		Mockito.when(mockDomainZoneResolutionService.resolveDomain(domain1.getDomainName())).thenReturn(dzi2);

		// channel
		Channel channel = new Channel();
		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain("tdmx.kidsmathstrainer.com");
		origin.setLocalname("user1");
		channel.setOrigin(origin);

		ChannelDestination dest = new ChannelDestination();
		dest.setDomain(domain1.getDomainName());
		dest.setLocalname(address1.getLocalName());
		dest.setServicename(service1.getServiceName());
		channel.setDestination(dest);

		GetMRSSession req = new GetMRSSession();
		req.setChannel(channel);

		GetMRSSessionResponse response = scs.getMRSSession(req);
		assertSuccess(response);

		assertNotNull(response.getSession());
		assertEquals(domain1.getDomainName(), response.getSession().getDomain());
		assertEquals(address1.getLocalName(), response.getSession().getLocalname());
		assertEquals(service1.getServiceName(), response.getSession().getServicename());
		assertNotNull(response.getSession().getServiceprovider());
		assertNotNull(response.getSession().getSessionId());

		assertNotNull(response.getEndpoint());

	}

	@Test
	public void test_getMRSSession_RequestToReceive() throws Exception {
		byte[] pkixKeystoreContents = FileUtils.getFileContents("src/test/resources/pkixtest.keystore");

		PKIXCredential pkixCred = KeyStoreUtils.getPrivateCredential(pkixKeystoreContents, "jks", "changeme",
				"kidsmathstrainer");
		authenticatedClientService.setAuthenticatedClient(pkixCred.getPublicCert());

		DomainZoneApexInfo dzi1 = new DomainZoneApexInfo();
		dzi1.setDomainName("tdmx.kidsmathstrainer.com");
		dzi1.setZoneApex("kidsmathstrainer.com");
		dzi1.setScsHostname("default.scs.kidsmathstrainer.com");
		Mockito.when(mockDomainZoneResolutionService.resolveDomain("tdmx.kidsmathstrainer.com")).thenReturn(dzi1);

		DomainZoneApexInfo dzi2 = new DomainZoneApexInfo();
		dzi2.setDomainName(domain1.getDomainName());
		dzi2.setZoneApex(zone.getZoneApex());
		dzi2.setScsHostname(MockZonePartitionIdInstaller.SCS_S1);
		Mockito.when(mockDomainZoneResolutionService.resolveDomain(domain1.getDomainName())).thenReturn(dzi2);

		// channel
		Channel channel = new Channel();
		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain(domain1.getDomainName());
		origin.setLocalname(address1.getLocalName());
		channel.setOrigin(origin);

		ChannelDestination dest = new ChannelDestination();
		dest.setDomain("tdmx.kidsmathstrainer.com");
		dest.setLocalname("user1");
		dest.setServicename("mathsservice");
		channel.setDestination(dest);

		GetMRSSession req = new GetMRSSession();
		req.setChannel(channel);

		GetMRSSessionResponse response = scs.getMRSSession(req);
		assertSuccess(response);

		assertNotNull(response.getSession());
		assertEquals(domain1.getDomainName(), response.getSession().getDomain());
		assertEquals(address1.getLocalName(), response.getSession().getLocalname());
		assertNull(response.getSession().getServicename());
		assertNotNull(response.getSession().getServiceprovider());
		assertNotNull(response.getSession().getSessionId());

		assertNotNull(response.getEndpoint());
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
