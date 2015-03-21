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
package org.tdmx.lib.zone.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ChannelAuthorizationServiceRepositoryUnitTest {

	@Autowired
	private TestDataGenerator dataGenerator;

	@Autowired
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	@Autowired
	private ChannelAuthorizationService channelAuthorizationService;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;

	private Zone zone;

	@Before
	public void doSetup() throws Exception {

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockZonePartitionIdInstaller.ZP1_S1);
		input.setNumZACs(1);
		input.setNumDomains(1);
		input.setNumServicesPerDomain(1);
		input.setNumDACsPerDomain(1);
		input.setNumAddressesPerDomain(1);
		input.setNumUsersPerAddress(1);

		data = dataGenerator.setUp(input);

		zone = data.getZone();

		zonePartitionIdProvider.setPartitionId(input.getZonePartitionId());
	}

	@After
	public void doTeardown() {
		zonePartitionIdProvider.clearPartitionId();

		dataGenerator.tearDown(input, data);

	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(dataGenerator);
		assertNotNull(channelAuthorizationService);
	}

	@Test
	public void testFindById_NotFound() throws Exception {
		ChannelAuthorization c = channelAuthorizationService.findById(new Random().nextLong());
		assertNull(c);
	}

	@Test
	public void testSearch_None() throws Exception {
		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));
		List<ChannelAuthorization> channelAuths = channelAuthorizationService.search(zone, criteria);
		assertNotNull(channelAuths);
		assertEquals(1, channelAuths.size());
	}

	@Test
	public void testSearch_OriginAll() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);

		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));
		criteria.getOrigin().setLocalName(ca.getOrigin().getLocalName());
		criteria.getOrigin().setDomainName(ca.getOrigin().getDomainName());
		criteria.getOrigin().setServiceProvider(ca.getOrigin().getServiceProvider());

		List<ChannelAuthorization> channelAuths = channelAuthorizationService.search(zone, criteria);
		assertNotNull(channelAuths);
		assertEquals(1, channelAuths.size());
	}

	@Test
	public void testSearch_DestinationAll() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);

		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));
		criteria.getDestination().setLocalName(ca.getDestination().getLocalName());
		criteria.getDestination().setDomainName(ca.getDestination().getDomainName());
		criteria.getDestination().setServiceName(ca.getDestination().getServiceName());
		criteria.getDestination().setServiceProvider(ca.getDestination().getServiceProvider());

		List<ChannelAuthorization> channelAuths = channelAuthorizationService.search(zone, criteria);
		assertNotNull(channelAuths);
		assertEquals(1, channelAuths.size());
	}

	@Test
	public void testSearch_OriginAndDestinationAll() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);

		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));
		criteria.getOrigin().setLocalName(ca.getOrigin().getLocalName());
		criteria.getOrigin().setDomainName(ca.getOrigin().getDomainName());
		criteria.getOrigin().setServiceProvider(ca.getOrigin().getServiceProvider());
		criteria.getDestination().setLocalName(ca.getDestination().getLocalName());
		criteria.getDestination().setDomainName(ca.getDestination().getDomainName());
		criteria.getDestination().setServiceName(ca.getDestination().getServiceName());
		criteria.getDestination().setServiceProvider(ca.getDestination().getServiceProvider());

		List<ChannelAuthorization> channelAuths = channelAuthorizationService.search(zone, criteria);
		assertNotNull(channelAuths);
		assertEquals(1, channelAuths.size());
	}

	@Test
	public void testLookup_FindByChannel() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);
		ChannelAuthorization storedCA = channelAuthorizationService.findByChannel(zone, ca.getOrigin(),
				ca.getDestination());
		assertNotNull(storedCA);
	}

	@Test
	public void testSearch_UnknownZone() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);

		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));
		criteria.getOrigin().setLocalName(ca.getOrigin().getLocalName());

		Zone gugus = new Zone(zone.getAccountZoneId(), zone.getZoneApex());
		gugus.setId(new Random().nextLong());

		List<ChannelAuthorization> channelAuths = channelAuthorizationService.search(gugus, criteria);
		assertNotNull(channelAuths);
		assertEquals(0, channelAuths.size());
	}

	@Test
	public void testModify() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);
		ChannelAuthorization storedCA = channelAuthorizationService.findByChannel(zone, ca.getOrigin(),
				ca.getDestination());
		assertNotNull(storedCA);
		storedCA.getUndeliveredBuffer().setHighMarkBytes(BigInteger.TEN);
		storedCA.getUndeliveredBuffer().setLowMarkBytes(BigInteger.ONE);
		storedCA.getUnsentBuffer().setHighMarkBytes(BigInteger.TEN);
		storedCA.getUnsentBuffer().setLowMarkBytes(BigInteger.ONE);
		channelAuthorizationService.createOrUpdate(storedCA);

		ChannelAuthorization modifiedCA = channelAuthorizationService.findByChannel(zone, ca.getOrigin(),
				ca.getDestination());
		assertNotNull(modifiedCA);
		assertEquals(storedCA.getUndeliveredBuffer().getHighMarkBytes(), modifiedCA.getUndeliveredBuffer()
				.getHighMarkBytes());
		assertEquals(storedCA.getUndeliveredBuffer().getLowMarkBytes(), modifiedCA.getUndeliveredBuffer()
				.getLowMarkBytes());
		assertEquals(storedCA.getUnsentBuffer().getHighMarkBytes(), modifiedCA.getUnsentBuffer().getHighMarkBytes());
		assertEquals(storedCA.getUnsentBuffer().getLowMarkBytes(), modifiedCA.getUnsentBuffer().getLowMarkBytes());

	}

}