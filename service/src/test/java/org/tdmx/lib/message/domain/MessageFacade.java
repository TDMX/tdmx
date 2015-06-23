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

import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.msg.FlowChannel;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;

public class MessageFacade {

	public static Message createMessage(Long flowId) throws Exception {
		Message m = new Message(UUID.randomUUID().toString(), new Date());

		// Header fields.
		m.setLiveUntilTS(new Date());
		m.setFlowId(flowId);
		m.setExternalReference("External Reference Text");
		m.setFlowSessionId("fs1");
		m.setHeaderSignature("12345");

		// payload fields
		m.setChunksCRC("12345678");
		m.setChunkSizeFactor(8);
		m.setEncryptionContext(new byte[] { 1, 2, 3 });
		m.setPayloadLength(1024);
		m.setPayloadSignature("1234");
		m.setPlaintextLength(8000);
		return m;
	}

	public static Chunk createChunk(String msgId, int pos) throws Exception {
		Chunk c = new Chunk(msgId, pos);
		c.setMac("1234");
		c.setData(new byte[] { 12, 1, 2, 3, 4, 5, 6 });
		return c;
	}

	public static Msg createMsg(PKIXCredential sourceUser, PKIXCredential targetUser, String serviceName)
			throws Exception {
		Msg msg = new Msg();
		// TODO
		Header hdr = new Header();
		msg.setHeader(hdr);

		FlowChannel fc = new FlowChannel();
		fc.setServicename(serviceName);
		fc.setSource(null); // TODO cred to uidentity
		fc.setTarget(null); // TODO
		hdr.setFlowchannel(fc);

		Payload payload = new Payload();
		payload.setLength(100);
		payload.setPlaintextLength(1000);
		payload.setEncryptionContext(new byte[] { 12, 1, 2, 3, 4, 5, 6 });
		payload.setChunksCRC("CRC");
		payload.setChunkSizeFactor(10);

		msg.setPayload(payload);

		org.tdmx.core.api.v01.msg.Chunk chunk = new org.tdmx.core.api.v01.msg.Chunk();
		// TODO
		msg.setChunk(chunk);

		return msg;
	}
}
