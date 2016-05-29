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
package org.tdmx.server.ws.mos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.IntegratedCryptoScheme;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.ContinuedAcknowledge;
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.mos.GetAddress;
import org.tdmx.core.api.v01.mos.GetAddressResponse;
import org.tdmx.core.api.v01.mos.GetChannel;
import org.tdmx.core.api.v01.mos.GetChannelResponse;
import org.tdmx.core.api.v01.mos.ListChannel;
import org.tdmx.core.api.v01.mos.ListChannelResponse;
import org.tdmx.core.api.v01.mos.Submit;
import org.tdmx.core.api.v01.mos.SubmitResponse;
import org.tdmx.core.api.v01.mos.Upload;
import org.tdmx.core.api.v01.mos.UploadResponse;
import org.tdmx.core.api.v01.mos.ws.MOS;
import org.tdmx.core.api.v01.msg.ChannelDestination;
import org.tdmx.core.api.v01.msg.ChannelDestinationFilter;
import org.tdmx.core.api.v01.msg.Chunk;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.tx.LocalTransactionSpecification;
import org.tdmx.core.api.v01.tx.Transaction;
import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.message.domain.MessageFacade;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.MessageState;
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
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.ros.client.RelayClientService;
import org.tdmx.server.ros.client.RelayStatus;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedClientService;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory;
import org.tdmx.server.ws.session.WebServiceSessionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MOSImplUnitTest {

	private static final Logger log = LoggerFactory.getLogger(MOSImplUnitTest.class);

	@Autowired
	private TestDataGenerator dataGenerator;
	@Autowired
	private AgentCredentialService agentCredentialService;
	@Autowired
	private AgentCredentialFactory agentCredentialFactory;

	@Autowired
	@Named("ws.MOS.SessionFactory")
	private WebServiceSessionFactory<MOSServerSession> serverSessionFactory;
	@Autowired
	@Named("ws.authenticatedClientService")
	private AuthenticatedClientService authenticatedClientService;
	@Autowired
	@Named("ws.MOS.ServerSessionManager")
	private WebServiceSessionManager serverSessionManager;

	@Autowired
	private RelayClientService mockRelayClientService;

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
	@Named("ws.MOS")
	private MOS mos;

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

		Map<AttributeId, Long> seedAttributeMap = new HashMap<>();
		seedAttributeMap.put(AttributeId.AccountZoneId, accountZone.getId());
		seedAttributeMap.put(AttributeId.ZoneId, zone.getId());
		seedAttributeMap.put(AttributeId.DomainId, domain.getId());
		seedAttributeMap.put(AttributeId.AddressId, address.getId());

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

		assertNotNull(zoneService);
		assertNotNull(agentCredentialService);
		assertNotNull(agentCredentialFactory);
		assertNotNull(domainService);
		assertNotNull(addressService);

		assertEquals(WebServiceApiName.MOS, serverSessionManager.getApiName());
		// the service under test...
		assertNotNull(mos);
	}

	@Test
	public void testGetAddress() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		GetAddress req = new GetAddress();
		req.setSessionId(UC_SESSION_ID);

		GetAddressResponse response = mos.getAddress(req);
		assertSuccess(response);
		assertNotNull(response.getOrigin());
		assertEquals(domain.getDomainName(), response.getOrigin().getDomain());
		assertEquals(address.getLocalName(), response.getOrigin().getLocalname());
	}

	@Test
	public void testGetChannel() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		GetChannel req = new GetChannel();
		req.setSessionId(UC_SESSION_ID);

		ChannelDestination cd = new ChannelDestination();
		cd.setDomain(domain.getDomainName());
		cd.setLocalname(address.getLocalName());
		cd.setServicename(service.getServiceName());
		req.setDestination(cd);

		GetChannelResponse response = mos.getChannel(req);
		assertSuccess(response);
		assertNotNull(response.getChannelinfo());

		// TODO others
	}

	@Test
	public void testListChannel_All() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		ListChannel req = new ListChannel();
		req.setSessionId(UC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		// TODO test without svc name fails

		ListChannelResponse response = mos.listChannel(req);
		assertSuccess(response);

		assertEquals(1, response.getChannelinfos().size());
	}

	@Test
	public void testListChannel_AllExplicit() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		ListChannel req = new ListChannel();
		req.setSessionId(UC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ChannelDestinationFilter cdf = new ChannelDestinationFilter();
		cdf.setDomain(domain.getDomainName());
		cdf.setLocalname(address.getLocalName());
		cdf.setServicename(service.getServiceName());
		cdf.setDomain(domain.getDomainName());
		req.setDestination(cdf);

		ListChannelResponse response = mos.listChannel(req);
		assertSuccess(response);

		assertEquals(1, response.getChannelinfos().size());
	}

	@Test
	public void testListChannel_OnlyServiceExplicit() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		ListChannel req = new ListChannel();
		req.setSessionId(UC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ChannelDestinationFilter cdf = new ChannelDestinationFilter();
		cdf.setServicename(service.getServiceName());
		req.setDestination(cdf);

		ListChannelResponse response = mos.listChannel(req);
		assertSuccess(response);

		assertEquals(1, response.getChannelinfos().size());
	}

	@Test
	public void testSubmitSmallMessageAndUploadChunk_Tx() throws Exception {
		// create a 16MB chunk 0 data, 2MB chunk 1 data
		List<byte[]> chunks = new ArrayList<>();
		chunks.add(EntropySource.getRandomBytes(2 * EnumUtils.MB));

		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		Mockito.when(mockRelayClientService.relayChannelMessage(Mockito.anyString(), Mockito.any(AccountZone.class),
				Mockito.any(Zone.class), Mockito.any(Domain.class), Mockito.any(Channel.class),
				Mockito.any(MessageState.class))).thenReturn(RelayStatus.success("ck", "rosTcpAddress"));

		Submit req = new Submit();
		req.setSessionId(UC_SESSION_ID);
		Transaction tx = new Transaction();
		tx.setXid("txId:" + System.currentTimeMillis());
		tx.setTxtimeout(60);
		req.setTransaction(tx);

		Msg msg = MessageFacade.createMsg(uc, uc, service.getServiceName(),
				IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1, chunks);

		req.setMsg(msg);

		SubmitResponse response = mos.submit(req);
		assertSuccess(response, false);

		// the transaction is not committed, so it shouldnt yet be relayed.
		Mockito.verifyZeroInteractions(mockRelayClientService);
	}

	@Test
	public void testSubmitLargeMessageAndUploadChunk_NoTx() throws Exception {
		// create a 16MB chunk 0 data, 2MB chunk 1 data
		List<byte[]> chunks = new ArrayList<>();
		chunks.add(EntropySource.getRandomBytes(16 * EnumUtils.MB));
		chunks.add(EntropySource.getRandomBytes(2 * EnumUtils.MB));

		Msg msg = MessageFacade.createMsg(uc, uc, service.getServiceName(),
				IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1, chunks);

		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		Submit req = new Submit(); // local tx
		req.setSessionId(UC_SESSION_ID);

		LocalTransactionSpecification local = new LocalTransactionSpecification();
		local.setClientId("Client-" + System.currentTimeMillis());
		local.setTxtimeout(60);
		req.setLocaltransaction(local);

		req.setMsg(msg);

		SubmitResponse response = mos.submit(req);
		assertSuccess(response, true);
		Mockito.verifyZeroInteractions(mockRelayClientService);

		Chunk chunk = MessageFacade.createChunk(msg.getHeader().getMsgId(), 1,
				IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1, chunks.get(1)); // 2MB

		Mockito.when(mockRelayClientService.relayChannelMessage(Mockito.anyString(), Mockito.any(AccountZone.class),
				Mockito.any(Zone.class), Mockito.any(Domain.class), Mockito.any(Channel.class),
				Mockito.any(MessageState.class))).thenReturn(RelayStatus.success("ck", "rosTcpAddress"));

		Upload upl = new Upload();
		upl.setSessionId(UC_SESSION_ID);

		upl.setContinuation(response.getContinuation());
		upl.setChunk(chunk);

		UploadResponse uplResponse = mos.upload(upl);
		assertSuccess(uplResponse, false); // final chunk has no continuationId

		// because we have no transaction, the receipt of the final chunk initiates relay
		Mockito.verify(mockRelayClientService).relayChannelMessage(Mockito.anyString(), Mockito.any(AccountZone.class),
				Mockito.any(Zone.class), Mockito.any(Domain.class), Mockito.any(Channel.class),
				Mockito.any(MessageState.class));
	}

	@Test
	public void testSubmitLargeMessageAndUploadChunk_Tx() throws Exception {
		// create a 16MB chunk 0 data, 2MB chunk 1 data
		List<byte[]> chunks = new ArrayList<>();
		chunks.add(EntropySource.getRandomBytes(16 * EnumUtils.MB));
		chunks.add(EntropySource.getRandomBytes(2 * EnumUtils.MB));

		Msg msg = MessageFacade.createMsg(uc, uc, service.getServiceName(),
				IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1, chunks);

		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		Submit req = new Submit();
		req.setSessionId(UC_SESSION_ID);
		req.setMsg(msg);

		Transaction tx = new Transaction();
		tx.setXid("txId:" + System.currentTimeMillis());
		tx.setTxtimeout(60);
		req.setTransaction(tx);

		SubmitResponse response = mos.submit(req);
		assertSuccess(response, true);
		Mockito.verifyZeroInteractions(mockRelayClientService);

		Chunk chunk = MessageFacade.createChunk(msg.getHeader().getMsgId(), 1,
				IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1, chunks.get(1)); // 2MB

		Upload upl = new Upload();
		upl.setSessionId(UC_SESSION_ID);

		upl.setContinuation(response.getContinuation());
		upl.setChunk(chunk);

		UploadResponse uplResponse = mos.upload(upl);
		assertSuccess(uplResponse, false); // final chunk has no continuationId

		Mockito.verifyZeroInteractions(mockRelayClientService);

		// TODO need to go further with commit and check that it's relayed then.
	}

	// TODO test 2pc prepare, commit ( happy-path )

	// TODO test 2pc prepare, recover (find xa), commit

	// TODO test 2pc prepare, rollback, no-recover possible

	// TODO test 2pc prepare, recover (find xa), rollback

	private void assertSuccess(ContinuedAcknowledge ack, boolean hasContinuation) {
		assertNotNull(ack);
		String errorDesc = ack.getError() != null ? ack.getError().getDescription() : "ok";
		assertTrue("Error " + errorDesc, ack.isSuccess());
		if (hasContinuation) {
			assertNotNull(ack.getContinuation());
		} else {
			assertNull(ack.getContinuation());
		}
		assertNull(ack.getError());
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
