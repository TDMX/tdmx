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
package org.tdmx.lib.chunk.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.IntegratedCryptoScheme;
import org.tdmx.lib.chunk.domain.Chunk;
import org.tdmx.lib.chunk.service.ChunkService;
import org.tdmx.lib.zone.domain.AgentSignature;
import org.tdmx.lib.zone.domain.ChannelMessage;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ChunkServiceRepositoryUnitTest {

	@Autowired
	private ChunkService service;

	private ChannelMessage msg;
	private int pos;

	@Before
	public void doSetup() throws Exception {
		msg = new ChannelMessage();
		msg.setMsgId("" + System.currentTimeMillis());
		AgentSignature sig = new AgentSignature();
		sig.setSignatureDate(new Date()); // needed to determine which db
		msg.setSignature(sig);
		msg.setTtlTimestamp(new Date());

		Chunk c = new Chunk(msg, 0);
		c.setMac("1234");
		c.setData(new byte[] { 12, 1, 2, 3, 4, 5, 6 });

		pos = c.getPos();

		assertTrue(service.storeChunk(msg, c));

	}

	@After
	public void doTeardown() {
		Chunk c = service.fetchChunk(msg, pos);
		if (c != null) {
			assertTrue(service.deleteChunks(msg));
		}
	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(service);
	}

	@Test
	public void testLookup() throws Exception {
		Chunk c = service.fetchChunk(msg, pos);
		assertNotNull(c);
	}

	@Test
	public void testLookup_NotFoundMsgId() throws Exception {
		ChannelMessage m = new ChannelMessage();
		// doesn't exist
		m.setMsgId("abcde" + System.currentTimeMillis());
		AgentSignature sig = new AgentSignature();
		sig.setSignatureDate(new Date()); // needed to determine which db
		m.setSignature(sig);
		m.setTtlTimestamp(new Date());

		Chunk c = service.fetchChunk(m, pos);
		assertNull(c);
	}

	@Test
	public void testFullLifecycle() throws Exception {
		ChannelMessage m = new ChannelMessage();
		m.setScheme(IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1);
		m.setMsgId(ByteArray.asHex(DigestAlgorithm.SHA_256.kdf(EntropySource.getRandomBytes(8))));
		AgentSignature sig = new AgentSignature();
		sig.setSignatureDate(new Date()); // needed to determine which db
		m.setSignature(sig);
		m.setTtlTimestamp(new Date());

		Chunk c = new Chunk(m, 0);
		c.setMac(ByteArray.asHex(DigestAlgorithm.SHA_1.kdf(EntropySource.getRandomBytes(4))));
		c.setData(new byte[m.getScheme().getChunkSize()]);

		assertTrue(service.storeChunk(m, c));
		assertTrue(service.storeChunk(m, c)); // repeat save ok
		assertTrue(service.storeChunk(m, c)); // repeat save ok

		Chunk existingChunk = service.fetchChunk(m, 0);
		assertNotNull(existingChunk);
		ByteArray.equals(c.getData(), existingChunk.getData());
		assertEquals(c.getMsgId(), existingChunk.getMsgId());
		assertEquals(c.getMac(), existingChunk.getMac());
		assertEquals(c.getPos(), existingChunk.getPos());
		assertEquals(c.getTtlTimestamp(), existingChunk.getTtlTimestamp());

		assertTrue(service.deleteChunks(m));
		assertNull(service.fetchChunk(m, 0));
	}

	@Test
	public void testLookup_NotFoundPos() throws Exception {
		Chunk c = service.fetchChunk(msg, (short) -1);
		assertNull(c);
	}

}