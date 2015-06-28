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

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.msg.FlowChannel;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.server.ws.DomainToApiMapper;

public class MessageFacade {

	public static Message createMessage() throws Exception {
		Message m = new Message(UUID.randomUUID().toString(), new Date());

		// Header fields.
		m.setLiveUntilTS(new Date());
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
		// define the message's timestamp
		Calendar msgTs = CalendarUtils.getTimestamp(new Date());

		Calendar ttlCal = CalendarUtils.getTimestamp(new Date());
		ttlCal.add(Calendar.HOUR, 24);

		// create the first chunk
		org.tdmx.core.api.v01.msg.Chunk chunk = new org.tdmx.core.api.v01.msg.Chunk();
		chunk.setData(new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 });
		chunk.setMac("MAC"); // TODO
		chunk.setPos(0);

		Header hdr = new Header();
		hdr.setTimestamp(msgTs);
		hdr.setTtl(ttlCal);

		FlowChannel fc = new FlowChannel();
		fc.setServicename(serviceName);
		fc.setSource(new DomainToApiMapper().mapUserIdentity(sourceUser.getCertificateChain()));
		fc.setTarget(new DomainToApiMapper().mapUserIdentity(targetUser.getCertificateChain()));
		hdr.setFlowchannel(fc);
		hdr.setFlowsessionId("TODO"); // TODO

		Payload payload = new Payload();
		payload.setLength(100);
		payload.setPlaintextLength(1000);
		payload.setEncryptionContext(new byte[] { 12, 1, 2, 3, 4, 5, 6 });
		payload.setChunksCRC("CRC");
		payload.setChunkSizeFactor(10);

		SignatureUtils.createPayloadSignature(sourceUser, SignatureAlgorithm.SHA_256_RSA, payload, hdr);

		// once we have the payload signature in the header, we can set the ID.
		SignatureUtils.setMsgId(hdr);
		// and sign the message header
		SignatureUtils.createHeaderSignature(sourceUser, SignatureAlgorithm.SHA_256_RSA, hdr);

		// link the chunk to the message
		chunk.setMsgId(hdr.getMsgId());

		Msg msg = new Msg();
		msg.setHeader(hdr);
		msg.setPayload(payload);
		msg.setChunk(chunk);
		return msg;
	}
}
