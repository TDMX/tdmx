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
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.mds.GetAddress;
import org.tdmx.core.api.v01.mds.GetAddressResponse;
import org.tdmx.core.api.v01.mds.GetFlowTarget;
import org.tdmx.core.api.v01.mds.GetFlowTargetResponse;
import org.tdmx.core.api.v01.mds.ListChannelAuthorization;
import org.tdmx.core.api.v01.mds.ListChannelAuthorizationResponse;
import org.tdmx.core.api.v01.mds.ListFlow;
import org.tdmx.core.api.v01.mds.ListFlowResponse;
import org.tdmx.core.api.v01.mds.SetFlowTargetSession;
import org.tdmx.core.api.v01.mds.SetFlowTargetSessionResponse;
import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.msg.Flowsession;
import org.tdmx.core.api.v01.msg.Flowtarget;
import org.tdmx.core.api.v01.msg.Flowtargetsession;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.Zone;
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
public class MDSImplUnitTest {

	private static final Logger log = LoggerFactory.getLogger(MDSImplUnitTest.class);

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private AgentCredentialService agentCredentialService;
	@Autowired
	private AgentCredentialFactory agentCredentialFactory;
	@Autowired
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;

	@Autowired
	private ZoneService zoneService;
	@Autowired
	private AuthenticatedAgentService authenticatedAgentService;
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

	// private String localName;
	// private String serviceName;
	// private String domainName;

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
		assertNotNull(mds);
	}

	@Test
	public void testGetAddress() {
		AuthorizationResult r = new AuthorizationResult(uc.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		GetAddress req = new GetAddress();

		GetAddressResponse response = mds.getAddress(req);
		assertSuccess(response);
		assertNotNull(response.getDestination());
		assertEquals(domain.getDomainName(), response.getDestination().getDomain());
		// TODO others
	}

	@Test
	public void testListChannelAuthorization_All() {
		AuthorizationResult r = new AuthorizationResult(uc.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ListChannelAuthorization req = new ListChannelAuthorization();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		req.setServicename(service.getServiceName());
		// TODO test without svc name fails

		ListChannelAuthorizationResponse response = mds.listChannelAuthorization(req);
		assertSuccess(response);

		// TODO others
		assertEquals(1, response.getChannelauthorizations().size());
	}

	@Test
	public void testListFlow_All() {
		AuthorizationResult r = new AuthorizationResult(uc.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		ListFlow req = new ListFlow();

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		req.setPage(p);
		req.setServicename(service.getServiceName());

		ListFlowResponse response = mds.listFlow(req);
		assertSuccess(response);

		// TODO others
		assertEquals(1, response.getFlows().size());
	}

	@Test
	public void testSetFlowTargetSession() {
		AuthorizationResult r = new AuthorizationResult(uc.getPublicCert(), accountZone, zone);
		authenticatedAgentService.setAuthenticatedAgent(r);

		SetFlowTargetSession req = new SetFlowTargetSession();
		req.setServicename(service.getServiceName());

		Flowtargetsession fts = new Flowtargetsession();
		Flowsession fs1 = new Flowsession();
		fs1.setFlowsessionId("id1");
		fs1.setValidFrom(Calendar.getInstance());
		fs1.setSessionKey(new byte[] { 1, 2, 3 });
		fs1.setScheme("scheme-name");
		fts.getFlowsessions().add(fs1);

		Flowtarget ft = new Flowtarget();
		ft.setFlowtargetsession(fts);
		ft.setServicename(service.getServiceName());
		SignatureUtils.createFlowTargetSessionSignature(uc, SignatureAlgorithm.SHA_256_RSA, new Date(), ft);

		req.setFlowtargetsession(fts);

		SetFlowTargetSessionResponse response = mds.setFlowTargetSession(req);
		assertSuccess(response);
		// TODO others

		// do getFlowTarget to confirm FTS created
		GetFlowTarget getReq = new GetFlowTarget();
		getReq.setServicename(service.getServiceName());
		GetFlowTargetResponse getRes = mds.getFlowTarget(getReq);
		assertSuccess(getRes);
		assertNotNull(getRes.getFlowtarget());

		// tamper with signature doesn't work
		fts.getSignaturevalue().setSignature("gugus");
		response = mds.setFlowTargetSession(req);
		assertError(ErrorCode.InvalidSignatureFlowTargetSession, response);

		// check that the channelflowtargets are set
		boolean more = true;
		// fetch ALL Channels which have this FlowTarget as Destination.
		for (int pageNo = 0; more; pageNo++) {
			ChannelSearchCriteria sc = new ChannelSearchCriteria(new PageSpecifier(pageNo, 5));
			sc.setDomain(domain);
			sc.getDestination().setLocalName(uc.getPublicCert().getCommonName());
			sc.getDestination().setDomainName(domain.getDomainName());
			sc.getDestination().setServiceName(service.getServiceName());
			// sc.getDestination().setServiceProvider(authorizedUser.getTdmxZoneInfo().getMrsUrl()); TODO

			List<Channel> channels = channelService.search(zone, sc);
			for (Channel channel : channels) {
				log.info("" + channel);
			}
			if (channels.isEmpty()) {
				more = false;
			}
		}

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
