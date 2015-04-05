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
package org.tdmx.core.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateFacade;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.ChannelEndpoint;
import org.tdmx.core.api.v01.msg.Destination;
import org.tdmx.core.api.v01.msg.EndpointPermission;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.system.lang.CalendarUtils;

public class SignatureUtilsTest {

	static {
		JCAProviderInitializer.init();
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_StringSignature_ZAC() throws Exception {
		PKIXCredential zac = CertificateFacade.createZAC("zone.root", 10);
		PKIXCredential dac = CertificateFacade.createDAC(zac, 2);
		PKIXCredential uc = CertificateFacade.createUC(dac, 1);

		ChannelEndpoint origin = new ChannelEndpoint();
		origin.setDomain("domain");
		origin.setLocalname("domain");
		origin.setServiceprovider("sp");

		Destination dest = new Destination();
		dest.setDomain("domain");
		dest.setLocalname("domain");
		dest.setServicename("service");
		dest.setServiceprovider("sp");

		Channel channel = new Channel();
		channel.setOrigin(origin);
		channel.setDestination(dest);

		EndpointPermission perm = new EndpointPermission();
		perm.setMaxPlaintextSizeBytes(BigInteger.ONE);
		perm.setPermission(Permission.ALLOW);
		perm.setValidUntil(CalendarUtils.getDate(new Date()));

		SignatureUtils
				.createEndpointPermissionSignature(dac, SignatureAlgorithm.SHA_256_RSA, new Date(), channel, perm);

		assertNotNull(perm.getAdministratorsignature().getSignaturevalue().getSignature());

		boolean signatureOk = SignatureUtils.checkEndpointPermissionSignature(channel, perm, true);
		assertTrue(signatureOk);

	}
}
