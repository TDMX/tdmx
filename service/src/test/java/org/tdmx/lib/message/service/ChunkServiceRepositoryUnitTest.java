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
package org.tdmx.lib.message.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.message.domain.Chunk;
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

		Chunk c = new Chunk(msg.getMsgId(), 0);
		c.setMac("1234");
		c.setData(new byte[] { 12, 1, 2, 3, 4, 5, 6 });
		c.setTtlTimestamp(msg.getTtlTimestamp()); // denormalized

		pos = c.getPos();

		service.storeChunk(msg, c);

	}

	@After
	public void doTeardown() {
		Chunk c = service.fetchChunk(msg, pos);
		if (c != null) {
			service.deleteChunks(msg);
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
		m.setMsgId("NEW" + System.currentTimeMillis());
		AgentSignature sig = new AgentSignature();
		sig.setSignatureDate(new Date()); // needed to determine which db
		m.setSignature(sig);
		m.setTtlTimestamp(new Date());

		Chunk c = service.fetchChunk(m, pos);
		assertNull(c);
	}

	@Test
	public void testLookup_NotFoundPos() throws Exception {
		Chunk c = service.fetchChunk(msg, (short) -1);
		assertNull(c);
	}

}