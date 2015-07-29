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

import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Chunk;
import org.tdmx.core.api.v01.msg.Destination;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msg;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.msg.UserIdentity;
import org.tdmx.core.system.lang.CalendarUtils;

public class MessageFacade {

	public static Chunk createChunk(String msgId, int pos) {
		Chunk chunk = new org.tdmx.core.api.v01.msg.Chunk();
		chunk.setMsgId(msgId);
		chunk.setData(new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 });
		chunk.setMac("MAC"); // TODO
		chunk.setPos(0);
		return chunk;
	}

	public static Msg createMsg(PKIXCredential sourceUser, PKIXCredential targetUser, String serviceName)
			throws Exception {
		// define the message's timestamp
		Calendar msgTs = CalendarUtils.getTimestamp(new Date());

		Calendar ttlCal = CalendarUtils.getTimestamp(new Date());
		ttlCal.add(Calendar.HOUR, 24);

		// channel
		Channel c = new Channel();
		ChannelEndpoint o = new ChannelEndpoint();
		o.setLocalname(sourceUser.getPublicCert().getTdmxUserName());
		o.setDomain(sourceUser.getPublicCert().getTdmxDomainName());
		c.setOrigin(o);
		Destination d = new Destination();
		d.setLocalname(targetUser.getPublicCert().getTdmxUserName());
		d.setDomain(targetUser.getPublicCert().getTdmxDomainName());
		d.setServicename(serviceName);
		c.setDestination(d);

		// the to user
		UserIdentity to = new UserIdentity();
		to.setUsercertificate(targetUser.getPublicCert().getX509Encoded());
		to.setDomaincertificate(targetUser.getIssuerPublicCert().getX509Encoded());
		to.setRootcertificate(targetUser.getZoneRootPublicCert().getX509Encoded());

		// create the first chunk
		Header hdr = new Header();
		hdr.setTimestamp(msgTs);
		hdr.setTtl(ttlCal);

		hdr.setChannel(c);
		hdr.setTo(to);

		hdr.setEncryptionContextId("TODO"); // TODO

		Payload payload = new Payload();
		payload.setLength(100);
		payload.setPlaintextLength(1000);
		payload.setEncryptionContext(new byte[] { 12, 1, 2, 3, 4, 5, 6 });
		payload.setChunkSize(2048); // TODO create payloadMACofMACs ( SHA256 ) #72
		payload.setMACofMACs("TODO"); // TODO

		SignatureUtils.createPayloadSignature(sourceUser, SignatureAlgorithm.SHA_256_RSA, payload, hdr);

		// once we have the payload signature in the header, we can set the ID.
		SignatureUtils.setMsgId(hdr);
		// and sign the message header
		SignatureUtils.createHeaderSignature(sourceUser, SignatureAlgorithm.SHA_256_RSA, hdr);

		// link the chunk to the message
		Chunk chunk = createChunk(hdr.getMsgId(), 0);

		Msg msg = new Msg();
		msg.setHeader(hdr);
		msg.setPayload(payload);
		msg.setChunk(chunk);
		return msg;
	}
}
