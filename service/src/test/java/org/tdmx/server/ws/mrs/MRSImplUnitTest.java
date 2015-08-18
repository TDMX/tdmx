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
package org.tdmx.server.ws.mrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
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
import org.tdmx.core.api.v01.mrs.Relay;
import org.tdmx.core.api.v01.mrs.RelayResponse;
import org.tdmx.core.api.v01.mrs.ws.MRS;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelDestination;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Grant;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.message.domain.MessageFacade;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DestinationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.MockZonePartitionIdInstaller;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedClientService;
import org.tdmx.server.ws.session.ApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory;
import org.tdmx.server.ws.session.WebServiceSessionManager;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MRSImplUnitTest {

	private static final Logger log = LoggerFactory.getLogger(MRSImplUnitTest.class);

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private AgentCredentialService agentCredentialService;
	@Autowired
	private AgentCredentialFactory agentCredentialFactory;

	@Autowired
	@Named("ws.MRS.SessionFactory")
	private WebServiceSessionFactory<MRSServerSession> serverSessionFactory;
	@Autowired
	private AuthenticatedClientService authenticatedClientService;
	@Autowired
	@Named("ws.MRS.ServerSessionManager")
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
	@Named("ws.MRS")
	private MRS mrs;

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

	private final String ZAC_SESSION_ID = "ZAC-1";
	private final String DAC_SESSION_ID = "DAC-1";

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

		Map<SeedAttribute, Long> seedAttributeMap = new HashMap<>();
		seedAttributeMap.put(SeedAttribute.AccountZoneId, accountZone.getId());
		seedAttributeMap.put(SeedAttribute.ZoneId, zone.getId());
		seedAttributeMap.put(SeedAttribute.DomainId, domain2.getId());
		seedAttributeMap.put(SeedAttribute.ChannelId, recvAuth2.getChannel().getId());

		serverSessionManager.createSession(ZAC_SESSION_ID, zac.getPublicCert(), seedAttributeMap);

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

		assertEquals(ApiName.MRS, serverSessionManager.getApiName());

		// the service under test...
		assertNotNull(mrs);
	}

	@Test
	public void testRelay_ChannelAuthorization_ReqSendSetAtOriginRelayedToDest() {
		// channel
		Channel channel = new Channel();
		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain(domain2.getDomainName());
		origin.setLocalname(address2.getLocalName());
		channel.setOrigin(origin);

		ChannelDestination dest = new ChannelDestination();
		dest.setDomain(domain1.getDomainName());
		dest.setLocalname(address1.getLocalName());
		dest.setServicename(service1.getServiceName());
		channel.setDestination(dest);

		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {
			TemporaryChannel tc = new TemporaryChannel(domain1, a2d.mapChannelOrigin(origin),
					a2d.mapChannelDestination(dest));
			channelService.create(tc);

			Map<SeedAttribute, Long> firstAttributeMap = new HashMap<>();
			firstAttributeMap.put(SeedAttribute.AccountZoneId, accountZone.getId());
			firstAttributeMap.put(SeedAttribute.ZoneId, zone.getId());
			firstAttributeMap.put(SeedAttribute.DomainId, domain1.getId());
			firstAttributeMap.put(SeedAttribute.TemporaryChannelId, tc.getId());

			serverSessionManager.createSession(DAC_SESSION_ID, dac2.getPublicCert(), firstAttributeMap);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		authenticatedClientService.setAuthenticatedClient(dac2.getPublicCert());

		Permission auth = new Permission();

		Date oneMonth = CalendarUtils.getDateWithOffset(new Date(), Calendar.MONTH, 1);
		auth.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		auth.setPermission(Grant.ALLOW);
		auth.setValidUntil(CalendarUtils.cast(oneMonth));
		SignatureUtils.createEndpointPermissionSignature(dac2, SignatureAlgorithm.SHA_256_RSA, new Date(), channel,
				auth);
		// signer is origin, so reqSend at destination

		Relay req = new Relay();
		req.setSessionId(DAC_SESSION_ID);

		req.setPermission(auth);

		RelayResponse response = mrs.relay(req);
		assertSuccess(response);

		// check the CA exists and reqSend is set.
		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {

			ChannelAuthorizationSearchCriteria casc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
			casc.setDomainName(domain1.getDomainName());
			casc.getDestination().setDomainName(domain1.getDomainName());
			casc.getDestination().setLocalName(address1.getLocalName());
			casc.getDestination().setServiceName(service1.getServiceName());
			List<org.tdmx.lib.zone.domain.Channel> channels = channelService.search(zone, casc);
			assertEquals(1, channels.size());
			org.tdmx.lib.zone.domain.Channel c = channels.get(0);
			assertNull(c.getAuthorization().getSendAuthorization());
			assertNull(c.getAuthorization().getRecvAuthorization());
			assertNull(c.getAuthorization().getReqRecvAuthorization());
			assertNotNull(c.getAuthorization().getReqSendAuthorization());

			// test that the temp channel is deleted
			assertNull(channelService.findByTemporaryChannel(zone, domain1, a2d.mapChannelOrigin(origin),
					a2d.mapChannelDestination(dest)));

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
	}

	@Test
	public void testRelay_ChannelAuthorization_ReqRecvSetAtDestRelayedToOrigin() {
		Channel channel = new Channel();
		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain(domain2.getDomainName());
		origin.setLocalname(address2.getLocalName());
		channel.setOrigin(origin);

		ChannelDestination dest = new ChannelDestination();
		dest.setDomain(domain1.getDomainName());
		dest.setLocalname(address1.getLocalName());
		dest.setServicename(service1.getServiceName());
		channel.setDestination(dest);

		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {
			TemporaryChannel tc = new TemporaryChannel(domain2, a2d.mapChannelOrigin(origin),
					a2d.mapChannelDestination(dest));
			channelService.create(tc);

			Map<SeedAttribute, Long> firstAttributeMap = new HashMap<>();
			firstAttributeMap.put(SeedAttribute.AccountZoneId, accountZone.getId());
			firstAttributeMap.put(SeedAttribute.ZoneId, zone.getId());
			firstAttributeMap.put(SeedAttribute.DomainId, domain2.getId());
			firstAttributeMap.put(SeedAttribute.TemporaryChannelId, tc.getId());

			serverSessionManager.createSession(DAC_SESSION_ID, dac1.getPublicCert(), firstAttributeMap);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		authenticatedClientService.setAuthenticatedClient(dac1.getPublicCert());

		Permission auth = new Permission();

		Date oneMonth = CalendarUtils.getDateWithOffset(new Date(), Calendar.MONTH, 1);
		auth.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		auth.setPermission(Grant.ALLOW);
		auth.setValidUntil(CalendarUtils.cast(oneMonth));
		SignatureUtils.createEndpointPermissionSignature(dac1, SignatureAlgorithm.SHA_256_RSA, new Date(), channel,
				auth);
		// signer is destination, so reqRecv at origin

		Relay req = new Relay();
		req.setSessionId(DAC_SESSION_ID);

		req.setPermission(auth);

		RelayResponse response = mrs.relay(req);
		assertSuccess(response);

		// check CA exists and that the authorization is set as a reqRecv by someother.

		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {

			ChannelAuthorizationSearchCriteria casc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
			casc.setDomainName(domain2.getDomainName());
			casc.getOrigin().setDomainName(domain2.getDomainName());
			casc.getOrigin().setLocalName(address2.getLocalName());
			List<org.tdmx.lib.zone.domain.Channel> channels = channelService.search(zone, casc);
			assertEquals(1, channels.size());
			org.tdmx.lib.zone.domain.Channel c = channels.get(0);
			assertNull(c.getAuthorization().getSendAuthorization());
			assertNull(c.getAuthorization().getRecvAuthorization());
			assertNotNull(c.getAuthorization().getReqRecvAuthorization());
			assertNull(c.getAuthorization().getReqSendAuthorization());

			// test that the temp channel is deleted
			assertNull(channelService.findByTemporaryChannel(zone, domain2, a2d.mapChannelOrigin(origin),
					a2d.mapChannelDestination(dest)));

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
	}

	@Test
	public void testRelay_ChannelDestination() {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		Channel channel = d2a.mapChannel(recvAuth2.getChannel());

		assertEquals(channel.getDestination().getDomain(), domain2.getDomainName());
		assertEquals(channel.getDestination().getLocalname(), address2.getLocalName());
		assertEquals(channel.getDestination().getServicename(), service2.getServiceName());

		Destinationsession ds = new Destinationsession();
		ds.setEncryptionContextId("id1");
		ds.setScheme("scheme");
		ds.setSessionKey(new byte[] { 1, 2, 3 });

		SignatureUtils.createDestinationSessionSignature(uc2, SignatureAlgorithm.SHA_256_RSA, new Date(), channel
				.getDestination().getServicename(), ds);
		// signer is destination, so reqRecv at origin

		Relay req = new Relay();
		req.setSessionId(ZAC_SESSION_ID);

		req.setDestinationsession(ds);

		RelayResponse response = mrs.relay(req);
		assertSuccess(response);

		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {

			// check CA exists and that the authorization is set as a reqRecv by someother.
			ChannelAuthorizationSearchCriteria casc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
			casc.setDomainName(domain2.getDomainName());
			casc.getOrigin().setDomainName(domain1.getDomainName());
			casc.getOrigin().setLocalName(address1.getLocalName());
			List<org.tdmx.lib.zone.domain.Channel> channels = channelService.search(zone, casc);
			assertEquals(1, channels.size());
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		// assert a second time relay of the same info is ok too.
		req = new Relay();
		req.setSessionId(ZAC_SESSION_ID);

		req.setDestinationsession(ds);

		response = mrs.relay(req);
		assertSuccess(response);

		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {

			// check CA exists and that the authorization is set as a reqRecv by someother.
			ChannelAuthorizationSearchCriteria casc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
			// check CA exists and that the authorization is set as a reqRecv by someother.
			casc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
			casc.setDomainName(domain2.getDomainName());
			casc.getOrigin().setDomainName(domain1.getDomainName());
			casc.getOrigin().setLocalName(address1.getLocalName());
			List<org.tdmx.lib.zone.domain.Channel> channels = channelService.search(zone, casc);
			assertEquals(1, channels.size());
			org.tdmx.lib.zone.domain.Channel c = channels.get(0);
			assertEquals(ds.getUsersignature().getSignaturevalue().getSignature(), c.getSession().getSignature()
					.getValue());
			// TODO check other ds values are set on the channel.
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

	}

	@Test
	public void testRelay_Message() throws Exception {
		authenticatedClientService.setAuthenticatedClient(zac.getPublicCert());

		Channel channel = d2a.mapChannel(recvAuth2.getChannel());

		assertEquals(channel.getDestination().getDomain(), domain2.getDomainName());
		assertEquals(channel.getDestination().getLocalname(), address2.getLocalName());
		assertEquals(channel.getDestination().getServicename(), service2.getServiceName());

		Msg msg = MessageFacade.createMsg(uc1, uc2, service2.getServiceName());

		Relay req = new Relay();
		req.setSessionId(ZAC_SESSION_ID);

		req.setMsg(msg);

		RelayResponse response = mrs.relay(req);
		assertSuccess(response);
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
