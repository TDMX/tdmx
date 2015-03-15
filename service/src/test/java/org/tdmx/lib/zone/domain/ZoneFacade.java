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
import org.tdmx.lib.common.domain.ZoneReference;

public class ZoneFacade {

	public static final BigInteger ONE_KB = BigInteger.valueOf(1024);
	public static final BigInteger ONE_MB = BigInteger.valueOf(1024 * 1024);
	public static final BigInteger ONE_GB = BigInteger.valueOf(1024 * 1024 * 1024);

	public static final String DUMMY_SP_URL = "https://localhost:9000/api/mrs/v1.0";

	public static Zone createZone(ZoneReference zone) throws Exception {
		Zone z = new Zone(zone);
		return z;
	}

	public static Domain createDomain(ZoneReference zone, String domainName) throws Exception {
		Domain d = new Domain(zone);
		d.setDomainName(domainName);
		return d;
	}

	public static Address createAddress(ZoneReference zone, String domainName, String localName) throws Exception {
		Address a = new Address(zone);
		a.setDomainName(domainName);
		a.setLocalName(localName);
		return a;
	}

	public static Service createService(ZoneReference zone, String domainName, String serviceName, int concurrencyLimit)
			throws Exception {
		Service s = new Service(zone);
		s.setDomainName(domainName);
		s.setServiceName(serviceName);
		s.setConcurrencyLimit(concurrencyLimit);
		return s;
	}

	public static ChannelOrigin createChannelOrigin(String localName, String domainName, String serviceProvider) {
		ChannelOrigin co = new ChannelOrigin();
		co.setLocalName(localName);
		co.setDomainName(domainName);
		co.setServiceProvider(serviceProvider);
		return co;
	}

	public static ChannelDestination createChannelDestination(String localName, String domainName, String serviceName,
			String serviceProvider) {
		ChannelDestination cd = new ChannelDestination();
		cd.setLocalName(localName);
		cd.setDomainName(domainName);
		cd.setServiceName(serviceName);
		cd.setServiceProvider(serviceProvider);
		return cd;
	}

	// TODO give DAC to be able to construct correct signatures
	public static ChannelAuthorization createChannelAuthorization(ZoneReference zone, ChannelOrigin origin,
			ChannelDestination dest) {
		ChannelAuthorization c = new ChannelAuthorization(zone);
		c.setOrigin(origin);
		c.setDestination(dest);

		EndpointPermission sendPermission = new EndpointPermission();
		sendPermission.setGrant(EndpointPermissionGrant.ALLOW);
		sendPermission.setMaxPlaintextSizeBytes(ONE_MB);
		sendPermission.setValidUntil(getDateYearsFromNow(1));
		AgentSignature sendPermSignature = new AgentSignature();
		sendPermSignature.setAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		sendPermSignature.setCertificateChainPem("certificateChainPem");
		sendPermSignature.setSignatureDate(new Date());
		sendPermSignature.setValue("hexvalueofsignature");
		sendPermission.setSignature(sendPermSignature);
		c.setSendAuthorization(sendPermission);

		EndpointPermission recvPermission = new EndpointPermission();
		recvPermission.setGrant(EndpointPermissionGrant.ALLOW);
		recvPermission.setMaxPlaintextSizeBytes(ONE_MB);
		recvPermission.setValidUntil(getDateYearsFromNow(1));
		AgentSignature recvPermSignature = new AgentSignature();
		recvPermSignature.setAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		recvPermSignature.setCertificateChainPem("certificateChainPem");
		recvPermSignature.setSignatureDate(new Date());
		recvPermSignature.setValue("hexvalueofsignature");
		recvPermission.setSignature(sendPermSignature);
		c.setRecvAuthorization(recvPermission);

		// pending authorizations not set.

		// TODO make a valid signature
		AgentSignature signature = new AgentSignature();
		signature.setAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		signature.setCertificateChainPem("certificateChainPem");
		signature.setSignatureDate(new Date());
		signature.setValue("hexvalueofsignature");
		c.setSignature(signature);

		FlowLimit undeliveredBuffer = new FlowLimit();
		undeliveredBuffer.setHighMarkBytes(ONE_GB);
		undeliveredBuffer.setLowMarkBytes(ONE_MB);
		c.setUndeliveredBuffer(undeliveredBuffer);

		FlowLimit unsentBuffer = new FlowLimit();
		unsentBuffer.setHighMarkBytes(ONE_GB);
		unsentBuffer.setLowMarkBytes(ONE_MB);
		c.setUnsentBuffer(unsentBuffer);
		return c;
	}

	public static Date getDateYearsFromNow(int years) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, years);
		return cal.getTime();
	}
}
