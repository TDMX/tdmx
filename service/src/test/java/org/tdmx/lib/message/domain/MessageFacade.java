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
package org.tdmx.lib.message.domain;

import java.util.Date;
import java.util.UUID;

public class MessageFacade {

	public static Message createMessage(Long flowId) throws Exception {
		Message m = new Message(UUID.randomUUID().toString(), new Date());

		// Header fields.
		m.setLiveUntilTS(new Date());
		m.setFlowId(flowId);
		m.setExternalReference("External Reference Text");
		m.setRecvAuthSignature("12345");
		m.setSendAuthSignature("12345");
		m.setSessionSignature("12345");
		m.setHeaderSignature("12345");

		// payload fields
		m.setChunksCRC("12345678");
		m.setChunkSizeFactor((short) 8);
		m.setEncryptionContext(new byte[] { 1, 2, 3 });
		m.setPayloadLength(1024);
		m.setPayloadSignature("1234");
		m.setPlaintextLength(8000);
		return m;
	}

	public static Chunk createChunk(String msgId, short pos) throws Exception {
		Chunk c = new Chunk(msgId, pos);
		c.setMac("1234");
		c.setData(new byte[] { 12, 1, 2, 3, 4, 5, 6 });
		return c;
	}
}
