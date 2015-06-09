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
import java.util.List;

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
import org.tdmx.core.api.v01.msg.Authorization;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Destination;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.FlowTargetService;
import org.tdmx.lib.zone.service.MockZonePartitionIdInstaller;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AgentCredentialAuthorizationService.AuthorizationResult;
import org.tdmx.server.ws.security.service.AuthenticatedAgentService;

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
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	@Autowired
	private AuthenticatedAgentService authenticatedAgentService;

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
	private FlowTargetService flowTargetService;

	@Autowired
	private MRS mrs;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;

	private Zone zone;
	private org.tdmx.lib.zone.domain.Domain domain1;
	private org.tdmx.lib.zone.domain.Domain domain2;
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

	// private String localName;
	// private String serviceName;
	// private String domainName;

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
		uc1 = data.getDomains().get(0).getAddresses().get(0).getUcs().get(0).getCredential();
		uc2 = data.getDomains().get(1).getAddresses().get(0).getUcs().get(0).getCredential();

		// ZAS should use the "authenticated agent" to set the partitionID
	}

	@After
	public void doTeardown() {
		authenticatedAgentService.clearAuthenticatedAgent();

		dataGenerator.tearDown(input, data);
	}

	@Test
	public void testAutowired() {
		assertNotNull(zoneService);
		assertNotNull(agentCredentialService);
		assertNotNull(agentCredentialFactory);
		assertNotNull(authenticatedAgentService);
		assertNotNull(domainService);
		assertNotNull(addressService);

		// the service under test...
		assertNotNull(mrs);
	}

	@Test
	public void testRelay_ChannelAuthorization_ReqSend() {
		Channel channel = new Channel();
		Destination dest = new Destination();
		dest.setDomain(domain1.getDomainName());
		dest.setLocalname(address1.getLocalName());
		dest.setServicename(service1.getServiceName());
		dest.setServiceprovider("SP"); // TODO
		channel.setDestination(dest);

		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain(domain2.getDomainName());
		origin.setLocalname(address2.getLocalName());
		origin.setServiceprovider("SP");
		channel.setOrigin(origin);

		Authorization auth = new Authorization();
		auth.setChannel(channel);

		Date oneMonth = CalendarUtils.getDateWithOffset(new Date(), Calendar.MONTH, 1);
		auth.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		auth.setPermission(Permission.ALLOW);
		auth.setValidUntil(CalendarUtils.getDateTime(oneMonth));
		SignatureUtils.createEndpointPermissionSignature(dac2, SignatureAlgorithm.SHA_256_RSA, new Date(), channel,
				auth);
		// signer is origin, so reqSend at destination

		Relay req = new Relay();
		req.setAuthorization(auth);

		RelayResponse response = mrs.relay(req);
		assertSuccess(response);

		// check the CA exists and reqSend is set.
		AuthorizationResult r = new AuthorizationResult(dac1.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ChannelAuthorizationSearchCriteria casc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
		casc.setDomainName(domain1.getDomainName());
		casc.getDestination().setDomainName(domain1.getDomainName());
		casc.getDestination().setLocalName(address1.getLocalName());
		casc.getDestination().setServiceName(service1.getServiceName());
		List<ChannelAuthorization> channelAuths = channelService.search(zone, casc);
		assertEquals(1, channelAuths.size());
		ChannelAuthorization ca = channelAuths.get(0);
		assertNull(ca.getSendAuthorization());
		assertNull(ca.getRecvAuthorization());
		assertNull(ca.getReqRecvAuthorization());
		assertNotNull(ca.getReqSendAuthorization());
	}

	@Test
	public void testRelay_ChannelAuthorization_ReqRecv() {
		Channel channel = new Channel();
		Destination dest = new Destination();
		dest.setDomain(domain1.getDomainName());
		dest.setLocalname(address1.getLocalName());
		dest.setServicename(service1.getServiceName());
		dest.setServiceprovider("SP"); // TODO
		channel.setDestination(dest);

		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain(domain2.getDomainName());
		origin.setLocalname(address2.getLocalName());
		origin.setServiceprovider("SP");
		channel.setOrigin(origin);

		Authorization auth = new Authorization();
		auth.setChannel(channel);

		Date oneMonth = CalendarUtils.getDateWithOffset(new Date(), Calendar.MONTH, 1);
		auth.setMaxPlaintextSizeBytes(ZoneFacade.ONE_GB);
		auth.setPermission(Permission.ALLOW);
		auth.setValidUntil(CalendarUtils.getDateTime(oneMonth));
		SignatureUtils.createEndpointPermissionSignature(dac1, SignatureAlgorithm.SHA_256_RSA, new Date(), channel,
				auth);
		// signer is destination, so reqRecv at origin

		Relay req = new Relay();
		req.setAuthorization(auth);

		RelayResponse response = mrs.relay(req);
		assertSuccess(response);

		// check CA exists and that the authorization is set as a reqRecv by someother.
		AuthorizationResult r = new AuthorizationResult(dac2.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ChannelAuthorizationSearchCriteria casc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
		casc.setDomainName(domain2.getDomainName());
		casc.getOrigin().setDomainName(domain2.getDomainName());
		casc.getOrigin().setLocalName(address2.getLocalName());
		List<ChannelAuthorization> channelAuths = channelService.search(zone, casc);
		assertEquals(1, channelAuths.size());
		ChannelAuthorization ca = channelAuths.get(0);
		assertNull(ca.getSendAuthorization());
		assertNull(ca.getRecvAuthorization());
		assertNotNull(ca.getReqRecvAuthorization());
		assertNull(ca.getReqSendAuthorization());
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

	private void removeFlowTargets(Domain domain) {
		// delete any FlowTarget on the domain
		org.tdmx.lib.zone.domain.FlowTargetSearchCriteria ftSc = new org.tdmx.lib.zone.domain.FlowTargetSearchCriteria(
				new PageSpecifier(0, 1000));
		ftSc.getTarget().setDomainName(domain.getDomainName());
		List<FlowTarget> ftlist = flowTargetService.search(zone, ftSc);
		for (FlowTarget ft : ftlist) {
			flowTargetService.delete(ft);
		}
	}

	private void removeChannelAuthorizations(Domain domain) {
		// delete any ChannelAuthorizations on the domain
		org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria caSc = new org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria(
				new PageSpecifier(0, 1000));
		caSc.setDomainName(domain.getDomainName());
		List<ChannelAuthorization> calist = channelService.search(zone, caSc);
		for (ChannelAuthorization ca : calist) {
			channelService.delete(ca.getChannel());
		}
	}

	private void removeAgentCredentials(Domain domain) {
		// delete any UC+DAC on the domain
		org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria dacSc = new org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria(
				new PageSpecifier(0, 1000));
		dacSc.setDomainName(domain.getDomainName());
		List<AgentCredential> list = agentCredentialService.search(zone, dacSc);
		for (AgentCredential ac : list) {
			agentCredentialService.delete(ac);
		}
	}

	private void removeAddresses(Domain domain) {
		// delete any Address on the domain
		org.tdmx.lib.zone.domain.AddressSearchCriteria adSc = new org.tdmx.lib.zone.domain.AddressSearchCriteria(
				new PageSpecifier(0, 1000));
		adSc.setDomainName(domain.getDomainName());
		List<org.tdmx.lib.zone.domain.Address> addresses = addressService.search(zone, adSc);
		for (org.tdmx.lib.zone.domain.Address ad : addresses) {
			addressService.delete(ad);
		}
	}

	private void removeServices(Domain domain) {
		// delete any services on the domain
		org.tdmx.lib.zone.domain.ServiceSearchCriteria sSc = new org.tdmx.lib.zone.domain.ServiceSearchCriteria(
				new PageSpecifier(0, 1000));
		sSc.setDomainName(domain.getDomainName());
		List<org.tdmx.lib.zone.domain.Service> services = serviceService.search(zone, sSc);
		for (org.tdmx.lib.zone.domain.Service s : services) {
			serviceService.delete(s);
		}
	}
}
