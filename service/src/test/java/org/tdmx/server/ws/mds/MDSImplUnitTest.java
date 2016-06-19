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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.IntegratedCryptoScheme;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.common.Acknowledge;
import org.tdmx.core.api.v01.common.Page;
import org.tdmx.core.api.v01.mds.AcknowledgeResponse;
import org.tdmx.core.api.v01.mds.Download;
import org.tdmx.core.api.v01.mds.DownloadResponse;
import org.tdmx.core.api.v01.mds.GetDestinationSession;
import org.tdmx.core.api.v01.mds.GetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.ListChannel;
import org.tdmx.core.api.v01.mds.ListChannelResponse;
import org.tdmx.core.api.v01.mds.Receive;
import org.tdmx.core.api.v01.mds.ReceiveResponse;
import org.tdmx.core.api.v01.mds.SetDestinationSession;
import org.tdmx.core.api.v01.mds.SetDestinationSessionResponse;
import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.msg.ChannelEndpointFilter;
import org.tdmx.core.api.v01.msg.Channelinfo;
import org.tdmx.core.api.v01.msg.Chunk;
import org.tdmx.core.api.v01.msg.ChunkReference;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.tx.Commit;
import org.tdmx.core.api.v01.tx.CommitResponse;
import org.tdmx.core.api.v01.tx.Localtransaction;
import org.tdmx.core.api.v01.tx.Prepare;
import org.tdmx.core.api.v01.tx.PrepareResponse;
import org.tdmx.core.api.v01.tx.Transaction;
import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.lib.chunk.domain.MessageFacade;
import org.tdmx.lib.chunk.service.ChunkService;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.control.service.MockDatabasePartitionInstaller;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.MessageStatus;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DestinationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;
import org.tdmx.server.ws.security.service.AuthenticatedClientService;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory;
import org.tdmx.server.ws.session.WebServiceSessionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/tdmx/test-context.xml")
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
	@Named("tdmx.lib.zone.ThreadLocalPartitionIdProvider")
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
	private ChunkService chunkService;
	@Autowired
	private DestinationService destinationService;

	private final DomainToApiMapper d2a = new DomainToApiMapper();
	private final ApiToDomainMapper a2d = new ApiToDomainMapper();

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
	private final String CLIENT_ID = "client-1";
	private final String XID = "xid-1";

	@Before
	public void doSetup() throws Exception {

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockDatabasePartitionInstaller.ZP1_S1);
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
		seedAttributeMap.put(AttributeId.ServiceId, service.getId());
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

		assertEquals(1, response.getChannelinfos().size());
		Channelinfo ch = response.getChannelinfos().get(0);
		assertNotNull(ch.getChannelauthorization());
		assertNotNull(ch.getSessioninfo());
		assertNotNull(ch.getStatus());
	}

	@Test
	public void testListChannelAuthorization_OriginDomain() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		ListChannel req = new ListChannel();
		req.setSessionId(UC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ChannelEndpointFilter filter = new ChannelEndpointFilter();
		filter.setDomain(domain.getDomainName());
		req.setOrigin(filter);

		ListChannelResponse response = mds.listChannel(req);
		assertSuccess(response);

		assertEquals(1, response.getChannelinfos().size());
	}

	@Test
	public void testListChannelAuthorization_OriginAll() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		ListChannel req = new ListChannel();
		req.setSessionId(UC_SESSION_ID);

		Page p = new Page();
		p.setNumber(0);
		p.setSize(10);
		req.setPage(p);

		ChannelEndpointFilter filter = new ChannelEndpointFilter();
		filter.setDomain(domain.getDomainName());
		filter.setLocalname(address.getLocalName());
		req.setOrigin(filter);

		ListChannelResponse response = mds.listChannel(req);
		assertSuccess(response);

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
		ds.setScheme(IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1.getName());

		SignatureUtils.createDestinationSessionSignature(uc, SignatureAlgorithm.SHA_256_RSA, new Date(),
				service.getServiceName(), ds);

		req.setDestinationsession(ds);

		SetDestinationSessionResponse response = mds.setDestinationSession(req);
		assertSuccess(response);

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
		List<Channel> channels = getDestinationChannels();
		for (Channel channel : channels) {
			log.info("" + channel);
			if (channel.getSession() != null) {
				setCft = true;
			}
		}
		assertTrue(setCft);
	}

	@Test
	public void testReceiveMessage_NoMsg_NoTx() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		Receive req = new Receive();
		req.setSessionId(UC_SESSION_ID);

		Localtransaction noTx = new Localtransaction();
		noTx.setClientId(CLIENT_ID);
		noTx.setTxtimeout(60);
		req.setLocaltransaction(noTx);

		req.setWaitTimeoutSec(5);
		ReceiveResponse res = mds.receive(req);
		assertSuccess(res);
		assertNull(res.getMsg());
		assertNull(res.getContinuation());
	}

	@Test
	public void testReceiveMessage_NoMsg_Tx() {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		Receive req = new Receive();
		req.setSessionId(UC_SESSION_ID);

		Transaction tx = new Transaction();
		tx.setXid(XID);
		tx.setTxtimeout(60);
		req.setTransaction(tx);

		req.setWaitTimeoutSec(5);
		ReceiveResponse res = mds.receive(req);
		assertSuccess(res);
		assertNull(res.getMsg());
		assertNull(res.getContinuation());
	}

	@Test
	public void testReceiveMessage_SmallMsg_NoTx() throws Exception {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		// 2MB chunk:0 data
		List<byte[]> chunks = new ArrayList<>();
		chunks.add(EntropySource.getRandomBytes(2 * EnumUtils.MB));

		Msg msg = MessageFacade.createMsg(uc, uc, service.getServiceName(),
				IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1, chunks);
		List<Channel> destinationChannels = getDestinationChannels();
		assertEquals(1, destinationChannels.size());

		ChannelMessage m = persistMessageForReceive(zone, msg, destinationChannels.get(0));
		org.tdmx.lib.chunk.domain.Chunk c = persistChunk(m, msg.getChunk());

		// now try and receive it.
		Receive req = new Receive();
		req.setSessionId(UC_SESSION_ID);
		req.setWaitTimeoutSec(5);

		Localtransaction noTx = new Localtransaction();
		noTx.setClientId(CLIENT_ID);
		noTx.setTxtimeout(60);
		req.setLocaltransaction(noTx);

		ReceiveResponse res = mds.receive(req);
		assertSuccess(res);
		assertNotNull(res.getMsg());
		assertNull(res.getContinuation());

		assertEquals(msg.getHeader().getMsgId(), res.getMsg().getHeader().getMsgId());
		assertNotNull(msg.getChunk());
		assertEquals(c.getMac(), msg.getChunk().getMac());
		assertArrayEquals(c.getData(), msg.getChunk().getData());

		// the message is not deleted until it is ACK'd
		assertNotNull(fetchMessage(m.getId()));
		assertNotNull(chunkService.fetchChunk(m, 0));

		// now try and ack the prior message - no new ones to recv.
		req.setMsgId(m.getMsgId());
		res = mds.receive(req);
		assertSuccess(res);
		assertNull(res.getMsg());
		assertNull(res.getContinuation());
		// the message is deleted because it is ACK'd
		assertNull(fetchMessage(m.getId()));
		assertNull(chunkService.fetchChunk(m, 0));
	}

	@Test
	public void testReceiveMessage_LargeMsg_NoTx() throws Exception {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		// 16MB chunk:0 data
		// 2MB chunk:1 data
		List<byte[]> chunks = new ArrayList<>();
		chunks.add(EntropySource.getRandomBytes(16 * EnumUtils.MB));
		chunks.add(EntropySource.getRandomBytes(2 * EnumUtils.MB));

		Msg msg = MessageFacade.createMsg(uc, uc, service.getServiceName(),
				IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1, chunks);
		List<Channel> destinationChannels = getDestinationChannels();
		assertEquals(1, destinationChannels.size());
		// persist chunk:0
		ChannelMessage m = persistMessageForReceive(zone, msg, destinationChannels.get(0));
		org.tdmx.lib.chunk.domain.Chunk c = persistChunk(m, msg.getChunk());
		// persist chunk:1
		Chunk chunk1 = MessageFacade.createChunk(m.getMsgId(), 1, m.getScheme(), chunks.get(1));
		org.tdmx.lib.chunk.domain.Chunk c1 = persistChunk(m, chunk1);

		// now try and receive msg+chunk:0.
		Receive req = new Receive();
		req.setSessionId(UC_SESSION_ID);
		req.setWaitTimeoutSec(5);

		Localtransaction noTx = new Localtransaction();
		noTx.setClientId(CLIENT_ID);
		noTx.setTxtimeout(60);
		req.setLocaltransaction(noTx);

		ReceiveResponse res = mds.receive(req);
		assertSuccess(res);
		assertNotNull(res.getMsg());
		assertNotNull(res.getContinuation()); // have next chunk

		assertEquals(msg.getHeader().getMsgId(), res.getMsg().getHeader().getMsgId());
		assertNotNull(msg.getChunk());
		assertEquals(c.getMac(), msg.getChunk().getMac());
		assertArrayEquals(c.getData(), msg.getChunk().getData());

		// the message is not deleted until it is ACK'd
		assertNotNull(fetchMessage(m.getId()));
		assertNotNull(chunkService.fetchChunk(m, 0));

		// now download the chunk:1
		Download downloadReq = new Download();
		downloadReq.setSessionId(UC_SESSION_ID);
		downloadReq.setContinuation(res.getContinuation());
		ChunkReference cr = new ChunkReference();
		cr.setMsgId(m.getMsgId());
		cr.setPos(1);
		downloadReq.setChunkref(cr);
		DownloadResponse downloadRes = mds.download(downloadReq);
		assertSuccess(downloadRes);
		assertNull(downloadRes.getContinuation());
		assertNotNull(downloadRes.getChunk());
		assertEquals(c1.getMac(), downloadRes.getChunk().getMac());
		assertArrayEquals(c1.getData(), downloadRes.getChunk().getData());

		// the message is still not deleted until it is ACK'd
		assertNotNull(fetchMessage(m.getId()));
		assertNotNull(chunkService.fetchChunk(m, 0));
		assertNotNull(chunkService.fetchChunk(m, 1));

		// now try and ack the prior message - no new ones to recv.
		org.tdmx.core.api.v01.mds.Acknowledge ackReq = new org.tdmx.core.api.v01.mds.Acknowledge();
		ackReq.setSessionId(UC_SESSION_ID);
		ackReq.setClientId(CLIENT_ID);
		ackReq.setMsgId(m.getMsgId());
		AcknowledgeResponse ackRes = mds.acknowledge(ackReq);
		assertSuccess(ackRes);

		// the message is deleted because it is ACK'd
		assertNull(fetchMessage(m.getId()));
		assertNull(chunkService.fetchChunk(m, 0));
		assertNull(chunkService.fetchChunk(m, 1));
	}

	@Test
	public void testReceiveMessage_SmallMsg_XATx_OnePhaseCommit() throws Exception {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		// 2MB chunk:0 data
		List<byte[]> chunks = new ArrayList<>();
		chunks.add(EntropySource.getRandomBytes(2 * EnumUtils.MB));

		Msg msg = MessageFacade.createMsg(uc, uc, service.getServiceName(),
				IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1, chunks);
		List<Channel> destinationChannels = getDestinationChannels();
		assertEquals(1, destinationChannels.size());

		ChannelMessage m = persistMessageForReceive(zone, msg, destinationChannels.get(0));
		org.tdmx.lib.chunk.domain.Chunk c = persistChunk(m, msg.getChunk());

		// now try and receive it.
		Receive req = new Receive();
		req.setSessionId(UC_SESSION_ID);
		req.setWaitTimeoutSec(5);

		Transaction tx = new Transaction();
		tx.setXid(XID);
		tx.setTxtimeout(60);
		req.setTransaction(tx);

		ReceiveResponse res = mds.receive(req);
		assertSuccess(res);
		assertNotNull(res.getMsg());
		assertNull(res.getContinuation());

		assertEquals(msg.getHeader().getMsgId(), res.getMsg().getHeader().getMsgId());
		assertNotNull(msg.getChunk());
		assertEquals(c.getMac(), msg.getChunk().getMac());
		assertArrayEquals(c.getData(), msg.getChunk().getData());

		// the message is not deleted until it is committed'd
		assertNotNull(fetchMessage(m.getId()));
		assertNotNull(chunkService.fetchChunk(m, 0));

		// now try and ack the prior message - no new ones to recv.
		Commit commitReq = new Commit();
		commitReq.setSessionId(UC_SESSION_ID);
		commitReq.setOnePhase(true);
		commitReq.setXid(XID);

		CommitResponse commitRes = mds.commit(commitReq);
		assertSuccess(commitRes);

		// the message is deleted because it is opc'd
		assertNull(fetchMessage(m.getId()));
		assertNull(chunkService.fetchChunk(m, 0));
	}

	@Test
	public void testReceiveMessage_SmallMsg_XATx_TwoPhaseCommit() throws Exception {
		authenticatedClientService.setAuthenticatedClient(uc.getPublicCert());

		// 2MB chunk:0 data
		List<byte[]> chunks = new ArrayList<>();
		chunks.add(EntropySource.getRandomBytes(2 * EnumUtils.MB));

		Msg msg = MessageFacade.createMsg(uc, uc, service.getServiceName(),
				IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1, chunks);
		List<Channel> destinationChannels = getDestinationChannels();
		assertEquals(1, destinationChannels.size());

		ChannelMessage m = persistMessageForReceive(zone, msg, destinationChannels.get(0));
		org.tdmx.lib.chunk.domain.Chunk c = persistChunk(m, msg.getChunk());

		// now try and receive it.
		Receive req = new Receive();
		req.setSessionId(UC_SESSION_ID);
		req.setWaitTimeoutSec(5);

		Transaction tx = new Transaction();
		tx.setXid(XID);
		tx.setTxtimeout(60);
		req.setTransaction(tx);

		ReceiveResponse res = mds.receive(req);
		assertSuccess(res);
		assertNotNull(res.getMsg());
		assertNull(res.getContinuation());

		assertEquals(msg.getHeader().getMsgId(), res.getMsg().getHeader().getMsgId());
		assertNotNull(msg.getChunk());
		assertEquals(c.getMac(), msg.getChunk().getMac());
		assertArrayEquals(c.getData(), msg.getChunk().getData());

		// the message is not deleted until it is committed'd
		assertNotNull(fetchMessage(m.getId()));
		assertNotNull(chunkService.fetchChunk(m, 0));

		Prepare prepareReq = new Prepare();
		prepareReq.setSessionId(UC_SESSION_ID);
		prepareReq.setXid(XID);
		PrepareResponse prepareRes = mds.prepare(prepareReq);
		assertSuccess(prepareRes);

		// the message is not deleted until it is committed'd
		assertNotNull(fetchMessage(m.getId()));
		assertNotNull(chunkService.fetchChunk(m, 0));

		// now try and ack the prior message - no new ones to recv.
		Commit commitReq = new Commit();
		commitReq.setSessionId(UC_SESSION_ID);
		commitReq.setOnePhase(false);
		commitReq.setXid(XID);
		CommitResponse commitRes = mds.commit(commitReq);
		assertSuccess(commitRes);

		// the message is deleted because it is opc'd
		assertNull(fetchMessage(m.getId()));
		assertNull(chunkService.fetchChunk(m, 0));
	}

	// TODO #101: receive with transactional rollback / recover / forget variants.

	private List<Channel> getDestinationChannels() {
		List<Channel> result = new ArrayList<>();
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
					result.add(channel);
				}
				if (channels.isEmpty()) {
					more = false;
				}
			}

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
		return result;
	}

	private ChannelMessage fetchMessage(Long id) {
		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {
			return channelService.findByMessageId(id);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
	}

	private ChannelMessage persistMessageForReceive(Zone zone, Msg msg, Channel channel) {
		ChannelMessage m = a2d.mapMessage(msg);
		m.setChannel(channel);
		m.initMessageState(zone, MessageStatus.READY, uc.getPublicCert().getSerialNumber(),
				uc.getPublicCert().getSerialNumber());

		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {
			channelService.relayMessage(zone, m);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
		return m;
	}

	private org.tdmx.lib.chunk.domain.Chunk persistChunk(ChannelMessage m, Chunk c) {
		org.tdmx.lib.chunk.domain.Chunk chunk = a2d.mapChunk(m, c);
		assertTrue(chunkService.storeChunk(m, chunk));
		return chunk;
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
