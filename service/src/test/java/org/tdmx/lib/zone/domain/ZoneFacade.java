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
package org.tdmx.lib.zone.domain;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.scheme.IntegratedCryptoScheme;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Grant;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.server.ws.ApiToDomainMapper;
import org.tdmx.server.ws.DomainToApiMapper;

public class ZoneFacade {

	public static final BigInteger ONE_KB = BigInteger.valueOf(1024);
	public static final BigInteger ONE_MB = BigInteger.valueOf(1024 * 1024);
	public static final BigInteger ONE_GB = BigInteger.valueOf(1024 * 1024 * 1024);

	public static final String DUMMY_SP_URL = "https://localhost:9000/api/mrs/v1.0";

	private static final DomainToApiMapper d2a = new DomainToApiMapper();
	private static final ApiToDomainMapper a2d = new ApiToDomainMapper();

	public static Zone createZone(Long accountZoneId, String zoneApex) throws Exception {
		Zone z = new Zone(accountZoneId, zoneApex);
		return z;
	}

	public static Domain createDomain(Zone zone, String domainName) throws Exception {
		Domain d = new Domain(zone, domainName);
		return d;
	}

	public static Address createAddress(Domain domain, String localName) throws Exception {
		Address a = new Address(domain, localName);
		return a;
	}

	public static Service createService(Domain domain, String serviceName) throws Exception {
		Service s = new Service(domain, serviceName);
		return s;
	}

	public static ChannelOrigin createChannelOrigin(String localName, String domainName, String serviceProvider) {
		ChannelOrigin co = new ChannelOrigin();
		co.setLocalName(localName);
		co.setDomainName(domainName);
		return co;
	}

	public static ChannelDestination createChannelDestination(String localName, String domainName, String serviceName) {
		ChannelDestination cd = new ChannelDestination();
		cd.setLocalName(localName);
		cd.setDomainName(domainName);
		cd.setServiceName(serviceName);
		return cd;
	}

	public static Destination createDestination(PKIXCredential userCred, Address address, Service service) {
		DestinationSession ds = new DestinationSession();
		ds.setEncryptionContextId("1");
		ds.setScheme(IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1);
		ds.setSessionKey(new byte[] { 1, 2, 3 }); // this is not a "propper" session key, but it is signed correctly.

		Destinationsession ads = d2a.mapDestinationSession(ds);
		SignatureUtils.createDestinationSessionSignature(userCred, SignatureAlgorithm.SHA_256_RSA, new Date(),
				service.getServiceName(), ads);

		return a2d.mapDestination(address, service, ads);
	}

	public static EndpointPermission createPermission(AgentCredential userAgent, PKIXCredential userCred,
			EndpointPermissionGrant grant, Domain domain, ChannelOrigin origin, ChannelDestination dest) {
		Channel channel = new Channel(domain, origin, dest);

		Permission p = new Permission();
		p.setPermission(Grant.valueOf(grant.toString()));
		p.setMaxPlaintextSizeBytes(ONE_GB);
		SignatureUtils.createEndpointPermissionSignature(userCred, SignatureAlgorithm.SHA_256_RSA, new Date(),
				d2a.mapChannel(channel), p);

		return a2d.mapEndpointPermission(p);
	}

	public static ChannelAuthorization createChannelAuthorization(Domain domain, PKIXCredential userCred,
			AgentCredential userAgent, ChannelOrigin origin, ChannelDestination dest, EndpointPermission sendPermission,
			EndpointPermission recvPermission) {
		Channel channel = new Channel(domain, origin, dest);

		ChannelAuthorization c = new ChannelAuthorization(channel);
		channel.setAuthorization(c);

		c.setSendAuthorization(sendPermission);
		c.setRecvAuthorization(recvPermission);
		// pending authorizations not set.

		c.setMaxRedeliveryCount(1);
		c.setRedeliveryDelaySec(60);

		FlowLimit l = new FlowLimit();
		l.setHighMarkBytes(ONE_GB);
		l.setLowMarkBytes(ONE_MB);
		c.setLimit(l);

		// make a valid signature by mapping back and from the API
		Currentchannelauthorization current = new Currentchannelauthorization();
		current.setOriginPermission(d2a.mapPermission(c.getSendAuthorization()));
		current.setDestinationPermission(d2a.mapPermission(c.getRecvAuthorization()));
		current.setMaxRedeliveryCount(c.getMaxRedeliveryCount());
		current.setMinRedeliveryDelaySec(c.getRedeliveryDelaySec());
		current.setLimit(d2a.mapLimit(c.getLimit()));
		SignatureUtils.createChannelAuthorizationSignature(userCred, SignatureAlgorithm.SHA_256_RSA, new Date(),
				d2a.mapChannel(channel), current);
		c.setSignature(a2d.mapAdministratorSignature(current.getAdministratorsignature()));

		channel.getQuota().updateAuthorizationInfo();
		return c;
	}

	public static Date getDateYearsFromNow(int years) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, years);
		return cal.getTime();
	}

	public static ChannelMessage createChannelMessage(String msgId, Zone zone, Channel channel, ProcessingState ps) {
		ChannelMessage cm = new ChannelMessage();
		cm.setMsgId(msgId);
		cm.setChannel(channel);

		cm.initMessageState(zone, MessageStatus.NEW, 1, 1);

		cm.getState().setProcessingState(ps);

		// not null properties
		AgentSignature sig = new AgentSignature();
		sig.setAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		sig.setSignatureDate(new Date());
		sig.setCertificateChainPem("SENDER CERT");
		sig.setValue("" + System.currentTimeMillis());
		cm.setSignature(sig);

		cm.setReceiverPem("RECEIVER CERT");

		cm.setEncryptionContext(new byte[] { 1, 2, 3, 4, 5 });
		cm.setTtlTimestamp(new Date());
		cm.setEncryptionContextId("" + System.currentTimeMillis());
		cm.setScheme(IntegratedCryptoScheme.ECDH384_AES256plusRSA_SLASH_AES256__16MB_SHA1);
		cm.setPayloadLength(100000000);
		cm.setPlaintextLength(100000000);
		cm.setMacOfMacs("MAC" + System.currentTimeMillis());
		return cm;
	}
}
